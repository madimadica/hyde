package com.madimadica.hyde.syntax;

/**
 * <a href="https://spec.commonmark.org/0.31.2/#atx-headings">CommonMark Spec - ATX headings</a>
 * <br>
 * Defined as:
 * <p>
 * An ATX heading consists of a string of characters, parsed as inline content, between an opening sequence of 1–6 unescaped # characters and an optional closing sequence of any number of unescaped # characters. The opening sequence of # characters must be followed by spaces or tabs, or by the end of line. The optional closing sequence of #s must be preceded by spaces or tabs and may be followed by spaces or tabs only. The opening # character may be preceded by up to three spaces of indentation. The raw contents of the heading are stripped of leading and trailing space or tabs before being parsed as inline content. The heading level is equal to the number of # characters in the opening sequence.
 * </p>
 *
 * <br>
 * For example:
 * <pre>
 # foo
 ## foo
 ### foo
 #### foo
 ##### foo
 ###### foo
 * </pre>
 *
 * Which each translate into this HTML: <code>&lt;hr /&gt;</code>
 */
public final class ATXHeading extends LeafBlock {

    private static final int MAX_HEADER_LEVEL = 6;
    private static final int MAX_INDENT = 3;

    /**
     * Check if the given line of input matches the spec
     * for an ATX heading.
     * @param line - line to check
     * @return <code>true</code> if the line is a heading.
     */
    public static boolean isATXHeading(String line) {
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
            return false;
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
                return false;
            }
        }

        // Has no #'s at the start, or too much indentation
        if (headerLevel == 0) {
            return false;
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
        if (i == len) return true; // Case 1
        final char c = chars[i]; // Guaranteed safe index
        return (c == ' ' || c == '\t');
    }


}
