package com.acme.jga.ports.dtos.system.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class SystemErrorFileDto {
    private SystemErrorTemporalDto temporal;
    private String moduleName;
    private String uid;
    private String fullFileName;
    private String content;
}