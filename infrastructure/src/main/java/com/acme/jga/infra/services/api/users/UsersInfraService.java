package com.acme.jga.infra.services.api.users;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.domain.model.v1.UserDisplay;
import com.acme.jga.jdbc.dql.PaginatedResults;
import io.opentelemetry.api.trace.Span;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UsersInfraService {

    CompositeId createUser(User user, Span parentSpan) throws FunctionalException;

    Optional<Long> emailUsed(String email, Span parentSpan);

    Optional<Long> loginUsed(String login, Span parentSpan);

    Integer updateUser(User user, Span parentSpan);

    Optional<User> findByUid(Long tenantId, Long orgId, String userUid, Span parentSpan) throws FunctionalException;

    List<User> findUsers(Long tenantId, Long orgId, Span parentSpan);

    Integer deleteUser(Long tenantId, Long orgId, Long userId, Span parentSpan);

    PaginatedResults<UserDisplay> filterUsers(Long tenantId, Long orgId, Span parentSpan, Map<String, Object> searchParams);

    Optional<User> findByUid(String userUid, Span parentSpan);

    Optional<User> findByEmail(String email, Span parentSpan);

    Optional<User> findByLogin(String login, Span parentSpan);
}
