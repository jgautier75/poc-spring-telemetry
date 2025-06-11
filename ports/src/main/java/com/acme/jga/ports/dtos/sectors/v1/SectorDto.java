package com.acme.jga.ports.dtos.sectors.v1;

import com.acme.jga.domain.model.api.IVersioned;
import com.acme.jga.domain.model.api.MainApiVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class SectorDto implements IVersioned {
    private String code;
    private String label;
    private String parentUid;

    @Override
    public MainApiVersion getVersion() {
        return MainApiVersion.V1;
    }

}
