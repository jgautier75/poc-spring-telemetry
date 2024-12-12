package com.acme.jga.domain.model.events.v1;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class AuditEvent {
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private String uid;
    private EventTarget target;
    private AuditAuthor author;
    private AuditScope scope;
    private String objectUid;
    private AuditAction action;
    private EventStatus status;
    private List<AuditChange> changes;
    private String payload;
    private String infos;
}
