package com.acme.jga.rest.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.vault.annotation.VaultPropertySource;

@Configuration
@VaultPropertySource("secret/poc-st")
@Slf4j
public class VaultSecrets implements InitializingBean {
    @Autowired
    Environment env;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("mySecret={}", env.getProperty("mySecret"));
    }
}
