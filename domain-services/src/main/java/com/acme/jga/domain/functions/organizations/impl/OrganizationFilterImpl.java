package com.acme.jga.domain.functions.organizations.impl;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.functions.organizations.api.OrganizationFilter;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.organizations.OrganizationsInfraService;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrganizationFilterImpl extends DomainFunction implements OrganizationFilter {
    private static final String INSTRUMENTATION_NAME = OrganizationFilterImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationsInfraService organizationsInfraService;

    public OrganizationFilterImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, OrganizationsInfraService organizationsInfraService, TenantFind tenantFind) {
        super(openTelemetryWrapper, bundleFactory);
        this.organizationsInfraService = organizationsInfraService;
        this.tenantFind = tenantFind;
    }

    @Override
    public PaginatedResults<Organization> execute(String tenantUuid, Span parentSpan, Map<String, Object> searchParams) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_ORGS_FIND_ALL", parentSpan, (span) -> {
            Tenant tenant = tenantFind.byUid(tenantUuid, span);
            return organizationsInfraService.filterOrganizations(tenant.getId(), span, searchParams);
        });
    }
}
