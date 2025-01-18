package com.acme.jga.infra.dao.impl.events;

import com.acme.jga.domain.model.events.v1.EventStatus;
import com.acme.jga.infra.dao.api.events.IEventsDao;
import com.acme.jga.infra.dao.extractors.AuditEventDbExtractor;
import com.acme.jga.infra.dto.events.v1.AuditEventDb;
import com.acme.jga.jdbc.dql.OrderByClause;
import com.acme.jga.jdbc.dql.OrderDirection;
import com.acme.jga.jdbc.dql.WhereClause;
import com.acme.jga.jdbc.dql.WhereOperator;
import com.acme.jga.jdbc.spring.AbstractJdbcDaoSupport;
import com.acme.jga.jdbc.utils.DaoConstants;
import com.acme.jga.utils.date.DateTimeUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class EventsDao extends AbstractJdbcDaoSupport implements IEventsDao {
    public EventsDao(DataSource ds, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(ds, namedParameterJdbcTemplate);
        super.loadQueryFilePath(new String[]{"events.properties"});
    }

    @Override
    public String insertEvent(AuditEventDb event) throws SQLException {
        String baseQuery = super.getQuery("event_create");
        String uuid = DaoConstants.generatedUUID();
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_UID, uuid);
        params.put("pCreatedAt", event.getCreatedAt());
        params.put("pUpdatedAt", event.getLastUpdatedAt());
        params.put("pTarget", event.getTarget().ordinal());
        params.put("pObjectUid", event.getObjectUid());
        params.put("pAction", event.getAction().name());
        params.put("pStatus", event.getStatus().ordinal());
        params.put("pPayload", super.buildPGobject(event.getPayload()));
        super.getNamedParameterJdbcTemplate().update(baseQuery, params);
        return uuid;
    }

    @Override
    public AuditEventDb findByUid(String uid) {
        String baseQuery = super.getQuery("event_sel_base");
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_UID, DaoConstants.P_UID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_UID)
                .paramValue(uid).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new ResultSetExtractor<>() {
            @Override
            @Nullable
            public AuditEventDb extractData(@NonNull ResultSet rs) throws SQLException, DataAccessException {
                return AuditEventDbExtractor.extractAuditEvent(rs, true);
            }
        });
    }

    @Override
    public List<AuditEventDb> findPendingEvents() {
        String baseQuery = super.getQuery("event_sel_base");
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_STATUS, DaoConstants.P_STATUS))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_STATUS)
                .paramValue(EventStatus.PENDING.ordinal())
                .build());
        Map<String, Object> params = super.buildParams(whereClauses);
        OrderByClause createdAtAsc = OrderByClause.builder().expression("created_at").orderDirection(OrderDirection.ASC).build();
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, List.of(createdAtAsc), (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new RowMapper<>() {
            @Override
            @Nullable
            public AuditEventDb mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                return AuditEventDbExtractor.extractAuditEvent(rs, false);
            }
        });
    }

    @Override
    public Integer updateEvents(List<String> eventsUidList, EventStatus eventStatus) {
        String updateQuery = super.getQuery("events_update_status");
        Map<String, Object> params = new HashMap<>();
        params.put("pUids", eventsUidList);
        params.put(DaoConstants.P_STATUS, eventStatus.ordinal());
        params.put("pUpdated", DateTimeUtils.nowIso());
        return super.getNamedParameterJdbcTemplate().update(updateQuery, params);
    }

}
