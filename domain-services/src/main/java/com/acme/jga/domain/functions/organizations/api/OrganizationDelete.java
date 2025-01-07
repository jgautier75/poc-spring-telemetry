package com.acme.jga.domain.functions.organizations.api;

import io.opentelemetry.api.trace.Span;

public interface OrganizationDelete {
    /**
     * Delete organization.
     *
     * @param tenantUid Tenant external id
     * @param orgUid    Organization external id
     * @return Nb of rows deleted
     */
    Integer execute(String tenantUid, String orgUid, Span parentSpan);
}
