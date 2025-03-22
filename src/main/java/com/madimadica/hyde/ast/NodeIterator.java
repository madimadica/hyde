package com.madimadica.hyde.ast;

import java.util.Iterator;

public class NodeIterator implements Iterator<NodeIterator.Event> {
    private final Node root;
    private Node current;
    private boolean isEntering = true;

    public NodeIterator(Node root) {
        this.root = root;
        this.current = root;
    }

    public record Event(Node node, boolean isEntering) {
        public boolean isExiting() {
            return !isEntering;
        }
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @Override
    public Event next() {
        Node node = current;
        Event event = new Event(node, isEntering);

        // Try to enter a container
        if (isEntering && node instanceof NodeContainer) {
            if (node.firstChild != null) {
                current = node.firstChild;
            } else {
                isEntering = false;
            }
        } else if (node == root) {
            // => hasNext = false
            current = null;
        } else if (node.next == null) {
            current = node.parent;
            isEntering = false;
        } else {
            current = node.next;
            isEntering = true;
        }

        return event;
    }
}
