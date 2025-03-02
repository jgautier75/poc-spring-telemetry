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
import com.acme.jga.infra.services.api.organizations.OrganizationsInfraService;
import com.acme.jga.infra.services.api.sectors.SectorsInfraService;
import com.acme.jga.infra.services.impl.events.EventsInfraServiceImpl;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.utils.otel.OtelContext;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationCreateImpl extends AbstractOrganizationFunction implements OrganizationCreate {
    private static final String INSTRUMENTATION_NAME = OrganizationCreateImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationsInfraService organizationsInfraService;
    private final SectorsInfraService sectorsInfraService;
    private final ILoggingFacade loggingFacade;

    public OrganizationCreateImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                                  EventsInfraServiceImpl eventsInfraServiceImpl, TenantFind tenantFind,
                                  OrganizationsInfraService organizationsInfraService,
                                  SectorsInfraService sectorsInfraService, ILoggingFacade loggingFacade) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraServiceImpl);
        this.tenantFind = tenantFind;
        this.organizationsInfraService = organizationsInfraService;
        this.sectorsInfraService = sectorsInfraService;
        this.loggingFacade = loggingFacade;
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
            loggingFacade.debugS(this.getClass().getName() + "-createOrganization", "Sector composite id [%s]", new Object[]{sectorCompositeId.getUid()}, OtelContext.fromSpan(span));

            // Generate audit event for organization
            List<AuditChange> auditChanges = List.of(new AuditChange("label", AuditOperation.ADD, null, organization.getCommons().getLabel()));
            generateOrgAuditEventAndPush(organization, tenant, span, AuditAction.CREATE, auditChanges);
            return orgCompositeId;
        });
    }
}
