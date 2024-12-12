package com.acme.jga.validation;

import java.io.Serial;
import java.util.List;

public class ValidationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 6518120039164331049L;
    private final List<ValidationError> validationErrors;

    public ValidationException(List<ValidationError> validationErrors) {
        super();
        this.validationErrors = validationErrors;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
}
