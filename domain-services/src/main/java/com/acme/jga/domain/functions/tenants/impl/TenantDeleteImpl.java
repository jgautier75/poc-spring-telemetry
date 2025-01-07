package com.acme.jga.domain.functions.tenants.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.functions.tenants.AbstractTenantFunction;
import com.acme.jga.domain.functions.tenants.api.TenantDelete;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.infra.services.api.tenants.ITenantInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class TenantDeleteImpl extends AbstractTenantFunction implements TenantDelete {
    private static final String INSTRUMENTATION_NAME = TenantDeleteImpl.class.getCanonicalName();
    private final ILogService logService;
    private final TenantFind tenantFind;
    private final ITenantInfraService tenantInfraService;

    public TenantDeleteImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                            IEventsInfraService eventsInfraService, ILogService logService,
                            TenantFind tenantFind, ITenantInfraService tenantInfraService) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.logService = logService;
        this.tenantFind = tenantFind;
        this.tenantInfraService = tenantInfraService;
    }

    @Override
    @Transactional
    @Audited
    public Integer execute(String uid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_DELETE", parentSpan, (span) -> {
            String callerName = this.getClass().getName() + "-deleteTenant";
            logService.infoS(callerName, "Delete tenant [%s]", new Object[]{uid});
            // Find tenant
            Tenant tenant = tenantFind.byUid(uid, span);

            // Delete users by tenantId
            logService.debugS(callerName, "Delete users for tenant [%s]", new Object[]{uid});
            tenantInfraService.deleteUsersByTenantId(tenant.getId(), span);

            // Delete sectors by tenant id
            logService.debugS(callerName, "Delete sectors for tenant [%s]", new Object[]{uid});
            tenantInfraService.deleteSectorsByTenantId(tenant.getId(), span);

            // Delete organizations by tenant id
            logService.debugS(callerName, "Delete organizations for tenant [%s]", new Object[]{uid});
            tenantInfraService.deleteOrganizationsByTenantId(tenant.getId(), span);

            // Delete tenant
            logService.debugS(callerName, "Delete tenant [%s] itself", new Object[]{uid});
            Integer nbDeleted = tenantInfraService.deleteTenant(tenant.getId(), span);

            // Create audit event
            generateTenantAuditEventAndPush(tenant, AuditAction.DELETE, Collections.emptyList());
            return nbDeleted;
        });
    }
}
