package com.acme.jga.ports.converters.tenant;

import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.ports.dtos.tenants.v1.TenantDisplayDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TenantsPortConverter {

    public Tenant tenantDtoToDomainTenant(TenantDto tenantDto) {
        return Optional.ofNullable(tenantDto).map(t -> Tenant.builder()
                .label(tenantDto.getLabel())
                .code(tenantDto.getCode())
                .build()).orElse(null);
    }

    public TenantDisplayDto tenantDomainToDisplay(Tenant tenant) {
        return tenant != null ? new TenantDisplayDto(tenant.getUid(), tenant.getCode(), tenant.getLabel()) : null;
    }

}
