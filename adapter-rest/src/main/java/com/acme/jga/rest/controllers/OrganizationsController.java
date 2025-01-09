package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.dtos.organizations.v1.OrganizationDto;
import com.acme.jga.ports.dtos.organizations.v1.OrganizationListLightDto;
import com.acme.jga.ports.dtos.search.v1.SearchFilterDto;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.services.api.organization.IOrganizationPortService;
import com.acme.jga.rest.versioning.WebApiVersions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrganizationsController extends AbstractController {
    private static final String INSTRUMENTATION_NAME = OrganizationsController.class.getCanonicalName();
    private IOrganizationPortService organizationPortService;

    public OrganizationsController(OpenTelemetryWrapper openTelemetryWrapper, IOrganizationPortService organizationPortService) {
        super(openTelemetryWrapper);
        this.organizationPortService = organizationPortService;
    }

    @PostMapping(value = WebApiVersions.OrganizationsResourceVersion.ROOT)
    public ResponseEntity<UidDto> createOrganization(@PathVariable("tenantUid") String tenantUid,
                                                     @RequestBody OrganizationDto organizationDto) throws FunctionalException {
        UidDto uidDto = withSpan(INSTRUMENTATION_NAME, "API_ORGS_CREATE", (span) -> organizationPortService.createOrganization(tenantUid, organizationDto, span));
        return new ResponseEntity<>(uidDto, HttpStatus.CREATED);
    }

    @GetMapping(value = WebApiVersions.OrganizationsResourceVersion.ROOT)
    public ResponseEntity<OrganizationListLightDto> findOrgsByTenant(@PathVariable("tenantUid") String tenantUid,
                                                                     @RequestParam(value = "filter", required = false) String searchFilter,
                                                                     @RequestParam(value = "index", required = false, defaultValue = "1") Integer pageIndex,
                                                                     @RequestParam(value = "size", required = false, defaultValue = "10") Integer pageSize,
                                                                     @RequestParam(value = "orderBy", required = false, defaultValue = "label") String orderBy)
            throws FunctionalException {
        SearchFilterDto searchFilterDto = new SearchFilterDto(searchFilter, pageSize, pageIndex, orderBy);
        OrganizationListLightDto lightList = withSpan(INSTRUMENTATION_NAME, "API_ORGS_LIST", (span) -> organizationPortService.filterOrganizations(tenantUid, searchFilterDto, span));
        return new ResponseEntity<>(lightList, HttpStatus.OK);
    }

    @GetMapping(value = WebApiVersions.OrganizationsResourceVersion.WITH_UID)
    public ResponseEntity<OrganizationDto> findOrgDetails(@PathVariable("tenantUid") String tenantUid,
                                                          @PathVariable("orgUid") String orgUid,
                                                          @RequestParam(name = "fetchSectors", defaultValue = "true") boolean fecthSectors)
            throws FunctionalException {
        OrganizationDto orgDto = withSpan(INSTRUMENTATION_NAME, "API_ORGS_FIND", (span) -> organizationPortService.findOrganizationByUid(tenantUid, orgUid, fecthSectors, span));
        return new ResponseEntity<>(orgDto, HttpStatus.OK);
    }

    @PostMapping(value = WebApiVersions.OrganizationsResourceVersion.WITH_UID)
    public ResponseEntity<Void> updateOrganization(@PathVariable("tenantUid") String tenantUid,
                                                   @PathVariable("orgUid") String orgUid,
                                                   @RequestBody OrganizationDto organizationDto)
            throws FunctionalException {
        withSpan(INSTRUMENTATION_NAME, "API_ORGS_UPDATE", (span) -> organizationPortService.updateOrganization(tenantUid, orgUid, organizationDto, span));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = WebApiVersions.OrganizationsResourceVersion.WITH_UID)
    public ResponseEntity<Void> deleteOrganization(@PathVariable("tenantUid") String tenantUid,
                                                   @PathVariable("orgUid") String orgUid)
            throws FunctionalException {
        withSpan(INSTRUMENTATION_NAME, "API_ORGS_DELETE", (span) -> organizationPortService.deleteOrganization(tenantUid, orgUid, span));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
