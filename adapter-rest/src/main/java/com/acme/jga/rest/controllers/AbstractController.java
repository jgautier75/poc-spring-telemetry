package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

public abstract class AbstractController {
    protected final OpenTelemetryWrapper openTelemetryWrapper;

    protected AbstractController(OpenTelemetryWrapper openTelemetryWrapper) {
        this.openTelemetryWrapper = openTelemetryWrapper;
    }

    protected <T> T withSpan(String instrumentationName, String operationName, SpanOperation<T> operation) throws FunctionalException {
        Span span = openTelemetryWrapper.withSpan(instrumentationName, operationName, null);
        try {
            return operation.execute(span);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
