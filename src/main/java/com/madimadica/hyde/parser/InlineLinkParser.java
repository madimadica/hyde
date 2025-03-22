package com.madimadica.hyde.parser;


import static com.madimadica.hyde.parser.ParserUtils.*;

public class InlineLinkParser {

    public record Result(String destination, String title, int closeParenIndex) {}
    public record Label(String label, int closeBracketIndex) {}

    /**
     * Assumes that offset is the start of the opening {@code "("}.
     * Also assumes that there are no blank lines in the input
     * @param input {@link CharSequence} input
     * @param offset offset into the {@code input}
     * @return a {@link Result} if a match was found, or null if this is not a valid inline-link start
     */
    public static Result parse(CharSequence input, final int offset) {
        final int len = input.length();
        if (offset >= len) {
            return null;
        }
        int index = offset;

        char start = input.charAt(offset);
        if (start != '(') {
            throw new IllegalStateException("Expected input to start with '('");
        }
        index++; // Skip the opening "("

        // Consume whitespaces after opening "("
        while (index < len && isAsciiWhitespace(input.charAt(index))) {
            index++;
        }
        if (index >= len) {
            return null; // Only had whitespaces after "("
        }

        char nextNonSpace = input.charAt(index);
        if (nextNonSpace == ')') {
            return new Result("", "", index);
        }

        boolean foundDestination = false;
        StringBuilder destinationBuilder = new StringBuilder();

        if (nextNonSpace == '<') {
            index++; // Advance past '<'
            /*
             * A sequence of zero or more characters between
             * an opening < and a closing > that contains no line endings
             * or unescaped < or > characters
             */
            while (index < len) {
                char ch = input.charAt(index);
                if (ch == '>') {
                    foundDestination = true;
                    index++; // Move cursor to after the closing rangle
                    break;
                } else if (ch == '<' || ch == '\n') {
                    return null;
                }
                index += 1 + escapeChar(input, destinationBuilder, ch, index, len);
            }
        } else {
            /*
             * Or a nonempty sequence of characters that does not start with <,
             * does not include ASCII control characters or space character, and
             * includes parentheses only if
             * (a) they are backslash-escaped or
             * (b) they are part of a balanced pair of unescaped parenthesis
             */
            int depth = 0; // parenthesis depth
            // Go until we run out of characters or we find a closing ')' for the entire link
            while (index < len) {
                char ch = input.charAt(index);
                if (Character.isWhitespace(ch)) {
                    break;
                }
                if (Character.isISOControl(ch)) {
                    return null;
                }
                if (ch == '(') {
                    depth++;
                } else if (ch == ')') {
                    if (--depth < 0) {
                        // We are at the terminal close paren, meaning there is no title
                        return new Result(destinationBuilder.toString(), "", index);
                    }
                }
                index += 1 + escapeChar(input, destinationBuilder, ch, index, len);
            }
            if (depth != 0) {
                return null;
            }
            foundDestination = true;
        }
        if (!foundDestination) {
            return null;
        }

        final String destination = destinationBuilder.toString();

        // Consume whitespaces
        int afterDestPos = index;
        while (index < len && isAsciiWhitespace(input.charAt(index))) {
            index++;
        }

        if (index >= len) {
            return null; // Only whitespaces, never had a closing ")"
        }

        char next = input.charAt(index);
        if (next == ')') {
            // Destination, optional whitespace, then closing ")"
            return new Result(destination, "", index);
        }

        // Check this after in case of "(<foo>)" immediate link closure
        // Found something after dest, but before ")", so likely a title
        if ((index - afterDestPos) == 0) {
            return null; // Needs at least one whitespace between dest/title
        }

        char closingChar;
        switch (next) {
            case '"'  -> closingChar = '"';
            case '\'' -> closingChar = '\'';
            case '('  -> closingChar = ')';
            default -> {
                return null; // Invalid start of title
            }
        }

        StringBuilder titleBuilder = new StringBuilder();
        boolean foundTitle = false;
        index++; // skip past opening

        final boolean isParenTitle = (next == '(');

        while (index < len) {
            char ch = input.charAt(index);
            if (ch == closingChar) {
                foundTitle = true;
                break;
            } else if (isParenTitle && ch == '(') {
                return null; // Cannot contain unescaped (
            }
            index += 1 + escapeChar(input, titleBuilder, ch, index, len);
        }

        if (!foundTitle) {
            return null; // Could not find end of title
        }

        index++; // Advance past the title closing
        // Consume extra whitespaces
        while (index < len && isAsciiWhitespace(input.charAt(index))) {
            index++;
        }
        // Check bounds
        if (index >= len) {
            return null;
        }

        if (input.charAt(index) != ')') {
            return null; // Something like (foo "bar" baz), expected (foo "bar") or (foo "bar"  )
        }

        String trimmedTitle = LinkParserUtils.normalizeMultilineTitle(titleBuilder);
        return new Result(destination, trimmedTitle, index);
    }

    /**
     * Attempt to parse a link label starting from the given offset, assuming
     * the character at the offset is an open bracket {@code "["}.
     * @param input input string
     * @param offset offset starting index for the input
     * @return {@code null} if no matches, or {@link Label} if a match is found.
     */
    public static Label parseLabel(CharSequence input, final int offset) {
        final int len = input.length();
        if (!ParserUtils.hasAtLeast(input, offset, 2)) { // Needs at least '[' and ']'
            return null;
        }
        if (input.charAt(offset) != '[') {
            return null;
        }
        int index = offset + 1; // Skip opening '['
        int labelStart = index;
        while (index < len) {
            char ch = input.charAt(index);
            if (ch == ']') {
                return new Label(input.subSequence(labelStart, index).toString(), index);
            }
            index += 1 + escapeCharLen(input, ch, index, len);
        }
        return null; // No matches / end-of-input
    }
}
