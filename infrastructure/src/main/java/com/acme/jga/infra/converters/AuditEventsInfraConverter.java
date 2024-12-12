package com.acme.jga.infra.converters;

import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.exceptions.TechnicalException;
import com.acme.jga.infra.dto.events.v1.AuditEventDb;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditEventsInfraConverter {

    public AuditEventDb convertAuditEventToDb(AuditEvent auditEvent, ObjectMapper objectMapper)
            throws JsonProcessingException {
        return Optional.ofNullable(auditEvent).map(a -> {
            try {
                String payload = objectMapper.writeValueAsString(auditEvent);
                return AuditEventDb.builder()
                        .action(auditEvent.getAction())
                        .createdAt(auditEvent.getCreatedAt())
                        .lastUpdatedAt(auditEvent.getLastUpdatedAt())
                        .objectUid(auditEvent.getObjectUid())
                        .payload(payload)
                        .status(auditEvent.getStatus())
                        .target(auditEvent.getTarget())
                        .uid(auditEvent.getUid())
                        .build();
            } catch (JsonProcessingException e) {
                throw new TechnicalException("Unable to serialize object", e);
            }
        }).orElse(null);
    }

    public AuditEvent convertAuditEventDbToDomain(AuditEventDb auditEventDb) {
        return Optional.ofNullable(auditEventDb).map(a -> AuditEvent.builder()
                .action(auditEventDb.getAction())
                .createdAt(auditEventDb.getCreatedAt())
                .lastUpdatedAt(auditEventDb.getLastUpdatedAt())
                .objectUid(auditEventDb.getObjectUid())
                .payload(auditEventDb.getPayload())
                .status(auditEventDb.getStatus())
                .target(auditEventDb.getTarget())
                .uid(auditEventDb.getUid())
                .build()).orElse(null);
    }

}
