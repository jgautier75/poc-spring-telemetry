package com.acme.jga.ports.services.api.system;

import com.acme.jga.ports.dtos.dependencies.v1.DependencyListDto;
import com.acme.jga.ports.dtos.system.v1.SystemErrorFileDto;
import com.acme.jga.ports.dtos.system.v1.SystemErrorList;
import com.acme.jga.ports.dtos.system.v1.SystemSecretDto;
import com.acme.jga.ports.dtos.system.v1.SystemSecretListDto;

public interface SystemPortService {
    SystemErrorList listErrorFiles(String errorPath);

    SystemErrorFileDto readErrorFile(String errorPath, String fileName);

    Integer storeSecret(String path, String secret, SystemSecretDto systemSecretDto);

    String readSecret(String path, String secret, String name);

    SystemSecretListDto readAllSecrets(String path, String secret);

    DependencyListDto readDependenciesVersions();
}
