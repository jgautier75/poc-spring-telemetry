package com.acme.jga.infra.services.api.organizations;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.jdbc.dql.PaginatedResults;
import io.opentelemetry.api.trace.Span;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrganizationsInfraService {
    CompositeId createOrganization(Organization organization, Span parentSpan);

    PaginatedResults<Organization> filterOrganizations(Long tenantId, Span parentSpan, Map<String,Object> searchParams);

    Optional<Organization> findOrganizationByUid(Long tenantId, String uid,Span parentSpan);

    Integer updateOrganization(Long tenantId, Long orgId, String code, String label, String country, OrganizationStatus status);

    Optional<Long> codeAlreadyUsed(String code, Span parentSpan);

    List<Organization> findOrgsByIdList(List<Long> orgIds);

    Integer deleteUsersByOrganization(Long tenantId, Long orgId);

    Integer deleteById(Long tenantId, Long orgId);

    Integer deleteSectors(Long tenantId, Long orgId);
}
