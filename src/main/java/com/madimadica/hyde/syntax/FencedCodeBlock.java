package com.madimadica.hyde.syntax;

import java.util.List;
import java.util.Objects;

public final class FencedCodeBlock extends LeafBlock {
    private final String infoString;
    private final List<String> lines;

    public FencedCodeBlock(String infoString, List<String> lines) {
        this.infoString = infoString;
        this.lines = lines;
    }

    public String getInfoString() {
        return infoString;
    }

    public List<String> getLines() {
        return lines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FencedCodeBlock that = (FencedCodeBlock) o;
        return Objects.equals(infoString, that.infoString) && Objects.equals(lines, that.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(infoString, lines);
    }

    @Override
    public String toString() {
        return "FencedCodeBlock{" +
                "infoString='" + infoString + '\'' +
                ", lines=" + lines +
                '}';
    }
}
