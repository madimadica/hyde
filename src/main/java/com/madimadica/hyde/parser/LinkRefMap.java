package com.madimadica.hyde.parser;

import com.madimadica.hyde.ast.LinkReferenceDefinitionNode;

import java.util.HashMap;
import java.util.Map;

public class LinkRefMap {
    /**
     * Map of normalized label keys to their node values
     */
    private final Map<String, LinkReferenceDefinitionNode> refs = new HashMap<>();

    /**
     * Add a link reference definition.
     * <br>
     * If there are multiple matching reference link definitions, the one that comes first in the document is used.
     * @param node link reference definition node
     * @return <code>true</code> if successfully added, false if duplicate
     */
    public boolean put(LinkReferenceDefinitionNode node) {
        var key = node.getNormalizedLabel();
        return refs.putIfAbsent(key, node) == null;
    }

    /**
     * Find the given node by label, after normalizing it
     * @param linkLabel label to normalize and search by
     * @return {@link LinkReferenceDefinitionNode} if found, else null.
     */
    public LinkReferenceDefinitionNode get(String linkLabel) {
        return get(linkLabel, true);
    }

    /**
     * Find the given node by the label, with optional normaliztion
     * @param linkLabel label to search by
     * @param needsNormalized if the {@code linkLabel} needs normalized.
     * @return {@link LinkReferenceDefinitionNode} if found, else null.
     */
    public LinkReferenceDefinitionNode get(String linkLabel, boolean needsNormalized) {
        if (needsNormalized) {
            return refs.get(LinkParserUtils.normalizeLabel(linkLabel));
        } else {
            return refs.get(linkLabel);
        }
    }

}
