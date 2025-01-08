package com.acme.jga.domain.functions.organizations.api;

import com.acme.jga.domain.model.v1.Organization;
import io.opentelemetry.api.trace.Span;

import java.util.List;

public interface OrganizationFind {
    /**
     * Find organizations.
     *
     * @param tenantId     Tenant internal id
     * @param orgUid       Organization external id
     * @param fetchSectors Fetch sectors
     * @return Organization
     */
    Organization byTenantIdAndUid(Long tenantId, String orgUid, boolean fetchSectors, Span parentSpan);

    /**
     * Find organizations by external id list.
     *
     * @param orgIds Organization id list
     * @return Organizations list
     */
    List<Organization> byIdList(List<Long> orgIds);
}
