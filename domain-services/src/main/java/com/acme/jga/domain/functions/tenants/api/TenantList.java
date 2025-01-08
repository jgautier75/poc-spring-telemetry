package com.acme.jga.domain.functions.tenants.api;

import com.acme.jga.domain.model.v1.Tenant;
import io.opentelemetry.api.trace.Span;

import java.util.List;

public interface TenantList {
    List<Tenant> execute(Span parentSpan);
}
