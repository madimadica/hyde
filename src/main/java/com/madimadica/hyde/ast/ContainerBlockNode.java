package com.madimadica.hyde.ast;

public abstract sealed class ContainerBlockNode
        extends BlockNode
        implements NodeContainer
        permits BlockQuoteNode, DocumentNode, ListItemNode, ListNode {

}
