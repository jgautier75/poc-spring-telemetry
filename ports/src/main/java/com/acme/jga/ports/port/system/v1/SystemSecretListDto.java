package com.acme.jga.ports.port.system.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class SystemSecretListDto {
    private List<SystemSecretDto> secrets;
}
