package com.madimadica.hyde.ast;

import java.util.Objects;

public final class FencedCodeBlockNode extends RawLeafBlockNode {
    private final String infoString;
    private final char fenceType;
    private final int fenceLength;
    private final int fenceOffset;

    public FencedCodeBlockNode(String infoString, char fenceType, int fenceLength, int fenceOffset) {
        this.infoString = infoString;
        this.fenceType = fenceType;
        this.fenceLength = fenceLength;
        this.fenceOffset = fenceOffset;
    }

    public String getInfoString() {
        return infoString;
    }

    public boolean hasInfoString() {
        return infoString != null && !infoString.isEmpty();
    }

    public char getFenceType() {
        return fenceType;
    }

    public int getFenceLength() {
        return fenceLength;
    }

    public int getFenceOffset() {
        return fenceOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FencedCodeBlockNode events = (FencedCodeBlockNode) o;
        return fenceType == events.fenceType && fenceLength == events.fenceLength && fenceOffset == events.fenceOffset && Objects.equals(infoString, events.infoString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), infoString, fenceType, fenceLength, fenceOffset);
    }
}
