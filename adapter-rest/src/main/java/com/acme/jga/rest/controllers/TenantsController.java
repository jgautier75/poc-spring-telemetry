package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantDisplayDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantListDisplayDto;
import com.acme.jga.ports.services.api.tenant.ITenantPortService;
import com.acme.jga.rest.annotations.MetricPoint;
import com.acme.jga.rest.versioning.WebApiVersions;
import io.opentelemetry.api.logs.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TenantsController {
    private final ITenantPortService tenantPortService;
    private final OpenTelemetryWrapper openTelemetryWrapper;

    @PostMapping(value = WebApiVersions.TenantsResourceVersion.ROOT, consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    @MetricPoint(alias = "TENANT_CREATE", method = "POST", version = WebApiVersions.V1, regex = "^/(.*)/api/v1/tenants")
    public ResponseEntity<UidDto> createTenant(@RequestBody TenantDto tenantDto) throws FunctionalException {
        UidDto uid = tenantPortService.createTenant(tenantDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(uid);
    }

    @GetMapping(value = WebApiVersions.TenantsResourceVersion.WITH_UID)
    @MetricPoint(alias = "TENANT_FIND_UID", method = "GET", version = WebApiVersions.V1, regex = "^/(.*)/api/v1/tenants/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    public ResponseEntity<TenantDisplayDto> findTenantByUid(@PathVariable(name = "uid", required = true) String uid)
            throws FunctionalException {
        TenantDisplayDto tenantDisplayDto = tenantPortService.findTenantByUid(uid);
        openTelemetryWrapper.log(Severity.INFO, "Find tenant with uid [" + uid + "]");
        openTelemetryWrapper.incrementCounter();
        return new ResponseEntity<>(tenantDisplayDto, HttpStatus.OK);
    }

    @GetMapping(value = WebApiVersions.TenantsResourceVersion.ROOT)
    public ResponseEntity<TenantListDisplayDto> listTenants() throws FunctionalException {
        TenantListDisplayDto tenantListDisplayDto = tenantPortService.findAllTenants();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(tenantListDisplayDto);
    }

    @PostMapping(value = WebApiVersions.TenantsResourceVersion.WITH_UID)
    public ResponseEntity<Void> updateTenantByUid(@PathVariable(name = "uid", required = true) String uid,
                                                  @RequestBody TenantDto tenantDto)
            throws FunctionalException {
        tenantPortService.updateTenant(uid, tenantDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = WebApiVersions.TenantsResourceVersion.WITH_UID)
    public ResponseEntity<Void> deleteTenantByUid(@PathVariable(name = "uid", required = true) String uid)
            throws FunctionalException {
        tenantPortService.deleteTenant(uid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
