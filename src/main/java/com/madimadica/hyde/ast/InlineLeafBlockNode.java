package com.madimadica.hyde.ast;

public abstract sealed class InlineLeafBlockNode
        extends ContentLeafBlockNode
        implements NodeContainer
        permits
        HeadingNode,
        ParagraphNode
{

}
