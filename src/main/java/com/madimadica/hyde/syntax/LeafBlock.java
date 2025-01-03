package com.madimadica.hyde.syntax;

public abstract sealed class LeafBlock extends ASTNode permits
        ATXHeading,
        IndentedCodeBlock,
        FencedCodeBlock,
        HTMLBlock,
        SetextHeading,
        ThematicBreak,
        LinkReferenceDefinition {

}
