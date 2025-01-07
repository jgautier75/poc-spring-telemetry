package com.acme.jga.domain.functions.tenants.api;

import io.opentelemetry.api.trace.Span;

public interface TenantDelete {
    Integer execute(String uid, Span parentSpan);
}
