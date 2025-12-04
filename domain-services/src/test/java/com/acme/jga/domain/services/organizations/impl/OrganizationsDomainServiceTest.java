package com.acme.jga.domain.services.organizations.impl;

import com.acme.jga.domain.events.EventBuilderOrganization;
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
import com.acme.jga.infra.services.impl.events.EventsInfraServiceImpl;
import com.acme.jga.infra.services.impl.organizations.OrganizationsInfraServiceImpl;
import com.acme.jga.infra.services.impl.sectors.SectorsInfraServiceImpl;
import com.acme.jga.logging.services.impl.LogService;
import com.acme.jga.logging.services.impl.LoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.integration.channel.PublishSubscribeChannel;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.*;

@ExtendWith(MockitoExtension.class)
public class OrganizationsDomainServiceTest {
    @RegisterExtension
    static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();
    @Mock
    OrganizationsInfraServiceImpl organizationsInfraServiceImpl;
    @Mock
    TenantFind tenantFind;
    @Mock
    MessageSource messageSource;
    @Mock
    LoggingFacade loggingFacade;
    @Mock
    LogService logService;
    @Mock
    SectorsInfraServiceImpl sectorsInfraServiceImpl;
    @Mock
    EventsInfraServiceImpl eventsInfraServiceImpl;
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

    @BeforeEach
    public void init() {
        openTelemetryWrapper.setSdkTracerProvider(otelTesting.getOpenTelemetry().getTracerProvider());
    }

    @org.junit.jupiter.api.Test
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
        Mockito.when(organizationsInfraServiceImpl.codeAlreadyUsed(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(organizationsInfraServiceImpl.createOrganization(Mockito.any(), Mockito.any())).thenReturn(compositeId);
        Mockito.when(eventsInfraServiceImpl.createEvent(Mockito.any(), Mockito.any())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(sectorsInfraServiceImpl.createSector(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(compositeId);
        Mockito.when(eventsInfraServiceImpl.createEvent(Mockito.any(), Mockito.any())).thenReturn(UUID.randomUUID().toString());
        Span rootSpan = otelTesting.getOpenTelemetry().getTracer("test").spanBuilder("test").startSpan();
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());
        // THEN
        CompositeId orgCompositeId = organizationCreate.execute(tenant.getUid(),
                organization, rootSpan);
        Assertions.assertNotNull(orgCompositeId, "Organization not null");
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
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        Assertions.assertThrows(WrappedFunctionalException.class,
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
        Mockito.when(organizationsInfraServiceImpl.findOrganizationByUid(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Optional.of(organization));
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());
        // THEN
        Integer nbUpdated = organizationUpdate.execute(tenant.getUid(), organization.getUid(), organization, new VoidSpan());
        Assertions.assertEquals(Integer.valueOf(0), nbUpdated);
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
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        Assertions.assertThrows(WrappedFunctionalException.class, () -> organizationUpdate.execute(tenant.getUid(), organization.getUid(), organization, null));
    }

    @Test
    public void deleteNominal() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        Organization organization = mockOrganization();

        // WHEN
        Mockito.when(tenantFind.byUid(Mockito.any(), Mockito.any())).thenReturn(tenant);
        Mockito.when(organizationsInfraServiceImpl.findOrganizationByUid(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Optional.of(organization));
        Mockito.when(organizationsInfraServiceImpl.deleteUsersByOrganization(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(organizationsInfraServiceImpl.deleteSectors(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(organizationsInfraServiceImpl.deleteById(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(eventsInfraServiceImpl.createEvent(Mockito.any(), Mockito.any())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        Integer nbDeleted = organizationDelete.execute(tenant.getUid(), organization.getUid(), null);
        Assertions.assertNotNull(nbDeleted, "NbDelete");
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
