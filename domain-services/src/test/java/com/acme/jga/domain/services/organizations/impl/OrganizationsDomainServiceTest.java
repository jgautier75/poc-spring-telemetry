package com.acme.jga.domain.services.organizations.impl;

import com.acme.jga.domain.events.EventBuilderOrganization;
import com.acme.jga.domain.functions.organizations.api.OrganizationCreate;
import com.acme.jga.domain.functions.organizations.api.OrganizationDelete;
import com.acme.jga.domain.functions.organizations.api.OrganizationUpdate;
import com.acme.jga.domain.functions.organizations.impl.OrganizationCreateImpl;
import com.acme.jga.domain.functions.organizations.impl.OrganizationDeleteImpl;
import com.acme.jga.domain.functions.organizations.impl.OrganizationUpdateImpl;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.*;
import com.acme.jga.domain.services.utils.VoidSpan;
import com.acme.jga.infra.services.impl.events.EventsInfraService;
import com.acme.jga.infra.services.impl.organizations.OrganizationsInfraService;
import com.acme.jga.infra.services.impl.sectors.SectorsInfraService;
import com.acme.jga.logging.services.impl.LogService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.integration.channel.PublishSubscribeChannel;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationsDomainServiceTest {
    @RegisterExtension
    static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();
    @Mock
    OrganizationsInfraService organizationsInfraService;
    @Mock
    TenantFind tenantFind;
    @Mock
    MessageSource messageSource;
    @Mock
    LogService logService;
    @Mock
    SectorsInfraService sectorsInfraService;
    @Mock
    EventsInfraService eventsInfraService;
    @Mock
    PublishSubscribeChannel eventAuditChannel;
    @Mock
    EventBuilderOrganization builderOrganization;
    @Mock
    OpenTelemetryWrapper openTelemetryWrapper;
    @InjectMocks
    OrganizationCreateImpl organizationCreate;
    @InjectMocks
    OrganizationUpdateImpl organizationUpdate;
    @InjectMocks
    OrganizationDeleteImpl organizationDelete;

    @Before
    public void init() {
        openTelemetryWrapper.setSdkTracerProvider(otelTesting.getOpenTelemetry().getTracerProvider());
    }

    @Test
    public void createOrganizationNominal() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        CompositeId compositeId = CompositeId.builder().id(1L).uid(UUID.randomUUID().toString()).build();
        OrganizationCommons organizationCommons = OrganizationCommons.builder().code("ren").country("fr")
                .kind(OrganizationKind.COMMUNITY).label("Rennes").status(OrganizationStatus.ACTIVE)
                .build();
        Organization organization = Organization.builder().commons(organizationCommons).tenantId(tenant.getId())
                .build();


        // WHEN
        Mockito.when(tenantFind.byUid(Mockito.any(), Mockito.any())).thenReturn(tenant);
        Mockito.when(organizationsInfraService.codeAlreadyUsed(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(organizationsInfraService.createOrganization(Mockito.any(), Mockito.any())).thenReturn(compositeId);
        Mockito.when(eventsInfraService.createEvent(Mockito.any(), Mockito.any())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(sectorsInfraService.createSector(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(compositeId);
        Mockito.when(eventsInfraService.createEvent(Mockito.any(), Mockito.any())).thenReturn(UUID.randomUUID().toString());
        Span rootSpan = otelTesting.getOpenTelemetry().getTracer("test").spanBuilder("test").startSpan();
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());
        // THEN
        CompositeId orgCompositeId = organizationCreate.execute(tenant.getUid(),
                organization, rootSpan);
        assertNotNull("Organization not null", orgCompositeId);
    }

    @Test
    public void createOrganizationNoTenant() throws FunctionalException {
        // GIVEN
        Organization organization = mockOrganization();
        Span rootSpan = otelTesting.getOpenTelemetry().getTracer("test").spanBuilder("test").startSpan();

        // WHEN
        Mockito.when(tenantFind.byUid(Mockito.any(), Mockito.any())).thenThrow(new WrappedFunctionalException(new FunctionalException(
                FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), null,
                FunctionalErrorsTypes.TENANT_NOT_FOUND.name())));
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        assertThrows(WrappedFunctionalException.class,
                () -> organizationCreate.execute(UUID.randomUUID().toString(),
                        organization, rootSpan));

    }

    @Test
    public void updateOrganizationNominal() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        Organization organization = mockOrganization();

        // WHEN
        Mockito.when(tenantFind.byUid(Mockito.any(), Mockito.any())).thenReturn(tenant);
        Mockito.when(organizationsInfraService.findOrganizationByUid(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Optional.of(organization));
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());
        // THEN
        Integer nbUpdated = organizationUpdate.execute(tenant.getUid(), organization.getUid(), organization, new VoidSpan());
        assertEquals(Integer.valueOf(0), nbUpdated);
    }

    @Test
    public void updateOrganizationNoTenant() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        Organization organization = mockOrganization();

        // WHEN
        Mockito.when(tenantFind.byUid(Mockito.any(), Mockito.any())).thenThrow(
                new WrappedFunctionalException(
                        new FunctionalException(
                                FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), null,
                                FunctionalErrorsTypes.TENANT_NOT_FOUND.name())));
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        assertThrows(WrappedFunctionalException.class, () -> organizationUpdate.execute(tenant.getUid(), organization.getUid(), organization, null));
    }

    @Test
    public void deleteNominal() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        Organization organization = mockOrganization();

        // WHEN
        Mockito.when(tenantFind.byUid(Mockito.any(), Mockito.any())).thenReturn(tenant);
        Mockito.when(organizationsInfraService.findOrganizationByUid(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Optional.of(organization));
        Mockito.when(organizationsInfraService.deleteUsersByOrganization(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(organizationsInfraService.deleteSectors(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(organizationsInfraService.deleteById(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(eventsInfraService.createEvent(Mockito.any(), Mockito.any())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        Integer nbDeleted = organizationDelete.execute(tenant.getUid(), organization.getUid(), null);
        assertNotNull("NbDelete", nbDeleted);
    }

    private Tenant mockTenant() {
        return Tenant.builder()
                .code("tenant")
                .id(1L)
                .label("label")
                .uid(UUID.randomUUID().toString())
                .build();
    }

    private Organization mockOrganization() {
        Tenant tenant = mockTenant();
        OrganizationCommons organizationCommons = OrganizationCommons.builder().code("ren").country("fr")
                .kind(OrganizationKind.COMMUNITY).label("Rennes").status(OrganizationStatus.ACTIVE)
                .build();
        return Organization.builder().commons(organizationCommons).tenantId(tenant.getId()).build();
    }

}
