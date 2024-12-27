package com.acme.jga.ports.dtos.tenants.v1;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class TenantDto {
    private String code;
    private String label;
}
