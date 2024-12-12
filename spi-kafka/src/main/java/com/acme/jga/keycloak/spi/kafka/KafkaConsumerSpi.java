package com.acme.jga.keycloak.spi.kafka;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class KafkaConsumerSpi implements Spi {
    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "kafka-consumer-spi";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return KafkaConsumerProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return KafkaConsumerProviderFactory.class;
    }
}
