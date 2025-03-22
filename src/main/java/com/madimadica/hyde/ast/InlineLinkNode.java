package com.madimadica.hyde.ast;

import java.util.Objects;

public final class InlineLinkNode extends InlineContainerNode {
    private String destination;
    private String title;
    private boolean isAutolink = false;

    public InlineLinkNode(String destination) {
        this(destination, "");
    }

    public InlineLinkNode(String destination, String title) {
        this.destination = destination;
        this.title = title;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAutolink() {
        return isAutolink;
    }

    public void setAutolink(boolean autolink) {
        isAutolink = autolink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InlineLinkNode events = (InlineLinkNode) o;
        return Objects.equals(destination, events.destination) && Objects.equals(title, events.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), destination, title);
    }

    @Override
    public String toString() {
        return super.toString() + "(href=\"%s\" title=\"%s\")".formatted(destination, title);
    }
}
