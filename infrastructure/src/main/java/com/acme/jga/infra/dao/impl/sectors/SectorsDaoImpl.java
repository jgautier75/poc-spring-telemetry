package com.acme.jga.infra.dao.impl.sectors;

import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.infra.dao.api.sectors.SectorsDao;
import com.acme.jga.infra.dao.extractors.SectorDbExtractor;
import com.acme.jga.infra.dto.sectors.v1.SectorDb;
import com.acme.jga.jdbc.dql.WhereClause;
import com.acme.jga.jdbc.dql.WhereOperator;
import com.acme.jga.jdbc.spring.AbstractJdbcDaoSupport;
import com.acme.jga.jdbc.utils.DaoConstants;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.acme.jga.utils.otel.OtelContext;
import io.opentelemetry.api.trace.Span;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class SectorsDaoImpl extends AbstractJdbcDaoSupport implements SectorsDao {
    private final ILoggingFacade loggingFacade;
    private static final String INSTRUMENTATION_NAME = SectorsDaoImpl.class.getCanonicalName();

    public SectorsDaoImpl(DataSource ds, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                          OpenTelemetryWrapper openTelemetryWrapper, ILoggingFacade loggingFacade) {
        super(ds, namedParameterJdbcTemplate, openTelemetryWrapper);
        super.loadQueryFilePath(new String[]{"sectors.properties"});
        this.loggingFacade = loggingFacade;
    }

    @Override
    public List<SectorDb> findSectorsByOrgId(Long tenantId, Long orgId, Span parentSpan) {
        return processWithSpan(INSTRUMENTATION_NAME, "DAO_SECTORS_FIND_BY_ORG", parentSpan, (span) -> {
            String baseQuery = super.getQuery("sector_base");
            List<WhereClause> whereClauses = new ArrayList<>();
            whereClauses.add(WhereClause.builder()
                    .expression(buildSQLEqualsExpression(DaoConstants.FIELD_TENANT_ID, DaoConstants.P_TENANT_ID))
                    .operator(WhereOperator.AND)
                    .paramName(DaoConstants.P_TENANT_ID)
                    .paramValue(tenantId)
                    .build());
            whereClauses.add(WhereClause.builder()
                    .expression(buildSQLEqualsExpression(DaoConstants.FIELD_ORG_ID, DaoConstants.P_ORG_ID))
                    .operator(WhereOperator.AND)
                    .paramName(DaoConstants.P_ORG_ID)
                    .paramValue(orgId)
                    .build());
            Map<String, Object> params = super.buildParams(whereClauses);
            String fullQuery = super.buildFullQuery(baseQuery, whereClauses, Collections.emptyList(), (String[]) null);
            loggingFacade.debugS(INSTRUMENTATION_NAME, "Sectors query [%s]", new Object[]{fullQuery}, OtelContext.fromSpan(span));
            return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new RowMapper<>() {
                @Override
                @Nullable
                public SectorDb mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                    return SectorDbExtractor.extractSector(rs, false);
                }
            });
        });
    }

    @Override
    public CompositeId createSector(Long tenantId, Long orgId, SectorDb sectorDb) {
        String baseQuery = super.getQuery("sector_create");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String uuid = DaoConstants.generatedUUID();
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue(DaoConstants.P_TENANT_ID, tenantId);
        mapSqlParameterSource.addValue(DaoConstants.P_ORG_ID, orgId);
        mapSqlParameterSource.addValue(DaoConstants.P_UID, uuid);
        mapSqlParameterSource.addValue(DaoConstants.P_CODE, sectorDb.getCode());
        mapSqlParameterSource.addValue(DaoConstants.P_LABEL, sectorDb.getLabel());
        mapSqlParameterSource.addValue("pRoot", sectorDb.isRoot());
        mapSqlParameterSource.addValue(DaoConstants.P_PARENT_ID, sectorDb.getParentId());
        super.getNamedParameterJdbcTemplate().update(baseQuery, mapSqlParameterSource, keyHolder);
        Long generatedId = super.extractGeneratedId(keyHolder, DaoConstants.FIELD_ID);
        return new CompositeId(generatedId, uuid);
    }

    @Override
    public Optional<SectorDb> findByUid(Long tenantId, Long orgId, String uid) {
        String baseQuery = super.getQuery("sector_base");
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_TENANT_ID, DaoConstants.P_TENANT_ID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_TENANT_ID)
                .paramValue(tenantId)
                .build());
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_ORG_ID, DaoConstants.P_ORG_ID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_ORG_ID)
                .paramValue(orgId)
                .build());
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_UID, DaoConstants.P_UID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_UID)
                .paramValue(uid)
                .build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, Collections.emptyList(), (String[]) null);
        return Optional.ofNullable(super.getNamedParameterJdbcTemplate().query(fullQuery, params, new ResultSetExtractor<>() {
            @Override
            @Nullable
            public SectorDb extractData(@NonNull ResultSet rs) throws SQLException, DataAccessException {
                return SectorDbExtractor.extractSector(rs, true);
            }
        }));
    }

    @Override
    public Optional<Long> existsByCode(String code) {
        String baseQuery = super.getQuery("sector_exists_by_code");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_CODE, code);
        Long sectorId = super.getNamedParameterJdbcTemplate().query(baseQuery, params, rs -> {
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                return null;
            }
        });
        return Optional.ofNullable(sectorId);
    }

    @Override
    public int updateSector(Long tenantId, Long orgId, SectorDb sectorDb) {
        String baseQuery = super.getQuery("sector_update");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_CODE, sectorDb.getCode());
        params.put(DaoConstants.P_LABEL, sectorDb.getLabel());
        params.put(DaoConstants.P_PARENT_ID, sectorDb.getParentId());
        params.put(DaoConstants.P_ID, sectorDb.getId());
        params.put(DaoConstants.P_ORG_ID, orgId);
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public int deleteSector(Long tenantId, Long orgId, Long sectorId) {
        String baseQuery = super.getQuery("sector_delete");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_ID, sectorId);
        params.put(DaoConstants.P_ORG_ID, orgId);
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

}
