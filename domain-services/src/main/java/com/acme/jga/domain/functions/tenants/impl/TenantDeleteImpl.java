package com.acme.jga.domain.functions.tenants.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.functions.tenants.AbstractTenantFunction;
import com.acme.jga.domain.functions.tenants.api.TenantDelete;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.events.EventsInfraService;
import com.acme.jga.infra.services.api.tenants.TenantInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.utils.otel.OtelContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class TenantDeleteImpl extends AbstractTenantFunction implements TenantDelete {
    private static final String INSTRUMENTATION_NAME = TenantDeleteImpl.class.getCanonicalName();
    private final ILoggingFacade loggingFacade;
    private final TenantFind tenantFind;
    private final TenantInfraService tenantInfraService;

    public TenantDeleteImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                            EventsInfraService eventsInfraService, ILoggingFacade loggingFacade,
                            TenantFind tenantFind, TenantInfraService tenantInfraService) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.loggingFacade = loggingFacade;
        this.tenantFind = tenantFind;
        this.tenantInfraService = tenantInfraService;
    }

    @Override
    @Transactional
    @Audited
    public Integer execute(String uid) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_DELETE", (span) -> {
            String callerName = this.getClass().getName() + "-deleteTenant";
            loggingFacade.infoS(callerName, "Delete tenant [%s]", new Object[]{uid}, OtelContext.fromSpan(span));
            // Find tenant
            Tenant tenant = tenantFind.byUid(uid);

            // Delete users by tenantId
            loggingFacade.debugS(callerName, "Delete users for tenant [%s]", new Object[]{uid}, OtelContext.fromSpan(span));
            tenantInfraService.deleteUsersByTenantId(tenant.getId());

            // Delete sectors by tenant id
            loggingFacade.debugS(callerName, "Delete sectors for tenant [%s]", new Object[]{uid}, OtelContext.fromSpan(span));
            tenantInfraService.deleteSectorsByTenantId(tenant.getId());

            // Delete organizations by tenant id
            loggingFacade.debugS(callerName, "Delete organizations for tenant [%s]", new Object[]{uid}, OtelContext.fromSpan(span));
            tenantInfraService.deleteOrganizationsByTenantId(tenant.getId());

            // Delete tenant
            loggingFacade.debugS(callerName, "Delete tenant [%s] itself", new Object[]{uid}, OtelContext.fromSpan(span));
            Integer nbDeleted = tenantInfraService.deleteTenant(tenant.getId());

            // Create audit event
            generateTenantAuditEventAndPush(tenant, AuditAction.DELETE, Collections.emptyList());
            return nbDeleted;
        });
    }
}
