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
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class TenantDeleteImpl extends AbstractTenantFunction implements TenantDelete {
    private static final String INSTRUMENTATION_NAME = TenantDeleteImpl.class.getCanonicalName();
    private final ILoggingFacade loggingFacade;
    private final TenantFind tenantFind;
    private final ITenantInfraService tenantInfraService;

    public TenantDeleteImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                            IEventsInfraService eventsInfraService, ILoggingFacade loggingFacade,
                            TenantFind tenantFind, ITenantInfraService tenantInfraService) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.loggingFacade = loggingFacade;
        this.tenantFind = tenantFind;
        this.tenantInfraService = tenantInfraService;
    }

    @Override
    @Transactional
    @Audited
    public Integer execute(String uid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_DELETE", parentSpan, (span) -> {
            String callerName = this.getClass().getName() + "-deleteTenant";
            loggingFacade.infoS(callerName, "Delete tenant [%s]", new Object[]{uid});
            // Find tenant
            Tenant tenant = tenantFind.byUid(uid, span);

            // Delete users by tenantId
            loggingFacade.debugS(callerName, "Delete users for tenant [%s]", new Object[]{uid});
            tenantInfraService.deleteUsersByTenantId(tenant.getId(), span);

            // Delete sectors by tenant id
            loggingFacade.debugS(callerName, "Delete sectors for tenant [%s]", new Object[]{uid});
            tenantInfraService.deleteSectorsByTenantId(tenant.getId(), span);

            // Delete organizations by tenant id
            loggingFacade.debugS(callerName, "Delete organizations for tenant [%s]", new Object[]{uid});
            tenantInfraService.deleteOrganizationsByTenantId(tenant.getId(), span);

            // Delete tenant
            loggingFacade.debugS(callerName, "Delete tenant [%s] itself", new Object[]{uid});
            Integer nbDeleted = tenantInfraService.deleteTenant(tenant.getId(), span);

            // Create audit event
            generateTenantAuditEventAndPush(tenant, AuditAction.DELETE, Collections.emptyList());
            return nbDeleted;
        });
    }
}
