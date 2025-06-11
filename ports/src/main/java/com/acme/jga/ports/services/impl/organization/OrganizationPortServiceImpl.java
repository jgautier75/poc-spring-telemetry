package com.acme.jga.ports.services.impl.organization;

import com.acme.jga.domain.functions.organizations.api.*;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.filtering.FilteringConstants;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.converters.organization.OrganizationsPortConverter;
import com.acme.jga.ports.dtos.organizations.v1.OrganizationDto;
import com.acme.jga.ports.dtos.organizations.v1.OrganizationLightDto;
import com.acme.jga.ports.dtos.organizations.v1.OrganizationListLightDto;
import com.acme.jga.ports.dtos.search.v1.SearchFilterDto;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.services.api.organization.OrganizationPortService;
import com.acme.jga.ports.services.impl.AbstractPortService;
import com.acme.jga.ports.validation.organizations.OrganizationsValidationEngine;
import com.acme.jga.search.filtering.parser.QueryParser;
import com.acme.jga.search.filtering.utils.ParsingResult;
import com.acme.jga.validation.ValidationException;
import com.acme.jga.validation.ValidationResult;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrganizationPortServiceImpl extends AbstractPortService implements OrganizationPortService {
    private static final String INSTRUMENTATION_NAME = OrganizationPortServiceImpl.class.getCanonicalName();
    private final TenantFind tenantFind;
    private final OrganizationCreate organizationCreate;
    private final OrganizationsPortConverter organizationsConverter;
    private final OrganizationsValidationEngine organizationsValidationEngine;
    private final QueryParser queryParser = new QueryParser();
    private final OrganizationFilter organizationFilter;
    private final OrganizationFind organizationFind;
    private final OrganizationUpdate organizationUpdate;
    private final OrganizationDelete organizationDelete;

    @Autowired
    public OrganizationPortServiceImpl(TenantFind tenantFind, OrganizationCreate organizationCreate, OrganizationsPortConverter organizationsConverter,
                                       OrganizationsValidationEngine organizationsValidationEngine, OpenTelemetryWrapper openTelemetryWrapper,
                                       OrganizationFilter organizationFilter, OrganizationFind organizationFind,
                                       OrganizationUpdate organizationUpdate, OrganizationDelete organizationDelete) {
        super(openTelemetryWrapper);
        this.organizationCreate = organizationCreate;
        this.tenantFind = tenantFind;
        this.organizationsConverter = organizationsConverter;
        this.organizationsValidationEngine = organizationsValidationEngine;
        this.organizationFilter = organizationFilter;
        this.organizationFind = organizationFind;
        this.organizationUpdate = organizationUpdate;
        this.organizationDelete = organizationDelete;
    }

    /**
     * @inheritDoc
     */
    @Override
    public UidDto createOrganization(String tenantUid, OrganizationDto organizationDto, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_ORGS_CREATE", parentSpan, (orgSpan) -> {
            ValidationResult validationResult = organizationsValidationEngine.validate(organizationDto);
            if (!validationResult.isSuccess()) {
                throw new ValidationException(validationResult.getErrors());
            }
            Organization org = organizationsConverter.convertOrganizationDtoToDomain(organizationDto);
            CompositeId compositeId = organizationCreate.execute(tenantUid, org, orgSpan);
            return new UidDto(compositeId.getUid());
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public OrganizationListLightDto filterOrganizations(String tenantUid, SearchFilterDto searchFilterDto, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_ORGS_FIND_LIGHT", parentSpan, (span -> {
            Tenant tenant = tenantFind.byUid(tenantUid, span);
            Map<String, Object> searchParams = extractSearchParams(searchFilterDto);
            PaginatedResults<Organization> paginatedResults = organizationFilter.execute(tenant.getId(), span, searchParams);
            List<OrganizationLightDto> lightOrgs = new ArrayList<>();
            paginatedResults.getResults().forEach(org -> lightOrgs.add(organizationsConverter.convertOrganizationToLightOrgDto(org)));
            return new OrganizationListLightDto(paginatedResults.getNbResults(), paginatedResults.getNbPages(), paginatedResults.getPageIndex(), paginatedResults.getPageSize(), lightOrgs);
        }));
    }

    /**
     * @inheritDoc
     */
    @Override
    public OrganizationDto findOrganizationByUid(String tenantUid, String orgUid, boolean fetchSectors, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_ORGS_FIND_UID", parentSpan, (span) -> {
            Tenant tenant = tenantFind.byUid(tenantUid, span);
            Organization org = organizationFind.byTenantIdAndUid(tenant.getId(), orgUid, fetchSectors, span);
            OrganizationDto organizationDto = organizationsConverter.convertOrganizationToDto(org);
            organizationDto.setTenantUid(tenantUid);
            return organizationDto;
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public Integer updateOrganization(String tenantUid, String orgUid, OrganizationDto organizationDto, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_ORGS_UPDATE", parentSpan, (span) -> {
            tenantFind.byUid(tenantUid, span);
            Organization org = organizationsConverter.convertOrganizationDtoToDomain(organizationDto);
            return organizationUpdate.execute(tenantUid, orgUid, org, span);
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public Integer deleteOrganization(String tenantUid, String orgUid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_ORGS_DELETE", parentSpan, (span) -> organizationDelete.execute(tenantUid, orgUid, span));
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
