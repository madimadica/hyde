package com.madimadica.hyde.ast;


import java.util.Objects;

public abstract sealed class BlockNode extends Node permits ContainerBlockNode, LeafBlockNode {
    protected boolean open;

    public BlockNode() {
        this.open = true;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isClosed() {
        return !open;
    }

    public void open() {
        this.open = true;
    }

    public void close() {
        this.open = false;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public BlockNode getParent() {
        return (BlockNode) this.parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BlockNode blockNode = (BlockNode) o;
        return open == blockNode.open;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), open);
    }

    @Override
    public String toString() {
        String positions = super.getPositions().toString();
        String className = getClass().getSimpleName();
        return className + "(" + positions + ")";
    }

}
