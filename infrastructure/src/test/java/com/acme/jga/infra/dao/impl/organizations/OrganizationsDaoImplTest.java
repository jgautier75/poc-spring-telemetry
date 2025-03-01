package com.acme.jga.infra.dao.impl.organizations;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.OrganizationKind;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.infra.dao.api.organizations.OrganizationsDao;
import com.acme.jga.infra.dao.api.tenants.TenantsDao;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = {DatabaseTestConfig.class, DaoTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
@ContextConfiguration(initializers = OrganizationsDaoImplTest.DataSourceInitializer.class)
class OrganizationsDaoImplTest {

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
    TenantsDao tenantsDao;

    @Autowired
    OrganizationsDao organizationDao;

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
        assertNotNull(compositeId, "Organization composite id not null");
        assertNotNull(compositeId.getId(), "Organization id not null");
        assertNotNull(compositeId.getUid(), "Organization uid not null");

        // Find by id
        OrganizationDb organizationRdbms = organizationDao.findOrganizationByTenantAndId(tenantCompositeId.getId(), compositeId.getId());
        assertNotNull(organizationRdbms, "Organization by id: not null");
        assertEquals(organizationDb.getCode(), organizationRdbms.getCode(), "Code match");
        assertEquals(organizationDb.getCountry(), organizationRdbms.getCountry(), "Country match");
        assertEquals(organizationDb.getKind(), organizationRdbms.getKind(), "Kind match");
        assertEquals(organizationDb.getLabel(), organizationRdbms.getLabel(), "Label match");
        assertEquals(organizationDb.getStatus(), organizationRdbms.getStatus(), "Status match");

        // Find by uid
        OrganizationDb organizationByUid = organizationDao.findOrganizationByTenantAndUid(
                tenantCompositeId.getId(),
                compositeId.getUid());
        assertNotNull(organizationByUid, "Organization by uid not null");

        // Update organization
        organizationDb.setCountry("de");
        organizationDb.setCode("003");
        organizationDb.setLabel("otest");
        organizationDb.setStatus(OrganizationStatus.INACTIVE);
        Integer nbRowsUpdated = organizationDao.updateOrganization(tenantCompositeId.getId(),
                compositeId.getId(),
                organizationDb.getCode(),
                organizationDb.getLabel(), organizationDb.getCountry(), organizationDb.getStatus());
        assertEquals((Integer) 1, nbRowsUpdated, "1 row updated");

        organizationRdbms = organizationDao.findOrganizationByTenantAndId(tenantCompositeId.getId(),
                compositeId.getId());
        assertEquals(organizationDb.getCountry(), organizationRdbms.getCountry(), "Country match");
        assertEquals(organizationDb.getCode(), organizationRdbms.getCode(), "Code match");
        assertEquals(organizationDb.getLabel(), organizationRdbms.getLabel(), "Label match");
        assertEquals(organizationDb.getStatus(), organizationRdbms.getStatus(), "Status match");

        // Delete organization
        Integer nbDeleted = organizationDao.deleteOrganization(tenantCompositeId.getId(), compositeId.getId());
        assertEquals((Integer) 1, nbDeleted, "1 row deleted");
        organizationRdbms = organizationDao.findOrganizationByTenantAndId(tenantCompositeId.getId(), compositeId.getId());
        assertNull(organizationRdbms, "Organization not found");

    }

}
