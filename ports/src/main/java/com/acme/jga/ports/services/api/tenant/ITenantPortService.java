package com.acme.jga.ports.services.api.tenant;

import com.acme.jga.ports.port.shared.UidDto;
import com.acme.jga.ports.port.tenants.v1.TenantDisplayDto;
import com.acme.jga.ports.port.tenants.v1.TenantDto;
import com.acme.jga.ports.port.tenants.v1.TenantListDisplayDto;

public interface ITenantPortService {

    /**
     * Create tenant.
     *
     * @param tenantDto Tenant payload
     * @return Generated uid
     */
    UidDto createTenant(TenantDto tenantDto);

    /**
     * Find tenant by uid.
     *
     * @param uid Tenant uid
     * @return Tenant
     */
    TenantDisplayDto findTenantByUid(String uid);

    /**
     * List all tenants for diaplay.
     *
     * @return Tenants list
     */
    TenantListDisplayDto findAllTenants();

    /**
     * Update tenant.
     *
     * @param uid       Tenant uid
     * @param tenantDto Tenant payload
     */
    Integer updateTenant(String uid, TenantDto tenantDto);

    /**
     * Delete tenant and related data.
     *
     * @param tenantUid Tenant uid
     */
    Integer deleteTenant(String tenantUid);
}
