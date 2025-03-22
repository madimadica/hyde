package com.madimadica.hyde.ast;

import com.madimadica.hyde.parser.LinkParserUtils;

import java.util.Objects;

public final class LinkReferenceDefinitionNode extends LeafBlockNode {
    private final String linkLabel;
    private final String linkDestination;
    private final String linkTitle; // nullable
    private final String normalizedLabel;

    public LinkReferenceDefinitionNode(String linkLabel, String linkDestination) {
        this(linkLabel, linkDestination, null);
    }

    public LinkReferenceDefinitionNode(String linkLabel, String linkDestination, String linkTitle) {
        this.linkLabel = linkLabel;
        this.linkDestination = linkDestination;
        this.linkTitle = linkTitle;

        /*
            One label matches another just in case their normalized forms are equal.
            To normalize a label, strip off the opening and closing brackets,
            perform the Unicode case fold, strip leading and trailing spaces, tabs, and line endings,
            and collapse consecutive internal spaces, tabs, and line endings to a single space.
         */
        this.normalizedLabel = LinkParserUtils.normalizeLabel(linkLabel);
        super.close();
    }

    public String getLinkLabel() {
        return linkLabel;
    }

    public String getLinkDestination() {
        return linkDestination;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public String getNormalizedLabel() {
        return normalizedLabel;
    }

    public boolean equalsIgnorePosition(LinkReferenceDefinitionNode node) {
        return Objects.equals(normalizedLabel, node.normalizedLabel) && Objects.equals(linkDestination, node.linkDestination) && Objects.equals(linkTitle, node.linkTitle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LinkReferenceDefinitionNode that = (LinkReferenceDefinitionNode) o;
        return Objects.equals(normalizedLabel, that.normalizedLabel) && Objects.equals(linkDestination, that.linkDestination) && Objects.equals(linkTitle, that.linkTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), linkDestination, linkTitle, normalizedLabel);
    }

    @Override
    public String toString() {
        String base = super.toString();
        if (linkTitle != null) {
            return base + " [" + normalizedLabel + "]: <" + linkDestination + ">" + " \"" + linkTitle + "\"";
        } else {
            return base + " [" + normalizedLabel + "]: <" + linkDestination + ">";
        }
    }
}
