package com.acme.jga.rest.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MetricPoint {
    String alias();

    String version();

    String regex();

    String method();
}
