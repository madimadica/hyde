package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import com.madimadica.hyde.syntax.ASTNode;

import java.util.Optional;

public interface Parser<T extends ASTNode> {

    /**
     * Attempt to convert the given context into an ASTNode
     * of this type.
     * <br>
     * On success, return a non-empty optional with the parsed
     * node, and a mutated lexer position & readPosition.
     * <br>
     * On failure, return an empty optional,
     * and return a non-mutated lexer.
     * @param lexer Lexer context to parse from
     * @return an ASTNode if it can be parsed.
     */
    Optional<T> parse(Lexer lexer);

}
