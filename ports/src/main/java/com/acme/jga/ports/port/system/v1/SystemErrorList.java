package com.acme.jga.ports.port.system.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SystemErrorList {
    private List<SystemErrorFileDto> errors;
}
