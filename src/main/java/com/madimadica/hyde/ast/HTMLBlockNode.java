package com.madimadica.hyde.ast;

import java.util.Objects;

public final class HTMLBlockNode extends RawLeafBlockNode {
    private final int typeId;

    public HTMLBlockNode(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public boolean allowsBlankLines() {
        return 1 <= typeId && typeId <= 5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HTMLBlockNode events = (HTMLBlockNode) o;
        return typeId == events.typeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), typeId);
    }
}
