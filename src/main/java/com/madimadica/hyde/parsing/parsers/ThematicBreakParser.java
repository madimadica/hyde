package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import com.madimadica.hyde.syntax.ThematicBreak;

import java.util.Optional;

/**
 * <a href="https://spec.commonmark.org/0.31.2/#thematic-breaks">CommonMark Spec - Thematic Breaks</a>
 * Parser for handling Thematic Breaks
 * <br>
 * Defined as:
 * <p>
 * A line consisting of optionally up to three spaces of indentation,
 * followed by a sequence of three or more matching -, _, or * characters,
 * each followed optionally by any number of spaces or tabs, forms a thematic break.
 * </p>
 *
 * <br>
 * For example:
 * <pre>
 * ***
 * ---
 * ___
 * </pre>
 *
 * Which each translate into this HTML: <code>&lt;hr /&gt;</code>
 *
 * @see ThematicBreak
 */
public class ThematicBreakParser implements Parser<ThematicBreak> {

    @Override
    public Optional<ThematicBreak> parse(Lexer lexer) {
        String line = lexer.previewLine();
        final char[] chars = line.toCharArray();
        final int len = chars.length;

        // Must be at least 3 chars
        if (len < 3) {
            return Optional.empty();
        }

        // Index of where we are at in the input
        int i = 0;

        // Skip up to 3 spaces, shifting index
        while (i < len && chars[i] == ' ' && (++i != 3));

        // Must have at least 3 *more* characters
        if (len - i < 3) {
            return Optional.empty();
        }

        // Make sure the next one is a valid option, shifting index
        char chosenDelimiter = chars[i++];
        if (chosenDelimiter != '-' && chosenDelimiter != '*' && chosenDelimiter != '_') {
            return Optional.empty();
        }

        // At this point we are locked into this choice of delimiter
        // Make sure all that remains is tabs, spaces, and this character
        int count = 1;
        for (; i < len; ++i) {
            char c = chars[i];
            if (c != chosenDelimiter && c != ' ' && c != '\t') {
                // Invalid
                return Optional.empty();
            }
            // Increase required counter if it isn't whitespace
            if (c == chosenDelimiter) {
                count++;
            }
        }

        // Require at least 3 of the chosen type
        if (count < 3) {
            return Optional.empty();
        }

        // We found a match
        lexer.skipLine();
        return Optional.of(new ThematicBreak());
    }

}
