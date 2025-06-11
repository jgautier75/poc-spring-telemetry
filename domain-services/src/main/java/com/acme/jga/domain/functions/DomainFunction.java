package com.acme.jga.domain.functions;

import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.utils.LogHttpUtils;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.utils.http.RequestCorrelationId;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

import java.util.function.Function;

import static com.acme.jga.utils.http.RequestCorrelationId.correlationKey;

public abstract class DomainFunction {
    private final OpenTelemetryWrapper openTelemetryWrapper;
    protected final BundleFactory bundleFactory;

    protected DomainFunction(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory) {
        this.openTelemetryWrapper = openTelemetryWrapper;
        this.bundleFactory = bundleFactory;
    }

    /**
     * Process function with OpenTelemetry span.
     *
     * @param instrumentationName Instrumentation name
     * @param operation           Operation
     * @param operationFunction   Function
     * @param <T>                 Result
     * @return Function result
     */
    protected <T> T processWithSpan(String instrumentationName, String operation, Function<Span, T> operationFunction) {
        Span span = openTelemetryWrapper.withSpan(instrumentationName, operation + "-" + correlationKey());
        try {
            span.setAttribute(AttributeKey.stringKey(LogHttpUtils.OTEL_CORRELATION_KEY), RequestCorrelationId.correlationKey());
            return operationFunction.apply(span);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    protected void throwWrappedException(String code, String bundleKey, Object[] parameters) {
        throw new WrappedFunctionalException(new FunctionalException(code, null, buildErrorMessage(bundleKey, parameters)));
    }

    protected String buildErrorMessage(String bundleKey, Object[] parameters) {
        return bundleFactory.getMessage(bundleKey, parameters);
    }
}
