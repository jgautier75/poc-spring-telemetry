package com.acme.jga.domain.functions.sectors;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;

import java.util.List;

import static com.acme.jga.domain.model.utils.AuditEventFactory.createSectorAuditEvent;

public abstract class AbstractSectorFunction extends DomainFunction {
    private IEventsInfraService eventsInfraService;

    public AbstractSectorFunction() {
        // Default constructor
    }

    public AbstractSectorFunction(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, IEventsInfraService eventsInfraService) {
        super(openTelemetryWrapper, bundleFactory);
        this.eventsInfraService = eventsInfraService;
    }

    protected void generateSectorAuditEventAndPush(Sector sector, Organization organization, Tenant tenant, AuditAction auditAction, Span span, List<AuditChange> auditChanges) {
        AuditEvent sectorAuditEvent = createSectorAuditEvent(sector.getUid(), organization, tenant, auditAction, auditChanges);
        eventsInfraService.createEvent(sectorAuditEvent, span);
    }

}
