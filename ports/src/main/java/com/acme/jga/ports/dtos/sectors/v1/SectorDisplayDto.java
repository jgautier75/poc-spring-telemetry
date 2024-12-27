package com.acme.jga.ports.dtos.sectors.v1;

import com.acme.jga.domain.model.api.MainApiVersion;
import com.acme.jga.domain.model.api.IVersioned;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class SectorDisplayDto implements IVersioned {
    private String uid;
    private String code;
    private String label;
    private boolean root;
    private String parentUid;
    private List<SectorDisplayDto> children;

    @Override
    public MainApiVersion getVersion() {
        return MainApiVersion.V1;
    }

    public void addSector(SectorDisplayDto sectorDisplayDto) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(sectorDisplayDto);
    }
}
