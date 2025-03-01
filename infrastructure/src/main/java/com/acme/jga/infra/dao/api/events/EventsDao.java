package com.acme.jga.infra.dao.api.events;

import com.acme.jga.domain.model.events.v1.EventStatus;
import com.acme.jga.infra.dto.events.v1.AuditEventDb;

import java.sql.SQLException;
import java.util.List;

public interface EventsDao {

    String insertEvent(AuditEventDb event) throws SQLException;

    AuditEventDb findByUid(String uid);

    List<AuditEventDb> findPendingEvents();

    Integer updateEvents(List<String> eventsUidList, EventStatus eventStatus);

}
