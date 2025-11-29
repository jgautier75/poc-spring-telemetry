package com.acme.jga.logging.services.impl;

import com.acme.jga.logging.services.api.IOtelLogService;
import com.acme.jga.logging.utils.LogHttpUtils;
import com.acme.jga.utils.http.RequestCorrelationId;
import com.acme.jga.utils.otel.OtelContext;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.logs.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OtelLogService implements IOtelLogService, InitializingBean {
    private final LoggerProvider sdkLoggerProvider;
    private Logger otelLogger;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.otelLogger = sdkLoggerProvider.loggerBuilder(OtelLogService.class.getCanonicalName()).build();
    }

    @Override
    public void info(String msg) {
        log(Severity.INFO, msg, null);
    }

    @Override
    public void warn(String msg) {
        log(Severity.WARN, msg, null);
    }

    @Override
    public void debug(String msg) {
        if (LogHttpUtils.LOG_FLAG.get() != null && LogHttpUtils.LOG_FLAG.get()) {
            log(Severity.DEBUG, msg, null);
        }
    }

    @Override
    public void error(String msg) {
        log(Severity.ERROR, msg, null);
    }

    @Override
    public void trace(String msg) {
        log(Severity.TRACE, msg, null);
    }

    @Override
    public void info(String msg, OtelContext context) {
        log(Severity.INFO, msg, context);
    }

    @Override
    public void warn(String msg, OtelContext context) {
        log(Severity.WARN, msg, context);
    }

    @Override
    public void debug(String msg, OtelContext context) {
        if (LogHttpUtils.LOG_FLAG.get() != null && LogHttpUtils.LOG_FLAG.get()) {
            log(Severity.DEBUG, msg, context);
        }
    }

    @Override
    public void error(String msg, OtelContext context) {
        log(Severity.ERROR, msg, context);
    }

    @Override
    public void trace(String msg, OtelContext context) {
        log(Severity.TRACE, msg, context);
    }

    private void log(Severity severity, String msg, OtelContext otelContext) {
        LogRecordBuilder logRecordBuilder = otelLogger.logRecordBuilder().setSeverity(severity).setObservedTimestamp(Instant.now()).setBody(msg)
                .setAttribute(AttributeKey.stringKey(LogHttpUtils.OTEL_CORRELATION_KEY), RequestCorrelationId.correlationKey());
        if (otelContext != null) {
            logRecordBuilder.setAttribute(AttributeKey.stringKey(LogHttpUtils.OTEL_TRACE_ID), otelContext.getTraceId());
            logRecordBuilder.setAttribute(AttributeKey.stringKey(LogHttpUtils.OTEL_SPAN_ID), otelContext.getSpanId());
        }
        logRecordBuilder.emit();
    }

}
