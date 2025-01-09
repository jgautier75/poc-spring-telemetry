package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import io.opentelemetry.api.trace.Span;

@FunctionalInterface
public interface SpanOperation<T> {
    T execute(Span s) throws FunctionalException;
}
