package com.acme.jga.domain.model.exceptions;

public class WrappedFunctionalException extends RuntimeException {
    public WrappedFunctionalException(FunctionalException e) {
        super(e);
    }

    public WrappedFunctionalException(String errorCode, String message) {
        super(new FunctionalException(errorCode, null, message));
    }

}
