package com.acme.jga.infra.converters;

import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.dto.tenants.v1.TenantDb;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TenantsInfraConverter {

    public TenantDb tenantDomainToTenantDb(Tenant tenant) {
        return Optional.ofNullable(tenant).map(t -> TenantDb.builder()
                .code(tenant.getCode())
                .label(tenant.getLabel())
                .build()).orElse(null);
    }

    public Tenant tenantDbToTenantDomain(TenantDb tenantDb) {
        return Optional.ofNullable(tenantDb).map(t -> Tenant.builder()
                .code(tenantDb.getCode())
                .id(tenantDb.getId())
                .uid(tenantDb.getUid())
                .label(tenantDb.getLabel())
                .build()).orElse(null);
    }

}
