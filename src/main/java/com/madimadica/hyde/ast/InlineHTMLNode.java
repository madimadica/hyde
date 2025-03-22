package com.madimadica.hyde.ast;

import java.util.Objects;

public final class InlineHTMLNode extends InlineLeafNode {
    private String literal;

    public InlineHTMLNode(String literal) {
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InlineHTMLNode events = (InlineHTMLNode) o;
        return Objects.equals(literal, events.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), literal);
    }
}
