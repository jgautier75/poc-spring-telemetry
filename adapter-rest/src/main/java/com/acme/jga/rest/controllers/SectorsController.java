package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.dtos.sectors.v1.SectorDisplayDto;
import com.acme.jga.ports.dtos.sectors.v1.SectorDto;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.services.api.sectors.ISectorsPortService;
import com.acme.jga.rest.versioning.WebApiVersions;
import com.acme.jga.utils.otel.OtelContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SectorsController extends AbstractController {
    private static final String INSTRUMENTATION_NAME = SectorsController.class.getCanonicalName();
    private final ISectorsPortService sectorsPortService;
    private final ILoggingFacade loggingFacade;

    public SectorsController(ISectorsPortService sectorsPortService, OpenTelemetryWrapper openTelemetryWrapper, ILoggingFacade loggingFacade) {
        super(openTelemetryWrapper);
        this.sectorsPortService = sectorsPortService;
        this.loggingFacade = loggingFacade;
    }

    @PostMapping(value = WebApiVersions.SectorsResourceVersion.ROOT)
    public ResponseEntity<UidDto> createSector(@PathVariable("tenantUid") String tenantUid,
                                               @PathVariable("orgUid") String orgUid,
                                               @RequestBody SectorDto sectorDto) throws FunctionalException {
        UidDto uidDto = withSpan(INSTRUMENTATION_NAME, "API_SECTORS_CREATE", (span) -> sectorsPortService.createSector(tenantUid, orgUid, sectorDto, span));
        return new ResponseEntity<>(uidDto, HttpStatus.CREATED);
    }

    @GetMapping(value = WebApiVersions.SectorsResourceVersion.ROOT)
    public ResponseEntity<SectorDisplayDto> findSectors(@PathVariable("tenantUid") String tenantUid,
                                                        @PathVariable("orgUid") String orgUid) throws FunctionalException {
        SectorDisplayDto sectorDisplayDto = withSpan(INSTRUMENTATION_NAME, "API_SECTORS_LIST", (span) -> {
            loggingFacade.infoS(INSTRUMENTATION_NAME, "Find sectors for tenant [%s] and org [%s]", new Object[]{tenantUid, orgUid}, OtelContext.fromSpan(span));
            return sectorsPortService.findSectors(tenantUid, orgUid, span);
        });
        return new ResponseEntity<>(sectorDisplayDto, HttpStatus.OK);
    }

    @PostMapping(value = WebApiVersions.SectorsResourceVersion.WITH_UID)
    public ResponseEntity<Void> updateSector(@PathVariable("tenantUid") String tenantUid,
                                             @PathVariable("orgUid") String orgUid,
                                             @PathVariable("sectorUid") String sectorUid,
                                             @RequestBody SectorDto sector) throws FunctionalException {
        withSpan(INSTRUMENTATION_NAME, "API_SECTORS_UPDATE", (span) -> sectorsPortService.updateSector(tenantUid, orgUid, sectorUid, sector, span));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = WebApiVersions.SectorsResourceVersion.WITH_UID)
    public ResponseEntity<Void> deleteSector(@PathVariable("tenantUid") String tenantUid,
                                             @PathVariable("orgUid") String orgUid,
                                             @PathVariable("sectorUid") String sectorUid) throws FunctionalException {
        withSpan(INSTRUMENTATION_NAME, "API_SECTORS_DELETE", (span) -> sectorsPortService.deleteSector(tenantUid, orgUid, sectorUid, span));
        return ResponseEntity.noContent().build();
    }

}
