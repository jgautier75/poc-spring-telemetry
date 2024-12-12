package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.ports.port.organizations.v1.OrganizationDto;
import com.acme.jga.ports.port.organizations.v1.OrganizationListLightDto;
import com.acme.jga.ports.port.search.v1.SearchFilterDto;
import com.acme.jga.ports.port.shared.UidDto;
import com.acme.jga.ports.services.api.organization.IOrganizationPortService;
import com.acme.jga.rest.versioning.WebApiVersions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrganizationsController {
    private static final String INSTRUMENTATION_NAME = OrganizationsController.class.getCanonicalName();
    private final IOrganizationPortService organizationPortService;

    @PostMapping(value = WebApiVersions.OrganizationsResourceVersion.ROOT)
    public ResponseEntity<UidDto> createOrganization(@PathVariable("tenantUid") String tenantUid,
                                                     @RequestBody OrganizationDto organizationDto) throws FunctionalException {
        UidDto uidDto = organizationPortService.createOrganization(tenantUid, organizationDto);
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
        OrganizationListLightDto lightList = organizationPortService.filterOrganizations(tenantUid, searchFilterDto);
        return new ResponseEntity<>(lightList, HttpStatus.OK);
    }

    @GetMapping(value = WebApiVersions.OrganizationsResourceVersion.WITH_UID)
    public ResponseEntity<OrganizationDto> findOrgDetails(@PathVariable("tenantUid") String tenantUid,
                                                          @PathVariable("orgUid") String orgUid,
                                                          @RequestParam(name = "fetchSectors", defaultValue = "true") boolean fecthSectors)
            throws FunctionalException {
        OrganizationDto orgDto = organizationPortService.findOrganizationByUid(tenantUid, orgUid, fecthSectors);
        return new ResponseEntity<>(orgDto, HttpStatus.OK);
    }

    @PostMapping(value = WebApiVersions.OrganizationsResourceVersion.WITH_UID)
    public ResponseEntity<Void> updateOrganization(@PathVariable("tenantUid") String tenantUid,
                                                   @PathVariable("orgUid") String orgUid, @RequestBody OrganizationDto organizationDto)
            throws FunctionalException {
        organizationPortService.updateOrganization(tenantUid, orgUid, organizationDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = WebApiVersions.OrganizationsResourceVersion.WITH_UID)
    public ResponseEntity<Void> deleteOrganization(@PathVariable("tenantUid") String tenantUid,
                                                   @PathVariable("orgUid") String orgUid)
            throws FunctionalException {
        organizationPortService.deleteOrganization(tenantUid, orgUid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
