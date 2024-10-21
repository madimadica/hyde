package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import com.madimadica.hyde.syntax.ATXHeading;

import java.util.Optional;

public class ATXHeadingParser implements Parser<ATXHeading> {

    public static final int MAX_HEADER_LEVEL = 6;
    public static final int MAX_INDENT = 3;

    public Optional<ATXHeading> parse(Lexer lexer) {
        String line = lexer.previewLine();
        // If the input is a valid header, advance the lexer to the end of the line.

        // We don't need to parse the content yet, only check that if it should be.
        // Therefore, we just need to check the starting conditions
        final char[] chars = line.toCharArray();
        final int len = chars.length;

        // Index of where we are at in the input
        int i = 0;

        // Skip up to 3 spaces, shifting index
        while (i < len && chars[i] == ' ' && (++i != MAX_INDENT));

        // Must have characters left
        if (len == i) {
            return Optional.empty();
        }

        // Get indentation levels, placing cursor
        // immediately after the last headerLevel;
        int headerLevel = 0;
        while (i < len) {
            char current = chars[i];
            if (current == '#') {
                headerLevel++;
                i++; // Only walk over #
            } else {
                break;
            }
            if (headerLevel > MAX_HEADER_LEVEL) {
                return Optional.empty();
            }
        }

        // Has no #'s at the start, or too much indentation
        if (headerLevel == 0) {
            return Optional.empty();
        }

        /*
         * We are now past all the opening #'s.
         * Now there are 3 cases.
         *
         * Case 1:
         *   End of line - Valid (no content)
         * Case 2:
         *   Tab|Space - Valid (unknown content)
         * Default:
         *    - Invalid (i.e. cannot have "###MyHeading")
         */

        if (i == len) {
            // Case 1 - valid and no content
            lexer.skipLine();
            return Optional.of(new ATXHeading(headerLevel));
        }

        final char c = chars[i];
        if (c != ' ' && c != '\t') {
            // Default
            return Optional.empty();
        }

        // Get the 'suffix' (part after (#{1,6}))
        String header = line.substring(i);
        // Trim any trailing whitespaces
        // (if content is empty "#  #", we get "  #" and not "#")
        header = header.stripTrailing();
        // Check for closing sequence of "([ \t]+)([#]+)"
        if (header.endsWith("#")) {
            // Walk backwards until we find a non-# char
            // Guaranteed at least one since it starts with a tab or space
            int firstNonPound = -1;
            for (int start = header.length(); start --> 0;) {
                if (header.charAt(start) != '#') {
                    firstNonPound = start;
                    break;
                }
            }
            char nonPoundChar = header.charAt(firstNonPound);

            if (nonPoundChar == ' ' || nonPoundChar == '\t') {
                // Ignore the ending #'s
                header = header.substring(0, firstNonPound);
            }
            // else leave them
        }
        // Now we can re-strip both sides to get the full content
        String rawContent = header.strip();
        // TODO This raw content will need to be inline parsed

        lexer.skipLine();
        return Optional.of(new ATXHeading(headerLevel, rawContent));
    }

}
