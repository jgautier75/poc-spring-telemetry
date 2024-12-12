package com.acme.jga.search.filtering.listener;

import com.acme.jga.search.filtering.exceptions.ParsingException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class AntlrErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throw new ParsingException(recognizer,offendingSymbol,line,charPositionInLine,msg,e);
    }

}
