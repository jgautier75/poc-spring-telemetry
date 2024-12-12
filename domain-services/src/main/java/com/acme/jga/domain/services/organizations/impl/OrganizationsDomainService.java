package com.acme.jga.domain.services.organizations.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.events.EventBuilderOrganization;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.events.v1.AuditOperation;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.services.AbstractDomainService;
import com.acme.jga.domain.services.organizations.api.IOrganizationsDomainService;
import com.acme.jga.domain.services.tenants.api.ITenantDomainService;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.infra.services.api.organizations.IOrganizationsInfraService;
import com.acme.jga.infra.services.api.sectors.ISectorsInfraService;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.acme.jga.domain.model.utils.AuditEventFactory.createOrganizationAuditEvent;
import static com.acme.jga.domain.model.utils.AuditEventFactory.createSectorAuditEvent;

@Service
public class OrganizationsDomainService extends AbstractDomainService implements IOrganizationsDomainService {
    private static final String INSTRUMENTATION_NAME = OrganizationsDomainService.class.getCanonicalName();
    private final IOrganizationsInfraService organizationsInfraService;
    private final ITenantDomainService tenantDomainService;
    private final ILogService logService;
    private final ISectorsInfraService sectorsInfraService;
    private final IEventsInfraService eventsInfraService;
    private final EventBuilderOrganization eventBuilderOrganization;

    @Autowired
    public OrganizationsDomainService(IOrganizationsInfraService organizationsInfraService, ITenantDomainService tenantDomainService, BundleFactory bundleFactory, ILogService logService,
                                      ISectorsInfraService sectorsInfraService, IEventsInfraService eventsInfraService, EventBuilderOrganization eventBuilderOrganization,
                                      OpenTelemetryWrapper openTelemetryWrapper) {
        super(openTelemetryWrapper, bundleFactory);
        this.organizationsInfraService = organizationsInfraService;
        this.tenantDomainService = tenantDomainService;
        this.logService = logService;
        this.sectorsInfraService = sectorsInfraService;
        this.eventsInfraService = eventsInfraService;
        this.eventBuilderOrganization = eventBuilderOrganization;
    }

    @Transactional
    @Override
    @Audited
    public CompositeId createOrganization(String tenantUid, Organization organization, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_ORGS_CREATE", parentSpan, (span) -> {
            Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, parentSpan);
            Optional<Long> orgCodeUsed = organizationsInfraService.codeAlreadyUsed(organization.getCommons().getCode(), parentSpan);
            if (orgCodeUsed.isPresent()) {
                throwWrappedException(FunctionalErrorsTypes.ORG_CODE_ALREADY_USED.name(), "org_code_already_used", new Object[]{organization.getCommons().getCode()});
            }
            organization.setTenantId(tenant.getId());

            // Persist organization
            CompositeId orgCompositeId = organizationsInfraService.createOrganization(organization, parentSpan);

            // Assign generated uid
            organization.setUid(orgCompositeId.getUid());

            // Create root sector
            Sector sector = Sector.builder().code(organization.getCommons().getCode()).label(organization.getCommons().getLabel()).orgId(orgCompositeId.getId()).root(true).tenantId(tenant.getId()).build();
            CompositeId sectorCompositeId = sectorsInfraService.createSector(tenant.getId(), orgCompositeId.getId(), sector, parentSpan);
            sector.setUid(sectorCompositeId.getUid());

            // Generate audit event for sector
            List<AuditChange> sectorAuditChanges = List.of(new AuditChange("label", AuditOperation.ADD, null, organization.getCommons().getLabel()));
            generateSectorAuditEventAndPush(organization, tenant, sector, span, AuditAction.CREATE, sectorAuditChanges);
            logService.debugS(this.getClass().getName() + "-createOrganization", "Sector composite id [%s]", new Object[]{sectorCompositeId.getUid()});

            // Generate audit event for organization
            List<AuditChange> auditChanges = List.of(new AuditChange("label", AuditOperation.ADD, null, organization.getCommons().getLabel()));
            generateOrgAuditEventAndPush(organization, tenant, span, AuditAction.CREATE, auditChanges);
            return orgCompositeId;
        });
    }

    @Override
    public PaginatedResults<Organization> filterOrganizations(Long tenantId, Span parentSpan, Map<String, Object> searchParams) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_ORGS_FIND_ALL", parentSpan, (span) -> organizationsInfraService.filterOrganizations(tenantId, span, searchParams));
    }

    @Override
    public Organization findOrganizationByTenantAndUid(Long tenantId, String orgUid, boolean fetchSectors, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_ORGS_FIND_UID", parentSpan, (span) -> {
            Optional<Organization> org = organizationsInfraService.findOrganizationByUid(tenantId, orgUid, span);
            if (org.isEmpty()) {
                throwWrappedException(FunctionalErrorsTypes.ORG_NOT_FOUND.name(), "org_not_found_by_uid", new Object[]{orgUid});
            }
            if (fetchSectors) {
                Sector sector = sectorsInfraService.fetchSectorsWithHierarchy(tenantId, org.get().getId());
                org.get().setSector(sector);
            }
            return org.get();
        });
    }

    @Transactional
    @Override
    @Audited
    public Integer updateOrganization(String tenantUid, String orgUid, Organization organization, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_ORGS_UPDATE", parentSpan, (span) -> {
            String callerName = this.getClass().getName() + "-updateOrganization";
            logService.infoS(callerName, "Update organization [%s] of tenant [%s]", new Object[]{tenantUid, orgUid});
            Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
            Optional<Organization> org = organizationsInfraService.findOrganizationByUid(tenant.getId(), orgUid, span);
            if (org.isEmpty()) {
                throwWrappedException(FunctionalErrorsTypes.ORG_NOT_FOUND.name(), "org_not_found_by_uid", new Object[]{orgUid});
            }
            organization.setId(org.get().getId());
            organization.setTenantId(tenant.getId());
            organization.setUid(orgUid);

            // Build audit changes
            List<AuditChange> auditChanges = eventBuilderOrganization.buildAuditsChange(org.get().getCommons(), organization.getCommons());
            boolean anythingChanged = !auditChanges.isEmpty();
            int nbUpdated = 0;

            if (anythingChanged) {
                nbUpdated = organizationsInfraService.updateOrganization(tenant.getId(),
                        organization.getId(),
                        organization.getCommons().getCode(),
                        organization.getCommons().getLabel(),
                        organization.getCommons().getCountry(),
                        organization.getCommons().getStatus());
                generateOrgAuditEventAndPush(organization, tenant, span, AuditAction.UPDATE, auditChanges);
            }
            return nbUpdated;
        });
    }

    @Override
    public List<Organization> findOrgsByIdList(List<Long> orgIds) {
        return organizationsInfraService.findOrgsByIdList(orgIds);
    }

    @Transactional
    @Override
    @Audited
    public Integer deleteOrganization(String tenantUid, String orgUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_ORGS_DELETE", parentSpan, (span) -> {
            String callerName = this.getClass().getName() + "-deleteOrganization";
            int totalDeleted = 0;
            Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
            Optional<Organization> org = organizationsInfraService.findOrganizationByUid(tenant.getId(), orgUid, span);
            if (org.isEmpty()) {
                throwWrappedException(FunctionalErrorsTypes.ORG_NOT_FOUND.name(), "org_not_found_by_uid", new Object[]{orgUid});
            }

            // Delete users
            Integer nbUsersDeleted = organizationsInfraService.deleteUsersByOrganization(tenant.getId(), org.get().getId());
            totalDeleted += nbUsersDeleted;
            logService.debugS(callerName, "Nb of users deleted: [%s]", new Object[]{nbUsersDeleted});

            // Delete sectors
            Integer nbSectorsDeleted = organizationsInfraService.deleteSectors(tenant.getId(), org.get().getId());
            totalDeleted += nbSectorsDeleted;
            logService.debugS(callerName, "Nb of sectors deleted: [%s]", new Object[]{nbSectorsDeleted});

            // Delete organization
            Integer nbOrgDeleted = organizationsInfraService.deleteById(tenant.getId(), org.get().getId());
            totalDeleted += nbOrgDeleted;
            logService.debugS(callerName, "Nb of organizations deleted: [%s]", new Object[]{nbOrgDeleted});

            logService.debugS(callerName, "Total nb of records deleted: [%s]", new Object[]{totalDeleted});

            // Create audit event
            generateOrgAuditEventAndPush(org.get(), tenant, span, AuditAction.DELETE, Collections.emptyList());
            return totalDeleted;
        });
    }

    /**
     * Create and persist audit event, send wake-up message.
     *
     * @param org          Organization
     * @param tenant       Tenant
     * @param span         Span
     * @param auditAction  Action
     * @param auditChanges Changes
     */
    private void generateOrgAuditEventAndPush(Organization org, Tenant tenant, Span span, AuditAction auditAction, List<AuditChange> auditChanges) {
        AuditEvent orgAuditEvent = createOrganizationAuditEvent(org, tenant, auditAction, auditChanges);
        eventsInfraService.createEvent(orgAuditEvent, span);
    }

    /**
     * Generate sector audit event and send wake up message.
     *
     * @param org          Organization
     * @param tenant       Tenant
     * @param sector       Sector
     * @param span         OpenTelemetry span
     * @param auditAction  Audit action
     * @param auditChanges Audit changes
     */
    private void generateSectorAuditEventAndPush(Organization org, Tenant tenant, Sector sector, Span span, AuditAction auditAction, List<AuditChange> auditChanges) {
        AuditEvent sectorAuditEvent = createSectorAuditEvent(sector.getUid(), org, tenant, auditAction, auditChanges);
        eventsInfraService.createEvent(sectorAuditEvent, span);
    }

}
