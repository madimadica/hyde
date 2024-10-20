package com.madimadica.hyde.syntax;

import java.util.regex.Pattern;

/**
 * <a href="https://spec.commonmark.org/0.31.2/#thematic-breaks">CommonMark Spec - Thematic Breaks</a>
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
 */
public class ThematicBreak {

    public static final Pattern REGEX_PATTERN = Pattern.compile("^[ ]{0,3}([-_*])\\1{2,}[ \\t]*$");

    public static boolean isThematicBreak(String line) {
        // non-regex implementation of ^[ ]{0,3}([-_*])\\1{2,}[ \\t]*$
        final char[] chars = line.toCharArray();
        final int len = chars.length;

        // Must be at least 3 chars
        if (len < 3) {
            return false;
        }

        // Index of where we are at in the input
        int i = 0;

        // Skip up to 3 spaces, shifting index
        while (i < len && chars[i] == ' ' && (++i != 3));

        // Must have at least 3 *more* characters
        if (len - i < 3) {
            return false;
        }

        // Make sure the next one is a valid option, shifting index
        char chosenDelimiter = chars[i++];
        if (chosenDelimiter != '-' && chosenDelimiter != '*' && chosenDelimiter != '_') {
            return false;
        }

        // Walk over as many duplicate delimiters as we find
        int count = 1;
        while (i < len && chars[i] == chosenDelimiter) {
            count++;
            i++;
        }

        // Make sure there are at least 3 used
        if (count < 3) {
            return false;
        }

        // Check that the end is optionally trailing whitespace
        while (i < len) {
            char trailingChar = chars[i];
            if (trailingChar != ' ' && trailingChar != '\t') {
                return false;
            }
            i++;
        }

        // We made it
        return true;
    }

}
