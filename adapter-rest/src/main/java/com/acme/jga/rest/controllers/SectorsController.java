package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.ports.port.sectors.v1.SectorDisplayDto;
import com.acme.jga.ports.port.sectors.v1.SectorDto;
import com.acme.jga.ports.port.shared.UidDto;
import com.acme.jga.ports.services.api.sectors.ISectorsPortService;
import com.acme.jga.rest.versioning.WebApiVersions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SectorsController {
    private final ISectorsPortService sectorsPortService;

    @PostMapping(value = WebApiVersions.SectorsResourceVersion.ROOT)
    public ResponseEntity<UidDto> createSector(@PathVariable("tenantUid") String tenantUid,
                                               @PathVariable("orgUid") String orgUid,
                                               @RequestBody SectorDto sectorDto) throws FunctionalException {
        UidDto uidDto = sectorsPortService.createSector(tenantUid, orgUid, sectorDto);
        return new ResponseEntity<>(uidDto, HttpStatus.CREATED);
    }

    @GetMapping(value = WebApiVersions.SectorsResourceVersion.ROOT)
    public ResponseEntity<SectorDisplayDto> findSectors(@PathVariable("tenantUid") String tenantUid,
                                                        @PathVariable("orgUid") String orgUid) throws FunctionalException {
        SectorDisplayDto sectorDisplayDto = sectorsPortService.findSectors(tenantUid, orgUid);
        return new ResponseEntity<>(sectorDisplayDto, HttpStatus.OK);
    }

    @PostMapping(value = WebApiVersions.SectorsResourceVersion.WITH_UID)
    public ResponseEntity<Void> updateSector(@PathVariable("tenantUid") String tenantUid,
                                             @PathVariable("orgUid") String orgUid,
                                             @PathVariable("sectorUid") String sectorUid,
                                             @RequestBody SectorDto sector) throws FunctionalException {
        sectorsPortService.updateSector(tenantUid, orgUid, sectorUid, sector);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = WebApiVersions.SectorsResourceVersion.WITH_UID)
    public ResponseEntity<Void> deleteSector(@PathVariable("tenantUid") String tenantUid,
                                             @PathVariable("orgUid") String orgUid,
                                             @PathVariable("sectorUid") String sectorUid) throws FunctionalException {
        sectorsPortService.deleteSector(tenantUid, orgUid, sectorUid);
        return ResponseEntity.noContent().build();
    }

}
