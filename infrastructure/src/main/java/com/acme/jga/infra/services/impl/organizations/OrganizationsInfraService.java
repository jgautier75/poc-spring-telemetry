package com.acme.jga.infra.services.impl.organizations;

import com.acme.jga.domain.model.filtering.FilteringConstants;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.infra.converters.OrganizationsInfraConverter;
import com.acme.jga.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.jga.infra.dto.organizations.v1.OrganizationDb;
import com.acme.jga.infra.services.api.organizations.IOrganizationsInfraService;
import com.acme.jga.infra.services.impl.AbstractInfraService;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrganizationsInfraService extends AbstractInfraService implements IOrganizationsInfraService {
    private static final String INSTRUMENTATION_NAME = OrganizationsInfraService.class.getCanonicalName();
    private final OrganizationsInfraConverter organizationsInfraConverter;
    private final IOrganizationsDao organizationsDao;

    @Autowired
    public OrganizationsInfraService(OrganizationsInfraConverter organizationsInfraConverter, IOrganizationsDao organizationsDao, OpenTelemetryWrapper openTelemetryWrapper) {
        super(openTelemetryWrapper);
        this.organizationsInfraConverter = organizationsInfraConverter;
        this.organizationsDao = organizationsDao;
    }

    @Transactional
    @Override
    public CompositeId createOrganization(Organization organization, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_ORG_CREATE", parentSpan, (span) -> {
            OrganizationDb orgDb = organizationsInfraConverter.convertOrganizationToOrganizationDb(organization);
            return organizationsDao.createOrganization(orgDb);
        });
    }

    @Override
    public PaginatedResults<Organization> filterOrganizations(Long tenantId, Span parentSpan, Map<String, Object> searchParams) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_ORG_FIND_ALL", parentSpan, (span) -> {
            PaginatedResults<OrganizationDb> paginatedResults = organizationsDao.filterOrganizations(tenantId, searchParams);
            List<Organization> orgs = paginatedResults.getResults().stream()
                    .map(organizationsInfraConverter::convertOrganizationDbToOrganization)
                    .toList();
            return new PaginatedResults<Organization>(
                    paginatedResults.getNbResults(),
                    paginatedResults.getNbPages(),
                    orgs,
                    (Integer) searchParams.get(FilteringConstants.PAGE_SIZE),
                    (Integer) searchParams.get(FilteringConstants.PAGE_INDEX)
            );
        });
    }

    @Override
    public Optional<Organization> findOrganizationByUid(Long tenantId, String uid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_ORG_FIND_BY_UID", parentSpan, (span) -> {
            OrganizationDb orgDb = organizationsDao.findOrganizationByTenantAndUid(tenantId, uid);
            return Optional.ofNullable(organizationsInfraConverter.convertOrganizationDbToOrganization(orgDb));
        });
    }

    @Transactional
    @Override
    public Integer updateOrganization(Long tenantId, Long orgId, String code, String label, String country, OrganizationStatus status) {
        return organizationsDao.updateOrganization(tenantId, orgId, code, label, country, status);
    }

    @Override
    public Optional<Long> codeAlreadyUsed(String code, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_ORG_CODE_ALREADY_USED", parentSpan, (span) -> organizationsDao.existsByCode(code));
    }

    @Override
    public List<Organization> findOrgsByIdList(List<Long> orgIds) {
        List<OrganizationDb> orgDbs = organizationsDao.findOrgsByIdList(orgIds);
        return orgDbs.stream().map(organizationsInfraConverter::convertOrganizationDbToOrganization).toList();
    }

    @Override
    public Integer deleteById(Long tenantId, Long orgId) {
        return organizationsDao.deleteById(tenantId, orgId);
    }

    @Override
    public Integer deleteUsersByOrganization(Long tenantId, Long orgId) {
        return organizationsDao.deleteUsersByOrganization(tenantId, orgId);
    }

    @Override
    public Integer deleteSectors(Long tenantId, Long orgId) {
        return organizationsDao.deleteSectorsByOrganization(tenantId, orgId);
    }

}
