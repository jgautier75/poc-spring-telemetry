package com.acme.jga.domain.functions.sectors.api;

import com.acme.jga.domain.model.v1.Sector;

public interface SectorUpdate {
    /**
     * Update sector.
     *
     * @param tenantUid       Tenant uid
     * @param organizationUid Organization uid
     * @param sectorUid       Sector uid
     * @param sector          Sector
     * @return Number of rows update
     */
    Integer execute(String tenantUid, String organizationUid, String sectorUid, Sector sector);

}
