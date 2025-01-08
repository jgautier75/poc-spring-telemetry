package com.acme.jga.domain.functions.organizations.api;

import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.jdbc.dql.PaginatedResults;
import io.opentelemetry.api.trace.Span;

import java.util.Map;

public interface OrganizationFilter {
    /**
     * Filter organizations.
     *
     * @param tenantId Tenant internal id
     * @param parentSpan OpenTelemetry Parent Span
     * @param searchParams Search parameters
     * @return Orgnizations list
     */
    PaginatedResults<Organization> execute(Long tenantId, Span parentSpan, Map<String,Object> searchParams);

}
