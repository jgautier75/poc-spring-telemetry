package com.acme.jga.logging.services.api;

import com.acme.jga.utils.otel.OtelContext;

public interface IOtelLogService {
    void info(String msg);
    void warn(String msg);
    void debug(String msg);
    void error(String msg);
    void trace(String msg);

    void info(String msg, OtelContext context);
    void warn(String msg, OtelContext context);
    void debug(String msg, OtelContext context);
    void error(String msg, OtelContext context);
    void trace(String msg, OtelContext context);

}
