package com.acme.jga.infra.services.impl;

import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.acme.jga.utils.http.RequestCorrelationId.correlationKey;

public abstract class AbstractInfraService {

    protected final OpenTelemetryWrapper openTelemetryWrapper;

    protected AbstractInfraService(OpenTelemetryWrapper openTelemetryWrapper) {
        this.openTelemetryWrapper = openTelemetryWrapper;
    }

    /**
     * Handle function with opentelemetry span.
     *
     * @param instrumentationName Instrumentation name
     * @param action              Action
     * @param parentSpan          Parent span (nullable)
     * @param operation            Function
     * @param <T>                 Typed property
     * @return Supplier result
     */
    protected <T> T processWithSpan(String instrumentationName, String action, Span parentSpan, Function<Span, T> operation) {
        Span span = openTelemetryWrapper.withSpan(instrumentationName, action + "-" + correlationKey(), parentSpan);
        try {
            return operation.apply(span);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
