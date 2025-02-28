package com.acme.jga.logging.services.api;

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
}
