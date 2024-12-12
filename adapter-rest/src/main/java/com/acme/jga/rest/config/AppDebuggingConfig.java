package com.acme.jga.rest.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.debugging")
@AllArgsConstructor(access = AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.NONE)
@Data
public class AppDebuggingConfig {
    private boolean forceDebugMode;
    private String headerName;
    private String debugValue;
}
