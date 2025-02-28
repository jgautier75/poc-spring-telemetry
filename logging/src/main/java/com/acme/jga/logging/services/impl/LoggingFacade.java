package com.acme.jga.logging.services.impl;

import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.logging.services.api.IOtelLogService;
import io.opentelemetry.api.logs.LoggerProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Service
@RequiredArgsConstructor
public class LoggingFacade implements ILoggingFacade {
    private final ILogService logService;
    private final LoggerProvider sdkLoggerProvider;
    private final IOtelLogService otelLogService;

    @Override
    public void infoB(String callerName, String bundleMessage, Object[] params) {
        String msg = logService.buildMessage(true, bundleMessage, params, callerName);
        logService.info(msg);
        otelLogService.info(msg);
    }

    @Override
    public void infoS(String callerName, String message, Object[] params) {
        String msg = logService.buildMessage(false, message, params, callerName);
        logService.info(msg);
        otelLogService.info(msg);
    }

    @Override
    public void debugB(String callerName, String bundleMessage, Object[] params) {
        String msg = logService.buildMessage(true, bundleMessage, params, callerName);
        logService.debug(msg);
        otelLogService.debug(msg);
    }

    @Override
    public void debugS(String callerName, String message, Object[] params) {
        String msg = logService.buildMessage(false, message, params, callerName);
        logService.debug(msg);
        otelLogService.debug(msg);
    }

    @Override
    public void traceB(String callerName, String bundleMessage, Object[] params) {
        String msg = logService.buildMessage(true, bundleMessage, params, callerName);
        logService.trace(msg);
        otelLogService.trace(msg);
    }

    @Override
    public void traceS(String callerName, String message, Object[] params) {
        String msg = logService.buildMessage(false, message, params, callerName);
        logService.trace(msg);
        otelLogService.trace(msg);
    }

    @Override
    public void warnB(String callerName, String bundleMessage, Object[] params) {
        String msg = logService.buildMessage(true, bundleMessage, params, callerName);
        logService.warn(msg);
        otelLogService.warn(msg);
    }

    @Override
    public void warnS(String callerName, String message, Object[] params) {
        String msg = logService.buildMessage(false, message, params, callerName);
        logService.warn(msg);
        otelLogService.warn(msg);
    }

    @Override
    public void error(String callerName, Exception e) {
        logService.error(callerName, e);
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            String stack = sw.toString();
            String msg = logService.buildMessage(false, stack, new Object[]{}, callerName);
            otelLogService.error(msg);
        } catch (IOException ioe) {
            // Silent catch
        }
    }

    @Override
    public void errorB(String callerName, String bundleMessage, Object[] params) {
        logService.errorB(callerName, bundleMessage, params);
    }

    @Override
    public void errorS(String callerName, String message, Object[] params) {
        logService.errorS(callerName, message, params);
    }

    @Override
    public void error(String callerName, Throwable t) {
        logService.error(callerName, t);
    }

    @Override
    public void logSimpleMessage(Level level, String message, Object[] params, String callerName) {
        logService.logSimpleMessage(level, message, params, callerName);
    }

    @Override
    public void logBundleMessage(Level level, String message, Object[] params, String callerName) {
        logService.logBundleMessage(level, message, params, callerName);
    }

}
