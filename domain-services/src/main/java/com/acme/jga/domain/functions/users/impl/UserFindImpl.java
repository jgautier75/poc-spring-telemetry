package com.acme.jga.domain.functions.users.impl;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.functions.users.api.UserFind;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.infra.services.api.users.UsersInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserFindImpl extends DomainFunction implements UserFind {
    private static final String INSTRUMENTATION_NAME = UserFindImpl.class.getCanonicalName();
    private final UsersInfraService usersInfraService;
    private final TenantFind tenantFind;
    private final OrganizationFind organizationFind;

    public UserFindImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, UsersInfraService usersInfraService, TenantFind tenantFind, OrganizationFind organizationFind) {
        super(openTelemetryWrapper, bundleFactory);
        this.usersInfraService = usersInfraService;
        this.tenantFind = tenantFind;
        this.organizationFind = organizationFind;
    }

    @Override
    public User byUid(String tenantUid, String orgUid, String userUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FIND_UID", parentSpan, (span) -> {
            try {
                Tenant tenant = tenantFind.byUid(tenantUid, span);
                Organization org = organizationFind.byTenantIdAndUid(tenant.getId(), orgUid, false, span);
                Optional<User> user = usersInfraService.findByUid(tenant.getId(), org.getId(), userUid, span);
                if (user.isEmpty()) {
                    throwWrappedException(FunctionalErrorsTypes.USER_NOT_FOUND.name(), "user_not_found", new Object[]{userUid});
                }
                user.get().setOrganization(org);
                return user.get();
            } catch (FunctionalException e) {
                throw new WrappedFunctionalException(e);
            }
        });
    }

    @Override
    public Optional<User> byEmail(String email, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FIND_EMAIL", parentSpan, (span) -> usersInfraService.findByEmail(email, span));
    }

    @Override
    public Optional<User> byLogin(String login, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FIND_LOGIN", parentSpan, (span) -> usersInfraService.findByLogin(login, span));
    }

    @Override
    public Optional<User> byUid(String uid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FIND_EMAIL", parentSpan, (span) -> usersInfraService.findByUid(uid, span));
    }
}
