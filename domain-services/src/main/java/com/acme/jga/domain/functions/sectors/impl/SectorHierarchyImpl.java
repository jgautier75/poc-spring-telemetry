package com.acme.jga.domain.functions.sectors.impl;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.sectors.api.SectorHierarchy;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.impl.sectors.SectorsInfraServiceImpl;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

@Service
public class SectorHierarchyImpl extends DomainFunction implements SectorHierarchy {
    private static final String INSTRUMENTATION_NAME = SectorHierarchyImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationFind organizationFind;
    private final SectorsInfraServiceImpl sectorsInfraServiceImpl;

    public SectorHierarchyImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                               TenantFind tenantFind, OrganizationFind organizationFind, SectorsInfraServiceImpl sectorsInfraServiceImpl) {
        super(openTelemetryWrapper, bundleFactory);
        this.tenantFind = tenantFind;
        this.organizationFind = organizationFind;
        this.sectorsInfraServiceImpl = sectorsInfraServiceImpl;
    }

    @Override
    public Sector execute(String tenantUid, String organizationUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_SECTORS_FIND_HIERARCHY", parentSpan, (span) -> {
            Tenant tenant = tenantFind.byUid(tenantUid, span);
            Organization organization = organizationFind.byTenantIdAndUid(tenant.getId(), organizationUid, false, span);
            return sectorsInfraServiceImpl.fetchSectorsWithHierarchy(tenant.getId(), organization.getId(), span);
        });
    }
}
