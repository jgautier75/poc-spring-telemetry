package com.acme.jga.ports.validation.users;

import com.acme.jga.ports.port.users.v1.UserCommonsDto;
import com.acme.jga.ports.port.users.v1.UserDto;
import com.acme.jga.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsersValidationEngine implements ValidationEngine<UserDto> {
    private final ValidationUtils validationUtils;

    @Override
    public ValidationResult validate(UserDto userDto) {
        ValidationResult validationResult = ValidationResult.builder().success(true).build();

        // Validate payload not null
        validationUtils.validateNotNull(validationResult, "payload", userDto);

        if (userDto != null) {
            // Validate commons not null
            if (validationUtils.validateNotNull(validationResult, "commons", userDto.getCommons())) {
                validateCommmons(validationResult, userDto.getCommons());
            }

            validateCredentials(userDto, validationResult);

            // Validate status
            validationUtils.validateNotNull(validationResult, "status", userDto.getStatus());
        }

        return validationResult;
    }

    private void validateCredentials(UserDto userDto, ValidationResult validationResult) {
        // Validate credentials
        if (validationUtils.validateNotNull(validationResult, "credentials", userDto.getCredentials())) {
            // Validate login
            if (validationUtils.validateNotNullNonEmpty(validationResult, "credentials.login", userDto.getCredentials().getLogin())) {
                validationUtils.validateTextLength(validationResult, "credentials.login", userDto.getCredentials().getLogin(), 1, 50);
            }

            // Validate email
            validateEmail(userDto, validationResult);
        }
    }

    private void validateEmail(UserDto userDto, ValidationResult validationResult) {
        if (validationUtils.validateNotNullNonEmpty(validationResult, "credentials.email", userDto.getCredentials().getEmail())) {
            validationUtils.validateTextLength(validationResult, "credentials.email", userDto.getCredentials().getLogin(), 1, 50);
            if (!validationUtils.isValidEmail(userDto.getCredentials().getEmail())) {
                validationResult.setSuccess(false);
                validationResult.addError(ValidationError.builder()
                        .fieldName("credentials.email")
                        .fieldValue(userDto.getCredentials().getEmail())
                        .message(validationUtils.buildInvalidEmail("credentials.email", ValidationUtils.EMAIL_REGEX.pattern()))
                        .validationRule(ValidationRule.EMAIL.name())
                        .build());
            }
        }
    }

    public void validateCommmons(ValidationResult validationResult, UserCommonsDto commonsDto) {
        // Validate firstName
        validationUtils.validateNotNullNonEmpty(validationResult, "commons.firstName", commonsDto.getFirstName());
        if (commonsDto.getFirstName() != null) {
            validationUtils.validateTextLength(validationResult, "commons.firstName", commonsDto.getFirstName(), 1, 50);
        }

        // Validate lastName
        validationUtils.validateNotNullNonEmpty(validationResult, "commons.lastName", commonsDto.getLastName());
        if (commonsDto.getLastName() != null) {
            validationUtils.validateTextLength(validationResult, "commons.lastName", commonsDto.getLastName(), 1, 50);
        }

    }

}
