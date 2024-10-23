package com.madimadica.hyde.syntax;

import java.util.List;
import java.util.Objects;

/**
 * AST Node representing an <a href="https://spec.commonmark.org/0.31.2/#indented-code-blocks">Indented Code Block</a>.
 *
 * @see com.madimadica.hyde.parsing.parsers.IndentedCodeBlockParser
 */
public final class IndentedCodeBlock extends LeafBlock {

    private final List<String> rawLines;

    public IndentedCodeBlock(List<String> rawLines) {
        this.rawLines = List.copyOf(rawLines);
    }

    public List<String> getRawLines() {
        return rawLines;
    }

    public int lineCount() {
        return rawLines.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndentedCodeBlock that = (IndentedCodeBlock) o;
        return Objects.equals(rawLines, that.rawLines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawLines);
    }

    @Override
    public String toString() {
        return "IndentedCodeBlock{" +
                "rawLines=" + rawLines +
                '}';
    }

}
