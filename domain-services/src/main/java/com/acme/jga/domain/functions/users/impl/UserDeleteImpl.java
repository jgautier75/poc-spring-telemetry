package com.acme.jga.domain.functions.users.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.functions.users.AbstractUserFunction;
import com.acme.jga.domain.functions.users.api.UserDelete;
import com.acme.jga.domain.functions.users.api.UserFind;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.infra.services.api.users.IUsersInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserDeleteImpl extends AbstractUserFunction implements UserDelete {
    private static final String INSTRUMENTATION_NAME = UserDeleteImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationFind organizationFind;
    private final UserFind userFind;
    private final IUsersInfraService usersInfraService;

    public UserDeleteImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, IEventsInfraService eventsInfraService,
                          TenantFind tenantFind, OrganizationFind organizationFind, UserFind userFind, IUsersInfraService usersInfraService) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.tenantFind = tenantFind;
        this.organizationFind = organizationFind;
        this.userFind = userFind;
        this.usersInfraService = usersInfraService;
    }

    @Override
    @Transactional
    @Audited
    public Integer execute(String tenantUid, String orgUid, String userUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_DELETE", parentSpan, (span) -> {
            Tenant tenant = tenantFind.byUid(tenantUid, span);
            Organization org = organizationFind.byTenantIdAndUid(tenant.getId(), orgUid, false, span);
            User user = userFind.byUid(tenantUid, orgUid, userUid, span);
            Integer nbRowsDeleted = usersInfraService.deleteUser(tenant.getId(), org.getId(), user.getId(), span);
            generateUserAuditEventAndPush(user, tenant, org, AuditAction.DELETE, span, Collections.emptyList());
            return nbRowsDeleted;
        });
    }

}
