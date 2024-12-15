package com.acme.jga.infra.dao.impl.organizations;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.OrganizationKind;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.jga.infra.dao.api.tenants.ITenantsDao;
import com.acme.jga.infra.dao.config.DaoTestConfig;
import com.acme.jga.infra.dao.config.DatabaseTestConfig;
import com.acme.jga.infra.dao.utils.DaoTestUtils;
import com.acme.jga.infra.dto.organizations.v1.OrganizationDb;
import com.acme.jga.utils.test.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = {DatabaseTestConfig.class, DaoTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
@ContextConfiguration(initializers = OrganizationsDaoTest.DataSourceInitializer.class)
class OrganizationsDaoTest {

    public static class DataSourceInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.test.database.replace=none",
                    "spring.datasource.url=" + database.getJdbcUrl(),
                    "spring.datasource.username=" + database.getUsername(),
                    "spring.datasource.password=" + database.getPassword());
        }
    }

    @Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>(TestUtils.POSTGRESQL_VERSION)
            .waitingFor(Wait.defaultWaitStrategy());

    @Autowired
    ITenantsDao tenantsDao;

    @Autowired
    IOrganizationsDao organizationDao;

    @BeforeEach
    void initDb() throws Exception {
        DaoTestUtils.performLiquibaseUpdate(database.getJdbcUrl(), database.getUsername(),
                database.getPassword());
    }

    @Test
    void createOrganization() {

        // Create tenant
        CompositeId tenantCompositeId = tenantsDao.createTenant("001", "root");

        // Create organization
        OrganizationDb organizationDb = OrganizationDb.builder()
                .country("fr")
                .kind(OrganizationKind.TENANT)
                .label("test")
                .status(OrganizationStatus.ACTIVE)
                .code("002")
                .tenantId(tenantCompositeId.getId())
                .build();
        CompositeId compositeId = organizationDao.createOrganization(organizationDb);
        assertNotNull("Organization composite id not null", compositeId);
        assertNotNull("Organization id not null", compositeId.getId());
        assertNotNull("Organization uid not null", compositeId.getUid());

        // Find by id
        OrganizationDb organizationRdbms = organizationDao.findOrganizationByTenantAndId(
                tenantCompositeId.getId(),
                compositeId.getId());
        assertNotNull("Organization by id: not null", organizationRdbms);
        assertEquals("Code match", organizationDb.getCode(), organizationRdbms.getCode());
        assertEquals("Country match", organizationDb.getCountry(), organizationRdbms.getCountry());
        assertEquals("Kind match", organizationDb.getKind(), organizationRdbms.getKind());
        assertEquals("Label match", organizationDb.getLabel(), organizationRdbms.getLabel());
        assertEquals("Status match", organizationDb.getStatus(), organizationRdbms.getStatus());

        // Find by uid
        OrganizationDb organizationByUid = organizationDao.findOrganizationByTenantAndUid(
                tenantCompositeId.getId(),
                compositeId.getUid());
        assertNotNull("Organization by uid not null", organizationByUid);

        // Update organization
        organizationDb.setCountry("de");
        organizationDb.setCode("003");
        organizationDb.setLabel("otest");
        organizationDb.setStatus(OrganizationStatus.INACTIVE);
        Integer nbRowsUpdated = organizationDao.updateOrganization(tenantCompositeId.getId(),
                compositeId.getId(),
                organizationDb.getCode(),
                organizationDb.getLabel(), organizationDb.getCountry(), organizationDb.getStatus());
        assertEquals("1 row updated", (Integer) 1, nbRowsUpdated);

        organizationRdbms = organizationDao.findOrganizationByTenantAndId(tenantCompositeId.getId(),
                compositeId.getId());
        assertEquals("Country match", organizationDb.getCountry(), organizationDb.getCountry());
        assertEquals("Code match", organizationDb.getCode(), organizationDb.getCode());
        assertEquals("Label match", organizationDb.getLabel(), organizationDb.getLabel());
        assertEquals("Status match", organizationDb.getStatus(), organizationDb.getStatus());

        // Delete organization
        Integer nbDeleted = organizationDao.deleteOrganization(tenantCompositeId.getId(), compositeId.getId());
        assertEquals("1 row deleted", (Integer) 1, nbDeleted);
        organizationRdbms = organizationDao.findOrganizationByTenantAndId(tenantCompositeId.getId(),
                compositeId.getId());
        assertNull("Organization not found", organizationRdbms);

    }

}
