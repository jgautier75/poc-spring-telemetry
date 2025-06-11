package com.acme.jga.domain.functions.users;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.events.v1.AuditOperation;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.infra.services.api.events.EventsInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;

import java.util.ArrayList;
import java.util.List;

import static com.acme.jga.domain.model.utils.AuditEventFactory.createUserAuditEvent;

public abstract class AbstractUserFunction extends DomainFunction {
    private final EventsInfraService eventsInfraService;

    protected AbstractUserFunction(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, EventsInfraService eventsInfraService) {
        super(openTelemetryWrapper, bundleFactory);
        this.eventsInfraService = eventsInfraService;
    }

    /**
     * Manage and persist audit events then send wake-up message.
     *
     * @param user   User
     * @param tenant Tenant
     * @param org    Organization
     * @param span   Span
     */
    protected void generateUserAuditEventAndPush(User user, Tenant tenant, Organization org, AuditAction auditAction, Span span, List<AuditChange> changes) {
        AuditEvent userAuditEvent = createUserAuditEvent(user, auditAction, tenant, org);
        userAuditEvent.setChanges(changes);
        eventsInfraService.createEvent(userAuditEvent, span);
    }

    protected List<AuditChange> createAuditChanges(User user) {
        List<AuditChange> auditChanges = new ArrayList<>();
        auditChanges.add(AuditChange.builder().to(user.getCommons().getFirstName()).object("firstName").operation(AuditOperation.ADD).build());
        auditChanges.add(AuditChange.builder().to(user.getCommons().getLastName()).object("lastName").operation(AuditOperation.ADD).build());
        auditChanges.add(AuditChange.builder().to(user.getCredentials().getEmail()).object("email").operation(AuditOperation.ADD).build());
        return auditChanges;
    }

}
