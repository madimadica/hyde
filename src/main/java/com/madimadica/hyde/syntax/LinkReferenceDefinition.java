package com.madimadica.hyde.syntax;

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

        // TODO normalize the label
        normalizedLabel = linkLabel;
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
