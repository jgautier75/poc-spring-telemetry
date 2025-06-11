package com.acme.jga.domain.functions.users.api;

import com.acme.jga.domain.model.v1.UserDisplay;
import com.acme.jga.jdbc.dql.PaginatedResults;

import java.util.Map;

public interface UserFilter {
    PaginatedResults<UserDisplay> execute(Long tenantId, Long orgId,Map<String, Object> searchParams);
}
