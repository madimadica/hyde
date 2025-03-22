package com.madimadica.hyde.parser;

import java.util.NoSuchElementException;

/**
 * Stack of doubly-linked nodes, exposed with direct access
 * @param <T>
 */
public class NodeStack<T> {

    public static class Node<T> {
        public Node<T> next;
        public Node<T> prev;
        public T value;

        public Node(T value) {
            this.value = value;
        }
    }

    private Node<T> top;
    private int size = 0;

    public NodeStack() {
    }

    public Node<T> push(T value) {
        size++;
        Node<T> newNode = new Node<>(value);
        if (top != null) {
            top.next = newNode;
            newNode.prev = top;
        }
        return top = newNode;
    }

    public Node<T> pop() {
        if (top == null) {
            throw new NoSuchElementException();
        }
        size--;
        Node<T> popped = top;
        top = popped.prev;
        if (top == null) {
            return popped; // Singleton popped
        }
        // Clear double links
        popped.prev = null;
        top.next = null;
        return popped;
    }

    public void removeBetween(Node<T> lower, Node<T> upper) {
        if (lower.next != upper) {
            lower.next = upper;
            upper.prev = lower;
            recount();
        }
    }

    public void remove(Node<T> delimiterNode) {
        // Assume this node exists in this stack
        if (size == 1 && delimiterNode == top) {
            clear();
        } else if (delimiterNode == top) {
            pop();
        } else {
            // somewhere in the middle or bottom
            size--;
            var prev = delimiterNode.prev;
            if (prev == null) {
                // Bottom of the stack
                delimiterNode.next.prev = null;
            } else {
                // Middle of the stack
                var next = delimiterNode.next;
                prev.next = next;
                next.prev = prev;
            }
            delimiterNode.next = null;
            delimiterNode.prev = null;
        }
    }

    public Node<T> peek() {
        return top;
    }

    public void clear() {
        top = null;
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isNotEmpty() {
        return size != 0;
    }

    public int size() {
        return size;
    }

    public Node<T> peekBottom() {
        if (size == 0) {
            return null;
        }
        var current = top;
        while (current.prev != null) {
            current = current.prev;
        }
        return current;
    }

    private void recount() {
        // Force refresh the size
        var current = top;
        int count = 0;
        while (current != null) {
            count++;
            current = current.prev;
        }
        this.size = count;
    }

}
