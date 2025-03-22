package com.madimadica.hyde.ast;

// Tag Interface. Must be able to contain children
public sealed interface NodeContainer permits ContainerBlockNode, InlineContainerNode, InlineLeafBlockNode {

}
