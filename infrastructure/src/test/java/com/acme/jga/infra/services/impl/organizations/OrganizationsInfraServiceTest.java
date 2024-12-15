package com.acme.jga.infra.services.impl.organizations;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.Organization;
import com.acme.jga.domain.model.v1.OrganizationCommons;
import com.acme.jga.domain.model.v1.OrganizationKind;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.infra.converters.OrganizationsInfraConverter;
import com.acme.jga.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.jga.infra.dto.organizations.v1.OrganizationDb;
import com.acme.jga.infra.utils.VoidSpan;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationsInfraServiceTest {
    private static final Long TENANT_ID = 1L;
    @InjectMocks
    private OrganizationsInfraService organizationsInfraService;
    @Mock
    private OrganizationsInfraConverter organizationsInfraConverter;
    @Mock
    private IOrganizationsDao organizationsDao;
    @Mock
    private OpenTelemetryWrapper openTelemetryWrapper;

    @Test
    public void createOrganization() {
        // GIVEN
        OrganizationCommons organizationCommons = OrganizationCommons.builder()
                .code("test-code")
                .country("fr")
                .kind(OrganizationKind.TENANT)
                .label("org-create")
                .status(OrganizationStatus.ACTIVE)
                .build();
        Organization org = Organization.builder()
                .tenantId(1L)
                .commons(organizationCommons)
                .uid(UUID.randomUUID().toString())
                .build();
        OrganizationDb orgDb = mockOrganizationDb();
        CompositeId compositeId = CompositeId.builder().id(2L).uid(UUID.randomUUID().toString()).build();

        // WHEN
        Mockito.when(organizationsInfraConverter.convertOrganizationToOrganizationDb(Mockito.any())).thenReturn(orgDb);
        Mockito.when(organizationsDao.createOrganization(Mockito.any())).thenReturn(compositeId);
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        CompositeId id = organizationsInfraService.createOrganization(org, null);
        assertNotNull("CompositeId not null", id);
    }

    @Test
    public void filterOrganizations() {
        // GIVEN
        OrganizationDb orgDb = mockOrganizationDb();
        List<OrganizationDb> organizationDbs = List.of(orgDb);
        PaginatedResults<OrganizationDb> paginatedResults = new PaginatedResults<>(organizationDbs.size(), 10, organizationDbs, 0, 0);
        Organization org = mockOrganization();

        // WHEN
        Mockito.when(organizationsDao.filterOrganizations(Mockito.any(), Mockito.any())).thenReturn(paginatedResults);
        Mockito.when(organizationsInfraConverter.convertOrganizationDbToOrganization(Mockito.any())).thenReturn(org);
        Mockito.when(openTelemetryWrapper.withSpan(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new VoidSpan());

        // THEN
        PaginatedResults<Organization> orgs = organizationsInfraService.filterOrganizations(TENANT_ID, Span.current(), Collections.emptyMap());
        assertNotNull("Organizations list", orgs);
    }

    /**
     * Mock organization.
     *
     * @return Organization
     */
    private Organization mockOrganization() {
        OrganizationCommons organizationCommons = OrganizationCommons.builder()
                .code("test-code")
                .country("fr")
                .kind(OrganizationKind.TENANT)
                .label("org-create")
                .status(OrganizationStatus.ACTIVE)
                .build();
        return Organization.builder()
                .tenantId(TENANT_ID)
                .commons(organizationCommons)
                .uid(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Mock organization Db.
     *
     * @return Organization db
     */
    private OrganizationDb mockOrganizationDb() {
        return OrganizationDb.builder()
                .code("test-code")
                .country("fr")
                .kind(OrganizationKind.TENANT)
                .label("org-label")
                .status(OrganizationStatus.ACTIVE)
                .tenantId(TENANT_ID)
                .build();
    }

}
