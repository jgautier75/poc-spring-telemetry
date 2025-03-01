package com.acme.jga.domain.functions.organizations.impl;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.infra.services.api.organizations.OrganizationsInfraService;
import com.acme.jga.infra.services.api.sectors.SectorsInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationFindImpl extends DomainFunction implements OrganizationFind {
    private static final String INSTRUMENTATION_NAME = OrganizationFindImpl.class.getCanonicalName();
    private final OrganizationsInfraService organizationsInfraService;
    private final SectorsInfraService sectorsInfraService;

    public OrganizationFindImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                                OrganizationsInfraService organizationsInfraService, SectorsInfraService sectorsInfraService) {
        super(openTelemetryWrapper, bundleFactory);
        this.organizationsInfraService = organizationsInfraService;
        this.sectorsInfraService = sectorsInfraService;
    }

    @Override
    public Organization byTenantIdAndUid(Long tenantId, String orgUid, boolean fetchSectors, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_ORGS_FIND_UID", parentSpan, (span) -> {
            Optional<Organization> org = organizationsInfraService.findOrganizationByUid(tenantId, orgUid, span);
            if (org.isEmpty()) {
                throwWrappedException(FunctionalErrorsTypes.ORG_NOT_FOUND.name(), "org_not_found_by_uid", new Object[]{orgUid});
            }
            if (fetchSectors) {
                Sector sector = sectorsInfraService.fetchSectorsWithHierarchy(tenantId, org.get().getId(), span);
                org.get().setSector(sector);
            }
            return org.get();
        });
    }

    @Override
    public List<Organization> byIdList(List<Long> orgIds) {
        return organizationsInfraService.findOrgsByIdList(orgIds);
    }
}
