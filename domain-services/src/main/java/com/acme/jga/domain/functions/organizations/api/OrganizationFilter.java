package com.acme.jga.domain.functions.organizations.api;

import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.jdbc.dql.PaginatedResults;

import java.util.Map;

public interface OrganizationFilter {
    /**
     * Filter organizations.
     *
     * @param tenantId Tenant internal id
     * @param searchParams Search parameters
     * @return Orgnizations list
     */
    PaginatedResults<Organization> execute(Long tenantId, Map<String,Object> searchParams);

}
