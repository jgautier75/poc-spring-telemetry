package com.acme.jga.infra.services.api.tenants;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Tenant;
import io.opentelemetry.api.trace.Span;

import java.util.List;
import java.util.Optional;

public interface TenantInfraService {
    CompositeId createTenant(Tenant tenant, Span parentSpan);

    Optional<Tenant> findTenantByUid(String uid, Span parentSpan);

    boolean tenantExistsByCode(String code, Span parentSpan);

    List<Tenant> findAllTenants(Span parentSpan);

    Integer updateTenant(Tenant tenant, Span parentSpan);

    Integer deleteUsersByTenantId(Long tenantId, Span parentSpan);

    Integer deleteOrganizationsByTenantId(Long tenantId, Span parentSpan);

    Integer deleteTenant(Long tenantId, Span parentSpan);

    Integer deleteSectorsByTenantId(Long tenantId, Span parentSpan);
}
