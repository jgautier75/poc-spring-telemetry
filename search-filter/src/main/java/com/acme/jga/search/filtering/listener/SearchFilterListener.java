package com.acme.jga.search.filtering.listener;

import com.acme.jga.search.filtering.antlr.FilterLexer;
import com.acme.jga.search.filtering.antlr.FilterListener;
import com.acme.jga.search.filtering.antlr.FilterParser;
import com.acme.jga.search.filtering.expr.Expression;
import com.acme.jga.search.filtering.expr.ExpressionType;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class SearchFilterListener implements FilterListener {
    private final List<Expression> expressions = new ArrayList<>();
    private final List<ErrorNode> errors = new ArrayList<>();

    public List<Expression> getExpressions() {
        return this.expressions;
    }

    public List<ErrorNode> getErrors() {
        return this.errors;
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        int symbolType = node.getSymbol().getType();
        switch (symbolType) {
            case FilterLexer.AND:
            case FilterLexer.OR:
                expressions.add(new Expression(ExpressionType.OPERATOR, node.getText()));
                break;
            case FilterLexer.COMPARISON:
                expressions.add(new Expression(ExpressionType.COMPARISON, node.getText()));
                break;
            case FilterLexer.CPAR:
                expressions.add(new Expression(ExpressionType.CLOSING_PARENTEHSIS, node.getText()));
                break;
            case FilterLexer.OPAR:
                expressions.add(new Expression(ExpressionType.OPENING_PARENTHESIS, node.getText()));
                break;
            case FilterLexer.VALUE:
                expressions.add(new Expression(ExpressionType.VALUE, node.getText()));
                break;
            case FilterLexer.PROPERTY:
                expressions.add(new Expression(ExpressionType.PROPERTY, node.getText()));
                break;
            case FilterLexer.NOT:
                expressions.add(new Expression(ExpressionType.NEGATION, node.getText()));
                break;
            default:
                break;
        }
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        this.errors.add(node);
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        // N/A
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        // N/A
    }

    @Override
    public void enterFilter(FilterParser.FilterContext ctx) {
        // N/A
    }

    @Override
    public void exitFilter(FilterParser.FilterContext ctx) {
        // N/A
    }

    @Override
    public void enterExpr(FilterParser.ExprContext ctx) {
        // N/A
    }

    @Override
    public void exitExpr(FilterParser.ExprContext ctx) {
        // N/A
    }

}
