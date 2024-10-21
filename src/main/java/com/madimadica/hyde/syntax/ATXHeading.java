package com.madimadica.hyde.syntax;

import java.util.Objects;

/**
 * <a href="https://spec.commonmark.org/0.31.2/#atx-headings">CommonMark Spec - ATX headings</a>
 * <br>
 * Defined as:
 * <p>
 * An ATX heading consists of a string of characters, parsed as inline content, between an opening sequence of 1–6 unescaped # characters and an optional closing sequence of any number of unescaped # characters. The opening sequence of # characters must be followed by spaces or tabs, or by the end of line. The optional closing sequence of #s must be preceded by spaces or tabs and may be followed by spaces or tabs only. The opening # character may be preceded by up to three spaces of indentation. The raw contents of the heading are stripped of leading and trailing space or tabs before being parsed as inline content. The heading level is equal to the number of # characters in the opening sequence.
 * </p>
 *
 * <br>
 * For example:
 * <pre>
 # foo
 ## foo
 ### foo
 #### foo
 ##### foo
 ###### foo
 * </pre>
 *
 * Which each translate into this HTML: <code>&lt;hr /&gt;</code>
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
