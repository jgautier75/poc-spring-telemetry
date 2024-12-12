package com.acme.jga.domain.services.sectors.api;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Sector;
import io.opentelemetry.api.trace.Span;

public interface ISectorsDomainService {

        /**
         * Create sector.
         * 
         * @param tenantUid       Tenant uid
         * @param organizationUid Organization uid
         * @param sector          Sector
         * @return Composite id (internal & external)
         */
        CompositeId createSector(String tenantUid, String organizationUid, Sector sector, Span parentSpan);

        /**
         * Find sector by uid.
         * @param tenantUid Tenant uid
         * @param organizationUid Organization uid
         * @param sectorUid Sector uid
         * @return SEctor
         */
        Sector findSectorByUidTenantOrg(String tenantUid, String organizationUid, String sectorUid, Span parentSpan);

        /**
         * Find sector.
         * 
         * @param tenantId       Tenant internal id
         * @param organizationId Organization internal id
         * @param sectorUid      Sector external id
         * @return Sector
         */
        Sector findSectorByUidTenantOrg(Long tenantId, Long organizationId, String sectorUid, Span parentSpan);

        /**
         * Find sectors hierarchy for an organization.
         * 
         * @param tenantUid       Tenant uid
         * @param organizationUid Organization uid
         * @return Root sector with children (recursive)
         */
        Sector fetchSectorsWithHierarchy(String tenantUid, String organizationUid, Span parentSpan);

        /**
         * Update sector.
         * 
         * @param tenantUid       Tenant uid
         * @param organizationUid Organization uid
         * @param sectorUid       Sector uid
         * @param sector          Sector
         * @return Number of rows update
         */
        Integer updateSector(String tenantUid, String organizationUid, String sectorUid, Sector sector, Span parentSpan);

        /**
         * Delete sector.
         * 
         * @param tenantUid       Tenant uid
         * @param organizationUid Organization uid
         * @param sectorUid       Sector uid
         * @return Nb of deleted sectors
         */
        Integer deleteSector(String tenantUid, String organizationUid, String sectorUid, Span parentSpan);

}
