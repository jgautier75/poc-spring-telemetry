package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantDisplayDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantListDisplayDto;
import com.acme.jga.ports.services.api.tenant.ITenantPortService;
import com.acme.jga.rest.annotations.MetricPoint;
import com.acme.jga.rest.versioning.WebApiVersions;
import com.acme.jga.utils.otel.OtelContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TenantsController extends AbstractController {
    private static final String INSTRUMENTATION_NAME = TenantsController.class.getCanonicalName();
    private ITenantPortService tenantPortService;
    private ILoggingFacade loggingFacade;

    public TenantsController(ITenantPortService tenantPortService, OpenTelemetryWrapper openTelemetryWrapper, ILoggingFacade loggingFacade) {
        super(openTelemetryWrapper);
        this.tenantPortService = tenantPortService;
        this.loggingFacade = loggingFacade;
    }

    @PostMapping(value = WebApiVersions.TenantsResourceVersion.ROOT, consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    @MetricPoint(alias = "API_TENANTS_CREATE", method = "POST", version = WebApiVersions.V1, regex = "^/(.*)/api/v1/tenants")
    public ResponseEntity<UidDto> createTenant(@RequestBody TenantDto tenantDto) throws FunctionalException {
        UidDto uid = withSpan(INSTRUMENTATION_NAME, "TENANTS_CREATE", (span) -> tenantPortService.createTenant(tenantDto, span));
        return ResponseEntity.status(HttpStatus.CREATED).body(uid);
    }

    @GetMapping(value = WebApiVersions.TenantsResourceVersion.WITH_UID)
    @MetricPoint(alias = "TENANT_FIND_UID", method = "GET", version = WebApiVersions.V1, regex = "^/(.*)/api/v1/tenants/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    public ResponseEntity<TenantDisplayDto> findTenantByUid(@PathVariable(name = "uid", required = true) String uid)
            throws FunctionalException {
        TenantDisplayDto tenantDisplayDto = withSpan(INSTRUMENTATION_NAME, "API_TENANTS_FIND", (span) -> {
            loggingFacade.infoS(INSTRUMENTATION_NAME, "Find tenant with uid [%s]", new Object[]{uid}, OtelContext.fromSpan(span));
            return tenantPortService.findTenantByUid(uid, span);
        });
        return new ResponseEntity<>(tenantDisplayDto, HttpStatus.OK);
    }

    @GetMapping(value = WebApiVersions.TenantsResourceVersion.ROOT)
    public ResponseEntity<TenantListDisplayDto> listTenants() throws FunctionalException {
        TenantListDisplayDto tenantListDisplayDto = withSpan(INSTRUMENTATION_NAME, "API_TENANTS_LIST", (span) -> tenantPortService.findAllTenants(span));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(tenantListDisplayDto);
    }

    @PostMapping(value = WebApiVersions.TenantsResourceVersion.WITH_UID)
    public ResponseEntity<Void> updateTenantByUid(@PathVariable(name = "uid", required = true) String uid,
                                                  @RequestBody TenantDto tenantDto)
            throws FunctionalException {
        withSpan(INSTRUMENTATION_NAME, "API_TENANTS_UPDATE", (span) -> tenantPortService.updateTenant(uid, tenantDto, span));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = WebApiVersions.TenantsResourceVersion.WITH_UID)
    public ResponseEntity<Void> deleteTenantByUid(@PathVariable(name = "uid", required = true) String uid)
            throws FunctionalException {
        withSpan(INSTRUMENTATION_NAME, "API_TENANTS_DELETE", (span) -> tenantPortService.deleteTenant(uid, span));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
