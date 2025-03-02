package com.acme.jga.domain.functions.organizations.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.events.EventBuilderOrganization;
import com.acme.jga.domain.functions.organizations.AbstractOrganizationFunction;
import com.acme.jga.domain.functions.organizations.api.OrganizationUpdate;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.organizations.OrganizationsInfraService;
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
public class OrganizationUpdateImpl extends AbstractOrganizationFunction implements OrganizationUpdate {
    private static final String INSTRUMENTATION_NAME = OrganizationUpdateImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationsInfraService organizationsInfraService;
    private final ILoggingFacade loggingFacade;
    private final EventBuilderOrganization eventBuilderOrganization;

    public OrganizationUpdateImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                                  EventsInfraServiceImpl eventsInfraServiceImpl, TenantFind tenantFind,
                                  OrganizationsInfraService organizationsInfraService, ILoggingFacade loggingFacade,
                                  EventBuilderOrganization eventBuilderOrganization) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraServiceImpl);
        this.tenantFind = tenantFind;
        this.organizationsInfraService = organizationsInfraService;
        this.loggingFacade = loggingFacade;
        this.eventBuilderOrganization = eventBuilderOrganization;
    }

    @Override
    @Transactional
    @Audited
    public Integer execute(String tenantUid, String orgUid, Organization organization, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_ORGS_UPDATE", parentSpan, (span) -> {
            String callerName = this.getClass().getName() + "-updateOrganization";
            loggingFacade.infoS(callerName, "Update organization [%s] of tenant [%s]", new Object[]{tenantUid, orgUid}, OtelContext.fromSpan(span));
            Tenant tenant = tenantFind.byUid(tenantUid, span);
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
}
