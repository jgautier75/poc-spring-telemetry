package com.acme.jga.infra.dao.processors;

import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.domain.model.filtering.FilteringConstants;
import com.acme.jga.domain.model.utils.DataType;
import com.acme.jga.domain.model.utils.KeyValuePair;
import com.acme.jga.domain.model.v1.OrganizationKind;
import com.acme.jga.domain.model.v1.OrganizationMetaData;
import com.acme.jga.jdbc.spring.AbstractJdbcDaoSupport;
import com.acme.jga.jdbc.utils.DaoConstants;
import com.acme.jga.search.filtering.expr.Expression;
import com.acme.jga.search.filtering.expr.FilterComparison;
import com.acme.jga.search.filtering.utils.ParsingResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class ExpressionsProcessor {

    /**
     * Compute pagination and orderBy SQL instructions.
     *
     * @param searchParams Search parameters
     * @return Pagination result
     */
    private AbstractJdbcDaoSupport.PaginationResult computePagination(Map<String, Object> searchParams, Map<String, KeyValuePair> columnsDefinitionsByAlias) {
        int pageIndex = getPageIndex(searchParams);
        int pageSize = getPageSize(searchParams);
        int start = (pageIndex - 1) * pageSize;

        String pagination = String.format(DaoConstants.PAGINATION_PATTERN, pageSize, start);
        String orderBy = getOrderBy(searchParams, columnsDefinitionsByAlias);

        return new AbstractJdbcDaoSupport.PaginationResult(pagination, orderBy);
    }

    /**
     * Translate expressions to SQL.
     *
     * @param expressions Expression list
     * @param params      Map<Parameters Name, Parameters Value> for SQL statements
     * @param sqlBuffer   SQL query buffer
     */
    private void buildSqlFromExpressions(List<Expression> expressions, Map<String, Object> params, StringBuilder sqlBuffer, Map<String, KeyValuePair> domainEntityMetaData) {
        int index = 0;
        StringBuilder paramName = new StringBuilder();
        StringBuilder propertyName = new StringBuilder();
        boolean isTypeNumber = false;
        boolean isLikeOperator = false;

        for (Expression expression : expressions) {
            switch (expression.getType()) {
                case OPENING_PARENTHESIS:
                    sqlBuffer.append(" ( ");
                    break;
                case COMPARISON:
                    isLikeOperator = isLikeOperator(expression);
                    sqlBuffer.append(" ").append(getSqlParam(expression)).append(" ");
                    break;
                case CLOSING_PARENTEHSIS:
                    sqlBuffer.append(" ) ");
                    break;
                case NEGATION:
                    sqlBuffer.append(" not ");
                    break;
                case OPERATOR:
                    sqlBuffer.append(" ").append(getSqlParam(expression)).append(" ");
                    break;
                case PROPERTY:
                    propertyName.setLength(0);
                    propertyName.append(stripEnclosingQuotes(expression.getValue()));
                    appendPropertyToSql(expression, domainEntityMetaData, paramName, sqlBuffer, index);
                    isTypeNumber = isNumberType(domainEntityMetaData, propertyName.toString());
                    break;
                case VALUE:
                    appendValueToSql(expression, params, sqlBuffer, paramName, isTypeNumber, isLikeOperator);
                    isTypeNumber = false;
                    isLikeOperator = false;
                    break;
                default:
                    break;
            }
            index++;
        }
    }

    /**
     * Build query based on search params.
     *
     * @param searchParams Search params
     * @return Composite object with where clause, pagination and order by
     */
    public AbstractJdbcDaoSupport.CompositeQuery buildFilterQuery(Map<String, Object> params, Map<String, Object> searchParams, Map<String, KeyValuePair> domainEntityMetaData) {
        ParsingResult parsingResult = (ParsingResult) searchParams.get(FilteringConstants.PARSING_RESULTS);
        StringBuilder sqlBuffer = new StringBuilder();
        boolean hasExpressions = parsingResult != null && !CollectionUtils.isEmpty(parsingResult.getExpressions());
        if (hasExpressions) {
            buildSqlFromExpressions(parsingResult.getExpressions(), params, sqlBuffer, domainEntityMetaData);
        }

        // Compute pagination
        AbstractJdbcDaoSupport.PaginationResult paginationResult = computePagination(searchParams, domainEntityMetaData);
        return new AbstractJdbcDaoSupport.CompositeQuery(hasExpressions, sqlBuffer.toString(), params, paginationResult.pagination(), paginationResult.orderBy());
    }

    /**
     * Strip enclosing single quotes.
     *
     * @param value Value
     * @return Stripped value
     */
    public String stripEnclosingQuotes(String value) {
        if (value != null && value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * Append property for parameter.
     *
     * @param expression     Expression
     * @param paramName      Parameter name
     * @param sqlBuffer      SQL query buffer
     * @param index          Index
     */
    private void appendPropertyToSql(Expression expression, Map<String, KeyValuePair> domainEntityMetaData, StringBuilder paramName, StringBuilder sqlBuffer, int index) {
        String propertyName = stripEnclosingQuotes(expression.getValue());
        KeyValuePair columnNameAndColumnType = domainEntityMetaData.get(propertyName);
        if (columnNameAndColumnType == null) {
            throw new WrappedFunctionalException(new FunctionalException(FunctionalErrorsTypes.INVALID_PROPERTY.name(), null, "Unmapped property named [" + propertyName + "]"));
        }

        sqlBuffer.append(columnNameAndColumnType.getKey());
        paramName.setLength(0);
        paramName.append("p").append(propertyName).append(index);
    }

    /**
     * Append value in SQL instruction.
     *
     * @param sqlBuffer      SQL query buffer
     * @param expression     Expression
     * @param paramName      Parameter name
     * @param params         Map<Parameter Name, Parameter Value>
     * @param isTypeInteger  Is value of type integer
     * @param isLikeOperator Is value of type like
     */
    private void appendValueToSql(Expression expression, Map<String, Object> params, StringBuilder sqlBuffer, StringBuilder paramName, boolean isTypeInteger, boolean isLikeOperator) {
        String value = stripEnclosingQuotes(expression.getValue());
        sqlBuffer.append(":").append(paramName);

        if (OrganizationMetaData.KIND.getAlias().equalsIgnoreCase(paramName.toString())) {
            params.put(paramName.toString(), OrganizationKind.valueOf(value).getCode());
        } else {
            params.put(paramName.toString(), isLikeOperator ? "%" + value + "%" : value);
        }
    }

    /**
     * Translate filter expression to SQL expression.
     *
     * @param expression Expression
     * @return SQL expression
     */
    private String getSqlParam(Expression expression) {
        return Objects.requireNonNull(FilterComparison.fromValueParam(expression.getValue())).getSqlParam();
    }

    /**
     * Compute page index.
     *
     * @param searchParams Search Parameters
     * @return Page index
     */
    private int getPageIndex(Map<String, Object> searchParams) {
        return Optional.ofNullable((Integer) searchParams.get(FilteringConstants.PAGE_INDEX))
                .filter(index -> index > 0)
                .orElse(1);
    }

    /**
     * Compute page size if present, otherwise use default page size.
     *
     * @param searchParams Search parameters
     * @return Page size
     */
    private int getPageSize(Map<String, Object> searchParams) {
        return Optional.ofNullable((Integer) searchParams.get(FilteringConstants.PAGE_SIZE))
                .orElse(DaoConstants.DEFAULT_PAGE_SIZE);
    }

    /**
     * Build orderBy instruction.
     *
     * @param searchParams Search parameters
     * @return OrderBy instruction
     */
    private String getOrderBy(Map<String, Object> searchParams, Map<String, KeyValuePair> columnsDefinitionsByAlias) {
        String orderByParam = (String) searchParams.get(FilteringConstants.ORDER_BY);
        if (StringUtils.isBlank(orderByParam)) {
            return "";
        }

        String orderDirection = orderByParam.substring(0, 1);
        if (!DaoConstants.ORDER_ASC_SIGN.equals(orderDirection) && !DaoConstants.ORDER_DESC_SIGN.equals(orderDirection)) {
            return "";
        }

        String orderColumn = orderByParam.substring(1);
        KeyValuePair columnNameAndType = columnsDefinitionsByAlias.get(orderColumn);
        if (columnNameAndType == null) {
            throw new WrappedFunctionalException(new FunctionalException(FunctionalErrorsTypes.INVALID_PROPERTY.name(), null, "Unknown orderBy named [" + orderColumn + "]"));
        }

        return DaoConstants.ORDER_BY + columnNameAndType.getKey() +
                (DaoConstants.ORDER_ASC_SIGN.equals(orderDirection) ? DaoConstants.ORDER_ASC_KEYWORD : DaoConstants.ORDER_DESC_KEYWORD);
    }

    private boolean isLikeOperator(Expression expression) {
        return FilterComparison.LIKE == FilterComparison.fromValueParam(expression.getValue());
    }

    private boolean isNumberType(Map<String, KeyValuePair> domainEntityMetaData, String propertyName) {
        KeyValuePair columnNameAndColumnType = domainEntityMetaData.get(propertyName);
        return columnNameAndColumnType != null && DataType.NUMBER.name().equals(columnNameAndColumnType.getValue());
    }
}
