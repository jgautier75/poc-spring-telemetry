package com.acme.jga.domain.functions.sectors.api;

public interface SectorDelete {
    /**
     * Delete sector.
     *
     * @param tenantUid       Tenant uid
     * @param organizationUid Organization uid
     * @param sectorUid       Sector uid
     * @return Nb of deleted sectors
     */
    Integer execute(String tenantUid, String organizationUid, String sectorUid);

}
