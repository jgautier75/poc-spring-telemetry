package com.acme.jga.infra.services.impl.users;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.filtering.FilteringConstants;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.domain.model.v1.UserDisplay;
import com.acme.jga.infra.converters.UsersInfraConverter;
import com.acme.jga.infra.dao.api.users.UsersDao;
import com.acme.jga.infra.dto.users.v1.UserDb;
import com.acme.jga.infra.dto.users.v1.UserDisplayDb;
import com.acme.jga.infra.services.api.users.UsersInfraService;
import com.acme.jga.infra.services.impl.AbstractInfraService;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.utils.otel.OtelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UsersInfraServiceImpl extends AbstractInfraService implements UsersInfraService {
    private static final String INSTRUMENTATION_NAME = UsersInfraServiceImpl.class.getCanonicalName();
    private final UsersDao usersDao;
    private final UsersInfraConverter usersInfraConverter;
    private final ILoggingFacade loggingFacade;

    @Autowired
    public UsersInfraServiceImpl(UsersDao usersDao, UsersInfraConverter usersInfraConverter, OpenTelemetryWrapper openTelemetryWrapper, ILoggingFacade loggingFacade) {
        super(openTelemetryWrapper);
        this.usersInfraConverter = usersInfraConverter;
        this.usersDao = usersDao;
        this.loggingFacade = loggingFacade;
    }

    @Transactional
    @Override
    public CompositeId createUser(User user) throws FunctionalException {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_CREATE", (span) -> {
            UserDb userDb = usersInfraConverter.convertUserToDb(user);
            return usersDao.createUser(userDb);
        });
    }

    @Override
    public Optional<Long> emailUsed(String email) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_EMAIL_USED",
                (span) -> usersDao.emailExists(email));
    }

    @Override
    public Optional<Long> loginUsed(String login) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_LOGIN_USED",
                (span) -> usersDao.loginExists(login));
    }

    @Override
    public Integer updateUser(User user) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_UPDATE", (span) -> {
            UserDb userDb = usersInfraConverter.convertUserToDb(user);
            return usersDao.updateUser(userDb);
        });
    }

    @Override
    public Optional<User> findByUid(Long tenantId, Long orgId, String userUid) throws FunctionalException {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_FIND_BY_UID", (span) -> {
            Optional<UserDb> userDb = usersDao.findByUid(tenantId, orgId, userUid);
            return userDb.map(usersInfraConverter::convertUserDbToUser);
        });
    }

    @Override
    public List<User> findUsers(Long tenantId, Long orgId) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_LIST", (span) -> {
            List<UserDb> users = usersDao.findUsers(tenantId, orgId);
            return users.stream().map(usersInfraConverter::convertUserDbToUser).toList();
        });
    }

    @Override
    public Integer deleteUser(Long tenantId, Long orgId, Long userId) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_DELETE", (span) -> usersDao.deleteUser(tenantId, orgId, userId));
    }

    @Override
    public PaginatedResults<UserDisplay> filterUsers(Long tenantId, Long orgId, Map<String, Object> searchParams) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_FILTER",  (span) -> {
            PaginatedResults<UserDisplayDb> paginatedResults = usersDao.filterUsers(tenantId, orgId, searchParams);
            loggingFacade.infoS(INSTRUMENTATION_NAME, "Found [%s] users", new Object[]{paginatedResults.getNbResults()}, OtelContext.fromSpan(span));
            List<UserDisplay> users = paginatedResults.getResults().stream()
                    .map(usersInfraConverter::convertUserDisplayDbToUserDisplay)
                    .toList();
            return new PaginatedResults<UserDisplay>(
                    paginatedResults.getNbResults(),
                    paginatedResults.getNbPages(),
                    users,
                    (Integer) searchParams.get(FilteringConstants.PAGE_SIZE),
                    (Integer) searchParams.get(FilteringConstants.PAGE_INDEX)
            );
        });
    }

    @Override
    public Optional<User> findByUid(String userUid) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_FIND_BY_UID", (span) -> {
            Optional<UserDb> userDb = usersDao.findByUid(userUid);
            return userDb.map(usersInfraConverter::convertUserDbToUser);
        });
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_FIND_BY_EMAIL", (span) -> {
            Optional<UserDb> userDb = usersDao.findByEmail(email);
            return userDb.map(usersInfraConverter::convertUserDbToUser);
        });
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_USERS_FIND_BY_LOGIN", (span) -> {
            Optional<UserDb> userDb = usersDao.findByLogin(login);
            return userDb.map(usersInfraConverter::convertUserDbToUser);
        });
    }

}

