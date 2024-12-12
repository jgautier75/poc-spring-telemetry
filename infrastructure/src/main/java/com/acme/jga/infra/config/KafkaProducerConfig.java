package com.acme.jga.infra.config;

import com.acme.jga.domain.model.events.protobuf.Event;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.kafka.producer")
@AllArgsConstructor(access = AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.NONE)
@Data
@EnableKafka
public class KafkaProducerConfig {
    public static final String AUDIT_WAKE_UP = "wakeUp";
    private List<String> bootstrapServers;
    private String schemaRegistry;
    private String acks;
    private String clientId;
    private Integer retries;
    private String topicNameAuditEvents;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class);
        // Uncomment for batch sending
        //props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1);
        //props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);
        return props;
    }

    @Bean
    public ProducerFactory<String, Event.AuditEventMessage> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean(name = "kakaTemplateAudit")
    public KafkaTemplate<String, Event.AuditEventMessage> kakaTemplateAudit() {
        return new KafkaTemplate<>(producerFactory());
    }

}
