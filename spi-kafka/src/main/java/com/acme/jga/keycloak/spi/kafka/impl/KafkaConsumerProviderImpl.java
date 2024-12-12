package com.acme.jga.keycloak.spi.kafka.impl;

import com.acme.jga.keycloak.spi.kafka.KafkaConsumerProvider;
import com.acme.jga.keycloak.spi.kafka.model.Event;
import com.acme.jga.keycloak.spi.kafka.utils.KafkaConsumerConstants;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import jakarta.persistence.TypedQuery;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jboss.logging.Logger;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.storage.StorageId;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka consumer provider implementation.
 */
public class KafkaConsumerProviderImpl implements KafkaConsumerProvider {
    private static final Logger LOGGER = Logger.getLogger(KafkaConsumerProviderImpl.class);
    private KeycloakSessionFactory keycloakSessionFactory;
    private KafkaConsumer<String, DynamicMessage> kafkaConsumer;

    public KafkaConsumerProviderImpl(KeycloakSessionFactory keycloakSessionFactory) {
        this.keycloakSessionFactory = keycloakSessionFactory;
    }

    /**
     * Start kafka consumer.
     */
    public void startKafkaConsumer() {
        // Start kafka consumer in a separate thread
        new Thread(() -> {
            LOGGER.debug("Start kafka consumer, lookup JpaConnectionProvider");
            this.kafkaConsumer = new KafkaConsumer<>(consumerProperties());
            kafkaConsumer.subscribe(List.of(getEnvVariable(KafkaConsumerConstants.TOPIC_NAME)));
            while (true) {
                ConsumerRecords<String, DynamicMessage> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(5000));
                processRecords(consumerRecords);
            }
        }).start();
    }

    /**
     * Process kafka records.
     *
     * @param consumerRecords Records
     */
    private void processRecords(ConsumerRecords<String, DynamicMessage> consumerRecords) {
        for (ConsumerRecord<String, DynamicMessage> consumerRecord : consumerRecords) {
            KeycloakModelUtils.runJobInTransaction(keycloakSessionFactory, session -> {
                try {
                    JpaConnectionProvider jpaConnectionProvider = session.getProvider(JpaConnectionProvider.class);
                    Event.AuditEventMessage auditEventMessage = Event.AuditEventMessage.newBuilder().build().getParserForType().parseFrom(consumerRecord.value().toByteArray());
                    LOGGER.infof("Processing audit event for tenant [%s] , organization [%s], target [%s], action [%s], object [%s]",
                            auditEventMessage.getScope().getTenantName(),
                            auditEventMessage.getScopeOrBuilder().getOrganizationName(),
                            auditEventMessage.getTarget(),
                            auditEventMessage.getAction(),
                            auditEventMessage.getObjectUid()
                    );
                    //Process only user audit events with update action (FirstName, LastName & Email update)
                    if (KafkaConsumerConstants.EVENT_USER == auditEventMessage.getTarget() && KafkaConsumerConstants.EVENT_UPDATE.equals(auditEventMessage.getAction())) {
                        // Ensure tenant (equals realm) exists otherwise skip message
                        String tenantName = auditEventMessage.getScope().getTenantName();
                        List<String> entities = findRealm(jpaConnectionProvider, tenantName);
                        if (!entities.isEmpty()) {
                            updateUser(auditEventMessage, jpaConnectionProvider);
                        }
                    }
                } catch (InvalidProtocolBufferException e) {
                    LOGGER.error(e);
                }
            });
        }
    }

    /**
     * Find realm.
     *
     * @param jpaConnectionProvider Jpa provider
     * @param tenantName            Tenant
     * @return Realm id list
     */
    private static List<String> findRealm(JpaConnectionProvider jpaConnectionProvider, String tenantName) {
        TypedQuery<String> query = jpaConnectionProvider.getEntityManager().createNamedQuery("getRealmIdByName", String.class);
        query.setParameter("name", tenantName);
        return query.getResultList();
    }

    /**
     * Update user properties.
     *
     * @param auditEventMessage     Audit event
     * @param jpaConnectionProvider Jpa provider
     */
    private void updateUser(Event.AuditEventMessage auditEventMessage, JpaConnectionProvider jpaConnectionProvider) {
        LOGGER.debugf("Storage util, search user width id [%s]", StorageId.externalId(auditEventMessage.getObjectUid()));
        UserEntity userEntity = jpaConnectionProvider.getEntityManager().find(UserEntity.class, StorageId.externalId(auditEventMessage.getObjectUid()));
        LOGGER.debugf("UserLocal [%s]", userEntity != null ? userEntity.getUsername() : "null");
        if (userEntity != null) {
            auditEventMessage.getChangesList().forEach(auditChange -> {
                if (KafkaConsumerConstants.EVENT_CHANGE_FIRST_NAME.equals(auditChange.getObject())) {
                    LOGGER.debugf("Updating firstName to [%s]", auditChange.getTo());
                    userEntity.setFirstName(auditChange.getTo());
                } else if (KafkaConsumerConstants.EVENT_CHANGE_LAST_NAME.equals(auditChange.getObject())) {
                    LOGGER.debugf("Updating lastName to [%s]", auditChange.getTo());
                    userEntity.setLastName(auditChange.getTo());
                } else if (KafkaConsumerConstants.EVENT_CHANGE_EMAIL.equals(auditChange.getObject())) {
                    LOGGER.debugf("Updating email to [%s]", auditChange.getTo());
                    userEntity.setEmail(auditChange.getTo(), false);
                }
            });
            jpaConnectionProvider.getEntityManager().persist(userEntity);
        }
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
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, getEnvVariable(KafkaConsumerConstants.SHEMA_REGISTRY));
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

}
