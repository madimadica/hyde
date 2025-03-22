package com.madimadica.hyde.ast;

public abstract sealed class InlineLeafNode
        extends InlineNode
        permits
        InlineCodeNode,
        InlineHTMLNode,
        InlineHardBreakNode,
        InlineSoftBreakNode,
        InlineTextNode
{
}
