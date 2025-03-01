package com.acme.jga.infra.dao.api.sectors;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.infra.dto.sectors.v1.SectorDb;
import io.opentelemetry.api.trace.Span;

import java.util.List;
import java.util.Optional;

public interface SectorsDao {

    Optional<SectorDb> findByUid(Long tenantId, Long orgId, String uid);

    List<SectorDb> findSectorsByOrgId(Long tenantId, Long orgId, Span parentSpan);

    CompositeId createSector(Long tenantId, Long orgId, SectorDb sectorDb);

    Optional<Long> existsByCode(String code);

    int updateSector(Long tenantId, Long orgId, SectorDb sectorDb);

    int deleteSector(Long tenantId, Long orgId, Long sectorId);
}
