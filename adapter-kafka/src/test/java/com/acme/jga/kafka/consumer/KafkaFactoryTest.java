package com.acme.jga.kafka.consumer;

import com.acme.users.mgt.events.protobuf.Event;
import com.google.protobuf.DynamicMessage;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Uuid;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.runner.Description;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KafkaFactoryTest {

    public static Network createNetwork(String networkId) {
        Network defaultDaprNetwork = new Network() {
            @Override
            public org.junit.runners.model.Statement apply(org.junit.runners.model.Statement base, Description description) {
                return null;
            }

            @Override
            public String getId() {
                return networkId;
            }

            @Override
            public void close() {
            }
        };
        List<com.github.dockerjava.api.model.Network> networks = DockerClientFactory.instance().client().listNetworksCmd().withNameFilter(networkId).exec();
        if (networks.isEmpty()) {
            Network.builder()
                    .createNetworkCmdModifier(cmd -> cmd.withName(networkId))
                    .build().getId();
            return defaultDaprNetwork;
        } else {
            return defaultDaprNetwork;
        }
    }

    public static ConfluentKafkaContainer createConfluentKafkaContainer(String version, int listenPort, Network network) {
        return new ConfluentKafkaContainer("confluentinc/cp-kafka:" + version)
                .withListener("kafka:" + listenPort)
                .withNetwork(network)
                .withReuse(true);
    }

    public static GenericContainer<?> createSchemaRegistryContainer(String version, int listenPort, Network network,
                                                                    ConfluentKafkaContainer confluentKafkaContainer, int kafkaListenPort) {
        return new GenericContainer<>("confluentinc/cp-schema-registry:" + version)
                .withExposedPorts(listenPort)
                .dependsOn(confluentKafkaContainer)
                .withNetworkAliases("schemaregistry")
                .withNetwork(network)
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:" + kafkaListenPort)
                .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:" + listenPort)
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schemaregistry")
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_SECURITY_PROTOCOL", "PLAINTEXT")
                .waitingFor(Wait.forHttp("/subjects"))
                .withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS));
    }

    public static KafkaTemplate<String, Event.AuditEventMessage> kafkaCreateTemplate(String bootstrapServers, String schemaRegistryBaseUrl) {
        return new KafkaTemplate<>(producerFactory(bootstrapServers, schemaRegistryBaseUrl));
    }

    private static ProducerFactory<String, Event.AuditEventMessage> producerFactory(String bootstrapServers, String schemaRegistryBaseUrl) {
        return new DefaultKafkaProducerFactory<>(producerConfigs(bootstrapServers, schemaRegistryBaseUrl));
    }

    private static Map<String, Object> producerConfigs(String bootstrapServers, String schemaRegistryBaseUrl) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class);
        props.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryBaseUrl);
        return props;
    }

    public static KafkaFuture<Uuid> kafkaCreateTopic(String bootStrapServers, String topicName, int nbOfPartitions, short replicationFactor) {
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

    public static KafkaConsumer<String, DynamicMessage> kafkaCreateConsumer(String bootstrapServers, String consumerGroupId, String schemaRegistryBaseUrl) {
        return new KafkaConsumer<>(consumerConfigs(bootstrapServers, consumerGroupId, schemaRegistryBaseUrl));
    }

    private static ConsumerFactory<Integer, String> consumerFactory(String bootstrapServers, String clientId, String schemaRegistryBaseUrl) {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(bootstrapServers, clientId, schemaRegistryBaseUrl));
    }

    private static KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Integer, String>> kafkaListenerContainerFactory(String bootstrapServers, String clientId, String schemaRegistryBaseUrl) {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(bootstrapServers, clientId, schemaRegistryBaseUrl));
        factory.setConcurrency(1);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    private static Map<String, Object> consumerConfigs(String bootstrapServers, String clientId, String schemaRegistryBaseUrl) {
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

}
