package com.acme.jga.infra.services.api.tenants;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Tenant;

import java.util.List;
import java.util.Optional;

public interface TenantInfraService {
    CompositeId createTenant(Tenant tenant);

    Optional<Tenant> findTenantByUid(String uid);

    boolean tenantExistsByCode(String code);

    List<Tenant> findAllTenants();

    Integer updateTenant(Tenant tenant);

    Integer deleteUsersByTenantId(Long tenantId);

    Integer deleteOrganizationsByTenantId(Long tenantId);

    Integer deleteTenant(Long tenantId);

    Integer deleteSectorsByTenantId(Long tenantId);
}
