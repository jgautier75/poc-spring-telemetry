package com.acme.jga.domain.functions.users.impl;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.functions.users.api.UserFilter;
import com.acme.jga.domain.model.v1.User;
import com.acme.jga.infra.services.api.users.IUsersInfraService;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserFilterImpl extends DomainFunction implements UserFilter {
    private static final String INSTRUMENTATION_NAME = UserFilterImpl.class.getCanonicalName();
    private final IUsersInfraService usersInfraService;

    public UserFilterImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory, IUsersInfraService usersInfraService) {
        super(openTelemetryWrapper, bundleFactory);
        this.usersInfraService = usersInfraService;
    }

    @Override
    public PaginatedResults<User> execute(Long tenantId, Long orgId, Span parentSpan, Map<String, Object> searchParams) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_USERS_FILTER", parentSpan, (span) -> usersInfraService.filterUsers(tenantId, orgId, span, searchParams));
    }
}
