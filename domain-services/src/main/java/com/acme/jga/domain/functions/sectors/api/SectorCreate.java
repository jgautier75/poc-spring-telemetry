package com.acme.jga.domain.functions.sectors.api;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Sector;

public interface SectorCreate {
    /**
     * Create sector.
     *
     * @param tenantUid       Tenant uid
     * @param organizationUid Organization uid
     * @param sector          Sector
     * @return Composite id (internal & external)
     */
    CompositeId execute(String tenantUid, String organizationUid, Sector sector);
}
