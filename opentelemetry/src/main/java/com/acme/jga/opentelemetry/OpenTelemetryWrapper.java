package com.acme.jga.opentelemetry;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpenTelemetryWrapper implements InitializingBean {
    @Autowired
    private TracerProvider sdkTracerProvider;

    @Autowired
    private MeterProvider sdkMeterProvider;

    private LongCounter counterTest;

    public void setSdkTracerProvider(TracerProvider provider) {
        this.sdkTracerProvider = provider;
    }

    public Span withSpan(String instrumentationName, String spanName, Span parentSpan) {
        Tracer tracer = sdkTracerProvider.get(instrumentationName);
        if (parentSpan != null) {
            return tracer.spanBuilder(spanName).setParent(Context.current().with(parentSpan)).startSpan();
        } else {
            return tracer.spanBuilder(spanName).startSpan();
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        this.counterTest = sdkMeterProvider.meterBuilder("meter-builder").build().counterBuilder("counter-test").setDescription("A test counter").build();
    }

    public void incrementCounter() {
        this.counterTest.add(1);
    }

}
