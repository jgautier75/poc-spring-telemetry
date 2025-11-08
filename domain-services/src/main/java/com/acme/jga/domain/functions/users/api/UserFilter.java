package com.acme.jga.domain.functions.users.api;

import com.acme.jga.domain.model.v1.UserDisplay;
import com.acme.jga.jdbc.dql.PaginatedResults;
import io.opentelemetry.api.trace.Span;

import java.util.Map;

public interface UserFilter {
    PaginatedResults<UserDisplay> execute(String tenantUid, String orgUid, Span parentSpan, Map<String, Object> searchParams);
}
