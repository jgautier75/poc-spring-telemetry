package com.acme.jga.kafka.consumer;

import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.users.mgt.events.protobuf.Event;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaSimpleConsumer {
    private final ILoggingFacade loggingFacade;

    @KafkaListener(topics = "${app.kafka.producer.topicNameAuditEvents}", groupId = "${app.kafka.consumer.auditEventsGroupId}")
    public void consume(ConsumerRecord<String, DynamicMessage> messageRecord) throws InvalidProtocolBufferException {
        Event.AuditEventMessage auditEventMessage = Event.AuditEventMessage.newBuilder().build().getParserForType().parseFrom(messageRecord.value().toByteArray());
        loggingFacade.infoS(this.getClass().getName(), "Received message with key [%s] and content : [%s]", new Object[]{messageRecord.key(), auditEventMessage.toString()});
    }

}
