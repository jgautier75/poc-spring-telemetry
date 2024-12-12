package com.acme.jga.search.filtering.utils;

import com.acme.jga.search.filtering.expr.Expression;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.tree.ErrorNode;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ParsingResult {
    private List<Expression> expressions;
    private List<ErrorNode> errorNodes;
    private boolean empty;
}
