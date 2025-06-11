package com.acme.jga.ports.services.api.sectors;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.ports.dtos.sectors.v1.SectorDisplayDto;
import com.acme.jga.ports.dtos.sectors.v1.SectorDto;
import com.acme.jga.ports.dtos.shared.UidDto;

public interface SectorsPortService {

    /**
     * Create sector.
     * 
     * @param tenantUid       Tenant uid
     * @param organizationUid Organization uid
     * @param sectorDto       Sector payload
     * @return Generated uid
     * @throws com.acme.jga.domain.model.exceptions.FunctionalException Functional error
     */
    UidDto createSector(String tenantUid, String organizationUid, SectorDto sectorDto) throws FunctionalException;

    /**
     * Find sectors hierarchy for an orgnanization.
     * 
     * @param tenantUid       Tenant uid
     * @param organizationUid Organization uid
     * @return Sector hierarchy
     * @throws FunctionalException Functional error
     */
    SectorDisplayDto findSectors(String tenantUid, String organizationUid) throws FunctionalException;

    /**
     * Update sector.
     * 
     * @param tenantUid       Tenant uid
     * @param organizationUid Organization uid
     * @param sectorUid       Sector uid
     * @param sectorDto       Sector DTO
     * @return Nb of updated sectors
     * @throws FunctionalException Functional error
     */
    Integer updateSector(String tenantUid, String organizationUid, String sectorUid, SectorDto sectorDto)
            throws FunctionalException;

    /**
     * Delete sector.
     * 
     * @param tenantUid       Tenant uid
     * @param organizationUid Organization uid
     * @param sectorUid       Sector uid
     * @return Nb of deleted sectors
     * @throws FunctionalException Functional error
     */
    Integer deleteSector(String tenantUid, String organizationUid, String sectorUid) throws FunctionalException;

}
