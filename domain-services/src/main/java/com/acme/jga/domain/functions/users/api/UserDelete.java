package com.acme.jga.domain.functions.users.api;

import io.opentelemetry.api.trace.Span;

public interface UserDelete {
    Integer execute(String tenantUid, String orgUid, String userUid, Span parentSpan);
}
