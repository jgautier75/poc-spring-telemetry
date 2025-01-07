package com.acme.jga.domain.functions.sectors.impl;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.sectors.api.SectorFind;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.sectors.ISectorsInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SectorFindImpl extends DomainFunction implements SectorFind {
    private static final String INSTRUMENTATION_NAME = SectorFindImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationFind organizationFind;
    private final ISectorsInfraService sectorsInfraService;

    public SectorFindImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, TenantFind tenantFind, OrganizationFind organizationFind, ISectorsInfraService sectorsInfraService) {
        super(openTelemetryWrapper, bundleFactory);
        this.tenantFind = tenantFind;
        this.organizationFind = organizationFind;
        this.sectorsInfraService = sectorsInfraService;
    }

    @Override
    public Sector byTenantOrgAndUid(String tenantUid, String organizationUid, String sectorUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_SECTORS_FIND_UID", parentSpan, (span) -> {
            Tenant tenant = tenantFind.byUid(tenantUid, span);
            Organization organization = organizationFind.byTenantIdAndUid(tenant.getId(), organizationUid, false, span);
            return byTenantOrgAndUid(tenant.getId(), organization.getId(), sectorUid, span);
        });
    }

    @Override
    public Sector byTenantOrgAndUid(Long tenantId, Long organizationId, String sectorUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_SECTORS_FIND_ID", parentSpan, (span) -> {
            Optional<Sector> sector = sectorsInfraService.findSectorByUid(tenantId, organizationId, sectorUid);
            if (sector.isEmpty()) {
                throwWrappedException(FunctionalErrorsTypes.SECTOR_NOT_FOUND.name(), "sector_not_found", new Object[]{sectorUid});
            }
            return sector.get();
        });
    }
}
