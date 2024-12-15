package com.acme.jga.infra.dao.impl.users;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.v1.OrganizationKind;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.domain.model.v1.UserStatus;
import com.acme.jga.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.jga.infra.dao.api.tenants.ITenantsDao;
import com.acme.jga.infra.dao.api.users.IUsersDao;
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

import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = {DatabaseTestConfig.class, DaoTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
@ContextConfiguration(initializers = UsersDaoTest.DataSourceInitializer.class)
class UsersDaoTest {

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
    IOrganizationsDao organizationsDao;
    @Autowired
    IUsersDao usersDao;

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
        assertNotNull("Organization composite id not null", orgCompositeId);
        assertNotNull("Organization id not null", orgCompositeId.getId());
        assertNotNull("Organization uid not null", orgCompositeId.getUid());

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
        assertNotNull("User create: compositeId not null", userCompositeId);

        // Find by id
        Optional<UserDb> userRdbms = usersDao.findById(tenantCompositeId.getId(), orgCompositeId.getId(),
                userCompositeId.getId());
        assertTrue("User find by id: user not null", userRdbms.isPresent());
        assertEquals("User find by id: id match", userCompositeId.getId(), userRdbms.get().getId());
        assertEquals("User find by id: email match", (String) userDb.getEmail(), (String) userRdbms.get().getEmail());
        assertEquals("User find by id: firstName match", userDb.getFirstName(), userRdbms.get().getFirstName());
        assertEquals("User find by id: lastName match", userDb.getLastName(), userRdbms.get().getLastName());
        assertEquals("User find by id: login match", userDb.getLogin(), userRdbms.get().getLogin());
        assertEquals("User find by id: middleName match", userDb.getMiddleName(), userRdbms.get().getMiddleName());
        assertEquals("User find by id: orgId match", orgCompositeId.getId(), userRdbms.get().getOrgId());
        assertEquals("User find by id: orgId match", userDb.getStatus(), userRdbms.get().getStatus());
        assertEquals("User find by id: tenantId match", tenantCompositeId.getId(), userRdbms.get().getTenantId());
        assertEquals("User find by id: uid match", userCompositeId.getUid(), userRdbms.get().getUid());

        // Find user by uid
        Optional<UserDb> userByUid = usersDao.findByUid(tenantCompositeId.getId(), orgCompositeId.getId(), userCompositeId.getUid());
        assertTrue("User by uid: not null", userByUid.isPresent());

        // Update user
        userByUid.get().setEmail("titi.toto@test.fr");
        userByUid.get().setFirstName("titi");
        userByUid.get().setLastName("toto");
        userByUid.get().setMiddleName("md");
        userByUid.get().setLogin("tutu");
        userByUid.get().setStatus(UserStatus.INACTIVE);
        Integer nbUpdated = usersDao.updateUser(userByUid.get());
        assertEquals("1 row updated", (Integer) 1, nbUpdated);

        Optional<UserDb> updatedUser = usersDao.findById(tenantCompositeId.getId(), orgCompositeId.getId(),
                userCompositeId.getId());
        assertEquals("Updated user: email match", (String) userByUid.get().getEmail(),
                (String) updatedUser.get().getEmail());
        assertEquals("Updated user: first name match", (String) userByUid.get().getFirstName(),
                (String) updatedUser.get().getFirstName());
        assertEquals("Updated user: last name match", (String) userByUid.get().getLastName(),
                (String) updatedUser.get().getLastName());
        assertEquals("Updated user: middle name match", (String) userByUid.get().getMiddleName(),
                (String) updatedUser.get().getMiddleName());
        assertEquals("Updated user: login match", (String) userByUid.get().getLogin(),
                (String) updatedUser.get().getLogin());
        assertEquals("Updated user: status match", (Integer) userByUid.get().getStatus().getCode(),
                (Integer) updatedUser.get().getStatus().getCode());

        // Delete user
        Integer nbDeleted = usersDao.deleteUser(tenantCompositeId.getId(), orgCompositeId.getId(),
                userCompositeId.getId());
        assertEquals("1 row deleted", (Integer) 1, nbDeleted);

        Optional<UserDb> userDeleted = usersDao.findById(tenantCompositeId.getId(), orgCompositeId.getId(), userCompositeId.getId());
        assertTrue("Deleted user not found", userDeleted.isEmpty());
    }

}
