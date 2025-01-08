package com.acme.jga.infra.dao.impl.users;

import com.acme.jga.domain.model.filtering.FilteringConstants;
import com.acme.jga.domain.model.ids.CompositeId;
import com.acme.jga.domain.model.utils.KeyValuePair;
import com.acme.jga.domain.model.v1.UserMetaData;
import com.acme.jga.infra.dao.api.users.IUsersDao;
import com.acme.jga.infra.dao.extractors.UsersDbExtractor;
import com.acme.jga.infra.dao.extractors.UsersDisplayDbExtractor;
import com.acme.jga.infra.dao.processors.ExpressionsProcessor;
import com.acme.jga.infra.dto.users.v1.UserDb;
import com.acme.jga.infra.dto.users.v1.UserDisplayDb;
import com.acme.jga.jdbc.dql.PaginatedResults;
import com.acme.jga.jdbc.dql.WhereClause;
import com.acme.jga.jdbc.dql.WhereOperator;
import com.acme.jga.jdbc.spring.AbstractJdbcDaoSupport;
import com.acme.jga.jdbc.utils.DaoConstants;
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
public class UsersDao extends AbstractJdbcDaoSupport implements IUsersDao {
    private final ExpressionsProcessor expressionsProcessor;
    private static final String BASE_SELECT = "user_sel_base";

    public UsersDao(DataSource ds, NamedParameterJdbcTemplate namedParameterJdbcTemplate, ExpressionsProcessor expressionsProcessor) {
        super(ds, namedParameterJdbcTemplate);
        this.expressionsProcessor = expressionsProcessor;
        super.loadQueryFilePath(new String[]{"users.properties"});
    }

    @Override
    public CompositeId createUser(UserDb userDb) {
        String baseQuery = super.getQuery("user_create");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String uuid = DaoConstants.generatedUUID();
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue(DaoConstants.P_UID, uuid);
        mapSqlParameterSource.addValue(DaoConstants.P_TENANT_ID, userDb.getTenantId());
        mapSqlParameterSource.addValue(DaoConstants.P_ORG_ID, userDb.getOrgId());
        mapSqlParameterSource.addValue(DaoConstants.P_LOGIN, userDb.getLogin());
        mapSqlParameterSource.addValue(DaoConstants.P_FIRST_NAME, userDb.getFirstName());
        mapSqlParameterSource.addValue(DaoConstants.P_LAST_NAME, userDb.getLastName());
        mapSqlParameterSource.addValue(DaoConstants.P_MIDDLE_NAME, userDb.getMiddleName());
        mapSqlParameterSource.addValue(DaoConstants.P_EMAIL, userDb.getEmail());
        mapSqlParameterSource.addValue(DaoConstants.P_STATUS, userDb.getStatus().getCode());
        mapSqlParameterSource.addValue(DaoConstants.P_SECRETS, userDb.getSecrets());
        super.getNamedParameterJdbcTemplate().update(baseQuery, mapSqlParameterSource, keyHolder);
        Long generatedId = super.extractGeneratedId(keyHolder, DaoConstants.FIELD_ID);
        return new CompositeId(generatedId, uuid);
    }

    @Override
    public Optional<UserDb> findById(Long tenantId, Long orgId, Long id) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_TENANT_ID, DaoConstants.P_TENANT_ID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_TENANT_ID)
                .paramValue(tenantId).build());
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_ORG_ID, DaoConstants.P_ORG_ID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_ORG_ID)
                .paramValue(orgId).build());
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_ID, DaoConstants.P_ID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_ID)
                .paramValue(id).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return Optional.ofNullable(super.getNamedParameterJdbcTemplate().query(fullQuery, params, rs -> {
            return UsersDbExtractor.extractUser(rs, true);
        }));
    }

    @Override
    public Optional<UserDb> findByUid(Long tenantId, Long orgId, String uid) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_TENANT_ID, DaoConstants.P_TENANT_ID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_TENANT_ID)
                .paramValue(tenantId).build());
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_ORG_ID, DaoConstants.P_ORG_ID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_ORG_ID)
                .paramValue(orgId).build());
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_UID, DaoConstants.P_UID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_UID)
                .paramValue(uid).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return Optional.ofNullable(super.getNamedParameterJdbcTemplate().query(fullQuery, params, rs -> {
            return UsersDbExtractor.extractUser(rs, true);
        }));
    }

    @Override
    public Optional<UserDb> findByUid(String uid) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_UID, DaoConstants.P_UID))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_UID)
                .paramValue(uid).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return Optional.ofNullable(super.getNamedParameterJdbcTemplate().query(fullQuery, params, rs -> {
            return UsersDbExtractor.extractUser(rs, true);
        }));
    }

    @Override
    public Optional<UserDb> findByLogin(String login) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_LOGIN, DaoConstants.P_LOGIN))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_LOGIN)
                .paramValue(login).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return Optional.ofNullable(super.getNamedParameterJdbcTemplate().query(fullQuery, params, rs -> {
            return UsersDbExtractor.extractUser(rs, true);
        }));
    }

    @Override
    public Optional<UserDb> findByEmail(String email) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(buildSQLEqualsExpression(DaoConstants.FIELD_EMAIL, DaoConstants.P_EMAIL))
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_EMAIL)
                .paramValue(email).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return Optional.ofNullable(super.getNamedParameterJdbcTemplate().query(fullQuery, params, rs -> {
            return UsersDbExtractor.extractUser(rs, true);
        }));
    }

    @Override
    public Integer updateUser(UserDb userDb) {
        String baseQuery = super.getQuery("user_update");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, userDb.getTenantId());
        params.put(DaoConstants.P_ORG_ID, userDb.getOrgId());
        params.put(DaoConstants.P_ID, userDb.getId());
        params.put(DaoConstants.P_LOGIN, userDb.getLogin());
        params.put(DaoConstants.P_EMAIL, userDb.getEmail());
        params.put(DaoConstants.P_FIRST_NAME, userDb.getFirstName());
        params.put(DaoConstants.P_LAST_NAME, userDb.getLastName());
        params.put(DaoConstants.P_MIDDLE_NAME, userDb.getMiddleName());
        params.put(DaoConstants.P_STATUS, userDb.getStatus().getCode());
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public Integer deleteUser(Long tenantId, Long orgId, Long userId) {
        String baseQuery = super.getQuery("user_delete");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        params.put(DaoConstants.P_ORG_ID, orgId);
        params.put(DaoConstants.P_ID, userId);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public Optional<Long> emailExists(String email) {
        String baseQuery = super.getQuery("user_id_by_email");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_EMAIL, email);
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
    public Optional<Long> loginExists(String login) {
        String baseQuery = super.getQuery("user_id_by_login");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_LOGIN, login);
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
    public List<UserDb> findUsers(@NonNull Long tenantId, @NonNull Long orgId) {
        String baseQuery = super.getQuery(BASE_SELECT);
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
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new RowMapper<>() {
            @Override
            @Nullable
            public UserDb mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                return UsersDbExtractor.extractUser(rs, false);
            }
        });
    }

    @Override
    public PaginatedResults<UserDisplayDb> filterUsers(Long tenantId, Long orgId, Map<String, Object> searchParams) {
        String baseQuery = super.getQuery("user_display");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        params.put(DaoConstants.P_ORG_ID, orgId);

        Map<String, KeyValuePair> columnsDefsByAlias = UserMetaData.columnsByAlias();

        CompositeQuery compositeQuery = expressionsProcessor.buildFilterQuery(params, searchParams, columnsDefsByAlias);
        String whereClause = DaoConstants.WHERE_CLAUSE
                + super.buildSQLEqualsExpression(DaoConstants.FIELD_TENANT_ID, DaoConstants.P_TENANT_ID)
                + DaoConstants.AND
                + super.buildSQLEqualsExpression(DaoConstants.FIELD_ORG_ID, DaoConstants.P_ORG_ID);

        // Count query
        String countQuery = super.getQuery("user_count");
        countQuery += whereClause;
        if (compositeQuery.notEmpty()) {
            whereClause += DaoConstants.AND + compositeQuery.query();
            countQuery += DaoConstants.AND + compositeQuery.query();
        }

        Integer nbResults = super.getNamedParameterJdbcTemplate().query(countQuery, params, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return null;
            }
        });

        // Select query
        String fullQuery = baseQuery + whereClause + compositeQuery.orderBy() + compositeQuery.pagination();
        List<UserDisplayDb> results = super.getNamedParameterJdbcTemplate().query(fullQuery, compositeQuery.parameters(), new RowMapper<>() {
            @Override
            @Nullable
            public UserDisplayDb mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                return UsersDisplayDbExtractor.extractUser(rs, false);
            }
        });
        return new PaginatedResults<>(nbResults,
                nbResults != null ? (nbResults / (Integer) searchParams.get(FilteringConstants.PAGE_SIZE) + 1) : 0,
                results,
                (Integer) searchParams.get(FilteringConstants.PAGE_INDEX),
                (Integer) searchParams.get(FilteringConstants.PAGE_SIZE)
        );
    }

}