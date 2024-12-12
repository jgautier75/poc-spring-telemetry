package com.acme.jga.domain.model.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FunctionalException extends Exception {
    private String code;
    private Throwable exception;

    public FunctionalException(String code, Throwable exception, String message) {
        super(message, exception);
        this.code = code;
    }

    public FunctionalException(String message, Throwable exception) {
        super(message, exception);
    }

}
