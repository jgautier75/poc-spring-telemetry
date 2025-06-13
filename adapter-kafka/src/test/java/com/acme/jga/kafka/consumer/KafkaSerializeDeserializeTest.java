package com.acme.jga.kafka.consumer;

import com.acme.jga.utils.date.DateTimeUtils;
import com.acme.users.mgt.events.protobuf.Event;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Uuid;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;

@Slf4j
class KafkaSerializeDeserializeTest {
    public static final String CONFLUENT_PLATFORM_VERSION = "7.9.1";
    private static final String KAFKA_NETWORK = "kafka-network";
    private static final int SCHEMA_REGISTRY_PORT = 8085;
    private static final int KAFKA_LISTENER_PORT = 19092;

    @Test
    public void Kafka_SchemaRegistry_Protobuf_Serialize_Deserialize() throws ExecutionException, InterruptedException {

        // Start kafka and schema registry containers
        Network network = KafkaFactoryTest.createNetwork(KAFKA_NETWORK);
        ConfluentKafkaContainer confluentKafkaContainer = KafkaFactoryTest.createConfluentKafkaContainer(CONFLUENT_PLATFORM_VERSION, KAFKA_LISTENER_PORT, network);
        GenericContainer<?> schemaRegistry = KafkaFactoryTest.createSchemaRegistryContainer(CONFLUENT_PLATFORM_VERSION, SCHEMA_REGISTRY_PORT, network, confluentKafkaContainer, KAFKA_LISTENER_PORT);

        schemaRegistry.start();
        String schemaRegistryBaseUrl = String.format("http://%s:%d", schemaRegistry.getHost(), schemaRegistry.getMappedPort(SCHEMA_REGISTRY_PORT));

        // Create audit events topic
        String topicName = "audit_events";
        String consumerGroupId = "audit_consumer";

        KafkaFuture<Uuid> uuidKafkaFuture = KafkaFactoryTest.kafkaCreateTopic(confluentKafkaContainer.getBootstrapServers(), topicName, 1, (short) 1);
        await().until(uuidKafkaFuture::isDone);
        log.info("Created topic [{}] with uuid [{}]", topicName, uuidKafkaFuture.get().toString());

        // Setup kafka template
        KafkaTemplate<String, Event.AuditEventMessage> kafkaTemplate = KafkaFactoryTest.kafkaCreateTemplate(confluentKafkaContainer.getBootstrapServers(), schemaRegistryBaseUrl);

        // Create record and send
        Event.AuditEventMessage producerEvent = createAuditEvent();
        ProducerRecord<String, Event.AuditEventMessage> producerRecord = new ProducerRecord<>(topicName, producerEvent);
        CompletableFuture<SendResult<String, Event.AuditEventMessage>> sendResult = kafkaTemplate.send(producerRecord);
        await().until(sendResult::isDone);

        try (KafkaConsumer<String, DynamicMessage> consumer = KafkaFactoryTest.kafkaCreateConsumer(confluentKafkaContainer.getBootstrapServers(), consumerGroupId, schemaRegistryBaseUrl)) {
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

            Assertions.assertFalse(protobufDeserializationError.get());
            Assertions.assertNotNull(auditEventMessage);
            Assertions.assertEquals(producerEvent.getObjectUid(), auditEventMessage.getObjectUid(), "Object uid match");
            listSchemaRegistry(schemaRegistryBaseUrl);
        }
    }

    private void listSchemaRegistry(String schemaRegistryBaseUrl) {
        CachedSchemaRegistryClient cachedSchemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryBaseUrl, 10);
        try {
            Collection<String> subjects = cachedSchemaRegistryClient.getAllSubjects();
            for (String subject : subjects) {
                log.info("Subject [{}]", subject);
                SchemaMetadata latestSchemaMetadata = cachedSchemaRegistryClient.getLatestSchemaMetadata(subject);
                log.info("Schema id [{}], type: [{}], schema [{}]", latestSchemaMetadata.getId(), latestSchemaMetadata.getSchemaType(), latestSchemaMetadata.getSchema());
            }
        } catch (Exception e) {
            log.error("listSchemaRegistry", e);
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