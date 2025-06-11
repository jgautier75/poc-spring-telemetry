package com.acme.jga.domain.functions.tenants.impl;

import com.acme.jga.domain.events.EventBuilderTenant;
import com.acme.jga.domain.functions.tenants.AbstractTenantFunction;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.functions.tenants.api.TenantUpdate;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.events.EventsInfraService;
import com.acme.jga.infra.services.api.tenants.TenantInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.utils.otel.OtelContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantUpdateImpl extends AbstractTenantFunction implements TenantUpdate {
    private static final String INSTRUMENTATION_NAME = TenantUpdateImpl.class.getCanonicalName();
    private final TenantInfraService tenantInfraService;
    private final ILoggingFacade loggingFacade;
    private final TenantFind tenantFind;
    private final EventBuilderTenant eventBuilderTenant;

    public TenantUpdateImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                            TenantInfraService tenantInfraService, ILoggingFacade loggingFacade,
                            EventsInfraService eventsInfraService, EventBuilderTenant eventBuilderTenant,
                            TenantFind tenantFind) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.tenantInfraService = tenantInfraService;
        this.loggingFacade = loggingFacade;
        this.eventBuilderTenant = eventBuilderTenant;
        this.tenantFind = tenantFind;
    }

    @Override
    public Integer execute(Tenant tenant) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_UPDATE", (span) -> {
            String callerName = this.getClass().getName() + "-updateTenant";
            loggingFacade.infoS(callerName, "Updating tenant [%s] ", new Object[]{tenant.getUid()}, OtelContext.fromSpan(span));
            // Ensure tenant already exists
            Tenant rbdmsTenant = tenantFind.byUid(tenant.getUid());
            tenant.setId(rbdmsTenant.getId());
            // Tenant update
            Integer nbRowsUpdated = tenantInfraService.updateTenant(tenant);
            // Create audit event
            List<AuditChange> auditChanges = eventBuilderTenant.buildAuditsChange(rbdmsTenant, tenant);
            generateTenantAuditEventAndPush(tenant, AuditAction.UPDATE, auditChanges);
            return nbRowsUpdated;
        });
    }
}
