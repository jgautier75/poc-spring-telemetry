package com.acme.jga.domain.services.system.impl;

import com.acme.jga.domain.model.exceptions.TechnicalException;
import com.acme.jga.domain.model.v1.SystemErrorFile;
import com.acme.jga.domain.services.system.api.ISystemService;
import com.acme.jga.logging.utils.LogHttpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SystemService implements ISystemService {
    private final VaultTemplate vaultTemplate;

    @Override
    public List<SystemErrorFile> listErrorFiles(String path) {
        return LogHttpUtils.listErrorFiles(path);
    }

    @Override
    public SystemErrorFile readErrorFile(String errorPath, String fullFileName) {
        String errorFileContent = LogHttpUtils.getError(errorPath, fullFileName);
        SystemErrorFile systemErrorFile = LogHttpUtils.convertFileToSystemError(fullFileName);
        systemErrorFile.setContent(errorFileContent);
        return systemErrorFile;
    }

    @Override
    public Integer storeSecret(String path, String secret, String name, String value) {
        VaultResponse response = vaultTemplate.opsForKeyValue(path, VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get(secret);
        Map<String, Object> mapSecrets = new HashMap<>();
        if (response != null) {
            mapSecrets = Optional.ofNullable(response.getData()).orElse(new HashMap<>());
        }
        mapSecrets.put(name, value);
        VaultKeyValueOperations vaultKeyValueOperations = vaultTemplate.opsForKeyValue(path, VaultKeyValueOperationsSupport.KeyValueBackend.KV_2);
        vaultKeyValueOperations.put(secret, mapSecrets);
        return 1;
    }

    @Override
    public String readSecret(String path, String secret, String name) {
        VaultResponse response = vaultTemplate.opsForKeyValue(path, VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get(secret);
        return (String) Optional.ofNullable(response.getData()).map(m -> m.get(name)).orElse(null);
    }

    @Override
    public Map<String, Object> readAllSecrets(String path, String secret) {
        VaultResponse response = vaultTemplate.opsForKeyValue(path, VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get(secret);
        return response != null ? Optional.ofNullable(response.getData()).orElse(new HashMap<>()) : new HashMap<>();
    }

    @Override
    public Map<String, String> readDependenciesVersions() {
        Properties p = new Properties();
        try {
            p.load(this.getClass().getClassLoader().getResourceAsStream("dependencies.properties"));
            Map<String, String> deps = new HashMap<>();
            p.forEach((key, value) -> deps.put((String) key, (String) value));
            return deps;
        } catch (IOException e) {
            throw new TechnicalException("Cannot read dependencies.properties file", e);
        }
    }

}
