package com.acme.jga.keycloak.spi.kafka.impl;

import com.acme.jga.keycloak.spi.kafka.KafkaConsumerProvider;
import com.acme.jga.keycloak.spi.kafka.KafkaConsumerProviderFactory;
import com.acme.jga.keycloak.spi.kafka.utils.KafkaConsumerConstants;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.PostMigrationEvent;

/**
 * Kafka consumer provider factory implementation.
 */
public class KafkaConsumerProviderFactoryImpl implements KafkaConsumerProviderFactory {
    private static final Logger LOGGER = Logger.getLogger(KafkaConsumerProviderFactoryImpl.class);

    @Override
    public KafkaConsumerProvider create(KeycloakSession session) {
        LOGGER.debug("Creating KafkaConsumer implementation");
        return new KafkaConsumerProviderImpl(session.getKeycloakSessionFactory());
    }

    @Override
    public void init(Config.Scope config) {
        LOGGER.debug("Init KafkaConsumerProviderFactory");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        factory.register((event) -> {
            if (event instanceof PostMigrationEvent) {
                try (KeycloakSession s = factory.create()) {
                    s.getProvider(KafkaConsumerProvider.class).startKafkaConsumer();
                }
            }
        });
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return KafkaConsumerConstants.FACTORY_NAME;
    }

}
