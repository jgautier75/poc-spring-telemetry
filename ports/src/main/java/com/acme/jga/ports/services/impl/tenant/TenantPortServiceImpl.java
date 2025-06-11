package com.acme.jga.ports.services.impl.tenant;

import com.acme.jga.domain.functions.tenants.api.*;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.converters.tenant.TenantsPortConverter;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantDisplayDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantListDisplayDto;
import com.acme.jga.ports.services.api.tenant.TenantPortService;
import com.acme.jga.ports.services.impl.AbstractPortService;
import com.acme.jga.ports.validation.tenants.TenantsValidationEngine;
import com.acme.jga.utils.lambdas.StreamUtil;
import com.acme.jga.utils.otel.OtelContext;
import com.acme.jga.validation.ValidationException;
import com.acme.jga.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantPortServiceImpl extends AbstractPortService implements TenantPortService {
    private static final String INSTRUMENTATION_NAME = TenantPortServiceImpl.class.getCanonicalName();
    private final TenantsPortConverter tenantsConverter;
    private final TenantCreate tenantCreate;
    private final TenantFind tenantFind;
    private final TenantList tenantList;
    private final TenantUpdate tenantUpdate;
    private final TenantDelete tenantDelete;
    private final TenantsValidationEngine tenantsValidationEngine;
    private final ILoggingFacade loggingFacade;

    @Autowired
    public TenantPortServiceImpl(TenantsPortConverter tenantsConverter, TenantCreate tenantCreate, TenantsValidationEngine tenantsValidationEngine,
                                 OpenTelemetryWrapper openTelemetryWrapper, TenantFind tenantFind, TenantList tenantList,
                                 TenantUpdate tenantUpdate, TenantDelete tenantDelete, ILoggingFacade loggingFacade) {
        super(openTelemetryWrapper);
        this.tenantsConverter = tenantsConverter;
        this.tenantCreate = tenantCreate;
        this.tenantFind = tenantFind;
        this.tenantList = tenantList;
        this.tenantUpdate = tenantUpdate;
        this.tenantDelete = tenantDelete;
        this.tenantsValidationEngine = tenantsValidationEngine;
        this.loggingFacade = loggingFacade;
    }

    /**
     * @inheritDoc
     */
    @Override
    public UidDto createTenant(TenantDto tenantDto) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_TENANTS_CREATE", (span) -> {
            ValidationResult validationResult = tenantsValidationEngine.validate(tenantDto);
            if (!validationResult.isSuccess()) {
                throw new ValidationException(validationResult.getErrors());
            }
            Tenant tenant = tenantsConverter.tenantDtoToDomainTenant(tenantDto);
            CompositeId compositeId = tenantCreate.execute(tenant);
            return new UidDto(compositeId.getUid());
        });
    }

    @Override
    public TenantDisplayDto findTenantByUid(String uid) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_TENANTS_FIND_UID", (span) -> {
            loggingFacade.infoS(INSTRUMENTATION_NAME, "Find tenant by uid [%s]", new Object[]{uid}, OtelContext.fromSpan(span));
            Tenant tenant = tenantFind.byUid(uid);
            loggingFacade.infoS(INSTRUMENTATION_NAME, "Convert tenant named [%s] from domain to dto", new Object[]{tenant.getLabel()}, OtelContext.fromSpan(span));
            return tenantsConverter.tenantDomainToDisplay(tenant);
        });
    }

    @Override
    public TenantListDisplayDto findAllTenants() {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_TENANTS_FIND_ALL", (span) -> {
            List<Tenant> tenants = tenantList.execute();
            List<TenantDisplayDto> tenantDisplayDtos = StreamUtil.ofNullableList(tenants).map(tenant -> tenantsConverter.tenantDomainToDisplay(tenant)).toList();
            return new TenantListDisplayDto(tenantDisplayDtos);
        });
    }

    @Override
    public Integer updateTenant(String uid, TenantDto tenantDto) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_TENANTS_UPDATE", (span) -> {
            Tenant tenant = tenantsConverter.tenantDtoToDomainTenant(tenantDto);
            tenant.setUid(uid);
            return tenantUpdate.execute(tenant);
        });
    }

    @Override
    public Integer deleteTenant(String tenantUid) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_TENANTS_DELETE", (span) -> tenantDelete.execute(tenantUid));
    }

}
