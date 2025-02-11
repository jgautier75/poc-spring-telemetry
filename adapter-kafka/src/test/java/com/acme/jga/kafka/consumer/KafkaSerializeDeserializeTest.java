package com.acme.jga.kafka.consumer;

import com.acme.jga.utils.date.DateTimeUtils;
import com.acme.users.mgt.events.protobuf.Event;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Uuid;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.SendResult;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import java.net.UnknownHostException;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

@Slf4j
class KafkaSerializeDeserializeTest {
    public static final String CONFLUENT_PLATFORM_VERSION = "7.7.1";
    private static final String KAFKA_NETWORK = "kafka-network";
    private static final int SCHEMA_REGISTRY_PORT = 8085;
    private static final int KAFKA_LISTENER_PORT = 19092;

    @Test
    public void test() throws UnknownHostException, ExecutionException, InterruptedException {

        // Start kafka and schema registry containers
        Network network = getNetwork();
        ConfluentKafkaContainer confluentKafkaContainer = new ConfluentKafkaContainer("confluentinc/cp-kafka:" + CONFLUENT_PLATFORM_VERSION)
                .withListener("kafka:" + KAFKA_LISTENER_PORT)
                .withNetwork(network)
                .withReuse(true);
        GenericContainer<?> schemaRegistry = new GenericContainer<>("confluentinc/cp-schema-registry:" + CONFLUENT_PLATFORM_VERSION)
                .withExposedPorts(SCHEMA_REGISTRY_PORT)
                .dependsOn(confluentKafkaContainer)
                .withNetworkAliases("schemaregistry")
                .withNetwork(network)
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:" + KAFKA_LISTENER_PORT)
                .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:" + SCHEMA_REGISTRY_PORT)
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schemaregistry")
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_SECURITY_PROTOCOL", "PLAINTEXT")
                .waitingFor(Wait.forHttp("/subjects"))
                .withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS));
        schemaRegistry.start();
        String schemaRegistryBaseUrl = String.format("http://%s:%d", schemaRegistry.getHost(), schemaRegistry.getMappedPort(SCHEMA_REGISTRY_PORT));

        // Create audit events topic
        String topicName = "audit_events";
        String consumerGroupId = "audit_consumer";

        KafkaFuture<Uuid> uuidKafkaFuture = kafkaCreateTopic(confluentKafkaContainer.getBootstrapServers(), topicName, 1, (short) 1);
        await().until(uuidKafkaFuture::isDone);
        log.info("Created topic [{}] with uuid [{}]", topicName, uuidKafkaFuture.get().toString());

        // Setup kafka template
        KafkaTemplate<String, Event.AuditEventMessage> kafkaTemplate = kafkaTemplateAudit(confluentKafkaContainer.getBootstrapServers(), schemaRegistryBaseUrl);

        // Create record and send
        Event.AuditEventMessage producerEvent = createAuditEvent();
        ProducerRecord<String, Event.AuditEventMessage> producerRecord = new ProducerRecord<>(topicName, producerEvent);
        CompletableFuture<SendResult<String, Event.AuditEventMessage>> sendResult = kafkaTemplate.send(producerRecord);
        await().until(sendResult::isDone);

        KafkaConsumer<String, DynamicMessage> consumer = new KafkaConsumer<>(consumerConfigs(confluentKafkaContainer.getBootstrapServers(), consumerGroupId, schemaRegistryBaseUrl));
        consumer.subscribe(List.of(topicName));
        AtomicBoolean protobufDeserializationError = new AtomicBoolean(false);
        Event.AuditEventMessage auditEventMessage = null;

        while (true) {
            ConsumerRecords<String, DynamicMessage> consumerRecords = consumer.poll(Duration.ofSeconds(10));
            for (ConsumerRecord<String, DynamicMessage> consumerRecord : consumerRecords) {
                try {
                    auditEventMessage = Event.AuditEventMessage.newBuilder().build().getParserForType().parseFrom(consumerRecord.value().toByteArray());
                } catch (InvalidProtocolBufferException e) {
                    protobufDeserializationError.set(true);
                }
                break;
            }
            break;
        }
        assertFalse(protobufDeserializationError.get());
        assertNotNull(auditEventMessage);
        assertEquals("Object uid match", producerEvent.getObjectUid(), auditEventMessage.getObjectUid());
    }

    public ConsumerFactory<Integer, String> consumerFactory(String bootstrapServers, String clientId, String schemaRegistryBaseUrl) {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(bootstrapServers, clientId, schemaRegistryBaseUrl));
    }

    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Integer, String>> kafkaListenerContainerFactory(String bootstrapServers, String clientId, String schemaRegistryBaseUrl) {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(bootstrapServers, clientId, schemaRegistryBaseUrl));
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    public Map<String, Object> consumerConfigs(String bootstrapServers, String clientId, String schemaRegistryBaseUrl) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, clientId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryBaseUrl);
        return props;
    }

    private KafkaTemplate<String, Event.AuditEventMessage> kafkaTemplateAudit(String bootstrapServers, String schemaRegistryBaseUrl) {
        return new KafkaTemplate<>(producerFactory(bootstrapServers, schemaRegistryBaseUrl));
    }

    public ProducerFactory<String, Event.AuditEventMessage> producerFactory(String bootstrapServers, String schemaRegistryBaseUrl) {
        return new DefaultKafkaProducerFactory<>(producerConfigs(bootstrapServers, schemaRegistryBaseUrl));
    }

    public Map<String, Object> producerConfigs(String bootstrapServers, String schemaRegistryBaseUrl) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class);
        props.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryBaseUrl);
        return props;
    }

    private KafkaFuture<Uuid> kafkaCreateTopic(String bootStrapServers, String topicName, int nbOfPartitions, short replicationFactor) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 1000);
        try (AdminClient adminClient = AdminClient.create(props)) {
            NewTopic topic = new NewTopic(topicName, nbOfPartitions, replicationFactor);
            CreateTopicsResult createTopicsResult = adminClient.createTopics(List.of(topic));
            return createTopicsResult.topicId(topicName);
        }
    }

    private Network getNetwork() {
        Network defaultDaprNetwork = new Network() {
            @Override
            public org.junit.runners.model.Statement apply(org.junit.runners.model.Statement base, Description description) {
                return null;
            }

            @Override
            public String getId() {
                return KAFKA_NETWORK;
            }

            @Override
            public void close() {
            }
        };
        List<com.github.dockerjava.api.model.Network> networks = DockerClientFactory.instance().client().listNetworksCmd().withNameFilter(KAFKA_NETWORK).exec();
        if (networks.isEmpty()) {
            Network.builder()
                    .createNetworkCmdModifier(cmd -> cmd.withName(KAFKA_NETWORK))
                    .build().getId();
            return defaultDaprNetwork;
        } else {
            return defaultDaprNetwork;
        }
    }

    private Event.AuditEventMessage createAuditEvent() {
        Event.AuditEventMessage.Builder auditEventMessageBuilder = Event.AuditEventMessage.newBuilder();
        auditEventMessageBuilder.setUid(UUID.randomUUID().toString());
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        auditEventMessageBuilder.setCreatedAt(isoFormatter.format(DateTimeUtils.nowIso().atZone(ZoneOffset.UTC)));
        auditEventMessageBuilder.setAuthor(Event.AuditAuthor.newBuilder().setName("author").setUid(UUID.randomUUID().toString()).build());
        auditEventMessageBuilder.setStatus(1);
        auditEventMessageBuilder.setTarget(Event.AuditTarget.USER);
        auditEventMessageBuilder.setAction(Event.AuditAction.UPDATE);
        auditEventMessageBuilder.setScope(
                Event.AuditScope.newBuilder()
                        .setOrganizationCode("org-01")
                        .setTenantCode("tenant-01")
                        .setOrganizationUid(UUID.randomUUID().toString())
                        .setTenantUid(UUID.randomUUID().toString())
                        .build()
        );
        auditEventMessageBuilder.setAction(Event.AuditAction.CREATE);
        auditEventMessageBuilder.addChanges(
                Event.AuditChange.newBuilder()
                        .setFrom("titi")
                        .setTo("toto")
                        .setObject("commons_firstName")
                        .build()
        );
        return auditEventMessageBuilder.build();
    }

}