package com.madimadica.hyde.parser;

public record SourcePosition(int line, int column) {
    @Override
    public String toString() {
        return line + ":" + column;
    }
}
