package com.madimadica.hyde.parser;


import java.util.Objects;

public class SourcePositions {
    public SourcePosition start;
    public SourcePosition end;

    public SourcePositions() {
        this(1, 1);
    }

    public SourcePositions(int startLine, int startCol) {
        start = new SourcePosition(startLine, startCol);
        end = new SourcePosition(0, 0);
    }

    public SourcePositions(int startLine, int startCol, int endLine, int endCol) {
        start = new SourcePosition(startLine, startCol);
        end = new SourcePosition(endLine, endCol);
    }

    public SourcePosition getStart() {
        return start;
    }

    public void setStart(SourcePosition start) {
        this.start = start;
    }

    public void setStart(int line, int column) {
        this.start = new SourcePosition(line, column);
    }

    public SourcePosition getEnd() {
        return end;
    }

    public void setEnd(SourcePosition end) {
        this.end = end;
    }

    public void setEnd(int line, int column) {
        this.end = new SourcePosition(line, column);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourcePositions that = (SourcePositions) o;
        return Objects.equals(start, that.start) && Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return start + "-" + end;
    }
}

