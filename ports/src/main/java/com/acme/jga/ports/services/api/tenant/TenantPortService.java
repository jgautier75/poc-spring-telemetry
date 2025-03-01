package com.acme.jga.ports.services.api.tenant;

import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantDisplayDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantListDisplayDto;
import io.opentelemetry.api.trace.Span;

public interface TenantPortService {

    /**
     * Create tenant.
     *
     * @param tenantDto Tenant payload
     * @return Generated uid
     */
    UidDto createTenant(TenantDto tenantDto, Span parentSpan);

    /**
     * Find tenant by uid.
     *
     * @param uid Tenant uid
     * @return Tenant
     */
    TenantDisplayDto findTenantByUid(String uid, Span parentSpan);

    /**
     * List all tenants for diaplay.
     *
     * @return Tenants list
     */
    TenantListDisplayDto findAllTenants(Span parentSpan);

    /**
     * Update tenant.
     *
     * @param uid       Tenant uid
     * @param tenantDto Tenant payload
     */
    Integer updateTenant(String uid, TenantDto tenantDto, Span parentSpan);

    /**
     * Delete tenant and related data.
     *
     * @param tenantUid Tenant uid
     */
    Integer deleteTenant(String tenantUid, Span parentSpan);
}
