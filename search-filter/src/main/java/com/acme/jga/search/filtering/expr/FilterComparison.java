package com.acme.jga.search.filtering.expr;

import java.util.Arrays;
import java.util.Optional;

public enum FilterComparison {

    GREATER_THAN("gt", ">"),
    LOWER_THAN("lt", "<"),
    GREATER_OR_EQUALS("ge", ">="),
    LOWER_OR_EQUALS("le", "<="),
    EQUALS("eq", "="),
    NOT_EQUALS("ne", "!="),
    AND("and", "and"),
    OR("or", "or"),
    LIKE("lk", "like");

    final String inputParam;
    final String sqlParam;

    FilterComparison(String input, String sql) {
        this.inputParam = input;
        this.sqlParam = sql;
    }

    public String getInputParam() {
        return this.inputParam;
    }

    public String getSqlParam() {
        return this.sqlParam;
    }

    public static FilterComparison fromValueParam(String valueParam) {
        Optional<FilterComparison> targetComp = Arrays.stream(FilterComparison.values())
                .filter(f -> f.getInputParam().equalsIgnoreCase(valueParam)).findFirst();
        if (targetComp.isEmpty()) {
            throw new RuntimeException("Invalid comparison [" + valueParam + "]");
        }
        return targetComp.orElse(null);
    }

}
