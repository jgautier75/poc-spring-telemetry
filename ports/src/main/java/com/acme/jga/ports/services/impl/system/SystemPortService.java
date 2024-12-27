package com.acme.jga.ports.services.impl.system;

import com.acme.jga.domain.model.v1.SystemErrorFile;
import com.acme.jga.domain.services.system.api.ISystemService;
import com.acme.jga.ports.converters.system.SystemConverter;
import com.acme.jga.ports.dtos.dependencies.v1.DependencyListDto;
import com.acme.jga.ports.dtos.system.v1.SystemErrorFileDto;
import com.acme.jga.ports.dtos.system.v1.SystemErrorList;
import com.acme.jga.ports.dtos.system.v1.SystemSecretDto;
import com.acme.jga.ports.dtos.system.v1.SystemSecretListDto;
import com.acme.jga.ports.services.api.system.ISystemPortService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.acme.jga.utils.lambdas.StreamUtil.ofNullableList;

@Service
@RequiredArgsConstructor
public class SystemPortService implements ISystemPortService {
    private final ISystemService systemService;

    @Override
    public SystemErrorList listErrorFiles(String path) {
        List<SystemErrorFile> systemErrorFiles = systemService.listErrorFiles(path);
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
    public Integer storeSecret(String moduleName, SystemSecretDto systemSecretDto) {
        return systemService.storeSecret(moduleName, systemSecretDto.getKey(), systemSecretDto.getValue());
    }

    @Override
    public String readSecret(String moduleName, String name) {
        return systemService.readSecret(moduleName, name);
    }

    @Override
    public SystemSecretListDto readAllSecrets(String moduleName) {
        Map<String, Object> mapSecrets = systemService.readAllSecrets(moduleName);
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
