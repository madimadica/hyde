package com.madimadica.hyde.syntax;


/**
 * AST Node representing a <a href="https://spec.commonmark.org/0.31.2/#thematic-breaks">Thematic Break</a>.
 *
 * @see com.madimadica.hyde.parsing.parsers.ThematicBreakParser
 */
public final class ThematicBreak extends LeafBlock {

    public ThematicBreak() {}

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // Stateless type
        return obj instanceof ThematicBreak;
    }

    @Override
    public String toString() {
        return "ThematicBreak{}";
    }
}
