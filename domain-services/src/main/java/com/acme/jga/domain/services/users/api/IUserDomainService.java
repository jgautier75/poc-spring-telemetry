package com.acme.jga.domain.services.users.api;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.jdbc.dql.PaginatedResults;
import io.opentelemetry.api.trace.Span;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserDomainService {

    CompositeId createUser(String tenantUid, String orgUid, User user, Span parentSpan);

    Integer updateUser(String tenantUid, String orgUid, User user, Span parentSpan);

    List<User> findUsers(String tenantUid, String orgUid, Span parentSpan);

    Integer deleteUser(String tenantUid, String orgUid, String userUid, Span parentSpan);

    User findByUid(String tenantUid, String orgUid, String userUid, Span parentSpan);

    PaginatedResults<User> filterUsers(Long tenantId, Long orgId, Span parentSpan, Map<String, Object> searchParams);

    Optional<User> findByEmail(String email, Span parentSpan);

    Optional<User> findByLogin(String login, Span parentSpan);

    Optional<User> findByUid(String userUid, Span parentSpan);
}
