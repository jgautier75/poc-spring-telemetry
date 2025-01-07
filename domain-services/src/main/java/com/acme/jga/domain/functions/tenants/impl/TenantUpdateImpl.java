package com.acme.jga.domain.functions.tenants.impl;

import com.acme.jga.domain.events.EventBuilderTenant;
import com.acme.jga.domain.functions.tenants.AbstractTenantFunction;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.functions.tenants.api.TenantUpdate;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.infra.services.api.tenants.ITenantInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantUpdateImpl extends AbstractTenantFunction implements TenantUpdate {
    private static final String INSTRUMENTATION_NAME = TenantUpdateImpl.class.getCanonicalName();
    private final ITenantInfraService tenantInfraService;
    private final ILogService logService;
    private final TenantFind tenantFind;
    private final EventBuilderTenant eventBuilderTenant;

    public TenantUpdateImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                            ITenantInfraService tenantInfraService, ILogService logService,
                            IEventsInfraService eventsInfraService, EventBuilderTenant eventBuilderTenant,
                            TenantFind tenantFind) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.tenantInfraService = tenantInfraService;
        this.logService = logService;
        this.eventBuilderTenant = eventBuilderTenant;
        this.tenantFind = tenantFind;
    }

    @Override
    public Integer execute(Tenant tenant, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_UPDATE", parentSpan, (span) -> {
            String callerName = this.getClass().getName() + "-updateTenant";
            logService.infoS(callerName, "Updating tenant [%s] ", new Object[]{tenant.getUid()});
            // Ensure tenant already exists
            Tenant rbdmsTenant = tenantFind.byUid(tenant.getUid(), span);
            tenant.setId(rbdmsTenant.getId());
            // Tenant update
            Integer nbRowsUpdated = tenantInfraService.updateTenant(tenant, span);
            // Create audit event
            List<AuditChange> auditChanges = eventBuilderTenant.buildAuditsChange(rbdmsTenant, tenant);
            generateTenantAuditEventAndPush(tenant, AuditAction.UPDATE, auditChanges);
            return nbRowsUpdated;
        });
    }
}
