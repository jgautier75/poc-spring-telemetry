package com.acme.jga.utils.otel;

import io.opentelemetry.api.trace.Span;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OtelContext {
    private String traceId;
    private String spanId;

    public static OtelContext fromSpan(Span span) {
        if (span == null || span.getSpanContext() == null) {
            return new OtelContext(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        } else {
            return new OtelContext(span.getSpanContext().getTraceId(), span.getSpanContext().getSpanId());
        }
    }

}
