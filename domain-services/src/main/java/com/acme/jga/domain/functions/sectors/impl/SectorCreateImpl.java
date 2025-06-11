package com.acme.jga.domain.functions.sectors.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.sectors.AbstractSectorFunction;
import com.acme.jga.domain.functions.sectors.api.SectorCreate;
import com.acme.jga.domain.functions.sectors.api.SectorFind;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditOperation;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.events.EventsInfraService;
import com.acme.jga.infra.services.api.sectors.SectorsInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SectorCreateImpl extends AbstractSectorFunction implements SectorCreate {
    private static final String INSTRUMENTATION_NAME = SectorCreateImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationFind organizationFind;
    private final SectorsInfraService sectorsInfraService;
    private final SectorFind sectorFind;

    public SectorCreateImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, TenantFind tenantFind,
                            OrganizationFind organizationFind, SectorsInfraService sectorsInfraService,
                            SectorFind sectorFind, EventsInfraService eventsInfraService) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.tenantFind = tenantFind;
        this.organizationFind = organizationFind;
        this.sectorsInfraService = sectorsInfraService;
        this.sectorFind = sectorFind;
    }

    @Override
    @Transactional
    @Audited
    public CompositeId execute(String tenantUid, String organizationUid, Sector sector) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_SECTORS_CREATE", (span) -> {
            Tenant tenant = tenantFind.byUid(tenantUid);
            Organization organization = organizationFind.byTenantIdAndUid(tenant.getId(), organizationUid, false);
            Optional<Long> optSectorId = sectorsInfraService.existsByCode(sector.getCode());
            if (optSectorId.isPresent()) {
                throwWrappedException(FunctionalErrorsTypes.SECTOR_CODE_ALREADY_USED.name(), "sector_code_already_used", new Object[]{sector.getCode()});
            }
            // Ensure parent sector exists
            Sector parentSector = sectorFind.byTenantOrgAndUid(tenantUid, organizationUid, sector.getParentUid());
            sector.setParentId(parentSector.getId());

            // Create sector
            CompositeId sectorCompositeId = sectorsInfraService.createSector(tenant.getId(), organization.getId(), sector);
            sector.setUid(sectorCompositeId.getUid());

            // Create audit event
            List<AuditChange> auditChanges = List.of(new AuditChange("label", AuditOperation.ADD, null, sector.getLabel()));
            generateSectorAuditEventAndPush(sector, organization, tenant, AuditAction.CREATE, auditChanges);

            return sectorCompositeId;
        });
    }
}
