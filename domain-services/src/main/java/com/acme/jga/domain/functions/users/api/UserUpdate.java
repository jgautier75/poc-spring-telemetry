package com.acme.jga.domain.functions.users.api;

import com.acme.jga.domain.model.v1.User;
import io.opentelemetry.api.trace.Span;

public interface UserUpdate {
    Integer execute(String tenantUid, String orgUid, User user, Span parentSpan);
}
