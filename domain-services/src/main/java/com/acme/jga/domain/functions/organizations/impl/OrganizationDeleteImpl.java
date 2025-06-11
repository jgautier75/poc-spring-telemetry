package com.acme.jga.domain.functions.organizations.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.functions.organizations.AbstractOrganizationFunction;
import com.acme.jga.domain.functions.organizations.api.OrganizationDelete;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.organizations.OrganizationsInfraService;
import com.acme.jga.infra.services.impl.events.EventsInfraServiceImpl;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.utils.otel.OtelContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
public class OrganizationDeleteImpl extends AbstractOrganizationFunction implements OrganizationDelete {
    private static final String INSTRUMENTATION_NAME = OrganizationDeleteImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationsInfraService organizationsInfraService;
    private final ILoggingFacade loggingFacade;

    public OrganizationDeleteImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                                  EventsInfraServiceImpl eventsInfraServiceImpl, TenantFind tenantFind,
                                  OrganizationsInfraService organizationsInfraService, ILoggingFacade loggingFacade) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraServiceImpl);
        this.tenantFind = tenantFind;
        this.organizationsInfraService = organizationsInfraService;
        this.loggingFacade = loggingFacade;
    }

    @Override
    @Transactional
    @Audited
    public Integer execute(String tenantUid, String orgUid) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_ORGS_DELETE", (span) -> {
            String callerName = this.getClass().getName() + "-deleteOrganization";
            int totalDeleted = 0;
            Tenant tenant = tenantFind.byUid(tenantUid);
            Optional<Organization> org = organizationsInfraService.findOrganizationByUid(tenant.getId(), orgUid);
            if (org.isEmpty()) {
                throwWrappedException(FunctionalErrorsTypes.ORG_NOT_FOUND.name(), "org_not_found_by_uid", new Object[]{orgUid});
            }

            // Delete users
            Integer nbUsersDeleted = organizationsInfraService.deleteUsersByOrganization(tenant.getId(), org.get().getId());
            totalDeleted += nbUsersDeleted;
            loggingFacade.debugS(callerName, "Nb of users deleted: [%s]", new Object[]{nbUsersDeleted}, OtelContext.fromSpan(span));

            // Delete sectors
            Integer nbSectorsDeleted = organizationsInfraService.deleteSectors(tenant.getId(), org.get().getId());
            totalDeleted += nbSectorsDeleted;
            loggingFacade.debugS(callerName, "Nb of sectors deleted: [%s]", new Object[]{nbSectorsDeleted}, OtelContext.fromSpan(span));

            // Delete organization
            Integer nbOrgDeleted = organizationsInfraService.deleteById(tenant.getId(), org.get().getId());
            totalDeleted += nbOrgDeleted;
            loggingFacade.debugS(callerName, "Nb of organizations deleted: [%s]", new Object[]{nbOrgDeleted}, OtelContext.fromSpan(span));
            loggingFacade.debugS(callerName, "Total nb of records deleted: [%s]", new Object[]{totalDeleted}, OtelContext.fromSpan(span));

            // Create audit event
            generateOrgAuditEventAndPush(org.get(), tenant, AuditAction.DELETE, Collections.emptyList());
            return totalDeleted;
        });
    }
}
