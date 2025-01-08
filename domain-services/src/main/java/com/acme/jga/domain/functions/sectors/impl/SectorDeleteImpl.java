package com.acme.jga.domain.functions.sectors.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.sectors.AbstractSectorFunction;
import com.acme.jga.domain.functions.sectors.api.SectorDelete;
import com.acme.jga.domain.functions.sectors.api.SectorFind;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.infra.services.api.sectors.ISectorsInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class SectorDeleteImpl extends AbstractSectorFunction implements SectorDelete {
    private static final String INSTRUMENTATION_NAME = SectorDeleteImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationFind organizationFind;
    private final ISectorsInfraService sectorsInfraService;
    private final SectorFind sectorFind;

    public SectorDeleteImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                            TenantFind tenantFind, OrganizationFind organizationFind,
                            ISectorsInfraService sectorsInfraService, IEventsInfraService eventsInfraService,
                            SectorFind sectorFind) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.tenantFind = tenantFind;
        this.organizationFind = organizationFind;
        this.sectorsInfraService = sectorsInfraService;
        this.sectorFind = sectorFind;
    }

    @Override
    @Transactional
    @Audited
    public Integer execute(String tenantUid, String organizationUid, String sectorUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_SECTORS_DELETE", parentSpan, (span) -> {
            Tenant tenant = tenantFind.byUid(tenantUid, span);
            Organization organization = organizationFind.byTenantIdAndUid(tenant.getId(), organizationUid, false, span);
            Sector rdbmsSector = sectorFind.byTenantOrgAndUid(tenant.getId(), organization.getId(), sectorUid, span);
            if (rdbmsSector.isRoot()) {
                throwWrappedException(FunctionalErrorsTypes.SECTOR_ROOT_DELETE_NOT_ALLOWED.name(), "sector_root_delete_deny", new Object[]{sectorUid});
            }
            generateSectorAuditEventAndPush(rdbmsSector, organization, tenant, AuditAction.DELETE, span, Collections.emptyList());
            return sectorsInfraService.deleteSector(tenant.getId(), organization.getId(), rdbmsSector.getId());
        });
    }
}
