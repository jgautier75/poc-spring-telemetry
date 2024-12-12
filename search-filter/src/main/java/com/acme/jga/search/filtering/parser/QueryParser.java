package com.acme.jga.search.filtering.parser;

import com.acme.jga.search.filtering.antlr.FilterLexer;
import com.acme.jga.search.filtering.antlr.FilterParser;
import com.acme.jga.search.filtering.expr.Expression;
import com.acme.jga.search.filtering.expr.ExpressionType;
import com.acme.jga.search.filtering.listener.AntlrErrorListener;
import com.acme.jga.search.filtering.listener.SearchFilterListener;
import com.acme.jga.search.filtering.utils.ParsingResult;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.util.ObjectUtils;

import java.util.Collections;

public class QueryParser {

    private static final int MIN_EXPRESSION_LENGTH = 3;
    private static final int[] OPENINGP_PROPERTY_IDX = {0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96, 100};
    private static final int[] COMPARISON_IDX = {1, 5, 9, 13, 17, 21, 25, 29, 33, 37, 41, 45, 49, 53, 57, 61, 65, 69, 73, 77, 81, 85, 89, 93, 97};
    private static final int[] VALUE_IDX = {2, 6, 10, 14, 18, 22, 26, 30, 34, 38, 42, 46, 50, 54, 58, 62, 66, 70, 74, 78, 82, 86, 90, 94, 98};
    private static final int[] OPERATOR_CLOSINGP_IDX = {3, 7, 11, 15, 19, 23, 27, 31, 35, 39, 43, 47, 51, 55, 59, 63, 67, 71, 75, 79, 83, 87, 91, 95, 99};

    public ParsingResult parseQuery(String query) {
        if (ObjectUtils.isEmpty(query)) {
            return new ParsingResult(Collections.emptyList(), Collections.emptyList(), true);
        }
        CodePointCharStream stream = CharStreams.fromString(query);
        FilterLexer lexer = new FilterLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FilterParser parser = new FilterParser(tokens);
        parser.removeErrorListeners();
        AntlrErrorListener antlrErrorListener = new AntlrErrorListener();
        parser.addErrorListener(antlrErrorListener);
        SearchFilterListener searchFilterListener = new SearchFilterListener();
        new ParseTreeWalker().walk(searchFilterListener, parser.filter());
        return new ParsingResult(searchFilterListener.getExpressions(), searchFilterListener.getErrors(), false);
    }

    public void validate(ParsingResult parsingResult) throws RuntimeException {
        if (parsingResult.getExpressions().size() < MIN_EXPRESSION_LENGTH) {
            throw new RuntimeException("Invalid search expression, expected at least 3 elements (property comparison value");
        }

        int shiftInc = 0;
        for (int inc = 0; inc < parsingResult.getExpressions().size(); inc++) {
            if (shouldExpectParenthesisOrProperty(inc, shiftInc)
                    && !isOpeningParenthesis(parsingResult.getExpressions().get(inc))
                    && !isClosingParenthesis(parsingResult.getExpressions().get(inc))
                    && !isProperty(parsingResult.getExpressions().get(inc))) {
                throw new RuntimeException("Expected opening parenthesis, property or closing parenthesis at index (" + (inc + 1) + ")");
            }
            if (shouldExpectComparison(inc, shiftInc) && !isComparison(parsingResult.getExpressions().get(inc))) {
                throw new RuntimeException("Expected comparison at index (" + (inc + 1) + ")");
            }
            if (shouldExpectValue(inc, shiftInc) && !isValue(parsingResult.getExpressions().get(inc))) {
                throw new RuntimeException("Expected value at index (" + (inc + 1) + ")");
            }
            if (shouldExpectOperator(inc, shiftInc) && !isOperator(parsingResult.getExpressions().get(inc)) && !isClosingParenthesis(parsingResult.getExpressions().get(inc))) {
                throw new RuntimeException("Expected operator or closing parenthesis at index (" + (inc + 1) + ")");
            }

            // If expression is an opening parenthesis or a closing parenthesis, right shift values by 1
            if (isOpeningParenthesis(parsingResult.getExpressions().get(inc)) || isClosingParenthesis(parsingResult.getExpressions().get(inc))) {
                shiftInc++;
            }
        }
    }

    private boolean isOpeningParenthesis(Expression expression) {
        return ExpressionType.OPENING_PARENTHESIS.equals(expression.getType());
    }

    private boolean isProperty(Expression expression) {
        return ExpressionType.PROPERTY.equals(expression.getType());
    }

    private boolean isComparison(Expression expression) {
        return ExpressionType.COMPARISON.equals(expression.getType());
    }

    private boolean isValue(Expression expression) {
        return ExpressionType.VALUE.equals(expression.getType());
    }

    private boolean isClosingParenthesis(Expression expression) {
        return ExpressionType.CLOSING_PARENTEHSIS.equals(expression.getType());
    }

    private boolean isOperator(Expression expression) {
        return ExpressionType.OPERATOR.equals(expression.getType());
    }

    private boolean shouldExpectParenthesisOrProperty(int inc, int shiftInc) {
        boolean expected = false;
        for (int pptyVal : OPENINGP_PROPERTY_IDX) {
            if ((pptyVal + shiftInc) == inc) {
                expected = true;
                break;
            }
        }
        return expected;
    }

    private boolean shouldExpectComparison(int inc, int shiftInc) {
        boolean expected = false;
        for (int pptyVal : COMPARISON_IDX) {
            if ((pptyVal + shiftInc) == inc) {
                expected = true;
                break;
            }
        }
        return expected;
    }

    private boolean shouldExpectValue(int inc, int shiftInc) {
        boolean expected = false;
        for (int pptyVal : VALUE_IDX) {
            if ((pptyVal + shiftInc) == inc) {
                expected = true;
                break;
            }
        }
        return expected;
    }

    private boolean shouldExpectOperator(int inc, int shiftInc) {
        boolean expected = false;
        for (int pptyVal : OPERATOR_CLOSINGP_IDX) {
            if ((pptyVal + shiftInc) == inc) {
                expected = true;
                break;
            }
        }
        return expected;
    }

}
