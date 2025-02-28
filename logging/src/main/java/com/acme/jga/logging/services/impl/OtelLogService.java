package com.acme.jga.logging.services.impl;

import com.acme.jga.logging.services.api.IOtelLogService;
import com.acme.jga.logging.utils.LogHttpUtils;
import com.acme.jga.utils.http.RequestCorrelationId;
import io.opentelemetry.api.common.AttributeKey;
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
        log(Severity.INFO, msg);
    }

    @Override
    public void warn(String msg) {
        log(Severity.WARN, msg);
    }

    @Override
    public void debug(String msg) {
        if (LogHttpUtils.APP_LOG_CTX.get() != null && LogHttpUtils.APP_LOG_CTX.get()) {
            log(Severity.DEBUG, msg);
        }
    }

    @Override
    public void error(String msg) {
        log(Severity.ERROR, msg);
    }

    @Override
    public void trace(String msg) {
        log(Severity.TRACE, msg);
    }

    private void log(Severity severity, String msg) {
        otelLogger.logRecordBuilder().setSeverity(severity).setObservedTimestamp(Instant.now()).setBody(msg).setAttribute(AttributeKey.stringKey(LogHttpUtils.OTEL_CORRELATION_KEY), RequestCorrelationId.correlationKey()).emit();
    }

}
