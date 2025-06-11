package com.acme.jga.infra.services.api.organizations;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.jdbc.dql.PaginatedResults;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrganizationsInfraService {
    CompositeId createOrganization(Organization organization);

    PaginatedResults<Organization> filterOrganizations(Long tenantId, Map<String,Object> searchParams);

    Optional<Organization> findOrganizationByUid(Long tenantId, String uid);

    Integer updateOrganization(Long tenantId, Long orgId, String code, String label, String country, OrganizationStatus status);

    Optional<Long> codeAlreadyUsed(String code);

    List<Organization> findOrgsByIdList(List<Long> orgIds);

    Integer deleteUsersByOrganization(Long tenantId, Long orgId);

    Integer deleteById(Long tenantId, Long orgId);

    Integer deleteSectors(Long tenantId, Long orgId);
}
