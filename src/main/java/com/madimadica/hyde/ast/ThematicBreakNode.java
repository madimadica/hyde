package com.madimadica.hyde.ast;

public final class ThematicBreakNode extends LeafBlockNode {

    public ThematicBreakNode() {
        super.close();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ThematicBreakNode;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
