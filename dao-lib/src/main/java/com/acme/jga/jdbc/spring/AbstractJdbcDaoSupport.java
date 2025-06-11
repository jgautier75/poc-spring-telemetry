package com.acme.jga.jdbc.spring;

import com.acme.jga.jdbc.dql.OrderByClause;
import com.acme.jga.jdbc.dql.WhereClause;
import com.acme.jga.jdbc.utils.DaoConstants;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

import static com.acme.jga.utils.http.RequestCorrelationId.correlationKey;

@Slf4j
public abstract class AbstractJdbcDaoSupport extends JdbcDaoSupport {
    private static final String DB_DAO_QUERY_FOLDER = "db/sql";

    public record PaginationResult(String pagination, String orderBy) {
    }

    public record CompositeQuery(Boolean notEmpty, String query, Map<String, Object> parameters, String pagination, String orderBy) {
    }

    protected final Properties queries = new Properties();
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    protected OpenTelemetryWrapper openTelemetryWrapper;

    protected AbstractJdbcDaoSupport() {
        // Empty constructor for injection
    }

    protected AbstractJdbcDaoSupport(DataSource dataSource, NamedParameterJdbcTemplate namedParameterJdbcTemplate, OpenTelemetryWrapper openTelemetryWrapper) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.openTelemetryWrapper = openTelemetryWrapper;
        setDataSource(dataSource);
    }

    protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    protected void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * Load SQL queries from properties paths using current thread class loader.
     *
     * @param queryFilePaths Properties paths
     */
    protected void loadQueryFilePath(String[] queryFilePaths) {
        if (queryFilePaths != null) {
            String sqlFile = queryFilePaths[0];
            boolean loadSucceeds;
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(DB_DAO_QUERY_FOLDER + "/" + sqlFile)) {
                loadSucceeds = is != null;
            } catch (Exception e) {
                loadSucceeds = false;
            }
            if (loadSucceeds) {
                loadQueryFilePath(Thread.currentThread().getContextClassLoader(), queryFilePaths);
            } else {
                loadQueryFilePath(AbstractJdbcDaoSupport.class.getClassLoader(), queryFilePaths);
            }
        }
    }

    /**
     * Load SQL queries from properties paths using the specified class loader.
     *
     * @param clazzLoader    Class loader
     * @param queryFilePaths Properties paths
     */
    protected void loadQueryFilePath(ClassLoader clazzLoader, String[] queryFilePaths) {
        Arrays.asList(queryFilePaths).forEach(queryFile -> {
            try (InputStream io = clazzLoader.getResourceAsStream(DB_DAO_QUERY_FOLDER + "/" + queryFile)) {
                queries.load(io);
            } catch (IOException e) {
                log.error("loadQueryFilePath [" + Arrays.toString(queryFilePaths) + "]: {0}", e);
            }
        });
    }

    /**
     * Load an SQL instruction from cache.
     *
     * @param pKey Query key
     * @return SQL instruction
     */
    protected String getQuery(String pKey) {
        return queries.getProperty(pKey);
    }

    /**
     * Build SQL query.
     *
     * @param baseQuery         Base query select
     * @param whereClauseList   Where clause(s) list
     * @param orderByClauseList OrderBy clause(s) list
     * @param groupByClause     GroupBy clause(s) list
     * @return SQL query
     */
    protected String buildFullQuery(String baseQuery, List<WhereClause> whereClauseList,
                                    List<OrderByClause> orderByClauseList, String... groupByClause) {
        StringBuilder queryBuilder = new StringBuilder(baseQuery);

        appendWhereClause(queryBuilder, whereClauseList);
        appendGroupByClause(queryBuilder, groupByClause);
        appendOrderByClause(queryBuilder, orderByClauseList);

        return queryBuilder.toString();
    }

    /**
     * Append where clause to SQL query.
     *
     * @param queryBuilder    Query buffer
     * @param whereClauseList Where clause list
     */
    private void appendWhereClause(StringBuilder queryBuilder, List<WhereClause> whereClauseList) {
        if (whereClauseList == null || whereClauseList.isEmpty()) {
            return;
        }
        queryBuilder.append(DaoConstants.WHERE_CLAUSE);
        for (int i = 0; i < whereClauseList.size(); i++) {
            WhereClause whereClause = whereClauseList.get(i);
            if (i > 0) {
                queryBuilder.append(" ").append(whereClause.getOperator().name()).append(" ");
            }
            queryBuilder.append("(").append(whereClause.getExpression()).append(")");
        }
    }

    /**
     * Append group by clause(s) if any.
     *
     * @param queryBuilder  Query buffer
     * @param groupByClause GrouBy clause(s)
     */
    private void appendGroupByClause(StringBuilder queryBuilder, String... groupByClause) {
        if (groupByClause == null || groupByClause.length == 0) {
            return;
        }
        queryBuilder.append(" group by ").append(groupByClause[0]);
    }

    /**
     * Append orderBy clause in SQL.
     *
     * @param queryBuilder      Query buffer
     * @param orderByClauseList OrderBy clause list
     */
    private void appendOrderByClause(StringBuilder queryBuilder, List<OrderByClause> orderByClauseList) {
        if (orderByClauseList == null || orderByClauseList.isEmpty()) {
            return;
        }
        queryBuilder.append(" order by");
        for (int i = 0; i < orderByClauseList.size(); i++) {
            OrderByClause orderByClause = orderByClauseList.get(i);
            if (i > 0) {
                queryBuilder.append(",");
            }
            queryBuilder.append(" ")
                    .append(orderByClause.getExpression())
                    .append(" ")
                    .append(orderByClause.getOrderDirection().name().toUpperCase());
        }
    }

    /**
     * Build parameters from where clauses list.
     *
     * @param whereClauseList Where clause list
     * @return Parameters
     */
    protected Map<String, Object> buildParams(List<WhereClause> whereClauseList) {
        final Map<String, Object> params = new HashMap<>();
        whereClauseList.forEach(whereClause -> {
            if (whereClause.getParamName() != null && whereClause.getParamValue() != null) {
                params.put(whereClause.getParamName(), whereClause.getParamValue());
            }
            if (!CollectionUtils.isEmpty(whereClause.getParamNames()) && !CollectionUtils.isEmpty(whereClause.getParamValues())) {
                int inc = 0;
                for (var pName : whereClause.getParamNames()) {
                    params.put(pName, whereClause.getParamValues().get(inc));
                    inc++;
                }
            }
        });
        return params;
    }

    /**
     * Execute SQL exists (select count > 0).
     *
     * @param query  Query
     * @param params Parameters
     * @return Boolean
     */
    public boolean executeExists(String query, Map<String, Object> params) {
        Integer nbResults = getNamedParameterJdbcTemplate().query(query, params, rs -> {
            Integer nbr = null;
            if (rs.next()) {
                nbr = rs.getInt(1);
            }
            return nbr;
        });
        return nbResults != null && nbResults > 0;
    }

    /**
     * Extract id generated from sequence.
     *
     * @param keyHolder   KeyHolder
     * @param targetField Target column name (usually 'id')
     * @return Generated id
     */
    public Long extractGeneratedId(KeyHolder keyHolder, String targetField) {
        if (keyHolder != null && keyHolder.getKeys() != null && keyHolder.getKeys().get(targetField) != null) {
            return Long.valueOf(keyHolder.getKeys().get(targetField).toString());
        } else {
            return null;
        }
    }

    /**
     * Build postgreSQL object from json value.
     *
     * @param json Json value
     * @return PGobject
     * @throws SQLException SQL exception
     */
    public PGobject buildPGobject(String json) throws SQLException {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("json");
        jsonObject.setValue(json);
        return jsonObject;
    }

    /**
     * Build SQL equals expression.
     *
     * @param columnName Column name
     * @param paramName  Parameter name
     * @return SQL equals expression
     */
    protected String buildSQLEqualsExpression(String columnName, String paramName) {
        return columnName + "=:" + paramName;
    }

    /**
     * Build SQL in expression.
     *
     * @param columnName Column name
     * @param paramName  Parameter name
     * @return SQL in expression
     */
    protected String buildSQLInExpression(String columnName, String paramName) {
        return columnName + " in (:" + paramName + ")";
    }

    protected <T> T processWithSpan(String instrumentationName, String action, Function<Span, T> operation) {
        Span span = openTelemetryWrapper.withSpan(instrumentationName, action + "-" + correlationKey());
        try {
            return operation.apply(span);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

}
