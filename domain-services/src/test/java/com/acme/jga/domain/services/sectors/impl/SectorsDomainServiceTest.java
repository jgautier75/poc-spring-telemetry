package com.acme.jga.domain.services.sectors.impl;

import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.*;
import com.acme.jga.domain.services.organizations.impl.OrganizationsDomainService;
import com.acme.jga.domain.services.tenants.impl.TenantDomainService;
import com.acme.jga.domain.services.utils.VoidSpan;
import com.acme.jga.infra.services.impl.events.EventsInfraService;
import com.acme.jga.infra.services.impl.sectors.SectorsInfraService;
import com.acme.jga.logging.bundle.BundleFactory;
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class SectorsDomainServiceTest {
    @Mock
    TenantDomainService tenantDomainService;
    @Mock
    OrganizationsDomainService organizationsDomainService;
    @Mock
    SectorsInfraService sectorsInfraService;
    @Mock
    MessageSource messageSource;
    @Mock
    EventsInfraService eventsInfraService;
    @Mock
    OpenTelemetryWrapper openTelemetryWrapper;
    @Mock
    BundleFactory bundleFactory;
    @Mock
    PublishSubscribeChannel eventAuditChannel;
    @InjectMocks
    SectorsDomainService sectorsDomainService;

    @Test
    public void createSectorNominal() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        Organization organization = mockOrganization(tenant);
        CompositeId compositeId = CompositeId.builder().id(1L).uid(UUID.randomUUID().toString()).build();
        Sector sector = Sector.builder().code("scode").id(1L).label("slabel").orgId(organization.getId())
                .root(false).tenantId(tenant.getId()).uid(UUID.randomUUID().toString()).build();

        // WHEN
        Mockito.when(tenantDomainService.findTenantByUid(Mockito.any(), Mockito.any())).thenReturn(tenant);
        Mockito.when(organizationsDomainService.findOrganizationByTenantAndUid(Mockito.any(), Mockito.any(),
                        Mockito.anyBoolean(), Mockito.any()))
                .thenReturn(organization);
        Mockito.when(sectorsInfraService.existsByCode(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(sectorsInfraService.findSectorByUid(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(Optional.of(sector));
        Mockito.when(sectorsInfraService.createSector(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(compositeId);
        Mockito.when(eventsInfraService.createEvent(Mockito.any(), Mockito.any())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        CompositeId sectorCompId = sectorsDomainService.createSector(tenant.getUid(), organization.getUid(), sector, null);
        assertNotNull(sectorCompId);
    }

    @Test
    public void createSectorTenantNotFound() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        Organization organization = mockOrganization(tenant);
        Sector sector = Sector.builder().code("scode").id(1L).label("slabel").orgId(organization.getId())
                .root(false).tenantId(tenant.getId()).uid(UUID.randomUUID().toString()).build();

        // WHEN
        Mockito.when(tenantDomainService.findTenantByUid(Mockito.any(), Mockito.any()))
                .thenThrow(new WrappedFunctionalException(new FunctionalException(FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), null,
                        FunctionalErrorsTypes.TENANT_NOT_FOUND.name())));
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        assertThrows(WrappedFunctionalException.class,
                () -> sectorsDomainService.createSector(tenant.getUid(), organization.getUid(), sector, null));
    }

    @Test
    public void createSectorNoOrganization() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        Organization organization = mockOrganization(tenant);
        Sector sector = Sector.builder().code("scode").id(1L).label("slabel").orgId(organization.getId())
                .root(false).tenantId(tenant.getId()).uid(UUID.randomUUID().toString()).build();

        // WHEN
        Mockito.when(tenantDomainService.findTenantByUid(Mockito.any(), Mockito.any())).thenReturn(tenant);
        Mockito.when(organizationsDomainService.findOrganizationByTenantAndUid(Mockito.any(), Mockito.any(),
                        Mockito.anyBoolean(), Mockito.any()))
                .thenThrow(new WrappedFunctionalException(new FunctionalException(FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), null,
                        FunctionalErrorsTypes.TENANT_NOT_FOUND.name())));
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        assertThrows(WrappedFunctionalException.class,
                () -> sectorsDomainService.createSector(tenant.getUid(), organization.getUid(), sector, null));
    }

    private Tenant mockTenant() {
        return Tenant.builder()
                .code("tcode")
                .id(1L)
                .label("tlabel")
                .uid(UUID.randomUUID().toString()).build();
    }

    private Organization mockOrganization(Tenant tenant) {
        OrganizationCommons organizationCommons = OrganizationCommons.builder().code("ren").country("fr")
                .kind(OrganizationKind.COMMUNITY).label("Rennes").status(OrganizationStatus.ACTIVE)
                .build();
        return Organization.builder().commons(organizationCommons).tenantId(tenant.getId()).build();
    }

}
