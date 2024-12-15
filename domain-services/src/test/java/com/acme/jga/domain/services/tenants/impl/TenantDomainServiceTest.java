package com.acme.jga.domain.services.tenants.impl;

import com.acme.jga.domain.events.EventBuilderTenant;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Tenant;
import com.acme.jga.domain.services.utils.VoidSpan;
import com.acme.jga.infra.services.impl.events.EventsInfraService;
import com.acme.jga.infra.services.impl.tenants.TenantInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
import com.acme.jga.logging.services.impl.LogService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.integration.channel.PublishSubscribeChannel;

import java.util.Optional;
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
    TenantInfraService tenantInfraService;
    @Mock
    EventsInfraService eventsInfraService;
    @Mock
    EventBuilderTenant eventBuilderTenant;
    @Mock
    PublishSubscribeChannel eventAuditChannel;
    @Mock
    BundleFactory bundleFactory;
    @InjectMocks
    TenantDomainService tenantDomainService;

    @Test
    public void createTenantNominal() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        CompositeId cid = CompositeId.builder().id(1L).uid(UUID.randomUUID().toString()).build();

        // WHEN
        Mockito.when(tenantInfraService.tenantExistsByCode(Mockito.anyString(), Mockito.any())).thenReturn(false);
        Mockito.when(tenantInfraService.createTenant(Mockito.any(), Mockito.any())).thenReturn(cid);
        Mockito.doNothing().when(logService).infoS(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(logService).debugS(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());
        Mockito.when(eventsInfraService.createEvent(Mockito.any(), Mockito.any())).thenReturn(UUID.randomUUID().toString());

        // THEN
        CompositeId compositeId = tenantDomainService.createTenant(tenant, null);
        assertNotNull("Composite id not null", compositeId);
    }

    @Test
    public void createTenantCodeExists() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();

        // WHEN
        Mockito.when(tenantInfraService.tenantExistsByCode(Mockito.anyString(), Mockito.any())).thenReturn(true);
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        assertThrows(WrappedFunctionalException.class, () -> tenantDomainService.createTenant(tenant, null));
    }

    @Test
    public void updateTenantNominal() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();

        // WHEN
        Mockito.doNothing().when(logService).infoS(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(logService).debugS(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.when(tenantInfraService.findTenantByUid(Mockito.any(), Mockito.any())).thenReturn(Optional.of(tenant));
        Mockito.when(tenantInfraService.updateTenant(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(eventsInfraService.createEvent(Mockito.any(), Mockito.any())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        Integer nbUpdated = tenantDomainService.updateTenant(tenant, null);
        assertEquals(1L, nbUpdated.longValue());
    }

    @Test
    public void deleteTenant() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();

        // WHEN
        Mockito.when(tenantInfraService.findTenantByUid(Mockito.any(), Mockito.any())).thenReturn(Optional.of(tenant));
        Mockito.doNothing().when(logService).infoS(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(logService).debugS(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.when(tenantInfraService.deleteUsersByTenantId(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(tenantInfraService.deleteSectorsByTenantId(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(tenantInfraService.deleteOrganizationsByTenantId(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(tenantInfraService.deleteTenant(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(eventsInfraService.createEvent(Mockito.any(), Mockito.any())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        Integer nbDeleted = tenantDomainService.deleteTenant(UUID.randomUUID().toString(), null);
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
