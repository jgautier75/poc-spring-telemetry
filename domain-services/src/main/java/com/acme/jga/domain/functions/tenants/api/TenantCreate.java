package com.acme.jga.domain.functions.tenants.api;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Tenant;
import io.opentelemetry.api.trace.Span;

public interface TenantCreate {

    CompositeId execute(Tenant tenant, Span parentSpan);

}
