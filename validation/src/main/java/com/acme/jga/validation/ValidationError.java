package com.acme.jga.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class ValidationError implements Serializable {
    private String fieldName;
    private Object fieldValue;
    private String validationRule;
    private String message;
}
