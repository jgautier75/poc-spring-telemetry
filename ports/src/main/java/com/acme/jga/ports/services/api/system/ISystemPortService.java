package com.acme.jga.ports.services.api.system;

import com.acme.jga.ports.port.dependencies.v1.DependencyListDto;
import com.acme.jga.ports.port.system.v1.SystemErrorFileDto;
import com.acme.jga.ports.port.system.v1.SystemErrorList;
import com.acme.jga.ports.port.system.v1.SystemSecretDto;
import com.acme.jga.ports.port.system.v1.SystemSecretListDto;

public interface ISystemPortService {
    SystemErrorList listErrorFiles(String errorPath);

    SystemErrorFileDto readErrorFile(String errorPath, String fileName);

    Integer storeSecret(String moduleName, SystemSecretDto systemSecretDto);

    String readSecret(String moduleName, String name);

    SystemSecretListDto readAllSecrets(String moduleName);

    DependencyListDto readDependenciesVersions();
}
