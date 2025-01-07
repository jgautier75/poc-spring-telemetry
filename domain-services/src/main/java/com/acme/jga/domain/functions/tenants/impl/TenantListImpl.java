package com.acme.jga.domain.functions.tenants.impl;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.functions.tenants.api.TenantList;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.tenants.ITenantInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantListImpl extends DomainFunction implements TenantList {
    private static final String INSTRUMENTATION_NAME = TenantListImpl.class.getCanonicalName();
    private final ITenantInfraService tenantInfraService;

    public TenantListImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, ITenantInfraService tenantInfraService) {
        super(openTelemetryWrapper, bundleFactory);
        this.tenantInfraService = tenantInfraService;
    }

    @Override
    public List<Tenant> execute(Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_FIND_ALL", parentSpan, tenantInfraService::findAllTenants);
    }
}
