package com.acme.jga.infra.dao.impl.events;

import com.acme.jga.domain.model.events.v1.*;
import com.acme.jga.infra.converters.AuditEventsInfraConverter;
import com.acme.jga.infra.dao.api.events.IEventsDao;
import com.acme.jga.infra.dao.config.DaoTestConfig;
import com.acme.jga.infra.dao.config.DatabaseTestConfig;
import com.acme.jga.infra.dao.utils.DaoTestUtils;
import com.acme.jga.infra.dto.events.v1.AuditEventDb;
import com.acme.jga.utils.date.DateTimeUtils;
import com.acme.jga.utils.test.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = {DatabaseTestConfig.class, DaoTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
@ContextConfiguration(initializers = EventsDaoTest.DataSourceInitializer.class)
class EventsDaoTest {

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
    IEventsDao eventsDao;

    @Autowired
    AuditEventsInfraConverter auditEventsInfraConverter;

    @Autowired
    ObjectMapper objectMapper;

    @Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>(TestUtils.POSTGRESQL_VERSION)
            .waitingFor(Wait.defaultWaitStrategy());

    @BeforeEach
    void initDb() throws Exception {
        DaoTestUtils.performLiquibaseUpdate(database.getJdbcUrl(), database.getUsername(),
                database.getPassword());
    }

    @Test
    void createEvent() throws Exception {
        AuditAuthor author = AuditAuthor.builder()
                .name("Jerome GAUTIER")
                .uid(UUID.randomUUID().toString())
                .build();
        AuditScope auditScope = AuditScope.builder()
                .organizationName("RENNES")
                .organizationUid(UUID.randomUUID().toString())
                .tenantName("SI")
                .tenantName(UUID.randomUUID().toString())
                .build();
        AuditChange auditChange = AuditChange.builder().from("a").object("test").to("b").build();

        AuditEvent auditEvent = AuditEvent.builder()
                .action(AuditAction.CREATE)
                .author(author)
                .changes(List.of(auditChange))
                .createdAt(DateTimeUtils.nowIso())
                .lastUpdatedAt(DateTimeUtils.nowIso())
                .objectUid(UUID.randomUUID().toString())
                .scope(auditScope)
                .status(EventStatus.PENDING)
                .target(EventTarget.SECTOR)
                .build();
        objectMapper.writeValueAsString(auditEvent);

        AuditEventDb auditEventDb = auditEventsInfraConverter.convertAuditEventToDb(auditEvent, objectMapper);
        String uid = eventsDao.insertEvent(auditEventDb);
        assertNotNull("Generated uid not null", uid);

        AuditEventDb rdbmsAudit = eventsDao.findByUid(uid);
        assertAll("Find by uid",
                () -> assertNotNull(rdbmsAudit, "Audit event not null"),
                () -> assertNotNull(rdbmsAudit.getPayload(), "Payload not null"),
                () -> assertEquals(auditEvent.getAction(), rdbmsAudit.getAction(), "Action match"),
                () -> assertNotNull(rdbmsAudit.getCreatedAt(), "Created at"),
                () -> assertNotNull(rdbmsAudit.getLastUpdatedAt(), "Last updated at"),
                () -> assertEquals(auditEvent.getObjectUid(), rdbmsAudit.getObjectUid(), "Object uid match"),
                () -> assertEquals(auditEvent.getStatus(), rdbmsAudit.getStatus(), "Status match"),
                () -> assertEquals(auditEvent.getTarget(), rdbmsAudit.getTarget(), "Target match"),
                () -> assertNotNull(rdbmsAudit.getUid(), "Uid not null"));
    }
}
