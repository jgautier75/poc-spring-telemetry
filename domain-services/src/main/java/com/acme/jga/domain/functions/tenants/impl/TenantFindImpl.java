package com.acme.jga.domain.functions.tenants.impl;

import com.acme.jga.domain.functions.DomainFunction;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.tenants.TenantInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.utils.otel.OtelContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenantFindImpl extends DomainFunction implements TenantFind {
    private static final String INSTRUMENTATION_NAME = TenantFindImpl.class.getCanonicalName();
    private final TenantInfraService tenantInfraService;
    private final ILoggingFacade loggingFacade;

    public TenantFindImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                          TenantInfraService tenantInfraService, ILoggingFacade loggingFacade) {
        super(openTelemetryWrapper, bundleFactory);
        this.tenantInfraService = tenantInfraService;
        this.loggingFacade = loggingFacade;
    }

    @Override
    public Tenant byUid(String uid) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_FIND_UID", (span) -> {
            loggingFacade.infoS(INSTRUMENTATION_NAME, "Find tenant by uid [%s]", new Object[]{uid}, OtelContext.fromSpan(span));
            Optional<Tenant> tenant = tenantInfraService.findTenantByUid(uid);
            if (tenant.isEmpty()) {
                throwWrappedException(FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), "tenant_not_found_by_uid", new Object[]{uid});
            }
            return tenant.get();
        });
    }
}
