package com.acme.jga.domain.functions.sectors.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.events.EventBuilderSector;
import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.sectors.AbstractSectorFunction;
import com.acme.jga.domain.functions.sectors.api.SectorFind;
import com.acme.jga.domain.functions.sectors.api.SectorUpdate;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.events.EventsInfraService;
import com.acme.jga.infra.services.api.sectors.SectorsInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectorUpdateImpl extends AbstractSectorFunction implements SectorUpdate {
    private static final String INSTRUMENTATION_NAME = SectorUpdateImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final SectorFind sectorFind;
    private final OrganizationFind organizationFind;
    private final EventBuilderSector eventBuilderSector;
    private final SectorsInfraService sectorsInfraService;

    public SectorUpdateImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                            EventsInfraService eventsInfraService, TenantFind tenantFind,
                            SectorFind sectorFind, EventBuilderSector eventBuilderSector,
                            SectorsInfraService sectorsInfraService, OrganizationFind organizationFind) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.tenantFind = tenantFind;
        this.sectorFind = sectorFind;
        this.organizationFind = organizationFind;
        this.eventBuilderSector = eventBuilderSector;
        this.sectorsInfraService = sectorsInfraService;
    }

    @Override
    @Transactional
    @Audited
    public Integer execute(String tenantUid, String organizationUid, String sectorUid, Sector sector, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_SECTORS_UPDATE", parentSpan, (span) -> {
            Tenant tenant = tenantFind.byUid(tenantUid, span);
            Organization organization = organizationFind.byTenantIdAndUid(tenant.getId(), organizationUid, false, span);
            Sector rdbmsSector = sectorFind.byTenantOrgAndUid(tenant.getId(), organization.getId(), sectorUid, span);
            sector.withId(rdbmsSector.getId()).withUid(rdbmsSector.getUid()).withTenantId(rdbmsSector.getTenantId()).withOrgId(rdbmsSector.getOrgId());
            if (sector.getParentUid() != null) {
                Sector parentSector = sectorFind.byTenantOrgAndUid(tenant.getId(), organization.getId(), sector.getParentUid(), span);
                sector.setParentId(parentSector.getId());
            }
            List<AuditChange> auditChanges = eventBuilderSector.buildAuditsChange(rdbmsSector, sector);
            generateSectorAuditEventAndPush(sector, organization, tenant, AuditAction.UPDATE, span, auditChanges);
            return sectorsInfraService.updateSector(tenant.getId(), organization.getId(), sector);
        });
    }
}
