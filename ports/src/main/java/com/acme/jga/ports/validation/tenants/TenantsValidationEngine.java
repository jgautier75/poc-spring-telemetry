package com.acme.jga.ports.validation.tenants;

import com.acme.jga.ports.dtos.tenants.v1.TenantDto;
import com.acme.jga.validation.ValidationEngine;
import com.acme.jga.validation.ValidationException;
import com.acme.jga.validation.ValidationResult;
import com.acme.jga.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantsValidationEngine implements ValidationEngine<TenantDto> {
    private final ValidationUtils validationUtils;

    @Override
    public void validate(TenantDto tenantDto) {
        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        if (validationUtils.validateNotNull(validationResult, "payload", tenantDto)) {
            validationUtils.validateNotNullNonEmpty(validationResult, "code", tenantDto.getCode());
            validationUtils.validateNotNullNonEmpty(validationResult, "label", tenantDto.getLabel());
        }
        if (!validationResult.isSuccess()) {
            throw new ValidationException(validationResult.getErrors());
        }
    }

}
