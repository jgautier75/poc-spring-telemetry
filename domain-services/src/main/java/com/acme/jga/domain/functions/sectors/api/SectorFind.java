package com.acme.jga.domain.functions.sectors.api;

import com.acme.jga.domain.model.v1.Sector;

public interface SectorFind {
    /**
     * Find sector by uid.
     *
     * @param tenantUid       Tenant uid
     * @param organizationUid Organization uid
     * @param sectorUid       Sector uid
     * @return SEctor
     */
    Sector byTenantOrgAndUid(String tenantUid, String organizationUid, String sectorUid);

    /**
     * Find sector.
     *
     * @param tenantId       Tenant internal id
     * @param organizationId Organization internal id
     * @param sectorUid      Sector external id
     * @return Sector
     */
    Sector byTenantOrgAndUid(Long tenantId, Long organizationId, String sectorUid);
}
