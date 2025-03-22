package com.madimadica.hyde.parser;

import com.madimadica.hyde.ast.AST;
import com.madimadica.hyde.ast.InlineLeafBlockNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class Parser {

    public static AST parse(String input) {
        return parse(input, ParserOptions.getDefaults());
    }

    /**
     * Parse an input into a full AST representation.
     * <p>
     *     Step 1: Parse into block elements. This is done synchronously
     * </p>
     * <p>
     *     Step 2: Parse inline leaf content. This is done concurrently using the host's processor count.
     * </p>
     * @param input Markdown text to parse
     * @return parsed abstract syntax tree
     */
    public static AST parse(String input, ParserOptions options) {
        // Step 1: Parse block level elements
        var blockParser = new BlockParser();
        var blockOutput = blockParser.parse(input);
        AST ast = blockOutput.ast();
        var linkRefMap = blockOutput.linkRefMap();

        // Step 2: Collect the nodes that need inline parsing
        List<InlineLeafBlockNode> inlineContentNodes = new ArrayList<>();
        for (var nodeEvent : ast) {
            var node = nodeEvent.node();
            if (nodeEvent.isExiting() || !(node instanceof InlineLeafBlockNode inlineContentNode)) {
                continue;
            }
            inlineContentNodes.add(inlineContentNode);
        }

        // Step 3: Distribute inline-parser work across platform threads
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        List<Future<?>> futures = new ArrayList<>(inlineContentNodes.size());
        try (var executor = Executors.newFixedThreadPool(availableProcessors)) {
            for (var node : inlineContentNodes) {
                futures.add(executor.submit(() -> {
                        InlineParser inlineParser = new InlineParser(linkRefMap, options);
                        inlineParser.parse(node);
                }));
            }
            // Wait for all the inline processors to finish
            for (var future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return ast;
    }

}
