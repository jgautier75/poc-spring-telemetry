package com.acme.jga.ports.services.impl.system;

import com.acme.jga.domain.model.v1.SystemErrorFile;
import com.acme.jga.domain.services.system.api.ISystemService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.converters.system.SystemConverter;
import com.acme.jga.ports.dtos.dependencies.v1.DependencyListDto;
import com.acme.jga.ports.dtos.system.v1.SystemErrorFileDto;
import com.acme.jga.ports.dtos.system.v1.SystemErrorList;
import com.acme.jga.ports.dtos.system.v1.SystemSecretDto;
import com.acme.jga.ports.dtos.system.v1.SystemSecretListDto;
import com.acme.jga.ports.services.api.system.SystemPortService;
import com.acme.jga.ports.services.impl.AbstractPortService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.acme.jga.utils.lambdas.StreamUtil.ofNullableList;

@Service
public class SystemPortServiceImpl extends AbstractPortService implements SystemPortService {
    private static final String INSTRUMENTATION_NAME = SystemPortServiceImpl.class.getCanonicalName();
    private final ISystemService systemService;

    public SystemPortServiceImpl(OpenTelemetryWrapper openTelemetryWrapper, ISystemService systemService) {
        super(openTelemetryWrapper);
        this.systemService = systemService;
    }

    @Override
    public SystemErrorList listErrorFiles(String path) {
        List<SystemErrorFile> systemErrorFiles = processWithSpan(INSTRUMENTATION_NAME, "PORT_ERRORS_LIST", (span) -> systemService.listErrorFiles(path));
        SystemErrorList systemErrorList = new SystemErrorList();
        systemErrorList.setErrors(ofNullableList(systemErrorFiles).map(SystemConverter::convertSystemErrorFile).toList());
        return systemErrorList;
    }

    @Override
    public SystemErrorFileDto readErrorFile(String errorPath, String fileName) {
        SystemErrorFile systemErrorFile = systemService.readErrorFile(errorPath, fileName);
        return SystemConverter.convertSystemErrorFile(systemErrorFile);
    }

    @Override
    public Integer storeSecret(String path, String secret, SystemSecretDto systemSecretDto) {
        return systemService.storeSecret(path, secret, systemSecretDto.getKey(), systemSecretDto.getValue());
    }

    @Override
    public String readSecret(String path, String secret, String name) {
        return systemService.readSecret(path, secret, name);
    }

    @Override
    public SystemSecretListDto readAllSecrets(String path, String secret) {
        Map<String, Object> mapSecrets = systemService.readAllSecrets(path, secret);
        List<SystemSecretDto> systemSecretDtos = SystemConverter.convertSecretsMap(mapSecrets);
        return new SystemSecretListDto(systemSecretDtos);
    }

    @Override
    public DependencyListDto readDependenciesVersions() {
        Map<String, String> dependenciesMap = systemService.readDependenciesVersions();
        DependencyListDto dependencyListDto = new DependencyListDto();
        dependenciesMap.forEach(dependencyListDto::addDependency);
        return dependencyListDto;
    }

}
