package com.acme.jga.search.filtering.expr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Expression {
    private ExpressionType type;
    private String value;
}
