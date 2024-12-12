package com.acme.jga.ports.port.tenants.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TenantListDisplayDto {
    private List<TenantDisplayDto> tenants;
}
