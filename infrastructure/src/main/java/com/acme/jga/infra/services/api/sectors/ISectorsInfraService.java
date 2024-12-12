package com.acme.jga.infra.services.api.sectors;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Sector;
import io.opentelemetry.api.trace.Span;

import java.util.Optional;

public interface ISectorsInfraService {
    Sector fetchSectorsWithHierarchy(Long tenantId, Long organizationId);

    CompositeId createSector(Long tenantId, Long organizationId, Sector sector, Span parentSpan);

    Optional<Sector> findSectorByUid(Long tenantId, Long orgId, String sectorUid);

    Optional<Long> existsByCode(String code);

    int updateSector(Long tenantId, Long orgId, Sector sector);

    int deleteSector(Long tenantId, Long organizationId, Long sectorId);
}
