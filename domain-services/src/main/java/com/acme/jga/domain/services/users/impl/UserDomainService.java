package com.acme.jga.domain.services.users.impl;

import com.acme.jga.crypto.CryptoEngine;
import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.events.EventBuilderUser;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditEvent;
import com.acme.jga.domain.model.events.v1.AuditOperation;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.domain.services.AbstractDomainService;
import com.acme.jga.domain.services.organizations.api.IOrganizationsDomainService;
import com.acme.jga.domain.services.tenants.api.ITenantDomainService;
import com.acme.jga.domain.services.users.api.IUserDomainService;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.infra.services.api.users.IUsersInfraService;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.utils.lambdas.StreamUtil;
import io.micrometer.common.util.StringUtils;
import io.opentelemetry.api.trace.Span;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;

import static com.acme.jga.domain.model.utils.AuditEventFactory.createUserAuditEvent;

@Service
public class UserDomainService extends AbstractDomainService implements IUserDomainService {
    private static final String INSTRUMENTATION_NAME = UserDomainService.class.getCanonicalName();
    private final IOrganizationsDomainService organizationsDomainService;
    private final ITenantDomainService tenantDomainService;
    private final IUsersInfraService usersInfraService;
    private final ILogService logService;
    private final IEventsInfraService eventsInfraService;
    private final EventBuilderUser eventBuilderUser;
    private final CryptoEngine cryptoEngine = new CryptoEngine();

    @Autowired
    public UserDomainService(IOrganizationsDomainService organizationsDomainService, ITenantDomainService tenantDomainService, IUsersInfraService usersInfraService,
                             ILogService logService, BundleFactory bundleFactory, IEventsInfraService eventsInfraService, OpenTelemetryWrapper openTelemetryWrapper, EventBuilderUser eventBuilderUser) {
        super(openTelemetryWrapper, bundleFactory);
        this.organizationsDomainService = organizationsDomainService;
        this.tenantDomainService = tenantDomainService;
        this.usersInfraService = usersInfraService;
        this.logService = logService;
        this.eventsInfraService = eventsInfraService;
        this.eventBuilderUser = eventBuilderUser;
    }

    @Transactional
    @Override
    @Audited
    public CompositeId createUser(String tenantUid, String orgUid, User user, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_CREATE", parentSpan, (span) -> {
            try {
                String callerName = this.getClass().getName() + "-createUser";
                // Ensure email is not already in use
                validateEmail(user, span, callerName);

                // Ensure login is not already in use
                validateLogin(user, span, callerName);

                // Find tenant and organization
                Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
                Organization org = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), orgUid, false, span);
                logService.infoS(callerName, "Create user with login [%s] for tenant [%s] and organization [%s]",
                        new Object[]{user.getCredentials().getLogin(), tenant.getCode(), org.getCommons().getCode()});

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

    private static @NotNull List<AuditChange> createAuditChanges(User user) {
        List<AuditChange> auditChanges = new ArrayList<>();
        auditChanges.add(AuditChange.builder().to(user.getCommons().getFirstName()).object("firstName").operation(AuditOperation.ADD).build());
        auditChanges.add(AuditChange.builder().to(user.getCommons().getLastName()).object("lastName").operation(AuditOperation.ADD).build());
        auditChanges.add(AuditChange.builder().to(user.getCredentials().getEmail()).object("email").operation(AuditOperation.ADD).build());
        return auditChanges;
    }

    private void validateLogin(User user, Span span, String callerName) {
        logService.debugS(callerName, "Check if login [%s] is not already in use", new Object[]{user.getCredentials().getLogin()});
        Optional<Long> loginAlreadyExist = usersInfraService.loginUsed(user.getCredentials().getLogin(), span);
        if (loginAlreadyExist.isPresent()) {
            throwWrappedException(FunctionalErrorsTypes.USER_LOGIN_ALREADY_USED.name(), "user_login_used", new Object[]{user.getCredentials().getEmail()});
        }
    }

    private void validateEmail(User user, Span span, String callerName) {
        logService.debugS(callerName, "Check if email [%s] is not already in use", new Object[]{user.getCredentials().getEmail()});
        Optional<Long> emailAlreadyExist = usersInfraService.emailUsed(user.getCredentials().getEmail(), span);
        if (emailAlreadyExist.isPresent()) {
            throwWrappedException(FunctionalErrorsTypes.USER_EMAIL_ALREADY_USED.name(), "user_email_used", new Object[]{user.getCredentials().getEmail()});
        }
    }

    @Transactional
    @Override
    @Audited
    public Integer updateUser(String tenantUid, String orgUid, User user, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_UPDATE", parentSpan, (span) -> {
            try {
                Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
                Organization org = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), orgUid, false, span);
                Optional<User> rdbmsUser = usersInfraService.findByUid(tenant.getId(), org.getId(), user.getUid(), span);
                if (rdbmsUser.isEmpty()) {
                    throwWrappedException(FunctionalErrorsTypes.USER_NOT_FOUND.name(), "user_not_found", new Object[]{user.getUid()});
                }

                // Ensure email is not already in use
                Optional<Long> emailAlreadyExist = usersInfraService.emailUsed(user.getCredentials().getEmail(), span);
                if (emailAlreadyExist.isPresent() && emailAlreadyExist.get().longValue() != rdbmsUser.get().getId().longValue()) {
                    throwWrappedException(FunctionalErrorsTypes.USER_EMAIL_ALREADY_USED.name(), "user_email_used", new Object[]{user.getCredentials().getEmail()});
                }

                // Ensure login is not already in use
                Optional<Long> loginAlreadyExist = usersInfraService.loginUsed(user.getCredentials().getLogin(), span);
                if (loginAlreadyExist.isPresent() && loginAlreadyExist.get().longValue() != rdbmsUser.get().getId().longValue()) {
                    throwWrappedException(FunctionalErrorsTypes.USER_LOGIN_ALREADY_USED.name(), "user_login_used", new Object[]{user.getCredentials().getEmail()});
                }
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

    @Override
    public List<User> findUsers(String tenantUid, String orgUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FIND", parentSpan, (span) -> {
            Long tenantId = null;
            Long orgId = null;
            if (!ObjectUtils.isEmpty(tenantUid)) {
                Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
                tenantId = tenant.getId();
            }
            if (!ObjectUtils.isEmpty(orgUid)) {
                Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenantId, orgUid, false, span);
                orgId = organization.getId();
            } else {
                if (ObjectUtils.isEmpty(tenantUid)) {
                    throwWrappedException(FunctionalErrorsTypes.TENANT_ORG_EXPECTED.name(), "tenant_org_filter_expected", null);
                }
            }
            List<User> users = usersInfraService.findUsers(tenantId, orgId, span);
            List<Long> orgIds = StreamUtil.ofNullableList(users).map(User::getOrganizationId).distinct().toList();
            if (!orgIds.isEmpty()) {
                List<Organization> organizations = organizationsDomainService.findOrgsByIdList(orgIds);
                users.forEach(user -> organizations.stream().filter(org -> org.getId().longValue() == user.getOrganizationId().longValue()).findFirst().ifPresent(user::setOrganization));
            }
            return users;
        });
    }

    @Override
    public User findByUid(String tenantUid, String orgUid, String userUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FIND_UID", parentSpan, (span) -> {
            try {
                Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
                Organization org = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), orgUid, false, span);
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

    @Transactional
    @Override
    @Audited
    public Integer deleteUser(String tenantUid, String orgUid, String userUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_DELETE", parentSpan, (span) -> {
            Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
            Organization org = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), orgUid, false, span);
            User user = findByUid(tenantUid, orgUid, userUid, span);
            Integer nbRowsDeleted = usersInfraService.deleteUser(tenant.getId(), org.getId(), user.getId(), span);
            generateUserAuditEventAndPush(user, tenant, org, AuditAction.DELETE, span, Collections.emptyList());
            return nbRowsDeleted;
        });
    }

    @Override
    public PaginatedResults<User> filterUsers(Long tenantId, Long orgId, Span parentSpan, Map<String, Object> searchParams) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FILTER", parentSpan, (span) -> usersInfraService.filterUsers(tenantId, orgId, span, searchParams));
    }

    @Override
    public Optional<User> findByEmail(String email, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FIND_EMAIL", parentSpan, (span) -> usersInfraService.findByEmail(email, span));
    }

    @Override
    public Optional<User> findByLogin(String login, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FIND_LOGIN", parentSpan, (span) -> usersInfraService.findByLogin(login, span));
    }

    @Override
    public Optional<User> findByUid(String uid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FIND_EMAIL", parentSpan, (span) -> usersInfraService.findByUid(uid, span));
    }

    /**
     * Manage and persist audit events then send wake-up message.
     *
     * @param user   User
     * @param tenant Tenant
     * @param org    Organization
     * @param span   Span
     */
    private void generateUserAuditEventAndPush(User user, Tenant tenant, Organization org, AuditAction auditAction, Span span, List<AuditChange> changes) {
        AuditEvent userAuditEvent = createUserAuditEvent(user, auditAction, tenant, org);
        userAuditEvent.setChanges(changes);
        eventsInfraService.createEvent(userAuditEvent, span);
    }
}
