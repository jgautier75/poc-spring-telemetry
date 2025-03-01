package com.acme.jga.infra.dao.impl.organizations;

import com.acme.jga.domain.model.filtering.FilteringConstants;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.utils.KeyValuePair;
import com.acme.jga.domain.model.v1.OrganizationMetaData;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.jga.infra.dao.extractors.OrganizationDbExtractor;
import com.acme.jga.infra.dao.processors.ExpressionsProcessor;
import com.acme.jga.infra.dto.organizations.v1.OrganizationDb;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.jdbc.dql.WhereClause;
import com.acme.jga.jdbc.dql.WhereOperator;
import com.acme.jga.jdbc.spring.AbstractJdbcDaoSupport;
import com.acme.jga.jdbc.utils.DaoConstants;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
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
public class OrganizationsDao extends AbstractJdbcDaoSupport implements IOrganizationsDao {
    private final ExpressionsProcessor expressionsProcessor;
    private static final String BASE_SELECT = "org_sel_base";

    public OrganizationsDao(DataSource ds, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                            ExpressionsProcessor expressionsProcessor, OpenTelemetryWrapper openTelemetryWrapper) {
        super(ds, namedParameterJdbcTemplate, openTelemetryWrapper);
        this.expressionsProcessor = expressionsProcessor;
        super.loadQueryFilePath(new String[]{"organizations.properties"});
    }

    @Override
    public CompositeId createOrganization(OrganizationDb org) {
        String baseQuery = super.getQuery("org_create");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String uuid = DaoConstants.generatedUUID();
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue(DaoConstants.P_TENANT_ID, org.getTenantId());
        mapSqlParameterSource.addValue(DaoConstants.P_UID, uuid);
        mapSqlParameterSource.addValue(DaoConstants.P_CODE, org.getCode());
        mapSqlParameterSource.addValue(DaoConstants.P_LABEL, org.getLabel());
        mapSqlParameterSource.addValue(DaoConstants.P_KIND, org.getKind().getCode());
        mapSqlParameterSource.addValue(DaoConstants.P_COUNTRY, org.getCountry());
        mapSqlParameterSource.addValue(DaoConstants.P_STATUS, org.getStatus().getCode());
        super.getNamedParameterJdbcTemplate().update(baseQuery, mapSqlParameterSource, keyHolder);
        Long generatedId = super.extractGeneratedId(keyHolder, DaoConstants.FIELD_ID);
        return new CompositeId(generatedId, uuid);
    }

    @Override
    public OrganizationDb findOrganizationByTenantAndId(Long tenantId, Long id) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_TENANT_ID, DaoConstants.P_TENANT_ID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_TENANT_ID)
                .paramValue(tenantId).build());
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_ID, DaoConstants.P_ID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_ID)
                .paramValue(id).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, rs -> {
            return OrganizationDbExtractor.extractOrganization(rs, true);
        });
    }

    @Override
    public OrganizationDb findOrganizationByTenantAndUid(Long tenantId, String uid) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_TENANT_ID, DaoConstants.P_TENANT_ID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_TENANT_ID)
                .paramValue(tenantId).build());
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_UID, DaoConstants.P_UID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_UID)
                .paramValue(uid).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, rs -> {
            return OrganizationDbExtractor.extractOrganization(rs, true);
        });
    }

    @Override
    public Integer updateOrganization(Long tenantId, Long orgId, String code, String label, String country, OrganizationStatus status) {
        String baseQuery = super.getQuery("org_update");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        params.put(DaoConstants.P_ORG_ID, orgId);
        params.put(DaoConstants.P_CODE, code);
        params.put(DaoConstants.P_LABEL, label);
        params.put(DaoConstants.P_COUNTRY, country);
        params.put(DaoConstants.P_STATUS, status.getCode());
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public Integer deleteOrganization(Long tenantId, Long orgId) {
        String baseQuery = super.getQuery("org_delete");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        params.put(DaoConstants.P_ID, orgId);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public PaginatedResults<OrganizationDb> filterOrganizations(Long tenantId, Map<String, Object> searchParams) {
        String baseQuery = super.getQuery(BASE_SELECT);
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        Map<String, KeyValuePair> columnsDefsByAlias = OrganizationMetaData.columnsByAlias();
        CompositeQuery compositeQuery = expressionsProcessor.buildFilterQuery(params, searchParams, columnsDefsByAlias);
        String countQuery = super.getQuery("org_count");

        // Where clause, force filtering on tenant
        String whereClause = DaoConstants.WHERE_CLAUSE + super.buildSQLEqualsExpression(DaoConstants.FIELD_TENANT_ID, DaoConstants.P_TENANT_ID);
        countQuery += whereClause;
        if (compositeQuery.notEmpty()) {
            whereClause += DaoConstants.AND + compositeQuery.query();
            countQuery += DaoConstants.AND + compositeQuery.query();
        }
        String fullQuery = baseQuery + whereClause + compositeQuery.orderBy() + compositeQuery.pagination();

        // Execute count query
        Integer nbResults = super.getNamedParameterJdbcTemplate().query(countQuery, compositeQuery.parameters(), resultSet -> {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return null;
            }
        });

        // Execute select query
        List<OrganizationDb> results = super.getNamedParameterJdbcTemplate().query(fullQuery, compositeQuery.parameters(), new RowMapper<>() {
            @Override
            @Nullable
            public OrganizationDb mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                return OrganizationDbExtractor.extractOrganization(rs, false);
            }
        });
        return new PaginatedResults<>(nbResults,
                nbResults != null ? (nbResults / (Integer) searchParams.get(FilteringConstants.PAGE_SIZE) + 1) : 0,
                results,
                (Integer) searchParams.get(FilteringConstants.PAGE_INDEX),
                (Integer) searchParams.get(FilteringConstants.PAGE_SIZE)
        );
    }

    @Override
    public Optional<Long> existsByCode(String code) {
        String baseQuery = super.getQuery("org_by_id_exists");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_CODE, code);
        Long userId = super.getNamedParameterJdbcTemplate().query(baseQuery, params, rs -> {
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                return null;
            }
        });
        return Optional.ofNullable(userId);
    }

    @Override
    public List<OrganizationDb> findOrgsByIdList(List<Long> orgIds) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLInExpression(DaoConstants.FIELD_ID, DaoConstants.P_ID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_ID)
                .paramValue(orgIds).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new RowMapper<>() {
            @Override
            @Nullable
            public OrganizationDb mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                return OrganizationDbExtractor.extractOrganization(rs, false);
            }
        });
    }

    @Override
    public Integer deleteById(Long tenantId, Long orgId) {
        String baseQuery = super.getQuery("org_delete_by_id");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        params.put(DaoConstants.P_ID, orgId);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public Integer deleteUsersByOrganization(Long tenantId, Long orgId) {
        String baseQuery = super.getQuery("org_delete_users");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        params.put(DaoConstants.P_ORG_ID, orgId);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public Integer deleteSectorsByOrganization(Long tenantId, Long orgId) {
        String baseQuery = super.getQuery("org_delete_sectors");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        params.put(DaoConstants.P_ORG_ID, orgId);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

}
