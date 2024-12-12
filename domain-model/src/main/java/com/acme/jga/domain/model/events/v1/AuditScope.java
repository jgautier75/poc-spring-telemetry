package com.acme.jga.domain.model.events.v1;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class AuditScope {
    private String tenantUid;
    private String tenantName;
    private String organizationUid;
    private String organizationName;
}
