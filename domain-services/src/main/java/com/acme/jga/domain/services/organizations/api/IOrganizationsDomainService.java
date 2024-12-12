package com.acme.jga.domain.services.organizations.api;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.jdbc.dql.PaginatedResults;
import io.opentelemetry.api.trace.Span;

import java.util.List;
import java.util.Map;

public interface IOrganizationsDomainService {

    /**
     * Create organization.
     * 
     * @param tenantUid    Tenant external id
     * @param organization Organization
     * @param parentSpan Parent Span
     * @return Composite id
     */
    CompositeId createOrganization(String tenantUid, Organization organization, Span parentSpan);

    /**
     * Filter organizations.
     * 
     * @param tenantId Tenant internal id
     * @param parentSpan OpenTelemetry Parent Span
     * @param searchParams Search parameters
     * @return Orgnizations list
     */
    PaginatedResults<Organization> filterOrganizations(Long tenantId, Span parentSpan, Map<String,Object> searchParams);

    /**
     * Find organizations.
     * 
     * @param tenantId     Tenant internal id
     * @param orgUid       Organization external id
     * @param fetchSectors Fetch sectors
     * @return Organization
     */
    Organization findOrganizationByTenantAndUid(Long tenantId, String orgUid, boolean fetchSectors, Span parentSpan);

    /**
     * Update organization.
     * 
     * @param tenantUid    Tenant external id
     * @param orgUid       Organization external id
     * @param organization Organization
     * @return Nb of rows updated
     */
    Integer updateOrganization(String tenantUid, String orgUid, Organization organization, Span parentSpan);

    /**
     * Find organizations by external id list.
     * 
     * @param orgIds Organization id list
     * @return Organizations list
     */
    List<Organization> findOrgsByIdList(List<Long> orgIds);

    /**
     * Delete organization.
     * 
     * @param tenantUid Tenant external id
     * @param orgUid    Organization external id
     * @return Nb of rows deleted
     */
    Integer deleteOrganization(String tenantUid, String orgUid, Span parentSpan);
}
