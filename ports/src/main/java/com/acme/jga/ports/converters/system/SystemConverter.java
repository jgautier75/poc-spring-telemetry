package com.acme.jga.ports.converters.system;

import com.acme.jga.domain.model.v1.SystemErrorFile;
import com.acme.jga.domain.model.v1.SystemErrorTemporal;
import com.acme.jga.ports.dtos.system.v1.SystemErrorFileDto;
import com.acme.jga.ports.dtos.system.v1.SystemErrorTemporalDto;
import com.acme.jga.ports.dtos.system.v1.SystemSecretDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemConverter {
    public static List<SystemSecretDto> convertSecretsMap(Map<String, Object> secretsMap) {
        final List<SystemSecretDto> secretsList = new ArrayList<>();
        Optional.ofNullable(secretsMap).get().forEach((k, v) -> secretsList.add(new SystemSecretDto(k, (String) v)));
        return secretsList;
    }

    public static SystemErrorFileDto convertSystemErrorFile(SystemErrorFile systemErrorFile) {
        return new SystemErrorFileDto().toBuilder()
                .fullFileName(systemErrorFile.getFullFileName())
                .moduleName(systemErrorFile.getModuleName())
                .uid(systemErrorFile.getUid())
                .temporal(convertSystemErrorTemporal(systemErrorFile.getTemporal()))
                .content(systemErrorFile.getContent())
                .build();
    }

    public static SystemErrorTemporalDto convertSystemErrorTemporal(SystemErrorTemporal systemErrorTemporal) {
        return new SystemErrorTemporalDto().toBuilder()
                .pattern(systemErrorTemporal.getPattern())
                .timestamp(systemErrorTemporal.getTimestamp())
                .build();
    }

}
