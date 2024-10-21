package com.madimadica.hyde.syntax;

public abstract sealed class LeafBlock extends ASTNode permits
        ATXHeading,
        SetextHeading,
        ThematicBreak {

}
