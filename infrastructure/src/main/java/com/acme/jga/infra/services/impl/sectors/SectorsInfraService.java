package com.acme.jga.infra.services.impl.sectors;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.infra.converters.SectorsConverter;
import com.acme.jga.infra.dao.api.sectors.ISectorsDao;
import com.acme.jga.infra.dto.sectors.v1.SectorDb;
import com.acme.jga.infra.services.api.sectors.ISectorsInfraService;
import com.acme.jga.infra.services.impl.AbstractInfraService;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.utils.otel.OtelContext;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class SectorsInfraService extends AbstractInfraService implements ISectorsInfraService {
    private static final String INSTRUMENTATION_NAME = SectorsInfraService.class.getCanonicalName();
    private final ISectorsDao sectorsDao;
    private final SectorsConverter sectorsConverter;
    private final ILoggingFacade loggingFacade;

    public SectorsInfraService(ISectorsDao sectorsDao, SectorsConverter sectorsConverter, ILoggingFacade loggingFacade, OpenTelemetryWrapper openTelemetryWrapper) {
        super(openTelemetryWrapper);
        this.sectorsDao = sectorsDao;
        this.sectorsConverter = sectorsConverter;
        this.loggingFacade = loggingFacade;
    }

    @Override
    public Sector fetchSectorsWithHierarchy(Long tenantId, Long organizationId, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_SECTORS_FIND_HIERARCHY", parentSpan, (span) -> {
            List<SectorDb> sectors = sectorsDao.findSectorsByOrgId(tenantId, organizationId, span);
            return sectors.stream()
                    .filter(SectorDb::isRoot)
                    .findFirst()
                    .map(rootSectorDb -> {
                        mapSectorsRecursively(rootSectorDb, sectors);
                        return sectorsConverter.convertSectorDbToDomain(rootSectorDb);
                    })
                    .orElse(null);
        });
    }

    @Override
    public CompositeId createSector(Long tenantId, Long organizationId, Sector sector, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_SECTOR_CREATE", parentSpan, (span) -> {
            SectorDb sectorDb = sectorsConverter.convertSectorDomaintoDb(sector);
            CompositeId compositeId = sectorsDao.createSector(tenantId, organizationId, sectorDb);
            loggingFacade.infoS(this.getClass().getName() + "createSector",
                    "Created sector with uid [%s] on tenant [%s] and organization [%s]",
                    new Object[]{compositeId.getUid(), tenantId, organizationId}, OtelContext.fromSpan(span));
            return compositeId;
        });
    }

    @Override
    public Optional<Sector> findSectorByUid(Long tenantId, Long orgId, String sectorUid) {
        return handleOptionalSectorDb(sectorsDao.findByUid(tenantId, orgId, sectorUid));
    }

    @Override
    public Optional<Long> existsByCode(String code) {
        return sectorsDao.existsByCode(code);
    }

    /**
     * Build sectors hierarchy.
     *
     * @param parentSector Parent sector
     * @param sectors      Sectors list
     */
    protected void mapSectorsRecursively(SectorDb parentSector, List<SectorDb> sectors) {
        List<SectorDb> children = sectors.stream()
                .filter(sect -> sect.getParentId() != null && sect.getParentId().equals(parentSector.getId()))
                .sorted(Comparator.comparing(SectorDb::getLabel))
                .toList();
        children.forEach(child -> {
            parentSector.addChild(child);
            mapSectorsRecursively(child, sectors);
        });
    }

    @Override
    public int updateSector(Long tenantId, Long orgId, Sector sector) {
        SectorDb sectorDb = sectorsConverter.convertSectorDomaintoDb(sector);
        return sectorsDao.updateSector(tenantId, orgId, sectorDb);
    }

    @Override
    public int deleteSector(Long tenantId, Long organizationId, Long sectorId) {
        return sectorsDao.deleteSector(tenantId, organizationId, sectorId);
    }

    private Optional<Sector> handleOptionalSectorDb(Optional<SectorDb> sectorDb) {
        return sectorDb.map(sectorsConverter::convertSectorDbToDomain);
    }
}
