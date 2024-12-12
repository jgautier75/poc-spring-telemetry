package com.acme.jga.ports.port.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class UidDto {
    private String uid;
}
