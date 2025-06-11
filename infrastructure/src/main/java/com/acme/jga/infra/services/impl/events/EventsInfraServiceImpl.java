package com.acme.jga.infra.services.impl.events;

import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.events.v1.EventStatus;
import com.acme.jga.domain.model.exceptions.TechnicalException;
import com.acme.jga.infra.converters.AuditEventsInfraConverter;
import com.acme.jga.infra.dao.api.events.EventsDao;
import com.acme.jga.infra.dto.events.v1.AuditEventDb;
import com.acme.jga.infra.services.api.events.EventsInfraService;
import com.acme.jga.infra.services.impl.AbstractInfraService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

@Service
public class EventsInfraServiceImpl extends AbstractInfraService implements EventsInfraService {
    private static final String INSTRUMENTATION_NAME = EventsInfraServiceImpl.class.getCanonicalName();
    private final EventsDao eventsDao;
    private final AuditEventsInfraConverter auditEventsInfraConverter;
    private final ObjectMapper objectMapper;

    @Autowired
    public EventsInfraServiceImpl(EventsDao eventsDao, AuditEventsInfraConverter auditEventsInfraConverter, ObjectMapper objectMapper, OpenTelemetryWrapper openTelemetryWrapper) {
        super(openTelemetryWrapper);
        this.eventsDao = eventsDao;
        this.auditEventsInfraConverter = auditEventsInfraConverter;
        this.objectMapper = objectMapper;
    }

    @Override
    public String createEvent(AuditEvent auditEvent) throws TechnicalException {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_EVENTS_CREATE", (span) -> {
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
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_EVENTS_FIND_PENDING",  (span) -> {
            List<AuditEventDb> auditEventDbs = eventsDao.findPendingEvents();
            return auditEventDbs.stream().map(auditEventsInfraConverter::convertAuditEventDbToDomain).toList();
        });
    }

    @Transactional
    @Override
    public Integer updateEventsStatus(List<String> uids, EventStatus eventStatus) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_EVENTS_UPDATE_STATUS", (span) -> eventsDao.updateEvents(uids, eventStatus));
    }


}
