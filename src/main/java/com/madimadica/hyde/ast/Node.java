package com.madimadica.hyde.ast;

import com.madimadica.hyde.parser.SourcePositions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract sealed class Node implements Iterable<NodeIterator.Event> permits BlockNode, InlineNode {
    protected SourcePositions positions = new SourcePositions();
    protected Node parent;
    protected Node firstChild;
    protected Node lastChild;
    protected Node prev;
    protected Node next;

    public SourcePositions getPositions() {
        return positions;
    }

    public void setPositions(SourcePositions positions) {
        this.positions = positions;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public boolean hasFirstChild() {
        return firstChild != null;
    }

    public boolean hasLastChild() {
        return lastChild != null;
    }

    public Node getFirstChild() {
        return firstChild;
    }

    public void setFirstChild(Node firstChild) {
        this.firstChild = firstChild;
    }

    public Node getLastChild() {
        return lastChild;
    }

    public void setLastChild(Node lastChild) {
        this.lastChild = lastChild;
    }

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public Node getNext() {
        return next;
    }

    public boolean hasNext() {
        return next != null;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void unlink() {
        if (this.prev != null) {
            this.prev.next = this.next;
        } else if (this.parent != null) {
            this.parent.firstChild = this.next;
        }
        if (this.next != null) {
            this.next.prev = this.prev;
        } else if (this.parent != null) {
            this.parent.lastChild = this.prev;
        }
        parent = next = prev = null;
    }

    public void appendChild(Node child) {
        child.unlink();
        child.parent = this;
        if (this.lastChild != null) {
            this.lastChild.next = child;
            child.prev = this.lastChild;
            lastChild = child;
        } else {
            firstChild = lastChild = child;
        }
    }

    public void prependChild(Node child) {
        child.unlink();
        child.parent = this;
        if (this.firstChild != null) {
            this.firstChild.prev = child;
            child.next = this.firstChild;
            this.firstChild = child;
        } else {
            firstChild = lastChild = child;
        }
    }

    public void insertBefore(Node sibling) {
        sibling.unlink();
        sibling.prev = this.prev;
        if (sibling.prev != null) {
            sibling.prev.next = sibling;
        }
        sibling.next = this;
        this.prev = sibling;
        sibling.parent = this.parent;
        if (sibling.prev == null) {
            sibling.parent.firstChild = sibling;
        }
    }

    public void insertAfter(Node sibling) {
        sibling.unlink();
        sibling.next = this.next;
        if (sibling.next != null) {
            sibling.next.prev = sibling;
        }
        sibling.prev = this;
        this.next = sibling;
        sibling.parent = this.parent;
        if (sibling.next == null) {
            sibling.parent.lastChild = sibling;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(positions, node.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positions);
    }

    @Override
    public NodeIterator iterator() {
        return new NodeIterator(this);
    }

    public List<NodeIterator.Event> toList() {
        List<NodeIterator.Event> events = new ArrayList<>();
        for (var event : this) {
            events.add(event);
        }
        return events;
    }
}
