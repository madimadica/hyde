package com.madimadica.hyde.syntax;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class LinkReferenceDefinition extends LeafBlock {
    private final String linkLabel;
    private final String linkDestination;
    private final String linkTitle; // nullable
    private final String normalizedLabel; // TODO

    public LinkReferenceDefinition(String linkLabel, String linkDestination) {
        this(linkLabel, linkDestination, null);
    }

    public LinkReferenceDefinition(String linkLabel, String linkDestination, String linkTitle) {
        this.linkLabel = linkLabel;
        this.linkDestination = linkDestination;
        this.linkTitle = linkTitle;

        /*
            One label matches another just in case their normalized forms are equal.
            To normalize a label, strip off the opening and closing brackets,
            perform the Unicode case fold, strip leading and trailing spaces, tabs, and line endings,
            and collapse consecutive internal spaces, tabs, and line endings to a single space.
         */
        normalizedLabel = String.join(" ", linkLabel.strip().toLowerCase(Locale.ROOT).split("\\s+"));
    }


    public String getLinkLabel() {
        return linkLabel;
    }

    public String getLinkDestination() {
        return linkDestination;
    }

    public Optional<String> getLinkTitle() {
        return Optional.ofNullable(linkTitle);
    }

    public String getNormalizedLabel() {
        return normalizedLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkReferenceDefinition that = (LinkReferenceDefinition) o;
        return Objects.equals(linkLabel, that.linkLabel) && Objects.equals(linkDestination, that.linkDestination) && Objects.equals(linkTitle, that.linkTitle) && Objects.equals(normalizedLabel, that.normalizedLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkLabel, linkDestination, linkTitle, normalizedLabel);
    }

    @Override
    public String toString() {
        return "LinkReferenceDefinition{" +
                "linkLabel='" + linkLabel + '\'' +
                ", linkDestination='" + linkDestination + '\'' +
                ", linkTitle='" + linkTitle + '\'' +
                ", normalizedLabel='" + normalizedLabel + '\'' +
                '}';
    }
}
