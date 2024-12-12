package com.acme.jga.utils.http;

import java.util.Optional;

public class RequestCorrelationId {
    public static final ThreadLocal<String> CORRELATION_KEY = new ThreadLocal<>();

    public static String correlationKey() {
        return Optional.ofNullable(CORRELATION_KEY.get()).orElse("no_key");
    }

}
