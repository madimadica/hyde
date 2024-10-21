package com.madimadica.hyde.syntax;

import java.util.List;
import java.util.Objects;

/**
 * AST Node representing a <a href="https://spec.commonmark.org/0.31.2/#setext-headings">Setext Heading</a>.
 *
 * @see com.madimadica.hyde.parsing.parsers.SetextHeadingParser
 */
public final class SetextHeading extends LeafBlock {
    private final List<String> rawContent; // TODO inline parse
    private final int level;

    public SetextHeading(List<String> rawContent, int level) {
        this.rawContent = List.copyOf(rawContent);
        this.level = level;
    }

    public List<String> getRawContent() {
        return rawContent;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetextHeading that = (SetextHeading) o;
        return level == that.level && Objects.equals(rawContent, that.rawContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawContent, level);
    }

    @Override
    public String toString() {
        return "SetextHeading{" +
                "content='" + rawContent + '\'' +
                ", level=" + level +
                '}';
    }
}
