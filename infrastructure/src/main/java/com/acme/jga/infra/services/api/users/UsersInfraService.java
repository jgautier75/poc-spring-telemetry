package com.acme.jga.infra.services.api.users;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.domain.model.v1.UserDisplay;
import com.acme.jga.jdbc.dql.PaginatedResults;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UsersInfraService {

    CompositeId createUser(User user) throws FunctionalException;

    Optional<Long> emailUsed(String email);

    Optional<Long> loginUsed(String login);

    Integer updateUser(User user);

    Optional<User> findByUid(Long tenantId, Long orgId, String userUid) throws FunctionalException;

    List<User> findUsers(Long tenantId, Long orgId);

    Integer deleteUser(Long tenantId, Long orgId, Long userId);

    PaginatedResults<UserDisplay> filterUsers(Long tenantId, Long orgId, Map<String, Object> searchParams);

    Optional<User> findByUid(String userUid);

    Optional<User> findByEmail(String email);

    Optional<User> findByLogin(String login);
}
