package com.madimadica.hyde.ast;

import java.util.Objects;

public final class InlineImageNode extends InlineContainerNode {
    private String destination;
    private String description;

    public InlineImageNode(String destination) {
        this(destination, "");
    }

    public InlineImageNode(String destination, String description) {
        this.destination = destination;
        this.description = description;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InlineImageNode events = (InlineImageNode) o;
        return Objects.equals(destination, events.destination) && Objects.equals(description, events.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), destination, description);
    }

    @Override
    public String toString() {
        return super.toString() + "(href=\"%s\" alt=\"%s\")".formatted(destination, description);
    }

}
