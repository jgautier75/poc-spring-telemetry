package com.acme.jga.domain.functions.tenants;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.events.EventsInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;

import java.util.List;

import static com.acme.jga.domain.model.utils.AuditEventFactory.createTenantAuditEvent;

public abstract class AbstractTenantFunction extends DomainFunction {
    protected final EventsInfraService eventsInfraService;

    public AbstractTenantFunction(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, EventsInfraService eventsInfraService) {
        super(openTelemetryWrapper, bundleFactory);
        this.eventsInfraService = eventsInfraService;
    }

    protected String generateTenantAuditEventAndPush(Tenant tenant, AuditAction action, List<AuditChange> auditChanges) {
        AuditEvent auditEvent = createTenantAuditEvent(tenant.getUid(), tenant, action);
        auditEvent.setChanges(auditChanges);
        return eventsInfraService.createEvent(auditEvent, null);
    }
}
