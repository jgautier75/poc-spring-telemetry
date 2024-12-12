package com.acme.jga.infra.dao.api.organizations;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.infra.dto.organizations.v1.OrganizationDb;
import com.acme.jga.jdbc.dql.PaginatedResults;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IOrganizationsDao {

	CompositeId createOrganization(OrganizationDb org);

	OrganizationDb findOrganizationByTenantAndId(Long tenantId, Long id);

	OrganizationDb findOrganizationByTenantAndUid(Long tenantId, String uid);

	Integer updateOrganization(Long tenantId, Long orgId, String code, String label, String country, OrganizationStatus status);

	Integer deleteOrganization(Long tenantId, Long orgId);

	PaginatedResults<OrganizationDb> filterOrganizations(Long tenantId, Map<String,Object> searchParams);

	Optional<Long> existsByCode(String code);

	List<OrganizationDb> findOrgsByIdList(List<Long> orgIds);

	Integer deleteUsersByOrganization(Long tenantId, Long orgId);

	Integer deleteById(Long tenantId, Long orgId);

	Integer deleteSectorsByOrganization(Long tenantId, Long orgId);

}
