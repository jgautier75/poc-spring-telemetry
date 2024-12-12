package com.acme.jga.ports.converters.sectors;

import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.ports.port.sectors.v1.SectorDisplayDto;
import com.acme.jga.ports.port.sectors.v1.SectorDto;
import com.acme.jga.utils.lambdas.StreamUtil;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SectorsPortConverter {

    public Sector convertSectorDtoToDomain(SectorDto sectorDto) {
        return Optional.ofNullable(sectorDto).map(s -> Sector.builder()
                .code(sectorDto.getCode())
                .label(sectorDto.getLabel())
                .parentUid(sectorDto.getParentUid())
                .build()).orElse(null);
    }

    public SectorDisplayDto convertSectorDomainToSectorDisplay(Sector sector) {
        return Optional.ofNullable(sector).map(s -> SectorDisplayDto.builder()
                .code(sector.getCode())
                .label(sector.getLabel())
                .parentUid(sector.getParentUid())
                .root(sector.isRoot())
                .uid(sector.getUid())
                .children(StreamUtil.ofNullableList(sector.getChildren()).map(this::convertSectorDomainToSectorDisplay).toList())
                .build()).orElse(null);
    }

}
