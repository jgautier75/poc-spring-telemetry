package com.acme.jga.infra.services.impl.tenants;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.infra.converters.TenantsInfraConverter;
import com.acme.jga.infra.dao.api.tenants.TenantsDao;
import com.acme.jga.infra.dto.tenants.v1.TenantDb;
import com.acme.jga.infra.services.api.tenants.TenantInfraService;
import com.acme.jga.infra.services.impl.AbstractInfraService;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TenantInfraServiceImpl extends AbstractInfraService implements TenantInfraService {
    private static final String INSTRUMENTATION_NAME = TenantInfraServiceImpl.class.getCanonicalName();
    private final TenantsDao tenantsDao;
    private final TenantsInfraConverter tenantsInfraConverter;
    private final ILoggingFacade loggingFacade;

    @Autowired
    public TenantInfraServiceImpl(TenantsDao tenantsDao, TenantsInfraConverter tenantsInfraConverter, ILoggingFacade loggingFacade, OpenTelemetryWrapper openTelemetryWrapper) {
        super(openTelemetryWrapper);
        this.loggingFacade = loggingFacade;
        this.tenantsDao = tenantsDao;
        this.tenantsInfraConverter = tenantsInfraConverter;
    }

    @Override
    public CompositeId createTenant(Tenant tenant, Span parentSpan) {
        return tenantsDao.createTenant(tenant.getCode(), tenant.getLabel());
    }

    @Override
    public Optional<Tenant> findTenantByUid(String uid, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_TENANT_FIND_BY_UID", parentSpan, (span) -> {
            Optional<TenantDb> tenantDb = tenantsDao.findByUid(uid);
            return tenantDb.map(tenantsInfraConverter::tenantDbToTenantDomain);
        });
    }

    @Override
    public boolean tenantExistsByCode(String code, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_TENANT_EXISTS_BY_CODE", parentSpan,
                (span) -> tenantsDao.existsByCode(code));
    }

    @Override
    public List<Tenant> findAllTenants(Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_TENANT_FIND_ALL", parentSpan, (span) -> {
            List<TenantDb> tenantDbs = tenantsDao.findAllTenants();
            return tenantDbs.stream().map(tenantsInfraConverter::tenantDbToTenantDomain).toList();
        });
    }

    @Override
    public Integer updateTenant(Tenant tenant, Span parentSpan) {
        loggingFacade.debugS(this.getClass().getCanonicalName() + "-updateTenant", "tenant " + tenant.getCode() + " updated", null);
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_TENANT_UPDATE", parentSpan, (span) ->
                tenantsDao.updateTenant(tenant.getId(), tenant.getCode(), tenant.getLabel())
        );
    }

    @Override
    public Integer deleteUsersByTenantId(Long tenantId, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_TENANT_DELETE_USERS", parentSpan, (span) ->
                tenantsDao.deleteUsersByTenantId(tenantId)
        );
    }

    @Override
    public Integer deleteOrganizationsByTenantId(Long tenantId, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_TENANT_DELETE_ORG", parentSpan, (span) ->
                tenantsDao.deleteOrganizationsByTenantId(tenantId)
        );
    }

    @Override
    public Integer deleteTenant(Long tenantId, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_TENANT_DELETE_TENANT", parentSpan, (span) ->
                tenantsDao.deleteTenant(tenantId)
        );
    }

    @Override
    public Integer deleteSectorsByTenantId(Long tenantId, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "INFRA_TENANT_DELETE_SECTORS", parentSpan, (span) ->
                tenantsDao.deleteSectorsByTenantId(tenantId)
        );
    }

}
