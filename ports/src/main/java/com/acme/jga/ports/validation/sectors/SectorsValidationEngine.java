package com.acme.jga.ports.validation.sectors;

import com.acme.jga.ports.dtos.sectors.v1.SectorDto;
import com.acme.jga.validation.ValidationEngine;
import com.acme.jga.validation.ValidationResult;
import com.acme.jga.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SectorsValidationEngine implements ValidationEngine<SectorDto> {
    private final ValidationUtils validationUtils;

    @Override
    public ValidationResult validate(SectorDto sectorDto) {
        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        if (validationUtils.validateNotNull(validationResult, "payload", sectorDto)) {
            validationUtils.validateNotNullNonEmpty(validationResult, "code", sectorDto.getCode());
            validationUtils.validateNotNullNonEmpty(validationResult, "label", sectorDto.getLabel());
            validationUtils.validateNotNullNonEmpty(validationResult, "parentUid", sectorDto.getParentUid());
        }
        return validationResult;
    }

}
