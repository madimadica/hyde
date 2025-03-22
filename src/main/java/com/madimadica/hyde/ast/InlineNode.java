package com.madimadica.hyde.ast;

public abstract sealed class InlineNode
        extends Node
        permits
        InlineContainerNode,
        InlineLeafNode
{
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
