package com.acme.jga.domain.model.utils;

import com.acme.jga.domain.model.events.v1.*;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.utils.date.DateTimeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuditEventFactory {

    public static AuditEvent createUserAuditEvent(User user, AuditAction auditAction, Tenant tenant, Organization org) {
        return AuditEvent.builder()
                .action(auditAction)
                .objectUid(user.getUid())
                .target(EventTarget.USER)
                .infos(user.getCredentials().getLogin())
                .scope(AuditScope.builder().tenantName(tenant.getCode()).tenantUid(tenant.getUid())
                        .organizationUid(org.getUid())
                        .organizationName(org.getCommons().getCode())
                        .build())
                .status(EventStatus.PENDING)
                .createdAt(DateTimeUtils.nowIso())
                .lastUpdatedAt(DateTimeUtils.nowIso())
                .build();
    }

    public static AuditEvent createTenantAuditEvent(String uid, Tenant tenant, AuditAction auditAction) {
        return AuditEvent.builder()
                .action(auditAction)
                .objectUid(uid)
                .target(EventTarget.TENANT)
                .scope(AuditScope.builder().tenantName(tenant.getCode()).tenantUid(uid).build())
                .status(EventStatus.PENDING)
                .createdAt(DateTimeUtils.nowIso())
                .lastUpdatedAt(DateTimeUtils.nowIso())
                .build();
    }

    public static AuditEvent createOrganizationAuditEvent(Organization organization, Tenant tenant, AuditAction auditAction, List<AuditChange> auditChanges) {
        return AuditEvent.builder()
                .action(auditAction)
                .objectUid(organization.getUid())
                .target(EventTarget.ORGANIZATION)
                .scope(AuditScope.builder().tenantName(tenant.getCode()).tenantUid(tenant.getUid())
                        .organizationUid(organization.getUid())
                        .organizationName(organization.getCommons().getCode())
                        .build())
                .status(EventStatus.PENDING)
                .createdAt(DateTimeUtils.nowIso())
                .lastUpdatedAt(DateTimeUtils.nowIso())
                .changes(auditChanges)
                .build();
    }

    public static AuditEvent createSectorAuditEvent(String uid, Organization organization, Tenant tenant, AuditAction auditAction, List<AuditChange> auditChanges) {
        return AuditEvent.builder()
                .action(auditAction)
                .objectUid(uid)
                .infos(organization.getCommons().getLabel())
                .target(EventTarget.SECTOR)
                .scope(AuditScope.builder().tenantName(tenant.getCode()).tenantUid(tenant.getUid())
                        .organizationUid(organization.getUid())
                        .organizationName(organization.getCommons().getCode())
                        .build())
                .status(EventStatus.PENDING)
                .createdAt(DateTimeUtils.nowIso())
                .lastUpdatedAt(DateTimeUtils.nowIso())
                .changes(auditChanges)
                .build();
    }

}
