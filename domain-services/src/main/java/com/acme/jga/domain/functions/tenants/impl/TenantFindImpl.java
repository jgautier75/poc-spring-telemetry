package com.acme.jga.domain.functions.tenants.impl;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.tenants.ITenantInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenantFindImpl extends DomainFunction implements TenantFind {
    private static final String INSTRUMENTATION_NAME = TenantFindImpl.class.getCanonicalName();
    private final ITenantInfraService tenantInfraService;

    public TenantFindImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                          ITenantInfraService tenantInfraService) {
        super(openTelemetryWrapper, bundleFactory);
        this.tenantInfraService = tenantInfraService;
    }

    @Override
    public Tenant byUid(String uid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_FIND_UID", parentSpan, (span) -> {
            Optional<Tenant> tenant = tenantInfraService.findTenantByUid(uid, span);
            if (tenant.isEmpty()) {
                throwWrappedException(FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), "tenant_not_found_by_uid", new Object[]{uid});
            }
            return tenant.get();
        });
    }
}
