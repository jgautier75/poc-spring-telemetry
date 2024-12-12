package com.acme.jga.infra.dao.api.tenants;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.infra.dto.tenants.v1.TenantDb;

import java.util.List;
import java.util.Optional;

public interface ITenantsDao {
    TenantDb findById(Long id);

    Optional<TenantDb> findByUid(String uid);

    Optional<TenantDb> findByCode(String code);

    CompositeId createTenant(String code, String label);

    Integer updateTenant(Long tenantId, String code, String label);

    Integer deleteTenant(Long tenantId);

    Boolean existsByCode(String code);

    List<TenantDb> findAllTenants();

    Integer deleteUsersByTenantId(Long tenantId);

    Integer deleteOrganizationsByTenantId(Long tenantId);

    Integer deleteSectorsByTenantId(Long tenantId);

}
