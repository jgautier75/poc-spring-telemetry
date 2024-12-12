package com.acme.jga.jdbc.dql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaginatedResults<T> {
    private Integer nbResults;
    private Integer nbPages;
    private List<T> results;
    private Integer pageSize;
    private Integer pageIndex;
}
