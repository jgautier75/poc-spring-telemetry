package com.acme.jga.domain.functions.users.api;

import com.acme.jga.domain.model.v1.User;
import io.opentelemetry.api.trace.Span;

import java.util.List;

public interface UserList {
    List<User> execute(String tenantUid, String orgUid, Span parentSpan);
}
