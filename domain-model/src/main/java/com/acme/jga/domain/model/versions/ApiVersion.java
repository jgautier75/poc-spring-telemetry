package com.acme.jga.domain.model.versions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiVersion {
    private String version;
    private String category;
    private String code;
    private String uri;
}
