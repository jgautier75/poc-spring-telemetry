package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.versions.ApiVersion;
import com.acme.jga.domain.model.versions.VersionsList;
import com.acme.jga.infra.config.KafkaProducerConfig;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.dtos.dependencies.v1.DependencyListDto;
import com.acme.jga.ports.dtos.system.v1.*;
import com.acme.jga.ports.services.api.system.ISystemPortService;
import com.acme.jga.rest.config.AppGenericConfig;
import com.acme.jga.rest.config.MicrometerPrometheus;
import com.acme.jga.rest.versioning.WebApiVersions;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
public class SystemController extends AbstractController {
    private static final String INSTRUMENTATION_NAME = SystemController.class.getCanonicalName();
    private final PublishSubscribeChannel eventAuditChannel;
    private final MicrometerPrometheus micrometerPrometheus;
    private final ISystemPortService systemPortService;
    private final AppGenericConfig appGenericConfig;

    public SystemController(OpenTelemetryWrapper openTelemetryWrapper, PublishSubscribeChannel eventAuditChannel, MicrometerPrometheus micrometerPrometheus, ISystemPortService systemPortService, AppGenericConfig appGenericConfig) {
        super(openTelemetryWrapper);
        this.eventAuditChannel = eventAuditChannel;
        this.micrometerPrometheus = micrometerPrometheus;
        this.systemPortService = systemPortService;
        this.appGenericConfig = appGenericConfig;
    }

    @PostMapping(value = WebApiVersions.SystemResourceVersion.KAFKA_WAKEUP)
    public ResponseEntity<Void> kafkaWakeUp() {
        eventAuditChannel.send(MessageBuilder.withPayload(KafkaProducerConfig.AUDIT_WAKE_UP).build());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = WebApiVersions.SystemResourceVersion.VERSIONS)
    public ResponseEntity<VersionsList> versions() {
        VersionsList versionsList = new VersionsList();
        Arrays.stream(WebApiVersions.TenantsResourceVersion.Endpoints.values()).forEach(endp -> versionsList.addApiVersion(new ApiVersion(endp.getVersion(), endp.getCategory(), endp.getCode(), endp.getUri())));
        Arrays.stream(WebApiVersions.SectorsResourceVersion.Endpoints.values()).forEach(endp -> versionsList.addApiVersion(new ApiVersion(endp.getVersion(), endp.getCategory(), endp.getCode(), endp.getUri())));
        Arrays.stream(WebApiVersions.UsersResourceVersion.Endpoints.values()).forEach(endp -> versionsList.addApiVersion(new ApiVersion(endp.getVersion(), endp.getCategory(), endp.getCode(), endp.getUri())));
        Arrays.stream(WebApiVersions.OrganizationsResourceVersion.Endpoints.values()).forEach(endp -> versionsList.addApiVersion(new ApiVersion(endp.getVersion(), endp.getCategory(), endp.getCode(), endp.getUri())));
        Arrays.stream(WebApiVersions.SystemResourceVersion.Endpoints.values()).forEach(endp -> versionsList.addApiVersion(new ApiVersion(endp.getVersion(), endp.getCategory(), endp.getCode(), endp.getUri())));
        return ResponseEntity.ok(versionsList);
    }

    @DeleteMapping(value = WebApiVersions.SystemResourceVersion.TECH_GAUGE_RESET)
    public ResponseEntity<Void> resetTechGaugeErrors() {
        micrometerPrometheus.getTechErrorsCounter().set(0);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = WebApiVersions.SystemResourceVersion.ERRORS_LIST)
    public ResponseEntity<SystemErrorList> listSystemErrors() throws FunctionalException {
        SystemErrorList systemErrorList = withSpan(INSTRUMENTATION_NAME, "API_ERRORS_LIST", (span) -> systemPortService.listErrorFiles(appGenericConfig.getErrorPath(), span));
        return ResponseEntity.ok(systemErrorList);
    }

    @GetMapping(value = WebApiVersions.SystemResourceVersion.ERRORS_READ)
    public ResponseEntity<SystemErrorFileDto> getSystemError(@PathVariable("fileName") String fileName) {
        SystemErrorFileDto systemErrorFileDto = systemPortService.readErrorFile(appGenericConfig.getErrorPath(), fileName);
        return ResponseEntity.ok(systemErrorFileDto);
    }

    @PostMapping(value = WebApiVersions.SystemResourceVersion.VAULT_STORE)
    public ResponseEntity<Void> storeSecret(@RequestBody SystemSecretDto systemSecretDto) {
        systemPortService.storeSecret(appGenericConfig.getVaultPath(), appGenericConfig.getVaultSecret(), systemSecretDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = WebApiVersions.SystemResourceVersion.VAULT_READ)
    public ResponseEntity<SystemSecretValueDto> readSecret(@RequestParam(value = "secretName") String secretName) {
        String secretValue = systemPortService.readSecret(appGenericConfig.getVaultPath(), appGenericConfig.getVaultSecret(), secretName);
        return ResponseEntity.ok(SystemSecretValueDto.builder().value(secretValue).build());
    }

    @GetMapping(value = WebApiVersions.SystemResourceVersion.VAULT_LIST)
    public ResponseEntity<SystemSecretListDto> listSecrets() {
        SystemSecretListDto systemSecretListDto = systemPortService.readAllSecrets(appGenericConfig.getVaultPath(), appGenericConfig.getVaultSecret());
        return ResponseEntity.ok().body(systemSecretListDto);
    }

    @GetMapping(value = WebApiVersions.SystemResourceVersion.DEPS_LIST)
    public ResponseEntity<DependencyListDto> listDependencies() {
        DependencyListDto dependencyListDto = systemPortService.readDependenciesVersions();
        return ResponseEntity.ok().body(dependencyListDto);
    }
}
