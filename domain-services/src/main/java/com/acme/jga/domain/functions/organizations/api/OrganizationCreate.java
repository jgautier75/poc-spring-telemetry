package com.acme.jga.domain.functions.organizations.api;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import io.opentelemetry.api.trace.Span;

public interface OrganizationCreate {
    /**
     * Create organization.
     *
     * @param tenantUid    Tenant external id
     * @param organization Organization
     * @param parentSpan Parent Span
     * @return Composite id
     */
    CompositeId execute(String tenantUid, Organization organization, Span parentSpan);
}
