package com.acme.jga.domain.functions.users.api;

import com.acme.jga.domain.model.v1.User;

import java.util.Optional;

public interface UserFind {
    User byUid(String tenantUid, String orgUid, String userUid);

    Optional<User> byEmail(String email);

    Optional<User> byLogin(String login);

    Optional<User> byUid(String userUid);
}
