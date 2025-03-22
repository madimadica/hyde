package com.madimadica.hyde.parser;

public record ParserOptions(
        boolean smartQuotes,
        boolean gcOriginalInlines,
        boolean safeMode,
        String safeModeText,
        String codeInfoPrefix,
        String softBreak
) {

    public static ParserOptions getDefaults() {
        return new ParserOptions(false, false, false, "<!-- SAFE MODE -->", "language-", "\n"); // TODO change back to false, true, true
    }

    // TODO builder pattern

}
