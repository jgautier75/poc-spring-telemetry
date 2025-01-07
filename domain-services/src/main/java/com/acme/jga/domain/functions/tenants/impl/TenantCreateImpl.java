package com.acme.jga.domain.functions.tenants.impl;

import com.acme.jga.domain.aspects.Audited;
import com.acme.jga.domain.functions.tenants.AbstractTenantFunction;
import com.acme.jga.domain.functions.tenants.api.TenantCreate;
import com.acme.jga.domain.model.events.v1.AuditAction;
import com.acme.jga.domain.model.events.v1.AuditChange;
import com.acme.jga.domain.model.events.v1.AuditOperation;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.services.api.events.IEventsInfraService;
import com.acme.jga.infra.services.api.tenants.ITenantInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantCreateImpl extends AbstractTenantFunction implements TenantCreate {
    private static final String INSTRUMENTATION_NAME = TenantCreateImpl.class.getCanonicalName();
    private final ITenantInfraService tenantInfraService;
    private final ILogService logService;

    public TenantCreateImpl(OpenTelemetryWrapper openTelemetryWrapper, BundleFactory bundleFactory,
                            ITenantInfraService tenantInfraService, ILogService logService,
                            IEventsInfraService eventsInfraService) {
        super(openTelemetryWrapper, bundleFactory, eventsInfraService);
        this.tenantInfraService = tenantInfraService;
        this.logService = logService;
    }

    @Audited
    @Transactional
    @Override
    public CompositeId execute(Tenant tenant, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DOMAIN_TENANTS_CREATE", parentSpan, (span) -> {
            String callerName = this.getClass().getName() + "-createTenant";
            boolean alreadyExist = tenantInfraService.tenantExistsByCode(tenant.getCode(), span);
            if (alreadyExist) {
                throwWrappedException(FunctionalErrorsTypes.TENANT_CODE_ALREADY_USED.name(), "tenant_code_already_used", new Object[]{tenant.getCode()});
            }
            if ("crash".equals(tenant.getCode())) {
                throw new NullPointerException("Fake error");
            }
            CompositeId compositeId = tenantInfraService.createTenant(tenant, span);
            tenant.setUid(compositeId.getUid());
            logService.infoS(callerName, "Created tenant [%s]", new Object[]{compositeId.getUid()});

            // Create audit event and send
            List<AuditChange> auditChanges = List.of(AuditChange.builder().to(tenant.getLabel()).object("label").operation(AuditOperation.ADD).build());
            generateTenantAuditEventAndPush(tenant, AuditAction.CREATE, auditChanges);
            return compositeId;
        });
    }
}
