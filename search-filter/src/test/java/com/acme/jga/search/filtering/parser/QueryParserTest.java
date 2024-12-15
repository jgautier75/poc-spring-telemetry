package com.acme.jga.search.filtering.parser;

import com.acme.jga.search.filtering.exceptions.ParsingException;
import com.acme.jga.search.filtering.expr.ExpressionType;
import com.acme.jga.search.filtering.utils.ParsingResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryParserTest {
    private final QueryParser queryParser = new QueryParser();

    @Test
    public void parseNominalComplex() {
        String query = "(name eq 'toto' and gender ne 'm') or (name eq 'tata' and gender eq 'f')";
        ParsingResult parsingResult = queryParser.parseQuery(query);
        queryParser.validate(parsingResult);
        assertNotNull(parsingResult, "Parsing result not null");
        assertNotNull(parsingResult.getExpressions(), "Expressions not null");
        assertEquals(19, parsingResult.getExpressions().size(), "9 expressions");
        assertSame(parsingResult.getExpressions().get(0).getType(), ExpressionType.OPENING_PARENTHESIS, "Opening parenthesis");
        assertTrue(parsingResult.getExpressions().get(1).getType() == ExpressionType.PROPERTY && "name".equals(parsingResult.getExpressions().get(1).getValue()), "Property name found");
        assertTrue(parsingResult.getExpressions().get(2).getType() == ExpressionType.COMPARISON && "eq".equals(parsingResult.getExpressions().get(2).getValue()), "Eq comparison found");
        assertTrue(parsingResult.getExpressions().get(3).getType() == ExpressionType.VALUE && "'toto'".equals(parsingResult.getExpressions().get(3).getValue()), "'toto' value found");
        assertTrue(parsingResult.getExpressions().get(4).getType() == ExpressionType.OPERATOR && "and".equals(parsingResult.getExpressions().get(4).getValue()), "And operator found");
        assertTrue(parsingResult.getExpressions().get(5).getType() == ExpressionType.PROPERTY && "gender".equals(parsingResult.getExpressions().get(5).getValue()), "Property gender found");
        assertTrue(parsingResult.getExpressions().get(6).getType() == ExpressionType.COMPARISON && "ne".equals(parsingResult.getExpressions().get(6).getValue()), "Ne comparison found");
        assertTrue(parsingResult.getExpressions().get(7).getType() == ExpressionType.VALUE && "'m'".equals(parsingResult.getExpressions().get(7).getValue()), "'m' value found");
        assertSame(parsingResult.getExpressions().get(8).getType(), ExpressionType.CLOSING_PARENTEHSIS, "Closing parenthesis");
    }

    @Test
    public void parseNominalSimple() {
        String query = "name eq 'test'";
        ParsingResult parsingResult = queryParser.parseQuery(query);
        queryParser.validate(parsingResult);
        assertNotNull(parsingResult, "Parsing result not null");
    }

    @Test
    public void parseInvalidComparison() {
        String query = "name neq 'test'";
        ParsingException parsingException = assertThrows(ParsingException.class, () -> queryParser.parseQuery(query));
        assertEquals(parsingException.getMessage(), "mismatched input 'neq' expecting COMPARISON");
    }

    @Test
    public void parseInvalidExpression() {
        String query = "name eq 'test' gender eq 'm'";
        ParsingResult parsingResult = queryParser.parseQuery(query);
        Exception thrownEx = null;
        try {
            queryParser.validate(parsingResult);
        } catch (RuntimeException e) {
            thrownEx = e;
        }
        assertNotNull(thrownEx, "Exception not null");
        assertEquals("Expected operator or closing parenthesis at index (4)", thrownEx.getMessage());
    }

    @Test
    public void parseInvalidNbExpressions() {
        String query = "name eq 'test' and ";
        ParsingResult parsingResult = null;
        Throwable thrownEx = null;
        try {
            parsingResult = queryParser.parseQuery(query);
            queryParser.validate(parsingResult);
        } catch (Throwable e) {
            thrownEx = e;
        }
        assertNotNull(thrownEx, "Exception not null");
    }
}