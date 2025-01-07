package com.acme.jga.domain.functions.sectors.api;

import io.opentelemetry.api.trace.Span;

public interface SectorDelete {
    /**
     * Delete sector.
     *
     * @param tenantUid       Tenant uid
     * @param organizationUid Organization uid
     * @param sectorUid       Sector uid
     * @return Nb of deleted sectors
     */
    Integer execute(String tenantUid, String organizationUid, String sectorUid, Span parentSpan);

}
