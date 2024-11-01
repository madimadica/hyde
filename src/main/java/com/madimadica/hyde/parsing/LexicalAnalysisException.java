package com.madimadica.hyde.parsing;

public class LexicalAnalysisException extends RuntimeException {
    public LexicalAnalysisException() {
    }

    public LexicalAnalysisException(String message) {
        super(message);
    }

    public LexicalAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    public LexicalAnalysisException(Throwable cause) {
        super(cause);
    }

    public LexicalAnalysisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
