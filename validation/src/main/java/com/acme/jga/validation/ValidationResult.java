package com.acme.jga.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class ValidationResult {
    private boolean success;
    private List<ValidationError> errors;

    public void addError(ValidationError e) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(e);
    }
}
