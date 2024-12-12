package com.acme.jga.validation;

public interface ValidationEngine<T> {
    ValidationResult validate(T object);
}
