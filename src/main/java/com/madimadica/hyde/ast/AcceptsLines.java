package com.madimadica.hyde.ast;

public sealed interface AcceptsLines permits ParagraphNode, RawLeafBlockNode {
    void acceptLine(String line);
}
