package com.acme.jga.keycloak.spi.kafka;

import org.keycloak.provider.Provider;

public interface KafkaConsumerProvider extends Provider {
    void startKafkaConsumer();
}
