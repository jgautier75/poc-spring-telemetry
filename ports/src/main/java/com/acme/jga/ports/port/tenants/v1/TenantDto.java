package com.acme.jga.ports.port.tenants.v1;

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
