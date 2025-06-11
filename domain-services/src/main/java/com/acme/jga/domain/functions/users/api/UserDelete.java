package com.acme.jga.domain.functions.users.api;

public interface UserDelete {
    Integer execute(String tenantUid, String orgUid, String userUid);
}
