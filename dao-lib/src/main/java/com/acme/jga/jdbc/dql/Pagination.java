package com.acme.jga.jdbc.dql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class Pagination {
	private Integer pageSize;
	private Integer page;
	private List<OrderByClause> sorts;
}
