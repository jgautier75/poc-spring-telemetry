package com.acme.jga.domain.functions.organizations;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.impl.events.EventsInfraServiceImpl;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;

import java.util.List;

import static com.acme.jga.domain.model.utils.AuditEventFactory.createOrganizationAuditEvent;
import static com.acme.jga.domain.model.utils.AuditEventFactory.createSectorAuditEvent;

public class AbstractOrganizationFunction extends DomainFunction {
    private final EventsInfraServiceImpl eventsInfraServiceImpl;

    public AbstractOrganizationFunction(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, EventsInfraServiceImpl eventsInfraServiceImpl) {
        super(openTelemetryWrapper, bundleFactory);
        this.eventsInfraServiceImpl = eventsInfraServiceImpl;
    }

    /**
     * Create and persist audit event, send wake-up message.
     *
     * @param org          Organization
     * @param tenant       Tenant
     * @param span         Span
     * @param auditAction  Action
     * @param auditChanges Changes
     */
    protected void generateOrgAuditEventAndPush(Organization org, Tenant tenant, Span span, AuditAction auditAction, List<AuditChange> auditChanges) {
        AuditEvent orgAuditEvent = createOrganizationAuditEvent(org, tenant, auditAction, auditChanges);
        eventsInfraServiceImpl.createEvent(orgAuditEvent, span);
    }

    /**
     * Generate sector audit event and send wake up message.
     *
     * @param org          Organization
     * @param tenant       Tenant
     * @param sector       Sector
     * @param span         OpenTelemetry span
     * @param auditAction  Audit action
     * @param auditChanges Audit changes
     */
    protected void generateSectorAuditEventAndPush(Organization org, Tenant tenant, Sector sector, Span span, AuditAction auditAction, List<AuditChange> auditChanges) {
        AuditEvent sectorAuditEvent = createSectorAuditEvent(sector.getUid(), org, tenant, auditAction, auditChanges);
        eventsInfraServiceImpl.createEvent(sectorAuditEvent, span);
    }

}
