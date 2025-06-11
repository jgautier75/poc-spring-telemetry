package com.acme.jga.domain.services.sectors.impl;

import com.acme.jga.domain.functions.organizations.api.OrganizationFind;
import com.acme.jga.domain.functions.sectors.impl.SectorCreateImpl;
import com.acme.jga.domain.functions.sectors.impl.SectorFindImpl;
import com.acme.jga.domain.functions.tenants.api.TenantFind;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.*;
import com.acme.jga.domain.services.utils.VoidSpan;
import com.acme.jga.infra.services.impl.events.EventsInfraServiceImpl;
import com.acme.jga.infra.services.impl.sectors.SectorsInfraServiceImpl;
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
    TenantFind tenantFind;
    @Mock
    OrganizationFind organizationFind;
    @Mock
    SectorsInfraServiceImpl sectorsInfraServiceImpl;
    @Mock
    MessageSource messageSource;
    @Mock
    EventsInfraServiceImpl eventsInfraServiceImpl;
    @Mock
    OpenTelemetryWrapper openTelemetryWrapper;
    @Mock
    BundleFactory bundleFactory;
    @Mock
    PublishSubscribeChannel eventAuditChannel;
    @Mock
    SectorFindImpl sectorFind;
    @InjectMocks
    SectorCreateImpl sectorCreate;

    @Test
    public void createSectorNominal() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        Organization organization = mockOrganization(tenant);
        CompositeId compositeId = CompositeId.builder().id(1L).uid(UUID.randomUUID().toString()).build();
        String parentUUID = UUID.randomUUID().toString();
        Sector parentSector = Sector.builder().code("pscode").id(2L).label("pslabel").orgId(organization.getId())
                .root(true).tenantId(tenant.getId()).uid(parentUUID).build();

        Sector sector = Sector.builder().code("scode").id(1L).label("slabel").orgId(organization.getId())
                .root(false).tenantId(tenant.getId()).parentUid(parentUUID).build();

        // WHEN
        Mockito.when(tenantFind.byUid(Mockito.any())).thenReturn(tenant);
        Mockito.when(organizationFind.byTenantIdAndUid(Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
                .thenReturn(organization);
        Mockito.when(sectorsInfraServiceImpl.existsByCode(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(sectorFind.byTenantOrgAndUid(Mockito.anyString(), Mockito.any(), Mockito.eq(parentUUID))).thenReturn(parentSector);
        Mockito.when(sectorsInfraServiceImpl.createSector(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(compositeId);
        Mockito.when(eventsInfraServiceImpl.createEvent(Mockito.any())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        CompositeId sectorCompId = sectorCreate.execute(tenant.getUid(), organization.getUid(), sector);
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
        Mockito.when(tenantFind.byUid(Mockito.any()))
                .thenThrow(new WrappedFunctionalException(new FunctionalException(FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), null,
                        FunctionalErrorsTypes.TENANT_NOT_FOUND.name())));
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        assertThrows(WrappedFunctionalException.class, () -> sectorCreate.execute(tenant.getUid(), organization.getUid(), sector));
    }

    @Test
    public void createSectorNoOrganization() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        Organization organization = mockOrganization(tenant);
        Sector sector = Sector.builder().code("scode").id(1L).label("slabel").orgId(organization.getId())
                .root(false).tenantId(tenant.getId()).uid(UUID.randomUUID().toString()).build();

        // WHEN
        Mockito.when(tenantFind.byUid(Mockito.any())).thenReturn(tenant);
        Mockito.when(organizationFind.byTenantIdAndUid(Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
                .thenThrow(new WrappedFunctionalException(new FunctionalException(FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), null,
                        FunctionalErrorsTypes.TENANT_NOT_FOUND.name())));
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        assertThrows(WrappedFunctionalException.class, () -> sectorCreate.execute(tenant.getUid(), organization.getUid(), sector));
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
