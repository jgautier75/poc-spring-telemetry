package com.acme.jga.utils.otel;

import io.opentelemetry.api.trace.Span;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OtelContext {
    private String traceId;
    private String spanId;

    public static OtelContext fromSpan(Span span) {
        return span != null ? new OtelContext(span.getSpanContext().getTraceId(), span.getSpanContext().getSpanId()) : null;
    }

}
