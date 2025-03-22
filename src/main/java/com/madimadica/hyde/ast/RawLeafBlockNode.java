package com.madimadica.hyde.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract sealed class RawLeafBlockNode
        extends ContentLeafBlockNode
        implements AcceptsLines
        permits
        FencedCodeBlockNode,
        IndentedCodeBlockNode,
        HTMLBlockNode
{
    protected List<String> rawLines = new ArrayList<>();

    @Override
    public void acceptLine(String line) {
        rawLines.add(line);
    }

    public List<String> getRawLines() {
        return rawLines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RawLeafBlockNode events = (RawLeafBlockNode) o;
        return Objects.equals(rawLines, events.rawLines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rawLines);
    }
}
