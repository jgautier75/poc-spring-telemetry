package com.acme.jga.domain.functions.users.impl;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.functions.users.api.UserList;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.infra.services.api.events.EventsInfraService;
import com.acme.jga.infra.services.api.users.UsersInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.utils.lambdas.StreamUtil;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class UserListImpl extends DomainFunction implements UserList {
    private static final String INSTRUMENTATION_NAME = UserListImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationFind organizationFind;
    private final UsersInfraService usersInfraService;

    public UserListImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, EventsInfraService eventsInfraService,
                        TenantFind tenantFind, OrganizationFind organizationFind, UsersInfraService usersInfraService) {
        super(openTelemetryWrapper, bundleFactory);
        this.tenantFind = tenantFind;
        this.organizationFind = organizationFind;
        this.usersInfraService = usersInfraService;
    }

    @Override
    public List<User> execute(String tenantUid, String orgUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FIND", parentSpan, (span) -> {
            Long tenantId = null;
            Long orgId = null;
            if (!ObjectUtils.isEmpty(tenantUid)) {
                Tenant tenant = tenantFind.byUid(tenantUid, span);
                tenantId = tenant.getId();
            }
            if (!ObjectUtils.isEmpty(orgUid)) {
                Organization organization = organizationFind.byTenantIdAndUid(tenantId, orgUid, false, span);
                orgId = organization.getId();
            } else {
                if (ObjectUtils.isEmpty(tenantUid)) {
                    throwWrappedException(FunctionalErrorsTypes.TENANT_ORG_EXPECTED.name(), "tenant_org_filter_expected", null);
                }
            }
            List<User> users = usersInfraService.findUsers(tenantId, orgId, span);
            List<Long> orgIds = StreamUtil.ofNullableList(users).map(User::getOrganizationId).distinct().toList();
            if (!orgIds.isEmpty()) {
                List<Organization> organizations = organizationFind.byIdList(orgIds);
                users.forEach(user -> organizations.stream().filter(org -> org.getId().longValue() == user.getOrganizationId().longValue()).findFirst().ifPresent(user::setOrganization));
            }
            return users;
        });
    }
}
