package com.acme.jga.ports.services.impl.organization;

import com.acme.jga.domain.model.filtering.FilteringConstants;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.services.organizations.api.IOrganizationsDomainService;
import com.acme.jga.domain.services.tenants.api.ITenantDomainService;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.converters.organization.OrganizationsPortConverter;
import com.acme.jga.ports.port.organizations.v1.OrganizationDto;
import com.acme.jga.ports.port.organizations.v1.OrganizationLightDto;
import com.acme.jga.ports.port.organizations.v1.OrganizationListLightDto;
import com.acme.jga.ports.port.search.v1.SearchFilterDto;
import com.acme.jga.ports.port.shared.UidDto;
import com.acme.jga.ports.services.api.organization.IOrganizationPortService;
import com.acme.jga.ports.services.impl.AbstractPortService;
import com.acme.jga.ports.validation.organizations.OrganizationsValidationEngine;
import com.acme.jga.search.filtering.parser.QueryParser;
import com.acme.jga.search.filtering.utils.ParsingResult;
import com.acme.jga.validation.ValidationException;
import com.acme.jga.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrganizationPortService extends AbstractPortService implements IOrganizationPortService {
    private static final String INSTRUMENTATION_NAME = OrganizationPortService.class.getCanonicalName();
    private ITenantDomainService tenantDomainService;
    private IOrganizationsDomainService organizationDomainService;
    private OrganizationsPortConverter organizationsConverter;
    private OrganizationsValidationEngine organizationsValidationEngine;
    private final QueryParser queryParser = new QueryParser();

    @Autowired
    public OrganizationPortService(ITenantDomainService tenantDomainService, IOrganizationsDomainService organizationDomainService, OrganizationsPortConverter organizationsConverter,
                                   OrganizationsValidationEngine organizationsValidationEngine, OpenTelemetryWrapper openTelemetryWrapper) {
        super(openTelemetryWrapper);
        this.organizationDomainService = organizationDomainService;
        this.tenantDomainService = tenantDomainService;
        this.organizationsConverter = organizationsConverter;
        this.organizationsValidationEngine = organizationsValidationEngine;
    }

    /**
     * @inheritDoc
     */
    @Override
    public UidDto createOrganization(String tenantUid, OrganizationDto organizationDto) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_ORGS_CREATE", null, (orgSpan) -> {
            ValidationResult validationResult = organizationsValidationEngine.validate(organizationDto);
            if (!validationResult.isSuccess()) {
                throw new ValidationException(validationResult.getErrors());
            }
            Organization org = organizationsConverter.convertOrganizationDtoToDomain(organizationDto);
            CompositeId compositeId = organizationDomainService.createOrganization(tenantUid, org, orgSpan);
            return new UidDto(compositeId.getUid());
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public OrganizationListLightDto filterOrganizations(String tenantUid, SearchFilterDto searchFilterDto) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_ORGS_FIND_LIGHT", null, (span -> {
            Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
            Map<String, Object> searchParams = extractSearchParams(searchFilterDto);
            PaginatedResults<Organization> paginatedResults = organizationDomainService.filterOrganizations(tenant.getId(), span, searchParams);
            List<OrganizationLightDto> lightOrgs = new ArrayList<>();
            paginatedResults.getResults().forEach(org -> lightOrgs.add(organizationsConverter.convertOrganizationToLightOrgDto(org)));
            return new OrganizationListLightDto(paginatedResults.getNbResults(), paginatedResults.getNbPages(), paginatedResults.getPageIndex(), paginatedResults.getPageSize(), lightOrgs);
        }));
    }

    /**
     * @inheritDoc
     */
    @Override
    public OrganizationDto findOrganizationByUid(String tenantUid, String orgUid, boolean fetchSectors) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_ORGS_FIND_UID", null, (span) -> {
            Tenant tenant = tenantDomainService.findTenantByUid(tenantUid, span);
            Organization org = organizationDomainService.findOrganizationByTenantAndUid(tenant.getId(), orgUid, fetchSectors, span);
            OrganizationDto organizationDto = organizationsConverter.convertOrganizationToDto(org);
            organizationDto.setTenantUid(tenantUid);
            return organizationDto;
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public Integer updateOrganization(String tenantUid, String orgUid, OrganizationDto organizationDto) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_ORGS_UPDATE", null, (span) -> {
            tenantDomainService.findTenantByUid(tenantUid, span);
            Organization org = organizationsConverter.convertOrganizationDtoToDomain(organizationDto);
            return organizationDomainService.updateOrganization(tenantUid, orgUid, org, span);
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public Integer deleteOrganization(String tenantUid, String orgUid) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_ORGS_DELETE", null, (span) -> organizationDomainService.deleteOrganization(tenantUid, orgUid, span));
    }

    private Map<String, Object> extractSearchParams(SearchFilterDto searchFilterDto) {
        ParsingResult parsingResult = queryParser.parseQuery(searchFilterDto.getFilter());
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put(FilteringConstants.PAGE_INDEX, searchFilterDto.getPageIndex());
        searchParams.put(FilteringConstants.PAGE_SIZE, searchFilterDto.getPageSize());
        searchParams.put(FilteringConstants.PARSING_RESULTS, parsingResult);
        searchParams.put(FilteringConstants.ORDER_BY, searchFilterDto.getOrderBy());
        return searchParams;
    }

}
