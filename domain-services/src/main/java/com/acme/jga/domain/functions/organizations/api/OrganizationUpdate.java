package com.acme.jga.domain.functions.organizations.api;

import com.acme.jga.domain.model.v1.Organization;
import io.opentelemetry.api.trace.Span;

public interface OrganizationUpdate {
    /**
     * Update organization.
     *
     * @param tenantUid    Tenant external id
     * @param orgUid       Organization external id
     * @param organization Organization
     * @return Nb of rows updated
     */
    Integer execute(String tenantUid, String orgUid, Organization organization, Span parentSpan);
}
