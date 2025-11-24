package com.acme.jga.validation;

public interface ValidationEngine<T> {
    void validate(T object) throws ValidationException;
}
