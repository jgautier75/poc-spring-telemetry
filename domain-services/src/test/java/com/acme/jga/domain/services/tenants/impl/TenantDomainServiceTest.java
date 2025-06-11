package com.acme.jga.domain.services.tenants.impl;

import com.acme.jga.domain.events.EventBuilderTenant;
import com.acme.jga.domain.functions.tenants.impl.TenantCreateImpl;
import com.acme.jga.domain.functions.tenants.impl.TenantDeleteImpl;
import com.acme.jga.domain.functions.tenants.impl.TenantFindImpl;
import com.acme.jga.domain.functions.tenants.impl.TenantUpdateImpl;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.services.utils.VoidSpan;
import com.acme.jga.infra.services.impl.events.EventsInfraServiceImpl;
import com.acme.jga.infra.services.impl.tenants.TenantInfraServiceImpl;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.impl.LogService;
import com.acme.jga.logging.services.impl.LoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.integration.channel.PublishSubscribeChannel;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class TenantDomainServiceTest {
    @Mock
    OpenTelemetryWrapper openTelemetryWrapper;
    @Mock
    LogService logService;
    @Mock
    MessageSource messageSource;
    @Mock
    TenantInfraServiceImpl tenantInfraServiceImpl;
    @Mock
    LoggingFacade loggingFacade;
    @Mock
    EventsInfraServiceImpl eventsInfraServiceImpl;
    @Mock
    EventBuilderTenant eventBuilderTenant;
    @Mock
    PublishSubscribeChannel eventAuditChannel;
    @Mock
    BundleFactory bundleFactory;
    @Mock
    TenantFindImpl tenantFind;
    @InjectMocks
    TenantCreateImpl tenantCreate;
    @InjectMocks
    TenantUpdateImpl tenantUpdate;
    @InjectMocks
    TenantDeleteImpl tenantDelete;

    @Test
    public void createTenantNominal() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        CompositeId cid = CompositeId.builder().id(1L).uid(UUID.randomUUID().toString()).build();

        // WHEN
        Mockito.when(tenantInfraServiceImpl.tenantExistsByCode(Mockito.anyString())).thenReturn(false);
        Mockito.when(tenantInfraServiceImpl.createTenant(Mockito.any())).thenReturn(cid);
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());
        Mockito.when(eventsInfraServiceImpl.createEvent(Mockito.any())).thenReturn(UUID.randomUUID().toString());

        // THEN
        CompositeId compositeId = tenantCreate.execute(tenant);
        assertNotNull("Composite id not null", compositeId);
    }

    @Test
    public void createTenantCodeExists() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();

        // WHEN
        Mockito.when(tenantInfraServiceImpl.tenantExistsByCode(Mockito.anyString())).thenReturn(true);
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        assertThrows(WrappedFunctionalException.class, () -> tenantCreate.execute(tenant));
    }

    @Test
    public void updateTenantNominal() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();

        // WHEN
        Mockito.when(tenantFind.byUid(Mockito.any())).thenReturn(tenant);
        Mockito.when(tenantInfraServiceImpl.updateTenant(Mockito.any())).thenReturn(1);
        Mockito.when(eventsInfraServiceImpl.createEvent(Mockito.any())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        Integer nbUpdated = tenantUpdate.execute(tenant);
        assertEquals(1L, nbUpdated.longValue());
    }

    @Test
    public void deleteTenant() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();

        // WHEN
        Mockito.when(tenantFind.byUid(Mockito.any())).thenReturn(tenant);
        Mockito.when(tenantInfraServiceImpl.deleteUsersByTenantId(Mockito.any())).thenReturn(1);
        Mockito.when(tenantInfraServiceImpl.deleteSectorsByTenantId(Mockito.any())).thenReturn(1);
        Mockito.when(tenantInfraServiceImpl.deleteOrganizationsByTenantId(Mockito.any())).thenReturn(1);
        Mockito.when(tenantInfraServiceImpl.deleteTenant(Mockito.any())).thenReturn(1);
        Mockito.when(eventsInfraServiceImpl.createEvent(Mockito.any())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        Integer nbDeleted = tenantDelete.execute(tenant.getUid());
        assertEquals(1L, nbDeleted.longValue());
    }

    /**
     * Mock tenant.
     *
     * @return Tenant
     */
    private Tenant mockTenant() {
        return Tenant.builder()
                .code("code")
                .id(1L)
                .label("label")
                .uid(UUID.randomUUID().toString())
                .build();
    }

}
