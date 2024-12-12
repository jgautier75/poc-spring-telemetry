package com.acme.jga.jdbc.dql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class WhereClause {
	private String expression;
	private WhereOperator operator;
	private String paramName;
	private Object paramValue;
	private List<String> paramNames;
	private List<Object> paramValues;
}
