package com.acme.jga.domain.services.sectors.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.events.EventBuilderSector;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.events.v1.AuditOperation;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.services.AbstractDomainService;
import com.acme.jga.domain.services.organizations.api.IOrganizationsDomainService;
import com.acme.jga.domain.services.sectors.api.ISectorsDomainService;
import com.acme.jga.domain.services.tenants.api.ITenantDomainService;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.infra.services.api.sectors.ISectorsInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.acme.jga.domain.model.utils.AuditEventFactory.createSectorAuditEvent;

@Service
public class SectorsDomainService extends AbstractDomainService implements ISectorsDomainService {
    private static final String INSTRUMENTATION_NAME = SectorsDomainService.class.getCanonicalName();
    private final ITenantDomainService tenantDomainService;
    private final IOrganizationsDomainService organizationsDomainService;
    private final ISectorsInfraService sectorsInfraService;
    private final IEventsInfraService eventsInfraService;
    private final EventBuilderSector eventBuilderSector;

    @Autowired
    public SectorsDomainService(ITenantDomainService tenantDomainService, IOrganizationsDomainService organizationsDomainService, ISectorsInfraService sectorsInfraService,
                                BundleFactory bundleFactory, IEventsInfraService eventsInfraService,
                                EventBuilderSector eventBuilderSector, OpenTelemetryWrapper openTelemetryWrapper) {
        super(openTelemetryWrapper, bundleFactory);
        this.tenantDomainService = tenantDomainService;
        this.organizationsDomainService = organizationsDomainService;
        this.sectorsInfraService = sectorsInfraService;
        this.eventsInfraService = eventsInfraService;
        this.eventBuilderSector = eventBuilderSector;
    }

    @Transactional
    @Override
    @Audited
    public CompositeId createSector(String tenantUid, String organizationUid, Sector sector, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_SECTORS_CREATE", parentSpan, (span) -> {
            Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
            Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), organizationUid, false, span);
            Optional<Long> optSectorId = sectorsInfraService.existsByCode(sector.getCode());
            if (optSectorId.isPresent()) {
                throwWrappedException(FunctionalErrorsTypes.SECTOR_CODE_ALREADY_USED.name(), "sector_code_already_used", new Object[]{sector.getCode()});
            }
            // Ensure parent sector exists
            Sector parentSector = findSectorByUidTenantOrg(tenantUid, organizationUid, sector.getParentUid(), span);
            sector.setParentId(parentSector.getId());

            // Create sector
            CompositeId sectorCompositeId = sectorsInfraService.createSector(tenant.getId(), organization.getId(), sector, span);
            sector.setUid(sectorCompositeId.getUid());

            // Create audit event
            List<AuditChange> auditChanges = List.of(new AuditChange("label", AuditOperation.ADD, null, sector.getLabel()));
            generateSectorAuditEventAndPush(sector, organization, tenant, AuditAction.CREATE, span, auditChanges);

            return sectorCompositeId;
        });
    }

    @Override
    public Sector findSectorByUidTenantOrg(String tenantUid, String organizationUid, String sectorUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_SECTORS_FIND_UID", parentSpan, (span) -> {
            Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
            Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), organizationUid, false, span);
            return findSectorByUidTenantOrg(tenant.getId(), organization.getId(), sectorUid, span);
        });
    }

    @Override
    public Sector findSectorByUidTenantOrg(Long tenantId, Long organizationId, String sectorUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_SECTORS_FIND_ID", parentSpan, (span) -> {
            Optional<Sector> sector = sectorsInfraService.findSectorByUid(tenantId, organizationId, sectorUid);
            if (sector.isEmpty()) {
                throwWrappedException(FunctionalErrorsTypes.SECTOR_NOT_FOUND.name(), "sector_not_found", new Object[]{sectorUid});
            }
            return sector.get();
        });
    }

    @Override
    public Sector fetchSectorsWithHierarchy(String tenantUid, String organizationUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_SECTORS_FIND_HIERARCHY", parentSpan, (span) -> {
            Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
            Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), organizationUid, false, span);
            return sectorsInfraService.fetchSectorsWithHierarchy(tenant.getId(), organization.getId());
        });
    }

    @Transactional
    @Override
    @Audited
    public Integer updateSector(String tenantUid, String organizationUid, String sectorUid, Sector sector, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_SECTORS_UPDATE", parentSpan, (span) -> {
            Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
            Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), organizationUid, false, span);
            Sector rdbmsSector = findSectorByUidTenantOrg(tenant.getId(), organization.getId(), sectorUid, span);
            sector.withId(rdbmsSector.getId()).withUid(rdbmsSector.getUid()).withTenantId(rdbmsSector.getTenantId()).withOrgId(rdbmsSector.getOrgId());
            if (sector.getParentUid() != null) {
                Sector parentSector = findSectorByUidTenantOrg(tenant.getId(), organization.getId(), sector.getParentUid(), span);
                sector.setParentId(parentSector.getId());
            }
            List<AuditChange> auditChanges = eventBuilderSector.buildAuditsChange(rdbmsSector, sector);
            generateSectorAuditEventAndPush(sector, organization, tenant, AuditAction.UPDATE, span, auditChanges);
            return sectorsInfraService.updateSector(tenant.getId(), organization.getId(), sector);
        });
    }

    @Transactional
    @Override
    @Audited
    public Integer deleteSector(String tenantUid, String organizationUid, String sectorUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_SECTORS_DELETE", parentSpan, (span) -> {
            Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
            Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), organizationUid, false, span);
            Sector rdbmsSector = findSectorByUidTenantOrg(tenant.getId(), organization.getId(), sectorUid, span);
            if (rdbmsSector.isRoot()) {
                throwWrappedException(FunctionalErrorsTypes.SECTOR_ROOT_DELETE_NOT_ALLOWED.name(), "sector_root_delete_deny", new Object[]{sectorUid});
            }
            generateSectorAuditEventAndPush(rdbmsSector, organization, tenant, AuditAction.DELETE, span, Collections.emptyList());
            return sectorsInfraService.deleteSector(tenant.getId(), organization.getId(), rdbmsSector.getId());
        });
    }

    /**
     * Create and persist audit event, send wake-up message in kafka.
     *
     * @param sector       Sector
     * @param organization Organization
     * @param tenant       Tenant
     * @param auditAction  Audit action
     * @param span         Span
     * @param auditChanges Audit changes
     */
    private void generateSectorAuditEventAndPush(Sector sector, Organization organization, Tenant tenant, AuditAction auditAction, Span span, List<AuditChange> auditChanges) {
        AuditEvent sectorAuditEvent = createSectorAuditEvent(sector.getUid(), organization, tenant, auditAction, auditChanges);
        eventsInfraService.createEvent(sectorAuditEvent, span);
    }
}
