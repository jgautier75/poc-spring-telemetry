package com.acme.jga.infra.dao.impl.users;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.OrganizationKind;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.domain.model.v1.UserStatus;
import com.acme.jga.infra.dao.api.organizations.OrganizationsDao;
import com.acme.jga.infra.dao.api.tenants.TenantsDao;
import com.acme.jga.infra.dao.api.users.UsersDao;
import com.acme.jga.infra.dao.config.DaoTestConfig;
import com.acme.jga.infra.dao.config.DatabaseTestConfig;
import com.acme.jga.infra.dao.utils.DaoTestUtils;
import com.acme.jga.infra.dto.organizations.v1.OrganizationDb;
import com.acme.jga.infra.dto.users.v1.UserDb;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = {DatabaseTestConfig.class, DaoTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
@ContextConfiguration(initializers = UsersDaoImplTest.DataSourceInitializer.class)
class UsersDaoImplTest {

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
    OrganizationsDao organizationsDao;
    @Autowired
    UsersDao usersDao;

    @BeforeEach
    void initDb() throws Exception {
        DaoTestUtils.performLiquibaseUpdate(database.getJdbcUrl(), database.getUsername(),
                database.getPassword());
    }

    @Test
    void createUser() {

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
        assertNotNull(orgCompositeId, "Organization composite id not null");
        assertNotNull(orgCompositeId.getUid(), "Organization uid not null");

        // Create user
        UserDb userDb = UserDb.builder()
                .email("test@test.fr")
                .firstName("ftest")
                .lastName("lname")
                .login("login_test")
                .middleName("jr")
                .orgId(orgCompositeId.getId())
                .status(UserStatus.ACTIVE)
                .tenantId(tenantCompositeId.getId())
                .build();

        CompositeId userCompositeId = usersDao.createUser(userDb);
        assertNotNull(userCompositeId, "User create: compositeId not null");

        // Find by id
        Optional<UserDb> userRdbms = usersDao.findById(tenantCompositeId.getId(), orgCompositeId.getId(),
                userCompositeId.getId());
        assertTrue(userRdbms.isPresent(), "User find by id: user not null");
        assertEquals(userCompositeId.getId(), userRdbms.get().getId(), "User find by id: id match");
        assertEquals(userDb.getEmail(), userRdbms.get().getEmail(), "User find by id: email match");
        assertEquals(userDb.getFirstName(), userRdbms.get().getFirstName(), "User find by id: firstName match");
        assertEquals(userDb.getLastName(), userRdbms.get().getLastName(), "User find by id: lastName match");
        assertEquals(userDb.getLogin(), userRdbms.get().getLogin(), "User find by id: login match");
        assertEquals(userDb.getMiddleName(), userRdbms.get().getMiddleName(), "User find by id: middleName match");
        assertEquals(orgCompositeId.getId(), userRdbms.get().getOrgId(), "User find by id: orgId match");
        assertEquals(userDb.getStatus(), userRdbms.get().getStatus(), "User find by id: orgId match");
        assertEquals(tenantCompositeId.getId(), userRdbms.get().getTenantId(), "User find by id: tenantId match");
        assertEquals(userCompositeId.getUid(), userRdbms.get().getUid(), "User find by id: uid match");

        // Find user by uid
        Optional<UserDb> userByUid = usersDao.findByUid(tenantCompositeId.getId(), orgCompositeId.getId(), userCompositeId.getUid());
        assertTrue(userByUid.isPresent(), "User by uid: not null");

        // Update user
        userByUid.get().setEmail("titi.toto@test.fr");
        userByUid.get().setFirstName("titi");
        userByUid.get().setLastName("toto");
        userByUid.get().setMiddleName("md");
        userByUid.get().setLogin("tutu");
        userByUid.get().setStatus(UserStatus.INACTIVE);
        Integer nbUpdated = usersDao.updateUser(userByUid.get());
        assertEquals((Integer) 1, nbUpdated, "1 row updated");

        Optional<UserDb> updatedUser = usersDao.findById(tenantCompositeId.getId(), orgCompositeId.getId(),
                userCompositeId.getId());
        assertEquals(userByUid.get().getEmail(), updatedUser.get().getEmail(), "Updated user: email match");
        assertEquals(userByUid.get().getFirstName(), updatedUser.get().getFirstName(), "Updated user: first name match");
        assertEquals(userByUid.get().getLastName(), updatedUser.get().getLastName(), "Updated user: last name match");
        assertEquals(userByUid.get().getMiddleName(), updatedUser.get().getMiddleName(), "Updated user: middle name match");
        assertEquals(userByUid.get().getLogin(), updatedUser.get().getLogin(), "Updated user: login match");
        assertEquals(userByUid.get().getStatus().getCode(), updatedUser.get().getStatus().getCode(), "Updated user: status match");

        // Delete user
        Integer nbDeleted = usersDao.deleteUser(tenantCompositeId.getId(), orgCompositeId.getId(), userCompositeId.getId());
        assertEquals((Integer) 1, nbDeleted, "1 row deleted");

        Optional<UserDb> userDeleted = usersDao.findById(tenantCompositeId.getId(), orgCompositeId.getId(), userCompositeId.getId());
        assertTrue(userDeleted.isEmpty(), "Deleted user not found");
    }

}
