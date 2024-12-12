package com.acme.jga.domain.services.tenants.api;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Tenant;
import io.opentelemetry.api.trace.Span;

import java.util.List;

public interface ITenantDomainService {

    CompositeId createTenant(Tenant tenant, Span parentSpan);

    Tenant findTenantByUid(String uid, Span parentSpan);

    List<Tenant> findAllTenants(Span parentSpan);

    Integer updateTenant(Tenant tenant, Span parentSpan);

    Integer deleteTenant(String tenantUid, Span parentSpan);

}
