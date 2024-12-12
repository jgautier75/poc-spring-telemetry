package com.acme.jga.ports.services.impl;

import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

import java.util.function.Function;

import static com.acme.jga.utils.http.RequestCorrelationId.correlationKey;

public abstract class AbstractPortService {
    protected final OpenTelemetryWrapper openTelemetryWrapper;

    protected AbstractPortService(OpenTelemetryWrapper openTelemetryWrapper) {
        this.openTelemetryWrapper = openTelemetryWrapper;
    }

    protected <T> T processWithSpan(String instrumentationName, String operation, Span parentSpan, Function<Span, T> operationFunction){
        Span span = openTelemetryWrapper.withSpan(instrumentationName, operation + "-" + correlationKey(), parentSpan);
        try {
            return operationFunction.apply(span);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

}
