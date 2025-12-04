package com.acme.jga.infra.dao.impl.events;

import com.acme.jga.domain.model.events.v1.*;
import com.acme.jga.infra.converters.AuditEventsInfraConverter;
import com.acme.jga.infra.dao.api.events.EventsDao;
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
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
class EventsDaoImplTest {

    @Autowired
    EventsDao eventsDao;

    @Autowired
    AuditEventsInfraConverter auditEventsInfraConverter;

    @Autowired
    ObjectMapper objectMapper;

    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(TestUtils.POSTGRESQL_VERSION).waitingFor(Wait.defaultWaitStrategy());

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    /*@Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>(TestUtils.POSTGRESQL_VERSION)
            .waitingFor(Wait.defaultWaitStrategy());*/

    @BeforeEach
    void initDb() throws Exception {
        DaoTestUtils.performLiquibaseUpdate(postgreSQLContainer.getJdbcUrl(), postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword());
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
