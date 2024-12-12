package com.acme.jga.search.filtering.expr;

public enum ExpressionType {
    /**
     * Property (name in example below).
     * e.g: name eq 'test'
     */
    PROPERTY,
    /**
     * Comparison.
     * e.g: eq, ne, lt, gt, ...
     */
    COMPARISON,
    /**
     * Operator (AND OR).
     */
    OPERATOR,
    /**
     * Negation (NOT).
     */
    NEGATION,
    /**
     * Opening parenthesis.
     */
    OPENING_PARENTHESIS,
    /**
     * Closing parenthesis.
     */
    CLOSING_PARENTEHSIS,
    /**
     * Value ('test' in example below).
     * e.g: name eq 'test'
     */
    VALUE;
}
