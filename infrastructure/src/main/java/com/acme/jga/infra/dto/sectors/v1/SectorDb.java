package com.acme.jga.infra.dto.sectors.v1;

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
public class SectorDb {
    private Long tenantId;
    private Long orgId;
    private Long id;
    private String uid;
    private String code;
    private String label;
    private boolean root;
    private Long parentId;
    private List<SectorDb> children = new ArrayList<>();

    public void addChild(SectorDb sectorDb) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(sectorDb);
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}
