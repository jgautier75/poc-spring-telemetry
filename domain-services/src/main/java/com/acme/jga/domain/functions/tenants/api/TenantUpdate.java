package com.acme.jga.domain.functions.tenants.api;

import com.acme.jga.domain.model.v1.Tenant;

public interface TenantUpdate {
    Integer execute(Tenant tenant);
}
