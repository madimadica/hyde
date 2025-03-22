package com.madimadica.hyde.ast;

public abstract sealed class LeafBlockNode
        extends BlockNode
        permits
        ContentLeafBlockNode,
        LinkReferenceDefinitionNode,
        BlankLineNode,
        ThematicBreakNode
{

}
