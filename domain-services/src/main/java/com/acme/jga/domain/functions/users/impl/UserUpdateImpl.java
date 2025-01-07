package com.acme.jga.domain.functions.users.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.events.EventBuilderUser;
import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.functions.users.AbstractUserFunction;
import com.acme.jga.domain.functions.users.api.UserUpdate;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.infra.services.api.users.IUsersInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserUpdateImpl extends AbstractUserFunction implements UserUpdate {
    private static final String INSTRUMENTATION_NAME = UserUpdateImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationFind organizationFind;
    private final IUsersInfraService usersInfraService;
    private final EventBuilderUser eventBuilderUser;

    public UserUpdateImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                          IEventsInfraService eventsInfraService, TenantFind tenantFind, OrganizationFind organizationFind,
                          IUsersInfraService usersInfraService, EventBuilderUser eventBuilderUser) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.tenantFind = tenantFind;
        this.organizationFind = organizationFind;
        this.usersInfraService = usersInfraService;
        this.eventBuilderUser = eventBuilderUser;
    }

    @Override
    @Transactional
    @Audited
    public Integer execute(String tenantUid, String orgUid, User user, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_UPDATE", parentSpan, (span) -> {
            try {
                Tenant tenant = tenantFind.byUid(tenantUid, span);
                Organization org = organizationFind.byTenantIdAndUid(tenant.getId(), orgUid, false, span);
                Optional<User> rdbmsUser = ensureUserExists(user, span, tenant, org);

                // Ensure email is not already in use
                validateEmail(usersInfraService.emailUsed(user.getCredentials().getEmail(), span), rdbmsUser, FunctionalErrorsTypes.USER_EMAIL_ALREADY_USED, "user_email_used", user);

                // Ensure login is not already in use
                validateLogin(usersInfraService.loginUsed(user.getCredentials().getLogin(), span), rdbmsUser, FunctionalErrorsTypes.USER_LOGIN_ALREADY_USED, "user_login_used", user);
                user.setId(rdbmsUser.get().getId());
                user.setOrganizationId(rdbmsUser.get().getOrganizationId());
                user.setTenantId(rdbmsUser.get().getTenantId());

                // Update user
                Integer nbUpdated = usersInfraService.updateUser(user, span);
                // Create user audit event
                List<AuditChange> auditChanges = eventBuilderUser.buildAuditsChange(rdbmsUser.get(), user);
                generateUserAuditEventAndPush(user, tenant, org, AuditAction.UPDATE, span, auditChanges);
                return nbUpdated;
            } catch (FunctionalException e) {
                throw new WrappedFunctionalException(e);
            }
        });
    }

    private void validateLogin(Optional<Long> usersInfraService, Optional<User> rdbmsUser, FunctionalErrorsTypes userLoginAlreadyUsed, String user_login_used, User user) {
        Optional<Long> loginAlreadyExist = usersInfraService;
        if (loginAlreadyExist.isPresent() && loginAlreadyExist.get().longValue() != rdbmsUser.get().getId().longValue()) {
            throwWrappedException(userLoginAlreadyUsed.name(), user_login_used, new Object[]{user.getCredentials().getEmail()});
        }
    }

    private void validateEmail(Optional<Long> usersInfraService, Optional<User> rdbmsUser, FunctionalErrorsTypes userEmailAlreadyUsed, String user_email_used, User user) {
        Optional<Long> emailAlreadyExist = usersInfraService;
        if (emailAlreadyExist.isPresent() && emailAlreadyExist.get().longValue() != rdbmsUser.get().getId().longValue()) {
            throwWrappedException(userEmailAlreadyUsed.name(), user_email_used, new Object[]{user.getCredentials().getEmail()});
        }
    }

    private @NotNull Optional<User> ensureUserExists(User user, Span span, Tenant tenant, Organization org) throws FunctionalException {
        Optional<User> rdbmsUser = usersInfraService.findByUid(tenant.getId(), org.getId(), user.getUid(), span);
        if (rdbmsUser.isEmpty()) {
            throwWrappedException(FunctionalErrorsTypes.USER_NOT_FOUND.name(), "user_not_found", new Object[]{user.getUid()});
        }
        return rdbmsUser;
    }
}
