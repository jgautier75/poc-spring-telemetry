package com.acme.jga.domain.functions.users.api;

import com.acme.jga.domain.model.v1.User;
import io.opentelemetry.api.trace.Span;

import java.util.Optional;

public interface UserFind {
    User byUid(String tenantUid, String orgUid, String userUid, Span parentSpan);

    Optional<User> byEmail(String email, Span parentSpan);

    Optional<User> byLogin(String login, Span parentSpan);

    Optional<User> byUid(String userUid, Span parentSpan);
}
