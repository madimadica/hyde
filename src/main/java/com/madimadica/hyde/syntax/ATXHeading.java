package com.madimadica.hyde.syntax;

import java.util.Objects;

/**
 * AST Node representing an <a href="https://spec.commonmark.org/0.31.2/#atx-headings">ATX Heading</a>.
 *
 * @see com.madimadica.hyde.parsing.parsers.ATXHeadingParser
 */
public final class ATXHeading extends LeafBlock {

    private final int level;
    private final String content; // TODO parse inline content

    public ATXHeading(int level) {
        this(level, "");
    }

    public ATXHeading(int level, String content) {
        this.level = level;
        this.content = content;
    }

    public int getLevel() {
        return level;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ATXHeading that = (ATXHeading) o;
        return level == that.level && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, content);
    }

    @Override
    public String toString() {
        return "ATXHeading{" +
                "level=" + level +
                ", content='" + content + '\'' +
                '}';
    }
}
