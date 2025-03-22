package com.madimadica.hyde.ast;


import java.util.Objects;

public abstract sealed class ContentLeafBlockNode
        extends LeafBlockNode
        permits
        InlineLeafBlockNode,
        RawLeafBlockNode {

    /**
     * The literal, unparsed and unescaped content.
     * <p>
     * In the case of raw content nodes, this should
     * represent the unescaped HTML.
     * </p>
     * <p>
     * In the case of inline content nodes, this should
     * represent the unparsed and unescaped inline content.
     * </p>
     */
    protected String literal = "";

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
        ContentLeafBlockNode events = (ContentLeafBlockNode) o;
        return Objects.equals(literal, events.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), literal);
    }
}
