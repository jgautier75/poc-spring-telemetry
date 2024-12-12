package com.acme.jga.infra.converters;

import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.infra.dto.sectors.v1.SectorDb;
import com.acme.jga.utils.lambdas.StreamUtil;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SectorsConverter {

    public Sector convertSectorDbToDomain(SectorDb sectorDb) {
        Sector sector = null;
        if (sectorDb != null) {
            sector = convertToDomain(sectorDb);
            traverseForDomain(sectorDb, sector);
        }
        return sector;
    }

    private void traverseForDomain(SectorDb parentSectorDb, Sector parentSector) {
        StreamUtil.ofNullableList(parentSectorDb.getChildren()).forEach(sdb -> {
            Sector kid = convertToDomain(sdb);
            parentSector.addChild(kid);
            traverseForDomain(sdb, kid);
        });
    }

    private Sector convertToDomain(SectorDb sectorDb) {
        return Sector.builder()
                .code(sectorDb.getCode())
                .id(sectorDb.getId())
                .label(sectorDb.getLabel())
                .orgId(sectorDb.getOrgId())
                .root(sectorDb.isRoot())
                .uid(sectorDb.getUid())
                .build();
    }

    public SectorDb convertSectorDomaintoDb(Sector sector) {
        return Optional.ofNullable(sector).map(s -> SectorDb.builder()
                .code(sector.getCode())
                .id(sector.getId())
                .label(sector.getLabel())
                .orgId(sector.getOrgId())
                .parentId(sector.getParentId())
                .root(sector.isRoot())
                .tenantId(sector.getTenantId())
                .uid(sector.getUid())
                .build()).orElse(null);
    }

}
