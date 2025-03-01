package com.acme.jga.infra.services.api.events;

import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.events.v1.EventStatus;
import com.acme.jga.domain.model.exceptions.TechnicalException;
import io.opentelemetry.api.trace.Span;

import java.util.List;

public interface EventsInfraService {
    String createEvent(AuditEvent auditEvent, Span parentSpan) throws TechnicalException;

    List<AuditEvent> findPendingEvents();

    Integer updateEventsStatus(List<String> uids, EventStatus eventStatus);
}
