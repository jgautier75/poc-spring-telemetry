package com.acme.jga.infra.dto.tenants.v1;

import com.acme.jga.domain.model.api.MainApiVersion;
import com.acme.jga.domain.model.api.IVersioned;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class TenantDb implements IVersioned {
    private Long id;
    private String uid;
    private String code;
    private String label;

    @Override
    public MainApiVersion getVersion() {
        return MainApiVersion.V1;
    }

}
