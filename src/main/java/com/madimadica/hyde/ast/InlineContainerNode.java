package com.madimadica.hyde.ast;

public abstract sealed class InlineContainerNode
        extends InlineNode
        implements NodeContainer
        permits
        InlineBoldNode,
        InlineImageNode,
        InlineItalicNode,
        InlineLinkNode
{

}
