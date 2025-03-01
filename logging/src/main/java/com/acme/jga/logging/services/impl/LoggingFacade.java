package com.acme.jga.logging.services.impl;

import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.logging.services.api.IOtelLogService;
import com.acme.jga.utils.otel.OtelContext;
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
    private final IOtelLogService otelLogService;

    @Override
    public void infoB(String callerName, String bundleMessage, Object[] params) {
        infoB(callerName, bundleMessage, params, null);
    }

    @Override
    public void infoS(String callerName, String message, Object[] params) {
        infoS(callerName, message, params, null);
    }

    @Override
    public void debugB(String callerName, String bundleMessage, Object[] params) {
        debugB(callerName, bundleMessage, params, null);
    }

    @Override
    public void debugS(String callerName, String message, Object[] params) {
        debugS(callerName, message, params, null);
    }

    @Override
    public void traceB(String callerName, String bundleMessage, Object[] params) {
        traceB(callerName, bundleMessage, params, null);
    }

    @Override
    public void traceS(String callerName, String message, Object[] params) {
        traceS(callerName, message, params, null);
    }

    @Override
    public void warnB(String callerName, String bundleMessage, Object[] params) {
        warnB(callerName, bundleMessage, params, null);
    }

    @Override
    public void warnS(String callerName, String message, Object[] params) {
        warnS(callerName, message, params, null);
    }

    @Override
    public void error(String callerName, Exception e) {
        logService.error(callerName, e);
        String stackTrace = dumpStackTrace(e);
        String msg = logService.buildMessage(false, stackTrace, new Object[]{}, callerName);
        otelLogService.error(msg, null);
    }

    @Override
    public void errorB(String callerName, String bundleMessage, Object[] params) {
        logService.errorB(callerName, bundleMessage, params);
        String msg = logService.buildMessage(true, bundleMessage, params, callerName);
        otelLogService.error(msg, null);
    }

    @Override
    public void errorS(String callerName, String message, Object[] params) {
        logService.errorS(callerName, message, params);
        otelLogService.error(logService.buildMessage(false, message, params, callerName), null);
    }

    @Override
    public void error(String callerName, Throwable t) {
        logService.error(callerName, t);
        String stackTrace = dumpStackTrace(t);
        otelLogService.error(logService.buildMessage(false, stackTrace, new Object[]{}, callerName));
    }

    @Override
    public void logSimpleMessage(Level level, String message, Object[] params, String callerName) {
        logService.logSimpleMessage(level, message, params, callerName);
    }

    @Override
    public void logBundleMessage(Level level, String message, Object[] params, String callerName) {
        logService.logBundleMessage(level, message, params, callerName);
    }

    @Override
    public void infoB(String callerName, String bundleMessage, Object[] params, OtelContext otelContext) {
        String msg = logService.buildMessage(true, bundleMessage, params, callerName);
        logService.info(msg);
        otelLogService.info(msg, otelContext);
    }

    @Override
    public void infoS(String callerName, String message, Object[] params, OtelContext otelContext) {
        String msg = logService.buildMessage(false, message, params, callerName);
        logService.info(msg);
        otelLogService.info(msg, otelContext);
    }

    @Override
    public void debugB(String callerName, String bundleMessage, Object[] params, OtelContext otelContext) {
        String msg = logService.buildMessage(true, bundleMessage, params, callerName);
        logService.debug(msg);
        otelLogService.debug(msg, otelContext);
    }

    @Override
    public void debugS(String callerName, String message, Object[] params, OtelContext otelContext) {
        String msg = logService.buildMessage(false, message, params, callerName);
        logService.debug(msg);
        otelLogService.debug(msg, otelContext);
    }

    @Override
    public void traceB(String callerName, String bundleMessage, Object[] params, OtelContext otelContext) {
        String msg = logService.buildMessage(true, bundleMessage, params, callerName);
        logService.trace(msg);
        otelLogService.trace(msg, otelContext);
    }

    @Override
    public void traceS(String callerName, String message, Object[] params, OtelContext otelContext) {
        String msg = logService.buildMessage(false, message, params, callerName);
        logService.trace(msg);
        otelLogService.trace(msg, otelContext);
    }

    @Override
    public void warnB(String callerName, String bundleMessage, Object[] params, OtelContext otelContext) {
        String msg = logService.buildMessage(true, bundleMessage, params, callerName);
        logService.warn(msg);
        otelLogService.warn(msg, otelContext);
    }

    @Override
    public void warnS(String callerName, String message, Object[] params, OtelContext otelContext) {
        String msg = logService.buildMessage(false, message, params, callerName);
        logService.warn(msg);
        otelLogService.warn(msg, otelContext);
    }

    @Override
    public void error(String callerName, Exception e, OtelContext otelContext) {
        String stackTrace = dumpStackTrace(e);
        String msg = logService.buildMessage(false, stackTrace, new Object[]{}, callerName);
        logService.error(callerName, e);
        otelLogService.error(msg, otelContext);
    }

    @Override
    public void errorB(String callerName, String bundleMessage, Object[] params, OtelContext otelContext) {
        logService.errorB(callerName, bundleMessage, params);
        String msg = logService.buildMessage(true, bundleMessage, params, callerName);
        otelLogService.error(msg, otelContext);
    }

    @Override
    public void errorS(String callerName, String message, Object[] params, OtelContext otelContext) {
        logService.errorS(callerName, message, params);
        String msg = logService.buildMessage(false, message, params, callerName);
        otelLogService.error(msg, otelContext);
    }

    @Override
    public void error(String callerName, Throwable t, OtelContext otelContext) {
        logService.error(callerName, t);
        String stackTrace = dumpStackTrace(t);
        otelLogService.error(stackTrace, otelContext);
    }

    private String dumpStackTrace(Throwable e) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return sw.toString();
        } catch (IOException ioe) {
            // Silent catch
        }
        return null;
    }

}
