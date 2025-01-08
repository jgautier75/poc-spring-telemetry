package com.acme.jga.infra.dao.api.users;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.infra.dto.users.v1.UserDb;
import com.acme.jga.infra.dto.users.v1.UserDisplayDb;
import com.acme.jga.jdbc.dql.PaginatedResults;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUsersDao {

    CompositeId createUser(UserDb userDb);

    Optional<UserDb> findById(Long tenantId, Long orgId, Long id);

    Optional<UserDb> findByUid(Long tenantId, Long orgId, String uid);

    Optional<UserDb> findByUid(String uid);

    Optional<UserDb> findByLogin(String login);

    Optional<UserDb> findByEmail(String email);

    Integer updateUser(UserDb userDb);

    Integer deleteUser(Long tenantId, Long orgId, Long userId);

    Optional<Long> emailExists(String email);

    Optional<Long> loginExists(String login);

    List<UserDb> findUsers(Long tenantId, Long orgId);

    PaginatedResults<UserDisplayDb> filterUsers(Long tenantId, Long orgId, Map<String, Object> searchParams);
}
