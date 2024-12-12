package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.versions.ApiVersion;
import com.acme.jga.domain.model.versions.VersionsList;
import com.acme.jga.infra.config.KafkaProducerConfig;
import com.acme.jga.ports.port.dependencies.v1.DependencyListDto;
import com.acme.jga.ports.port.system.v1.*;
import com.acme.jga.ports.services.api.system.ISystemPortService;
import com.acme.jga.rest.config.AppGenericConfig;
import com.acme.jga.rest.config.MicrometerPrometheus;
import com.acme.jga.rest.config.VaultSecrets;
import com.acme.jga.rest.versioning.WebApiVersions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
public class SystemController {
    private final PublishSubscribeChannel eventAuditChannel;
    private final MicrometerPrometheus micrometerPrometheus;
    private final ISystemPortService systemPortService;
    private final AppGenericConfig appGenericConfig;
    private final VaultSecrets vaultSecrets;

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
    public ResponseEntity<SystemErrorList> listSystemErrors() {
        SystemErrorList systemErrorList = systemPortService.listErrorFiles(appGenericConfig.getErrorPath());
        return ResponseEntity.ok(systemErrorList);
    }

    @GetMapping(value = WebApiVersions.SystemResourceVersion.ERRORS_READ)
    public ResponseEntity<SystemErrorFileDto> getSystemError(@PathVariable("fileName") String fileName) {
        SystemErrorFileDto systemErrorFileDto = systemPortService.readErrorFile(appGenericConfig.getErrorPath(), fileName);
        return ResponseEntity.ok(systemErrorFileDto);
    }

    @PostMapping(value = WebApiVersions.SystemResourceVersion.VAULT_STORE)
    public ResponseEntity<Void> storeSecret(@RequestBody SystemSecretDto systemSecretDto) {
        systemPortService.storeSecret(appGenericConfig.getModuleName(), systemSecretDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = WebApiVersions.SystemResourceVersion.VAULT_READ)
    public ResponseEntity<SystemSecretValueDto> readSecret(@RequestParam(value = "secretName") String secretName) {
        String secretValue = systemPortService.readSecret(appGenericConfig.getModuleName(), secretName);
        return ResponseEntity.ok(SystemSecretValueDto.builder().value(secretValue).build());
    }

    @GetMapping(value = WebApiVersions.SystemResourceVersion.VAULT_LIST)
    public ResponseEntity<SystemSecretListDto> listSecrets() {
        SystemSecretListDto systemSecretListDto = systemPortService.readAllSecrets(appGenericConfig.getModuleName());
        return ResponseEntity.ok().body(systemSecretListDto);
    }

    @GetMapping(value = WebApiVersions.SystemResourceVersion.DEPS_LIST)
    public ResponseEntity<DependencyListDto> listDependencies() {
        DependencyListDto dependencyListDto = systemPortService.readDependenciesVersions();
        return ResponseEntity.ok().body(dependencyListDto);
    }
}
