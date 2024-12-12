package com.acme.jga.infra.dto.events.v1;

import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.EventStatus;
import com.acme.jga.domain.model.events.v1.EventTarget;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class AuditEventDb {
    private String uid;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private EventTarget target;
    private String objectUid;
    private AuditAction action;
    private EventStatus status;
    private String payload;
}
