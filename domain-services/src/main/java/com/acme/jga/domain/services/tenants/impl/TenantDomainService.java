package com.acme.jga.domain.services.tenants.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.events.EventBuilderTenant;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.events.v1.AuditOperation;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.services.AbstractDomainService;
import com.acme.jga.domain.services.tenants.api.ITenantDomainService;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.infra.services.api.tenants.ITenantInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.acme.jga.domain.model.utils.AuditEventFactory.createTenantAuditEvent;

@Service
public class TenantDomainService extends AbstractDomainService implements ITenantDomainService {
    private static final String INSTRUMENTATION_NAME = TenantDomainService.class.getCanonicalName();
    private final ITenantInfraService tenantInfraService;
    private final ILogService logService;
    private final IEventsInfraService eventsInfraService;
    private final EventBuilderTenant eventBuilderTenant;

    public TenantDomainService(ITenantInfraService tenantInfraService, ILogService logService, BundleFactory bundleFactory,
                               IEventsInfraService eventsInfraService, OpenTelemetryWrapper openTelemetryWrapper, EventBuilderTenant eventBuilderTenant) {
        super(openTelemetryWrapper, bundleFactory);
        this.tenantInfraService = tenantInfraService;
        this.logService = logService;
        this.eventsInfraService = eventsInfraService;
        this.eventBuilderTenant = eventBuilderTenant;
    }

    @Override
    @Audited
    @Transactional
    public CompositeId createTenant(Tenant tenant, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_CREATE", parentSpan, (span) -> {
            String callerName = this.getClass().getName() + "-createTenant";
            boolean alreadyExist = tenantInfraService.tenantExistsByCode(tenant.getCode(), span);
            if (alreadyExist) {
                throwWrappedException(FunctionalErrorsTypes.TENANT_CODE_ALREADY_USED.name(), "tenant_code_already_used", new Object[]{tenant.getCode()});
            }
            if ("crash".equals(tenant.getCode())) {
                throw new NullPointerException("Fake error");
            }
            CompositeId compositeId = tenantInfraService.createTenant(tenant, span);
            tenant.setUid(compositeId.getUid());
            logService.infoS(callerName, "Created tenant [%s]", new Object[]{compositeId.getUid()});

            // Create audit event and send
            List<AuditChange> auditChanges = List.of(AuditChange.builder().to(tenant.getLabel()).object("label").operation(AuditOperation.ADD).build());
            generateTenantAuditEventAndPush(tenant, AuditAction.CREATE, auditChanges);
            return compositeId;
        });
    }

    @Override
    public Tenant findTenantByUid(String uid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_FIND_UID", parentSpan, (span) -> {
            Optional<Tenant> tenant = tenantInfraService.findTenantByUid(uid, span);
            if (tenant.isEmpty()) {
                throwWrappedException(FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), "tenant_not_found_by_uid", new Object[]{uid});
            }
            return tenant.get();
        });
    }

    @Override
    public List<Tenant> findAllTenants(Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_FIND_ALL", parentSpan, tenantInfraService::findAllTenants);
    }

    @Override
    @Transactional
    @Audited
    public Integer updateTenant(Tenant tenant, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_UPDATE", parentSpan, (span) -> {
            String callerName = this.getClass().getName() + "-updateTenant";
            logService.infoS(callerName, "Updating tenant [%s] ", new Object[]{tenant.getUid()});
            // Ensure tenant already exists
            Tenant rbdmsTenant = findTenantByUid(tenant.getUid(), span);
            tenant.setId(rbdmsTenant.getId());
            // Tenant update
            Integer nbRowsUpdated = tenantInfraService.updateTenant(tenant, span);
            // Create audit event
            List<AuditChange> auditChanges = eventBuilderTenant.buildAuditsChange(rbdmsTenant, tenant);
            generateTenantAuditEventAndPush(tenant, AuditAction.UPDATE, auditChanges);
            return nbRowsUpdated;
        });
    }

    @Override
    @Transactional
    @Audited
    public Integer deleteTenant(String tenantUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_DELETE", parentSpan, (span) -> {
            String callerName = this.getClass().getName() + "-deleteTenant";
            logService.infoS(callerName, "Delete tenant [%s]", new Object[]{tenantUid});
            // Find tenant
            Tenant tenant = findTenantByUid(tenantUid, span);

            // Delete users by tenantId
            logService.debugS(callerName, "Delete users for tenant [%s]", new Object[]{tenantUid});
            tenantInfraService.deleteUsersByTenantId(tenant.getId(), span);

            // Delete sectors by tenant id
            logService.debugS(callerName, "Delete sectors for tenant [%s]", new Object[]{tenantUid});
            tenantInfraService.deleteSectorsByTenantId(tenant.getId(), span);

            // Delete organizations by tenant id
            logService.debugS(callerName, "Delete organizations for tenant [%s]", new Object[]{tenantUid});
            tenantInfraService.deleteOrganizationsByTenantId(tenant.getId(), span);

            // Delete tenant
            logService.debugS(callerName, "Delete tenant [%s] itself", new Object[]{tenantUid});
            Integer nbDeleted = tenantInfraService.deleteTenant(tenant.getId(), span);

            // Create audit event
            generateTenantAuditEventAndPush(tenant, AuditAction.DELETE, Collections.emptyList());
            return nbDeleted;
        });
    }

    /**
     * Create, persist and send audit event.
     *
     * @param tenant Tenant
     * @param action Action
     */
    private void generateTenantAuditEventAndPush(Tenant tenant, AuditAction action, List<AuditChange> auditChanges) {
        AuditEvent auditEvent = createTenantAuditEvent(tenant.getUid(), tenant, action);
        auditEvent.setChanges(auditChanges);
        String eventUid = eventsInfraService.createEvent(auditEvent, null);
        logService.debugS(INSTRUMENTATION_NAME, "Created event [%s]", new Object[]{eventUid});
    }

}
