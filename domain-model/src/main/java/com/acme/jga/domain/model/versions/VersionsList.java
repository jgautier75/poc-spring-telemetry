package com.acme.jga.domain.model.versions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class VersionsList {
    private List<ApiVersion> apiVersions;

    public void addApiVersion(ApiVersion apiVersion) {
        if (this.apiVersions == null) {
            this.apiVersions = new ArrayList<>();
        }
        this.apiVersions.add(apiVersion);
    }
}
