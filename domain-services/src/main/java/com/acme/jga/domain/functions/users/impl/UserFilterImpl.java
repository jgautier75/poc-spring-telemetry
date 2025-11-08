package com.acme.jga.domain.functions.users.impl;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.functions.users.api.UserFilter;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.model.v1.UserDisplay;
import com.acme.jga.infra.services.api.users.UsersInfraService;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserFilterImpl extends DomainFunction implements UserFilter {
    private static final String INSTRUMENTATION_NAME = UserFilterImpl.class.getCanonicalName();
    private final UsersInfraService usersInfraService;
    private final TenantFind tenantFind;
    private final OrganizationFind organizationFind;

    public UserFilterImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                          UsersInfraService usersInfraService,
                          TenantFind tenantFind,
                          OrganizationFind organizationFind
    ) {
        super(openTelemetryWrapper, bundleFactory);
        this.usersInfraService = usersInfraService;
        this.tenantFind = tenantFind;
        this.organizationFind = organizationFind;
    }

    @Override
    public PaginatedResults<UserDisplay> execute(String tenantUid, String orgUid, Span parentSpan, Map<String, Object> searchParams) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FILTER", parentSpan, (span) -> {
            // Ensure tenant exists
            Tenant tenant = tenantFind.byUid(tenantUid, span);
            // Ensure organization exists
            Organization org = organizationFind.byTenantIdAndUid(tenant.getId(), orgUid, false, span);
            return usersInfraService.filterUsers(tenant.getId(), org.getId(), span, searchParams);
        });
    }
}
