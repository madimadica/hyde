package com.madimadica.hyde.parser;

public record ParserOptions(
        boolean smartQuotes,
        boolean smartSymbols,
        boolean gcOriginalInlines,
        boolean safeMode,
        String safeModeText,
        String codeInfoPrefix,
        String softBreak
) {

    public static ParserOptions getDefaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean smartQuotes = false;
        private boolean smartSymbols = false;
        private boolean gcOriginalInlines = false;
        private boolean safeMode = false;
        private String safeModeText = "<!-- SAFE MODE -->";
        private String codeInfoPrefix = "language-";
        private String softBreak = "\n";

        public Builder setSmartQuotes(boolean smartQuotes) {
            this.smartQuotes = smartQuotes;
            return this;
        }

        public Builder setSmartSymbols(boolean smartSymbols) {
            this.smartSymbols = smartSymbols;
            return this;
        }

        public Builder setGcOriginalInlines(boolean gcOriginalInlines) {
            this.gcOriginalInlines = gcOriginalInlines;
            return this;
        }

        public Builder setSafeMode(boolean safeMode) {
            this.safeMode = safeMode;
            return this;
        }

        public Builder setSafeModeText(String safeModeText) {
            this.safeModeText = safeModeText;
            return this;
        }

        public Builder setCodeInfoPrefix(String codeInfoPrefix) {
            this.codeInfoPrefix = codeInfoPrefix;
            return this;
        }

        public Builder setSoftBreak(String softBreak) {
            this.softBreak = softBreak;
            return this;
        }

        public ParserOptions build() {
            return new ParserOptions(
                    smartQuotes,
                    smartSymbols,
                    gcOriginalInlines,
                    safeMode,
                    safeModeText,
                    codeInfoPrefix,
                    softBreak
            );
        }
    }

}
