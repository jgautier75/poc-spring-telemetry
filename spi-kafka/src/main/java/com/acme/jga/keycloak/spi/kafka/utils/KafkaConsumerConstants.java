package com.acme.jga.keycloak.spi.kafka.utils;

public class KafkaConsumerConstants {
    private KafkaConsumerConstants() {
        // Hidden constructor for utility class
    }

    public static final String SPI_NAME = "kafka-consumer-provider";
    public static final String FACTORY_NAME = "kafka-consumer-factory";
    public static final String TOPIC_NAME = "kafka_consumer_topic";
    public static final String BOOTSTRAP_SERVERS = "kafka_consumer_bootstrap_servers";
    public static final String GROUP_ID = "kafka_consumer_group_id";
    public static final String SHEMA_REGISTRY = "kafka_consumer_schema_registry_url";
    public static final int EVENT_USER = 2;
    public static final String EVENT_UPDATE = "UPDATE";
    public static final String EVENT_CHANGE_FIRST_NAME = "commons_firstName";
    public static final String EVENT_CHANGE_LAST_NAME = "commons_lastName";
    public static final String EVENT_CHANGE_EMAIL = "credentials_email";
}
