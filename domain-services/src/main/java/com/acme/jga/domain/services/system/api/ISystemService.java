package com.acme.jga.domain.services.system.api;

import com.acme.jga.domain.model.v1.SystemErrorFile;

import java.util.List;
import java.util.Map;

public interface ISystemService {
    List<SystemErrorFile> listErrorFiles(String path);

    SystemErrorFile readErrorFile(String errorPath, String fullFileName);

    Integer storeSecret(String moduleName,String name, String value);

    String readSecret(String moduleName, String name);

    Map<String,Object> readAllSecrets(String moduleName);

    Map<String,String> readDependenciesVersions();
}
