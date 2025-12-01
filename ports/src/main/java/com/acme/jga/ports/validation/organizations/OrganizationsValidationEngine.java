package com.acme.jga.ports.validation.organizations;

import com.acme.jga.ports.dtos.organizations.v1.OrganizationDto;
import com.acme.jga.validation.ValidationEngine;
import com.acme.jga.validation.ValidationException;
import com.acme.jga.validation.ValidationResult;
import com.acme.jga.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganizationsValidationEngine implements ValidationEngine<OrganizationDto> {
    private final ValidationUtils validationUtils;

    @Override
    public void validate(OrganizationDto organizationDto) {
        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        // Validate payload
        if (validationUtils.validateNotNull(validationResult, "payload", organizationDto)
                && validationUtils.validateNotNull(validationResult, "commons", organizationDto.getCommons())) {
            if (validationUtils.validateNotNullNonEmpty(validationResult, "commons.code", organizationDto.getCommons().getCode())) {
                validationUtils.validateTextLength(validationResult, "commons.code", organizationDto.getCommons().getCode(), 1, 50);
            }
            if (validationUtils.validateNotNullNonEmpty(validationResult, "commons.country", organizationDto.getCommons().getCountry())) {
                validationUtils.validateCountry(validationResult, "commons.country", organizationDto.getCommons().getCountry());
            }
            if (validationUtils.validateNotNullNonEmpty(validationResult, "commons.label", organizationDto.getCommons().getLabel())) {
                validationUtils.validateTextLength(validationResult, "commons.label", organizationDto.getCommons().getLabel(), 1, 80);
            }
            validationUtils.validateNotNull(validationResult, "commons.kind", organizationDto.getCommons().getKind());
            validationUtils.validateNotNull(validationResult, "commons.status", organizationDto.getCommons().getStatus());
        }
        if (!validationResult.isSuccess()) {
            throw new ValidationException(validationResult.getErrors());
        }
    }

}
