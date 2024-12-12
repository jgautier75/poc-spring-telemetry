package com.acme.jga.domain.model.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SystemErrorTemporal {
    private String timestamp;
    private String pattern;
}
