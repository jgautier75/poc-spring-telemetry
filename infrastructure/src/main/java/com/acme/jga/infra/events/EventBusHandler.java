package com.acme.jga.infra.events;

import com.acme.jga.domain.model.events.protobuf.Event;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.events.v1.EventStatus;
import com.acme.jga.infra.config.KafkaProducerConfig;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.logging.services.api.ILogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.acme.jga.utils.lambdas.StreamUtil.ofNullableList;

@Service
@RequiredArgsConstructor
public class EventBusHandler implements MessageHandler, InitializingBean {
    private final KafkaProducerConfig kafkaProducerConfig;
    private final KafkaTemplate<String, Event.AuditEventMessage> kakaTemplateAudit;
    private final IEventsInfraService eventsInfraService;
    private final ILogService logService;
    private final PublishSubscribeChannel eventAuditChannel;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        eventAuditChannel.subscribe(this);
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String callerName = this.getClass().getName();
        logService.debugS(callerName, "Handling wake-up message", null);
        if (!isRunning.get()) {
            try {
                isRunning.set(true);
                List<AuditEvent> auditEvents = eventsInfraService.findPendingEvents();
                if (CollectionUtils.isEmpty(auditEvents)) {
                    logService.warnS(callerName, "No pending event to send", null);
                }
                auditEvents.forEach(auditEvent -> convertAndSend(auditEvent, callerName));
                markEventsAsProcessed(auditEvents);
            } finally {
                isRunning.set(false);
            }
        }
    }

    /**
     * Mark events as processed in RDBMS.
     *
     * @param auditEvents Audit Events
     */
    private void markEventsAsProcessed(List<AuditEvent> auditEvents) {
        List<String> uids = auditEvents.stream().map(AuditEvent::getUid).distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(uids)) {
            eventsInfraService.updateEventsStatus(uids, EventStatus.PROCESSED);
        }
    }

    /**
     * Convert event to protobuf format and publish in kefka topic.
     *
     * @param auditEvent Audit event
     * @param callerName Caller name
     */
    private void convertAndSend(AuditEvent auditEvent, String callerName) {
        try {
            Event.AuditEventMessage auditEventMessage = protobufConversion(auditEvent.getPayload());
            ProducerRecord<String, Event.AuditEventMessage> producerRecord = new ProducerRecord<>(kafkaProducerConfig.getTopicNameAuditEvents(), auditEvent.getObjectUid(), auditEventMessage);
            kakaTemplateAudit.send(producerRecord);
        } catch (JsonProcessingException e) {
            logService.error(callerName, e);
        }
    }

    /**
     * Convert audit event to protobuf format.
     *
     * @param payload JSON Payload
     * @return Audit event in protobuf format
     */
    private Event.AuditEventMessage protobufConversion(String payload) throws JsonProcessingException {
        Event.AuditEventMessage.Builder auditEventMessageBuilder = Event.AuditEventMessage.newBuilder();

        AuditEvent auditEvent = objectMapper.readValue(payload, AuditEvent.class);
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        auditEventMessageBuilder.setAction(auditEvent.getAction().name());
        auditEventMessageBuilder.setCreatedAt(auditEvent.getCreatedAt().atZone(ZoneOffset.UTC).format(isoFormatter));
        auditEventMessageBuilder.setLastUpdatedAt(auditEvent.getLastUpdatedAt().atZone(ZoneOffset.UTC).format(isoFormatter));
        auditEventMessageBuilder.setUid(auditEvent.getObjectUid());
        auditEventMessageBuilder.setObjectUid(auditEvent.getObjectUid());
        auditEventMessageBuilder.setAction(auditEvent.getAction().name());
        auditEventMessageBuilder.setStatus(auditEvent.getStatus().getValue());
        if (auditEvent.getTarget() != null) {
            auditEventMessageBuilder.setTarget(auditEvent.getTarget().getValue());
        }
        if (auditEvent.getAuthor() != null) {
            auditEventMessageBuilder.setAuthor(Event.AuditAuthor.newBuilder().setName(auditEvent.getAuthor().getName()).setUid(auditEvent.getAuthor().getUid()).build());
        }
        convertScope(auditEvent, auditEventMessageBuilder);
        List<Event.AuditChange> evtChanges = ofNullableList(auditEvent.getChanges()).map(EventBusHandler::convertAuditChange).toList();
        auditEventMessageBuilder.addAllChanges(evtChanges);
        return auditEventMessageBuilder.build();
    }

    private static Event.AuditChange convertAuditChange(AuditChange auditChange) {
        Event.AuditChange.Builder builder = Event.AuditChange.newBuilder();
        if (auditChange.getFrom() != null) {
            builder.setFrom(auditChange.getFrom());
        }
        if (auditChange.getTo() != null) {
            builder.setTo(auditChange.getTo());
        }
        if (auditChange.getObject() != null) {
            builder.setObject(auditChange.getObject());
        }
        if (auditChange.getOperation() != null) {
            builder.setOperation(auditChange.getOperation().name());
        }
        return builder.build();
    }

    private static void convertScope(AuditEvent auditEvent, Event.AuditEventMessage.Builder auditEventMessageBuilder) {
        if (auditEvent.getScope() != null) {
            Event.AuditScope.Builder auditScopeBuilder = Event.AuditScope
                    .newBuilder()
                    .setTenantUid(auditEvent.getScope().getTenantUid())
                    .setTenantName(auditEvent.getScope().getTenantName());
            if (auditEvent.getScope().getOrganizationName() != null) {
                auditScopeBuilder.setOrganizationName(auditEvent.getScope().getOrganizationName());
            }
            if (auditEvent.getScope().getOrganizationUid() != null) {
                auditScopeBuilder.setOrganizationUid(auditEvent.getScope().getOrganizationUid());
            }
            auditEventMessageBuilder.setScope(auditScopeBuilder.build());
        }
    }

}
