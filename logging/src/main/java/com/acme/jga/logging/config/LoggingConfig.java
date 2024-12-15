package com.acme.jga.logging.config;

import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.logging.services.impl.LogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class LoggingConfig {
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean
    public ILogService loggingService(ResourceBundleMessageSource messageSource){
        return new LogService(messageSource);
    }

}
