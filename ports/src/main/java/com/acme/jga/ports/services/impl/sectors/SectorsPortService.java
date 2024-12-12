package com.acme.jga.ports.services.impl.sectors;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Sector;
import com.acme.jga.domain.services.sectors.api.ISectorsDomainService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.converters.sectors.SectorsPortConverter;
import com.acme.jga.ports.port.sectors.v1.SectorDisplayDto;
import com.acme.jga.ports.port.sectors.v1.SectorDto;
import com.acme.jga.ports.port.shared.UidDto;
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
    private final ISectorsDomainService sectorsDomainService;
    private final SectorsPortConverter sectorsConverter;
    private final SectorsValidationEngine sectorsValidationEngine;

    @Autowired
    public SectorsPortService(OpenTelemetryWrapper openTelemetryWrapper, ISectorsDomainService sectorsDomainService,
                              SectorsPortConverter sectorsConverter, SectorsValidationEngine sectorsValidationEngine) {
        super(openTelemetryWrapper);
        this.sectorsDomainService = sectorsDomainService;
        this.sectorsConverter = sectorsConverter;
        this.sectorsValidationEngine = sectorsValidationEngine;
    }

    @Override
    public UidDto createSector(String tenantUid, String organizationUid, SectorDto sectorDto) {
        return processWithSpan(INSTRUMENTATION_NAME, "SECTOR_PORT_CREATE", null, (span) -> {
            ValidationResult validationResult = sectorsValidationEngine.validate(sectorDto);
            if (!validationResult.isSuccess()) {
                throw new ValidationException(validationResult.getErrors());
            }
            Sector sector = sectorsConverter.convertSectorDtoToDomain(sectorDto);
            CompositeId compositeId = sectorsDomainService.createSector(tenantUid, organizationUid, sector, span);
            return UidDto.builder().uid(compositeId.getUid()).build();
        });
    }

    @Override
    public SectorDisplayDto findSectors(String tenantUid, String organizationUid) {
        return processWithSpan(INSTRUMENTATION_NAME, "SECTORS_PORT_LIST", null, (span) -> {
            Sector rootSector = sectorsDomainService.fetchSectorsWithHierarchy(tenantUid, organizationUid, span);
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
            return sectorsDomainService.updateSector(tenantUid, organizationUid, sectorUid, sector, span);
        });
    }

    @Override
    public Integer deleteSector(String tenantUid, String organizationUid, String sectorUid) {
        return processWithSpan(INSTRUMENTATION_NAME, "SECTORS_PORT_DELETE", null, (span) -> sectorsDomainService.deleteSector(tenantUid, organizationUid, sectorUid, span));
    }

}
