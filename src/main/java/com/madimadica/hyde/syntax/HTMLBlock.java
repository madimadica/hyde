package com.madimadica.hyde.syntax;

import java.util.List;
import java.util.Objects;

/**
 * AST Node representing an <a href="https://spec.commonmark.org/0.31.2/#html-blocks">HTML Block</a>
 * <br>
 * An HTML block is a group of lines that is treated as raw HTML (and will not be escaped in HTML output).
 */
public final class HTMLBlock extends LeafBlock {


    private final List<String> rawLines;

    public HTMLBlock(List<String> rawLines) {
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
        HTMLBlock htmlBlock = (HTMLBlock) o;
        return Objects.equals(rawLines, htmlBlock.rawLines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawLines);
    }

    @Override
    public String toString() {
        return "HTMLBlock{" +
                "rawLines=" + rawLines +
                '}';
    }
}
