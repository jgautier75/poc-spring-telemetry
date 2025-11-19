package com.acme.jga.keycloak.spi.kafka.impl;

import com.acme.jga.keycloak.spi.kafka.KafkaConsumerProvider;
import com.acme.jga.keycloak.spi.kafka.utils.KafkaConsumerConstants;
import com.acme.users.mgt.events.protobuf.Event;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jboss.logging.Logger;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.storage.StorageId;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.StreamSupport;

/**
 * Kafka consumer provider implementation.
 */
public class KafkaConsumerProviderImpl implements KafkaConsumerProvider {
    private static final Logger LOGGER = Logger.getLogger(KafkaConsumerProviderImpl.class);
    private static final Long POLL_DURATION = 5_000L;
    private final KeycloakSessionFactory keycloakSessionFactory;
    private KafkaConsumer<String, DynamicMessage> kafkaConsumer;

    public KafkaConsumerProviderImpl(KeycloakSessionFactory keycloakSessionFactory) {
        this.keycloakSessionFactory = keycloakSessionFactory;
    }

    /**
     * Start kafka consumer.
     */
    public void startKafkaConsumer() {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                this.kafkaConsumer = new KafkaConsumer<>(consumerProperties());
                kafkaConsumer.subscribe(List.of(getEnvVariable(KafkaConsumerConstants.TOPIC_NAME)));
                while (true) {
                    ConsumerRecords<String, DynamicMessage> consumerRecords = kafkaConsumer
                            .poll(Duration.ofMillis(POLL_DURATION));
                    processRecords(consumerRecords);
                }
            });
        } catch (Exception e) {
            LOGGER.debug("Error starting Kafka Consumer", e);
        }
    }

    /**
     * Process kafka records.
     *
     * @param consumerRecords Records
     */
    private void processRecords(ConsumerRecords<String, DynamicMessage> consumerRecords) {
        List<Event.AuditEventMessage> userEvents = new ArrayList<>();
        List<String> eventTenants = new ArrayList<>();
        List<String> knownTenants = new ArrayList<>();

        StreamSupport.stream(consumerRecords.records(getEnvVariable(KafkaConsumerConstants.TOPIC_NAME)).spliterator(), false).forEach(record -> {
            try {
                Event.AuditEventMessage auditEventMessage = Event.AuditEventMessage.newBuilder().build()
                        .getParserForType().parseFrom(record.value().toByteArray());
                if (isUserUpdateEvent(auditEventMessage)) {
                    userEvents.add(auditEventMessage);
                    eventTenants.add(auditEventMessage.getScope().getTenantCode());
                }
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error("Error parsing event", e);
            }
        });

        eventTenants.forEach(tenantName -> {
            if (!knownTenants.contains(tenantName)) {
                try (KeycloakSession session = keycloakSessionFactory.create()) {
                    JpaConnectionProvider jpaConnectionProvider = session.getProvider(JpaConnectionProvider.class);
                    List<String> entities = findRealm(jpaConnectionProvider, tenantName);
                    if (!entities.isEmpty()) {
                        knownTenants.add(tenantName);
                    } else {
                        LOGGER.error("Unable to find any entity for tenant " + tenantName);
                    }
                }
            }
        });

        final List<UserEntity> userEntities = new ArrayList<>();
        userEvents.forEach(userEvent -> {
            if (knownTenants.contains(userEvent.getScope().getTenantCode())) {
                KeycloakModelUtils.runJobInTransaction(keycloakSessionFactory, session -> {
                    JpaConnectionProvider jpaConnectionProvider = session.getProvider(JpaConnectionProvider.class);
                    userEntities.add(updateUser(userEvent, jpaConnectionProvider));
                });
            }
        });

        KeycloakModelUtils.runJobInTransaction(keycloakSessionFactory, session -> {
            JpaConnectionProvider jpaConnectionProvider = session.getProvider(JpaConnectionProvider.class);
            EntityTransaction tx = jpaConnectionProvider.getEntityManager().getTransaction();
            tx.begin();
            jpaConnectionProvider.getEntityManager().merge(userEntities);
            tx.commit();
        });
    }

    /**
     * Find realm.
     *
     * @param jpaConnectionProvider Jpa provider
     * @param tenantName            Tenant
     * @return Realm id list
     */
    private static List<String> findRealm(JpaConnectionProvider jpaConnectionProvider, String tenantName) {
        TypedQuery<String> query = jpaConnectionProvider.getEntityManager().createNamedQuery("getRealmIdByName",
                String.class);
        query.setParameter("name", tenantName);
        return query.getResultList();
    }

    /**
     * Update user properties.
     *
     * @param auditEventMessage     Audit event
     * @param jpaConnectionProvider Jpa provider
     */
    private UserEntity updateUser(Event.AuditEventMessage auditEventMessage, JpaConnectionProvider jpaConnectionProvider) {
        LOGGER.infof("Storage util, search user width id [%s]",
                StorageId.externalId(auditEventMessage.getObjectUid()));
        UserEntity userEntity = jpaConnectionProvider.getEntityManager().find(UserEntity.class,
                StorageId.externalId(auditEventMessage.getObjectUid()));

        LOGGER.infof("UserLocal [%s]", userEntity != null ? userEntity.getUsername() : "null");
        if (userEntity != null) {
            LOGGER.infof("Audit nb of changes [%s]", auditEventMessage.getChangesCount());
            auditEventMessage.getChangesList().forEach(auditChange -> {
                LOGGER.infof("Processing audit change with object [%s]", auditChange.getObject());
                if (KafkaConsumerConstants.EVENT_CHANGE_FIRST_NAME.equals(auditChange.getObject())) {
                    LOGGER.infof("Updating firstName to [%s]", auditChange.getTo());
                    userEntity.setFirstName(auditChange.getTo());
                } else if (KafkaConsumerConstants.EVENT_CHANGE_LAST_NAME.equals(auditChange.getObject())) {
                    LOGGER.infof("Updating lastName to [%s]", auditChange.getTo());
                    userEntity.setLastName(auditChange.getTo());
                } else if (KafkaConsumerConstants.EVENT_CHANGE_EMAIL.equals(auditChange.getObject())) {
                    LOGGER.infof("Updating email to [%s]", auditChange.getTo());
                    userEntity.setEmail(auditChange.getTo(), false);
                }
            });
        }
        return userEntity;
    }

    /**
     * Build consumer properties.
     *
     * @return Consumer properties
     */
    private Map<String, Object> consumerProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getEnvVariable(KafkaConsumerConstants.BOOTSTRAP_SERVERS));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, getEnvVariable(KafkaConsumerConstants.GROUP_ID));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                getEnvVariable(KafkaConsumerConstants.SHEMA_REGISTRY));
        LOGGER.infof("Initializing kafka consumer with properties [%s]", props);
        return props;
    }

    /**
     * Get environment variable.
     *
     * @param varName Variable name
     * @return Variable value
     */
    private String getEnvVariable(String varName) {
        return System.getenv(varName);
    }

    @Override
    public void close() {
        if (this.kafkaConsumer != null) {
            try {
                this.kafkaConsumer.close();
            } catch (Exception e) {
                // Silent catch
            }
        }
    }

    private boolean isUserUpdateEvent(Event.AuditEventMessage auditEventMessage) {
        return KafkaConsumerConstants.EVENT_USER == auditEventMessage.getTarget().getNumber() && KafkaConsumerConstants.EVENT_UPDATE.equals(auditEventMessage.getAction().name());
    }

}
