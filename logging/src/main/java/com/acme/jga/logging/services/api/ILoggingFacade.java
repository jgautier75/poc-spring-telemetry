package com.acme.jga.logging.services.api;

import com.acme.jga.utils.otel.OtelContext;
import org.slf4j.event.Level;

public interface ILoggingFacade {
    void infoB(String callerName, String bundleMessage, Object[] params);
    void infoS(String callerName, String message, Object[] params);
    void debugB(String callerName, String bundleMessage, Object[] params);
    void debugS(String callerName, String message, Object[] params);
    void traceB(String callerName, String bundleMessage, Object[] params);
    void traceS(String callerName, String message, Object[] params);
    void warnB(String callerName, String bundleMessage, Object[] params);
    void warnS(String callerName, String message, Object[] params);
    void error(String callerName, Exception e);
    void errorB(String callerName, String bundleMessage, Object[] params);
    void errorS(String callerName, String message, Object[] params);
    void error(String callerName, Throwable t);
    void logSimpleMessage(Level level, String message, Object[] params, String callerName);
    void logBundleMessage(Level level, String message, Object[] params, String callerName);


    void infoB(String callerName, String bundleMessage, Object[] params, OtelContext otelContext);
    void infoS(String callerName, String message, Object[] params, OtelContext otelContext);
    void debugB(String callerName, String bundleMessage, Object[] params, OtelContext otelContext);
    void debugS(String callerName, String message, Object[] params, OtelContext otelContext);
    void traceB(String callerName, String bundleMessage, Object[] params, OtelContext otelContext);
    void traceS(String callerName, String message, Object[] params, OtelContext otelContext);
    void warnB(String callerName, String bundleMessage, Object[] params, OtelContext otelContext);
    void warnS(String callerName, String message, Object[] params, OtelContext otelContext);
    void error(String callerName, Exception e, OtelContext otelContext);
    void errorB(String callerName, String bundleMessage, Object[] params, OtelContext otelContext);
    void errorS(String callerName, String message, Object[] params, OtelContext otelContext);
    void error(String callerName, Throwable t, OtelContext otelContext);

}
