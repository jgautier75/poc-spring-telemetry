package com.acme.jga.domain.functions.organizations.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.functions.organizations.AbstractOrganizationFunction;
import com.acme.jga.domain.functions.organizations.api.OrganizationCreate;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditOperation;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.organizations.IOrganizationsInfraService;
import com.acme.jga.infra.services.api.sectors.ISectorsInfraService;
import com.acme.jga.infra.services.impl.events.EventsInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationCreateImpl extends AbstractOrganizationFunction implements OrganizationCreate {
    private static final String INSTRUMENTATION_NAME = OrganizationCreateImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final IOrganizationsInfraService organizationsInfraService;
    private final ISectorsInfraService sectorsInfraService;
    private final ILogService logService;

    public OrganizationCreateImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                                  EventsInfraService eventsInfraService, TenantFind tenantFind,
                                  IOrganizationsInfraService organizationsInfraService,
                                  ISectorsInfraService sectorsInfraService, ILogService logService) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.tenantFind = tenantFind;
        this.organizationsInfraService = organizationsInfraService;
        this.sectorsInfraService = sectorsInfraService;
        this.logService = logService;
    }

    @Override
    @Transactional
    @Audited
    public CompositeId execute(String tenantUid, Organization organization, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_ORGS_CREATE", parentSpan, (span) -> {
            Tenant tenant = tenantFind.byUid(tenantUid, parentSpan);
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
}
