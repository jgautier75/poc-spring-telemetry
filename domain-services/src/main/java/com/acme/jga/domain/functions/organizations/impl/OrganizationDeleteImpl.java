package com.acme.jga.domain.functions.organizations.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.functions.organizations.AbstractOrganizationFunction;
import com.acme.jga.domain.functions.organizations.api.OrganizationDelete;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.organizations.IOrganizationsInfraService;
import com.acme.jga.infra.services.impl.events.EventsInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
public class OrganizationDeleteImpl extends AbstractOrganizationFunction implements OrganizationDelete {
    private static final String INSTRUMENTATION_NAME = OrganizationDeleteImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final IOrganizationsInfraService organizationsInfraService;
    private final ILoggingFacade loggingFacade;

    public OrganizationDeleteImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                                  EventsInfraService eventsInfraService, TenantFind tenantFind,
                                  IOrganizationsInfraService organizationsInfraService, ILoggingFacade loggingFacade) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.tenantFind = tenantFind;
        this.organizationsInfraService = organizationsInfraService;
        this.loggingFacade = loggingFacade;
    }

    @Override
    @Transactional
    @Audited
    public Integer execute(String tenantUid, String orgUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_ORGS_DELETE", parentSpan, (span) -> {
            String callerName = this.getClass().getName() + "-deleteOrganization";
            int totalDeleted = 0;
            Tenant tenant = tenantFind.byUid(tenantUid, span);
            Optional<Organization> org = organizationsInfraService.findOrganizationByUid(tenant.getId(), orgUid, span);
            if (org.isEmpty()) {
                throwWrappedException(FunctionalErrorsTypes.ORG_NOT_FOUND.name(), "org_not_found_by_uid", new Object[]{orgUid});
            }

            // Delete users
            Integer nbUsersDeleted = organizationsInfraService.deleteUsersByOrganization(tenant.getId(), org.get().getId());
            totalDeleted += nbUsersDeleted;
            loggingFacade.debugS(callerName, "Nb of users deleted: [%s]", new Object[]{nbUsersDeleted});

            // Delete sectors
            Integer nbSectorsDeleted = organizationsInfraService.deleteSectors(tenant.getId(), org.get().getId());
            totalDeleted += nbSectorsDeleted;
            loggingFacade.debugS(callerName, "Nb of sectors deleted: [%s]", new Object[]{nbSectorsDeleted});

            // Delete organization
            Integer nbOrgDeleted = organizationsInfraService.deleteById(tenant.getId(), org.get().getId());
            totalDeleted += nbOrgDeleted;
            loggingFacade.debugS(callerName, "Nb of organizations deleted: [%s]", new Object[]{nbOrgDeleted});

            loggingFacade.debugS(callerName, "Total nb of records deleted: [%s]", new Object[]{totalDeleted});

            // Create audit event
            generateOrgAuditEventAndPush(org.get(), tenant, span, AuditAction.DELETE, Collections.emptyList());
            return totalDeleted;
        });
    }
}
