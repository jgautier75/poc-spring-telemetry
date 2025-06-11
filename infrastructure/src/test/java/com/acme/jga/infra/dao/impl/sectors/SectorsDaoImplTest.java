package com.acme.jga.infra.dao.impl.sectors;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.OrganizationKind;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.infra.dao.api.organizations.OrganizationsDao;
import com.acme.jga.infra.dao.api.sectors.SectorsDao;
import com.acme.jga.infra.dao.api.tenants.TenantsDao;
import com.acme.jga.infra.dao.config.DaoTestConfig;
import com.acme.jga.infra.dao.config.DatabaseTestConfig;
import com.acme.jga.infra.dao.utils.DaoTestUtils;
import com.acme.jga.infra.dto.organizations.v1.OrganizationDb;
import com.acme.jga.infra.dto.sectors.v1.SectorDb;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = {DatabaseTestConfig.class, DaoTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
@ContextConfiguration(initializers = SectorsDaoImplTest.DataSourceInitializer.class)
class SectorsDaoImplTest {

    @Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>(TestUtils.POSTGRESQL_VERSION)
            .waitingFor(Wait.defaultWaitStrategy());

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

    @Autowired
    SectorsDao sectorsDao;

    @Autowired
    TenantsDao tenantsDao;

    @Autowired
    OrganizationsDao organizationsDao;

    @BeforeEach
    void initDb() throws Exception {
        DaoTestUtils.performLiquibaseUpdate(database.getJdbcUrl(), database.getUsername(),
                database.getPassword());
    }

    @Test
    void createSector() {

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
        CompositeId orgCompositeId = organizationsDao.createOrganization(organizationDb);

        SectorDb sectorDb = SectorDb.builder().code("ctest").label("ctest").root(true)
                .tenantId(tenantCompositeId.getId()).build();

        // Create sector
        CompositeId sectorCompositeId = sectorsDao.createSector(tenantCompositeId.getId(),
                orgCompositeId.getId(),
                sectorDb);
        assertAll(() -> assertNotNull(sectorCompositeId, "CompositeId not null"),
                () -> assertNotNull(sectorCompositeId.getId(), "Id not null"),
                () -> assertNotNull(sectorCompositeId.getUid(), "Uid not null"));

        // Exists by code
        Optional<Long> optSectorId = sectorsDao.existsByCode(sectorDb.getCode());
        assertAll("Exists by code", () -> assertTrue(optSectorId.isPresent(), "Sector exists"));

        Optional<SectorDb> rdbmsSector = sectorsDao.findByUid(tenantCompositeId.getId(), orgCompositeId.getId(), sectorCompositeId.getUid());
        assertAll("Sector by uid",
                () -> assertNotNull(sectorDb, "RDBMS sector not null"),
                () -> assertEquals(sectorDb.getCode(), rdbmsSector.get().getCode(), "Code match"),
                () -> assertEquals(sectorDb.getLabel(), rdbmsSector.get().getLabel(), "Label match"),
                () -> assertEquals(sectorDb.isRoot(), rdbmsSector.get().isRoot(), "Root flag match"),
                () -> assertEquals(tenantCompositeId.getId(), rdbmsSector.get().getId(), "Tenant id match"));

        // Find sectors list
        List<SectorDb> sectors = sectorsDao.findSectorsByOrgId(tenantCompositeId.getId(), orgCompositeId.getId());
        assertAll("Sectors",
                () -> assertNotNull(sectors, "Sectors list"),
                () -> assertEquals(1, sectors.size(), "1 sector in list"),
                () -> assertEquals(sectorCompositeId.getId(), sectors.getFirst().getId(), "Sector id match"));
    }

}
