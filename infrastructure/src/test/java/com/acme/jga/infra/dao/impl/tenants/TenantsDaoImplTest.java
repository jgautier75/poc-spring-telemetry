package com.acme.jga.infra.dao.impl.tenants;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.infra.dao.api.tenants.TenantsDao;
import com.acme.jga.infra.dao.config.DaoTestConfig;
import com.acme.jga.infra.dao.config.DatabaseTestConfig;
import com.acme.jga.infra.dao.utils.DaoTestUtils;
import com.acme.jga.infra.dto.tenants.v1.TenantDb;
import com.acme.jga.utils.test.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureDataSourceInitialization;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = {DatabaseTestConfig.class, DaoTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
class TenantsDaoImplTest {

    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(TestUtils.POSTGRESQL_VERSION).waitingFor(Wait.defaultWaitStrategy());

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    TenantsDao tenantsDao;

    @BeforeEach
    public void beforeTests() throws Exception {
        DaoTestUtils.performLiquibaseUpdate(postgreSQLContainer.getJdbcUrl(), postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword());
    }

    @Test
    void createTenantTest() throws Exception {

        String tenantCode = "001";

        // Create tenant
        CompositeId compositeId = tenantsDao.createTenant(tenantCode, "root");
        assertNotNull(compositeId, "Composite id not null");
        assertNotNull(compositeId.getId(), "Id from sequence not null");
        assertNotNull(compositeId.getUid(), "Generated uid not null");

        // Find by id
        TenantDb tenantById = tenantsDao.findById(compositeId.getId());
        assertNotNull(tenantById, "Tenant by id not null");
        assertNotNull(tenantById.getCode(), "Tenant by id: code not null");
        assertNotNull(tenantById.getId(), "Tenant by id: id not null");
        assertNotNull(tenantById.getLabel(), "Tenant by id: label not null");
        assertNotNull(tenantById.getUid(), "Tenant by id: uid not null");
        assertEquals(compositeId.getUid(), tenantById.getUid());

        // Find by uid
        Optional<TenantDb> tenantByUid = tenantsDao.findByUid(compositeId.getUid());
        assertTrue(tenantByUid.isPresent(), "Tenant by uid not null");

        // Find by code
        Optional<TenantDb> tenantByCode = tenantsDao.findByCode(tenantCode);
        assertTrue(tenantByCode.isPresent(), "Tenant by code not null");

        // Check returns null
        Optional<TenantDb> tenantDummy = tenantsDao.findByCode("123456");
        assertTrue(tenantDummy.isEmpty(), "Dummy tenant not found");

        // Update tenant
        tenantById.setCode("123456");
        tenantById.setLabel("test");
        Integer nbUpdated = tenantsDao.updateTenant(compositeId.getId(), tenantById.getCode(), tenantById.getLabel());
        assertEquals((Integer) 1, nbUpdated, "1 row updated");

        // Delete tenant
        Integer nbDeleted = tenantsDao.deleteTenant(compositeId.getId());
        assertEquals((Integer) 1, nbDeleted, "1 row deleted");

    }

}
