package com.acme.jga.jdbc.dql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class OrderByClause {
	private String expression;
	private OrderDirection orderDirection;
}
