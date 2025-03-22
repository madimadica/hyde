package com.madimadica.hyde.ast;

import java.util.Objects;

public final class InlineCodeNode extends InlineLeafNode {
    private final String literal;

    public InlineCodeNode(String literal) {
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InlineCodeNode events = (InlineCodeNode) o;
        return Objects.equals(literal, events.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), literal);
    }
}
