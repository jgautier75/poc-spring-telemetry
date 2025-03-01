package com.acme.jga.domain.functions.users.impl;

import com.acme.jga.crypto.CryptoEngine;
import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.functions.users.AbstractUserFunction;
import com.acme.jga.domain.functions.users.api.UserCreate;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.infra.services.api.users.IUsersInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.utils.otel.OtelContext;
import io.micrometer.common.util.StringUtils;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserCreateImpl extends AbstractUserFunction implements UserCreate {
    private static final String INSTRUMENTATION_NAME = UserCreateImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationFind organizationFind;
    private final ILoggingFacade loggingFacade;
    private final CryptoEngine cryptoEngine;
    private final IUsersInfraService usersInfraService;

    protected UserCreateImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, IEventsInfraService eventsInfraService,
                             TenantFind tenantFind, OrganizationFind organizationFind, ILoggingFacade loggingFacade,
                             CryptoEngine cryptoEngine, IUsersInfraService usersInfraService) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.tenantFind = tenantFind;
        this.organizationFind = organizationFind;
        this.loggingFacade = loggingFacade;
        this.cryptoEngine = cryptoEngine;
        this.usersInfraService = usersInfraService;
    }

    @Override
    @Transactional
    @Audited
    public CompositeId execute(String tenantUid, String orgUid, User user, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_CREATE", parentSpan, (span) -> {
            try {
                String callerName = this.getClass().getName() + "-createUser";
                // Ensure email is not already in use
                validateEmail(user, span, callerName);

                // Ensure login is not already in use
                validateLogin(user, span, callerName);

                // Find tenant and organization
                Tenant tenant = tenantFind.byUid(tenantUid, span);
                Organization org = organizationFind.byTenantIdAndUid(tenant.getId(), orgUid, false, span);
                loggingFacade.infoS(callerName, "Create user with login [%s] for tenant [%s] and organization [%s]",
                        new Object[]{user.getCredentials().getLogin(), tenant.getCode(), org.getCommons().getCode()}, OtelContext.fromSpan(span));

                user.setTenantId(tenant.getId());
                user.setOrganizationId(org.getId());

                if (!StringUtils.isEmpty(user.getCredentials().getDefaultPassword())) {
                    String encryptedPassword = cryptoEngine.encode(user.getCredentials().getDefaultPassword());
                    user.getCredentials().setEncryptedPassword(encryptedPassword);
                }

                List<AuditChange> auditChanges = createAuditChanges(user);

                CompositeId userCompositeId = usersInfraService.createUser(user, span);
                user.setUid(userCompositeId.getUid());

                // Create user audit event
                generateUserAuditEventAndPush(user, tenant, org, AuditAction.CREATE, span, auditChanges);

                return userCompositeId;
            } catch (FunctionalException e) {
                throw new WrappedFunctionalException(e);
            }
        });
    }

    private void validateLogin(User user, Span span, String callerName) {
        loggingFacade.debugS(callerName, "Check if login [%s] is not already in use", new Object[]{user.getCredentials().getLogin()});
        Optional<Long> loginAlreadyExist = usersInfraService.loginUsed(user.getCredentials().getLogin(), span);
        if (loginAlreadyExist.isPresent()) {
            throwWrappedException(FunctionalErrorsTypes.USER_LOGIN_ALREADY_USED.name(), "user_login_used", new Object[]{user.getCredentials().getEmail()});
        }
    }

    private void validateEmail(User user, Span span, String callerName) {
        loggingFacade.debugS(callerName, "Check if email [%s] is not already in use", new Object[]{user.getCredentials().getEmail()});
        Optional<Long> emailAlreadyExist = usersInfraService.emailUsed(user.getCredentials().getEmail(), span);
        if (emailAlreadyExist.isPresent()) {
            throwWrappedException(FunctionalErrorsTypes.USER_EMAIL_ALREADY_USED.name(), "user_email_used", new Object[]{user.getCredentials().getEmail()});
        }
    }

}
