package com.acme.jga.ports.port.sectors.v1;

import com.acme.jga.domain.model.api.MainApiVersion;
import com.acme.jga.domain.model.api.IVersioned;
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
