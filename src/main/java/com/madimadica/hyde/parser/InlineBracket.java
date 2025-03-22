package com.madimadica.hyde.parser;

import com.madimadica.hyde.ast.InlineTextNode;

public class InlineBracket {
    InlineTextNode textNode;
    NodeStack.Node<InlineDelimiter> prevDelim;
    int index;
    boolean isImage;
    boolean active = true;
    boolean bracketAfter = false;

    public boolean isLink() {
        return !isImage;
    }
}
