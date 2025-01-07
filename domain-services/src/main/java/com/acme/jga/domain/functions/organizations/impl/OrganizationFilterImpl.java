package com.acme.jga.domain.functions.organizations.impl;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.functions.organizations.api.OrganizationFilter;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.infra.services.api.organizations.IOrganizationsInfraService;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrganizationFilterImpl extends DomainFunction implements OrganizationFilter {
    private static final String INSTRUMENTATION_NAME = OrganizationFilterImpl.class.getCanonicalName();
    private final IOrganizationsInfraService organizationsInfraService;

    public OrganizationFilterImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, IOrganizationsInfraService organizationsInfraService) {
        super(openTelemetryWrapper, bundleFactory);
        this.organizationsInfraService = organizationsInfraService;
    }

    @Override
    public PaginatedResults<Organization> execute(Long tenantId, Span parentSpan, Map<String, Object> searchParams) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_ORGS_FIND_ALL", parentSpan, (span) -> organizationsInfraService.filterOrganizations(tenantId, span, searchParams));
    }
}
