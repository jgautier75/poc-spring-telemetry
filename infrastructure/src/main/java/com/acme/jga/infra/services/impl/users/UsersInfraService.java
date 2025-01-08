package com.acme.jga.infra.services.impl.users;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.filtering.FilteringConstants;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.domain.model.v1.UserDisplay;
import com.acme.jga.infra.converters.UsersInfraConverter;
import com.acme.jga.infra.dao.api.users.IUsersDao;
import com.acme.jga.infra.dto.users.v1.UserDb;
import com.acme.jga.infra.dto.users.v1.UserDisplayDb;
import com.acme.jga.infra.services.api.users.IUsersInfraService;
import com.acme.jga.infra.services.impl.AbstractInfraService;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UsersInfraService extends AbstractInfraService implements IUsersInfraService {
    private static final String INSTRUMENTATION_NAME = UsersInfraService.class.getCanonicalName();
    private final IUsersDao usersDao;
    private final UsersInfraConverter usersInfraConverter;

    @Autowired
    public UsersInfraService(IUsersDao usersDao, UsersInfraConverter usersInfraConverter, OpenTelemetryWrapper openTelemetryWrapper) {
        super(openTelemetryWrapper);
        this.usersInfraConverter = usersInfraConverter;
        this.usersDao = usersDao;
    }

    @Transactional
    @Override
    public CompositeId createUser(User user, Span parentSpan) throws FunctionalException {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_CREATE", parentSpan, () -> {
            UserDb userDb = usersInfraConverter.convertUserToDb(user);
            return usersDao.createUser(userDb);
        });
    }

    @Override
    public Optional<Long> emailUsed(String email, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_EMAIL_USED", parentSpan, () -> usersDao.emailExists(email));
    }

    @Override
    public Optional<Long> loginUsed(String login, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_LOGIN_USED", parentSpan, () -> usersDao.loginExists(login));
    }

    @Override
    public Integer updateUser(User user, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_UPDATE", parentSpan, () -> {
            UserDb userDb = usersInfraConverter.convertUserToDb(user);
            return usersDao.updateUser(userDb);
        });
    }

    @Override
    public Optional<User> findByUid(Long tenantId, Long orgId, String userUid, Span parentSpan) throws FunctionalException {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_FIND_BY_UID", parentSpan, () -> {
            Optional<UserDb> userDb = usersDao.findByUid(tenantId, orgId, userUid);
            return userDb.map(usersInfraConverter::convertUserDbToUser);
        });
    }

    @Override
    public List<User> findUsers(Long tenantId, Long orgId, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_LIST", parentSpan, () -> {
            List<UserDb> users = usersDao.findUsers(tenantId, orgId);
            return users.stream().map(usersInfraConverter::convertUserDbToUser).toList();
        });
    }

    @Override
    public Integer deleteUser(Long tenantId, Long orgId, Long userId, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_DELETE", parentSpan, () -> usersDao.deleteUser(tenantId, orgId, userId));
    }

    @Override
    public PaginatedResults<UserDisplay> filterUsers(Long tenantId, Long orgId, Span parentSpan, Map<String, Object> searchParams) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_FILTER", parentSpan, () -> {
            PaginatedResults<UserDisplayDb> paginatedResults = usersDao.filterUsers(tenantId, orgId, searchParams);
            List<UserDisplay> users = paginatedResults.getResults().stream()
                    .map(usersInfraConverter::convertUserDisplayDbToUserDisplay)
                    .toList();
            return new PaginatedResults<>(
                    paginatedResults.getNbResults(),
                    paginatedResults.getNbPages(),
                    users,
                    (Integer) searchParams.get(FilteringConstants.PAGE_SIZE),
                    (Integer) searchParams.get(FilteringConstants.PAGE_INDEX)
            );
        });
    }

    @Override
    public Optional<User> findByUid(String userUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_FIND_BY_UID", parentSpan, () -> {
            Optional<UserDb> userDb = usersDao.findByUid(userUid);
            return userDb.map(usersInfraConverter::convertUserDbToUser);
        });
    }

    @Override
    public Optional<User> findByEmail(String email, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_FIND_BY_EMAIL", parentSpan, () -> {
            Optional<UserDb> userDb = usersDao.findByEmail(email);
            return userDb.map(usersInfraConverter::convertUserDbToUser);
        });
    }

    @Override
    public Optional<User> findByLogin(String login, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_FIND_BY_LOGIN", parentSpan, () -> {
            Optional<UserDb> userDb = usersDao.findByLogin(login);
            return userDb.map(usersInfraConverter::convertUserDbToUser);
        });
    }

}

