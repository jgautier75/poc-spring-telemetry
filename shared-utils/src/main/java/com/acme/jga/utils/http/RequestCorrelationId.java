package com.acme.jga.utils.http;

import java.util.Optional;

public class RequestCorrelationId {
    //public static final ThreadLocal<String> CORRELATION_KEY = new ThreadLocal<>();
    public static final ScopedValue<String> CORRELATION_KEY = ScopedValue.newInstance();

    public static String correlationKey() {
        return CORRELATION_KEY.get();
    }

}
