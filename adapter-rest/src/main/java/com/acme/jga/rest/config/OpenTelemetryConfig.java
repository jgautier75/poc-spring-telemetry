package com.acme.jga.rest.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class OpenTelemetryConfig {

    private final AppGenericConfig appGenericConfig;

    @Bean
    public Resource otelResource() {
        return Resource.getDefault().toBuilder()
                .put("service.name", appGenericConfig.getModuleName())
                .put("service.version", "1.0.0").build();
    }

    @Bean
    @Primary
    public SdkTracerProvider tracerProvider(Resource otelResource) {
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(appGenericConfig.getOtlpEndpoint()).build();
        return SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).setScheduleDelay(Duration.ofSeconds(appGenericConfig.getOtlpPushFrequency())).build())
                .setResource(otelResource)
                .build();
    }

    @Bean
    @Primary
    public SdkMeterProvider meterProvider(Resource otelResource) {
        OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.builder()
                .setEndpoint(appGenericConfig.getOtlpEndpoint()).build();
        return SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.builder(metricExporter).setInterval(Duration.ofSeconds(appGenericConfig.getOtlpPushFrequency())).build())
                .setResource(otelResource)
                .build();
    }

    @Bean
    @Primary
    public SdkLoggerProvider loggerProvider(Resource otelResource) {
        OtlpGrpcLogRecordExporter logRecordExporter = OtlpGrpcLogRecordExporter.builder()
                .setEndpoint(appGenericConfig.getOtlpEndpoint()).build();
        return SdkLoggerProvider.builder()
                .addLogRecordProcessor(BatchLogRecordProcessor.builder(logRecordExporter).setScheduleDelay(Duration.ofSeconds(appGenericConfig.getOtlpPushFrequency())).build())
                .addResource(otelResource)
                .build();
    }

    @Bean
    public OpenTelemetry openTelemetry(SdkTracerProvider tracerProvider, SdkMeterProvider sdkMeterProvider, SdkLoggerProvider sdkLoggerProvider) {
        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setMeterProvider(sdkMeterProvider)
                .setLoggerProvider(sdkLoggerProvider)
                .setPropagators(ContextPropagators.create(TextMapPropagator
                        .composite(W3CTraceContextPropagator.getInstance(),
                                W3CBaggagePropagator.getInstance())))
                .buildAndRegisterGlobal();
    }

}
