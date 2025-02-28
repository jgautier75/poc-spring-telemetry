package com.acme.jga.logging.services.api;

public interface IOtelLogService {
    void info(String msg);
    void warn(String msg);
    void debug(String msg);
    void error(String msg);
    void trace(String msg);
}
