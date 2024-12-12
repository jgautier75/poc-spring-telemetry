package com.acme.jga.domain.model.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SystemErrorFile {
    private SystemErrorTemporal temporal;
    private String moduleName;
    private String uid;
    private String fullFileName;
    private String content;
}
