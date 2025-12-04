package com.acme.jga.domain.config;

import com.acme.jga.domain.events.EventBusErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
public class EventBusConfig {
    private final EventBusErrorHandler errorHandler;

    @Bean
    public PublishSubscribeChannel eventAuditChannel() {
        PublishSubscribeChannel exportChannel = new PublishSubscribeChannel();
        exportChannel.setErrorHandler(errorHandler);
        return exportChannel;
    }
}
