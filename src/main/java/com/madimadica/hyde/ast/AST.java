package com.madimadica.hyde.ast;

public class AST implements Iterable<NodeIterator.Event> {
    private final DocumentNode root;

    public AST() {
        this.root = new DocumentNode();
    }

    public DocumentNode getRoot() {
        return root;
    }

    @Override
    public NodeIterator iterator() {
        return root.iterator();
    }

    @Override
    public String toString() {
        return toTree();
    }

    public String toTree() {
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        for (var it : this) {
            int tempDepth = depth;
            // Only adjust depth on containers
            if (it.node() instanceof NodeContainer) {
                if (it.isEntering()) {
                    depth++;
                } else {
                    depth--;
                    continue;
                }
            }
            sb.repeat('\t', tempDepth);
            sb.append(it.node());
            sb.append('\n');
        }
        return sb.toString();
    }
}
