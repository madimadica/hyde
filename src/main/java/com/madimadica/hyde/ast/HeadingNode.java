package com.madimadica.hyde.ast;

import java.util.Objects;

public sealed abstract class HeadingNode
        extends InlineLeafBlockNode
        permits
        ATXHeadingNode,
        SetextHeadingNode
{
    private final int level;

    public HeadingNode(int level, int max) {
        if (level < 1 || level > max) {
            throw new IllegalArgumentException("Heading level must be between levels 1 and " + max + ". Instead found " + level);
        }
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HeadingNode events = (HeadingNode) o;
        return level == events.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), level);
    }

    @Override
    public String toString() {
        return super.toString() + ": h" + level + ", literal='" + super.literal + "'";
    }
}
