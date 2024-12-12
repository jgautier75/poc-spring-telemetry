package com.acme.jga.search.filtering.exceptions;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class ParsingException extends RuntimeException {
    private Recognizer<?, ?> recognizer;
    private Object offendingSymbol;
    private int line;
    private int charPositionInLine;
    private RecognitionException recognitionException;

    public ParsingException(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        super(msg);
        this.charPositionInLine = charPositionInLine;
        this.offendingSymbol = offendingSymbol;
        this.line = line;
        this.recognizer = recognizer;
    }

    public Recognizer<?, ?> getRecognizer() {
        return recognizer;
    }

    public Object getOffendingSymbol() {
        return offendingSymbol;
    }

    public int getLine() {
        return line;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    public RecognitionException getRecognitionException() {
        return recognitionException;
    }

}
