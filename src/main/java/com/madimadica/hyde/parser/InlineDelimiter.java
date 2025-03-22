package com.madimadica.hyde.parser;

import com.madimadica.hyde.ast.InlineTextNode;

/**
 * Based around a <a href="https://spec.commonmark.org/0.31.2/#an-algorithm-for-parsing-nested-emphasis-and-links">CommonMark Spec Algorithm</a>
 */
public class InlineDelimiter {
    InlineTextNode textNode;
    int currentRun;

    final char type;
    final boolean potentialOpener;
    final boolean potentialCloser;
    final int originalRun;
    final int originalRunMod3;
    final int customHash;

    public InlineDelimiter(char type, int runLength, boolean potentialOpener, boolean potentialCloser) {
        this.type = type;
        this.currentRun = runLength;
        this.originalRun = runLength;
        this.originalRunMod3 = runLength % 3;
        this.potentialOpener = potentialOpener;
        this.potentialCloser = potentialCloser;
        this.customHash = (potentialOpener ? 3 : 0) + originalRunMod3;
    }

    public boolean hasPotential() {
        return potentialOpener || potentialCloser;
    }
}
