package com.acme.jga.domain.aspects;

import com.acme.jga.infra.config.KafkaProducerConfig;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    private final PublishSubscribeChannel eventAuditChannel;

    @Around("@annotation(Audited)")
    public Object publishChannelWakeup(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } finally {
            eventAuditChannel.send(MessageBuilder.withPayload(KafkaProducerConfig.AUDIT_WAKE_UP).build());
        }
    }
}
