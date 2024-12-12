package com.acme.jga.infra.services.impl.events;

import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.events.v1.EventStatus;
import com.acme.jga.domain.model.exceptions.TechnicalException;
import com.acme.jga.infra.converters.AuditEventsInfraConverter;
import com.acme.jga.infra.dao.api.events.IEventsDao;
import com.acme.jga.infra.dto.events.v1.AuditEventDb;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.infra.services.impl.AbstractInfraService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

@Service
public class EventsInfraService extends AbstractInfraService implements IEventsInfraService {
    private static final String INSTRUMENTATION_NAME = EventsInfraService.class.getCanonicalName();
    private final IEventsDao eventsDao;
    private final AuditEventsInfraConverter auditEventsInfraConverter;
    private final ObjectMapper objectMapper;

    @Autowired
    public EventsInfraService(IEventsDao eventsDao, AuditEventsInfraConverter auditEventsInfraConverter, ObjectMapper objectMapper, OpenTelemetryWrapper openTelemetryWrapper) {
        super(openTelemetryWrapper);
        this.eventsDao = eventsDao;
        this.auditEventsInfraConverter = auditEventsInfraConverter;
        this.objectMapper = objectMapper;
    }

    @Override
    public String createEvent(AuditEvent auditEvent, Span parentSpan) throws TechnicalException {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_EVENTS_CREATE", parentSpan, () -> {
            try {
                AuditEventDb auditEventDb = auditEventsInfraConverter.convertAuditEventToDb(auditEvent, objectMapper);
                return eventsDao.insertEvent(auditEventDb);
            } catch (JsonProcessingException | SQLException e) {
                throw new TechnicalException("", e);
            }
        });
    }

    @Override
    public List<AuditEvent> findPendingEvents() {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_EVENTS_FIND_PENDING", null, () -> {
            List<AuditEventDb> auditEventDbs = eventsDao.findPendingEvents();
            return auditEventDbs.stream().map(auditEventsInfraConverter::convertAuditEventDbToDomain).toList();
        });
    }

    @Transactional
    @Override
    public Integer updateEventsStatus(List<String> uids, EventStatus eventStatus) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_EVENTS_UPDATE_STATUS", null, () -> eventsDao.updateEvents(uids, eventStatus));
    }


}
