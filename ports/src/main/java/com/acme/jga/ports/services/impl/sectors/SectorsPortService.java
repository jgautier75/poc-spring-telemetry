package com.acme.jga.ports.services.impl.sectors;

import com.acme.jga.domain.functions.sectors.api.SectorCreate;
import com.acme.jga.domain.functions.sectors.api.SectorDelete;
import com.acme.jga.domain.functions.sectors.api.SectorHierarchy;
import com.acme.jga.domain.functions.sectors.api.SectorUpdate;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.converters.sectors.SectorsPortConverter;
import com.acme.jga.ports.dtos.sectors.v1.SectorDisplayDto;
import com.acme.jga.ports.dtos.sectors.v1.SectorDto;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.services.api.sectors.ISectorsPortService;
import com.acme.jga.ports.services.impl.AbstractPortService;
import com.acme.jga.ports.validation.sectors.SectorsValidationEngine;
import com.acme.jga.validation.ValidationException;
import com.acme.jga.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SectorsPortService extends AbstractPortService implements ISectorsPortService {
    private static final String INSTRUMENTATION_NAME = SectorsPortService.class.getCanonicalName();
    private final SectorCreate sectorCreate;
    private final SectorHierarchy sectorHierarchy;
    private final SectorUpdate sectorUpdate;
    private final SectorDelete sectorDelete;
    private final SectorsPortConverter sectorsConverter;
    private final SectorsValidationEngine sectorsValidationEngine;

    @Autowired
    public SectorsPortService(OpenTelemetryWrapper openTelemetryWrapper, SectorCreate sectorCreate,
                              SectorsPortConverter sectorsConverter, SectorsValidationEngine sectorsValidationEngine,
                              SectorHierarchy sectorHierarchy, SectorUpdate sectorUpdate,
                              SectorDelete sectorDelete) {
        super(openTelemetryWrapper);
        this.sectorCreate = sectorCreate;
        this.sectorHierarchy = sectorHierarchy;
        this.sectorsConverter = sectorsConverter;
        this.sectorsValidationEngine = sectorsValidationEngine;
        this.sectorUpdate = sectorUpdate;
        this.sectorDelete = sectorDelete;
    }

    @Override
    public UidDto createSector(String tenantUid, String organizationUid, SectorDto sectorDto) {
        return processWithSpan(INSTRUMENTATION_NAME, "SECTOR_PORT_CREATE", null, (span) -> {
            ValidationResult validationResult = sectorsValidationEngine.validate(sectorDto);
            if (!validationResult.isSuccess()) {
                throw new ValidationException(validationResult.getErrors());
            }
            Sector sector = sectorsConverter.convertSectorDtoToDomain(sectorDto);
            CompositeId compositeId = sectorCreate.execute(tenantUid, organizationUid, sector, span);
            return UidDto.builder().uid(compositeId.getUid()).build();
        });
    }

    @Override
    public SectorDisplayDto findSectors(String tenantUid, String organizationUid) {
        return processWithSpan(INSTRUMENTATION_NAME, "SECTORS_PORT_LIST", null, (span) -> {
            Sector rootSector = sectorHierarchy.execute(tenantUid, organizationUid, span);
            return sectorsConverter.convertSectorDomainToSectorDisplay(rootSector);
        });
    }

    @Override
    public Integer updateSector(String tenantUid, String organizationUid, String sectorUid, SectorDto sectorDto) {
        return processWithSpan(INSTRUMENTATION_NAME, "SECTORS_PORT_UPDATE", null, (span) -> {
            ValidationResult validationResult = sectorsValidationEngine.validate(sectorDto);
            if (!validationResult.isSuccess()) {
                throw new ValidationException(validationResult.getErrors());
            }
            Sector sector = sectorsConverter.convertSectorDtoToDomain(sectorDto);
            return sectorUpdate.execute(tenantUid, organizationUid, sectorUid, sector, span);
        });
    }

    @Override
    public Integer deleteSector(String tenantUid, String organizationUid, String sectorUid) {
        return processWithSpan(INSTRUMENTATION_NAME, "SECTORS_PORT_DELETE", null, (span) -> sectorDelete.execute(tenantUid, organizationUid, sectorUid, span));
    }

}
