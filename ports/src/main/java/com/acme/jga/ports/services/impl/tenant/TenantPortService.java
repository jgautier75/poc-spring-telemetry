package com.acme.jga.ports.services.impl.tenant;

import com.acme.jga.domain.functions.tenants.api.*;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.ports.converters.tenant.TenantsPortConverter;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantDisplayDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantDto;
import com.acme.jga.ports.dtos.tenants.v1.TenantListDisplayDto;
import com.acme.jga.ports.services.api.tenant.ITenantPortService;
import com.acme.jga.ports.services.impl.AbstractPortService;
import com.acme.jga.ports.validation.tenants.TenantsValidationEngine;
import com.acme.jga.utils.lambdas.StreamUtil;
import com.acme.jga.validation.ValidationException;
import com.acme.jga.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantPortService extends AbstractPortService implements ITenantPortService {
    private static final String INSTRUMENTATION_NAME = TenantPortService.class.getCanonicalName();
    private final TenantsPortConverter tenantsConverter;
    private final TenantCreate tenantCreate;
    private final TenantFind tenantFind;
    private final TenantList tenantList;
    private final TenantUpdate tenantUpdate;
    private final TenantDelete tenantDelete;
    private final TenantsValidationEngine tenantsValidationEngine;

    @Autowired
    public TenantPortService(TenantsPortConverter tenantsConverter, TenantCreate tenantCreate, TenantsValidationEngine tenantsValidationEngine,
                             OpenTelemetryWrapper openTelemetryWrapper, TenantFind tenantFind, TenantList tenantList,
                             TenantUpdate tenantUpdate, TenantDelete tenantDelete) {
        super(openTelemetryWrapper);
        this.tenantsConverter = tenantsConverter;
        this.tenantCreate = tenantCreate;
        this.tenantFind = tenantFind;
        this.tenantList = tenantList;
        this.tenantUpdate = tenantUpdate;
        this.tenantDelete = tenantDelete;
        this.tenantsValidationEngine = tenantsValidationEngine;
    }

    /**
     * @inheritDoc
     */
    @Override
    public UidDto createTenant(TenantDto tenantDto) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_TENANTS_CREATE", null, (span) -> {
            ValidationResult validationResult = tenantsValidationEngine.validate(tenantDto);
            if (!validationResult.isSuccess()) {
                throw new ValidationException(validationResult.getErrors());
            }
            Tenant tenant = tenantsConverter.tenantDtoToDomainTenant(tenantDto);
            CompositeId compositeId = tenantCreate.execute(tenant, null);
            return new UidDto(compositeId.getUid());
        });
    }

    @Override
    public TenantDisplayDto findTenantByUid(String uid) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_TENANTS_FIND_UID", null, (span) -> {
            Tenant tenant = tenantFind.byUid(uid, span);
            TenantDisplayDto tenantDisplayDto = tenantsConverter.tenantDomainToDisplay(tenant);
            return tenantDisplayDto;
        });
    }

    @Override
    public TenantListDisplayDto findAllTenants() {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_TENANTS_FIND_ALL", null, (span) -> {
            List<Tenant> tenants = tenantList.execute(span);
            List<TenantDisplayDto> tenantDisplayDtos = StreamUtil.ofNullableList(tenants).map(tenant -> tenantsConverter.tenantDomainToDisplay(tenant)).toList();
            return new TenantListDisplayDto(tenantDisplayDtos);
        });
    }

    @Override
    public Integer updateTenant(String uid, TenantDto tenantDto) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_TENANTS_UPDATE", null, (span) -> {
            Tenant tenant = tenantsConverter.tenantDtoToDomainTenant(tenantDto);
            tenant.setUid(uid);
            return tenantUpdate.execute(tenant, span);
        });
    }

    @Override
    public Integer deleteTenant(String tenantUid) {
        return processWithSpan(INSTRUMENTATION_NAME, "PORT_TENANTS_DELETE", null, (span) -> tenantDelete.execute(tenantUid, span));
    }

}
