package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.BackslashEscapeLexer;
import com.madimadica.hyde.parsing.Lexer;
import com.madimadica.hyde.parsing.Position;
import com.madimadica.hyde.syntax.LinkReferenceDefinition;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <a href="https://spec.commonmark.org/0.31.2/#link-reference-definitions">CommonMark Spec - Link reference definitions</a>
 * Parses link reference definitions.
 */
public class LinkReferenceDefinitionParser implements Parser<LinkReferenceDefinition> {

    public static final Pattern REGEX_BLANK_LINES = Pattern.compile("\n[ \t]*\n");

    @Override
    public Optional<LinkReferenceDefinition> parse(Lexer lexer) {
        int startingLineNumber = lexer.getLineNumber();
        // optionally preceded by up to three spaces of indentation
        if (!lexer.hasStandardIndent(startingLineNumber)) {
            return Optional.empty();
        }

        // Begins with a link label
        String startingLineLTrimmed = lexer.getUnindentedLine(startingLineNumber).get();
        // A link label begins with a left bracket `[`
        if (startingLineLTrimmed.isEmpty() || startingLineLTrimmed.charAt(0) != '[') {
            return Optional.empty();
        }

        var lexerIterator = lexer.iterator(
                // Start from after the opening `[`
                new Position(startingLineNumber, lexer.getLineIndentation(startingLineNumber) + 1)
        );

        StringBuilder labelBuilder = new StringBuilder();
        boolean foundLabel = false;
        while (lexerIterator.hasNext()) {
            char c = lexerIterator.next();
            // and ends with the first right bracket `]` that is not backslash-escaped
            if (c == ']') {
                foundLabel = true;
                break;
            }
            handleNextChar(labelBuilder, c, lexer, lexerIterator);
        }

        // Never found the end of the label
        if (!foundLabel) {
            return Optional.empty();
        }

        String label = labelBuilder.toString();
        // Between these brackets there must be at least one character that is not a space, tab, or line ending.
        // A link label can have at most 999 characters inside the square brackets.
        // Must have another character followed by a colon (:)
        if (label.isBlank() || label.length() > 999 || !lexerIterator.nextEqualsSafe(':')) {
            return Optional.empty();
        }
        // Cannot have empty lines
        if (hasBlankLine(label)) {
            return Optional.empty();
        }
        // Consume the colon, and double check it's a colon (:)
        if (lexerIterator.next() != ':') {
            return Optional.empty();
        }

        // Optional spaces or tabs (including up to one line ending)
        String whitespaceAfterColon = lexerIterator.consumeWhitespace();
        // Cannot have a blank line between the colon and destination
        if (hasBlankLine(whitespaceAfterColon)) {
            return Optional.empty();
        }


        boolean foundDestination = false;
        if (!lexerIterator.hasNext()) {
            return Optional.empty();
        }
        StringBuilder destinationBuilder = new StringBuilder();
        if (lexerIterator.previewNext() == '<') {
            lexerIterator.next();
            /*
             * A sequence of zero or more characters between
             * an opening < and a closing > that contains no line endings
             * or unescaped < or > characters
             */
            while (lexerIterator.hasNext()) {
                char next = lexerIterator.next();
                if (next == '>') {
                    foundDestination = true;
                    break;
                } else if (next == '<') {
                    return Optional.empty();
                } else if (next == '\n') {
                    return Optional.empty();
                }
                handleNextChar(destinationBuilder, next, lexer, lexerIterator);
            }
        } else {
            /*
             * Or a nonempty sequence of characters that does not start with <,
             * does not include ASCII control characters or space character, and
             * includes parentheses only if
             * (a) they are backslash-escaped or
             * (b) they are part of a balanced pair of unescaped parenthesis
             */
            int parenDepth = 0;
            while (lexerIterator.hasNext()) {
                // Leave the whitespace intact for link title parsing
                if (Character.isWhitespace(lexerIterator.previewNext())) {
                    break;
                }
                char c = lexerIterator.next();
                if (Character.isISOControl(c)) {
                    return Optional.empty();
                }
                if (c == '(') {
                    parenDepth++;
                } else if (c == ')') {
                    parenDepth--;
                }
                if (parenDepth < 0) {
                    return Optional.empty();
                }
                handleNextChar(destinationBuilder, c, lexer, lexerIterator);
            }
            if (parenDepth != 0) {
                return Optional.empty();
            }
            // Whitespace or EOF
            foundDestination = true;
        }
        if (!foundDestination) {
            return Optional.empty();
        }

        String destination = destinationBuilder.toString();

        /*
         * At this point we have a label and destination,
         * which may be enough on its own, but we still need
         * to check for a title, and depending on if it is invalid
         * we might not have a result at all.
         */

        Position endOfDestination = lexerIterator.getCurrentPosition();

        String whitespaceAfterDest = lexerIterator.consumeWhitespace();
        if (hasBlankLine(whitespaceAfterDest)) {
            // Has a blank line, so no title at all.
            return withoutTitle(label, destination, lexer, endOfDestination);
        }
        if (whitespaceAfterDest.isEmpty()) {
            if (lexerIterator.hasNext()) {
                // Invalid because there must be at least some whitespace.
                return Optional.empty(); // e.g. `[foo]: <bar>"baz"`
            } else {
                // EOF is fine
                return withoutTitle(label, destination, lexer, endOfDestination);
            }
        }
        // Now we have the possibility of a link title on the same or next line.
        // If it is invalid on the same line then the entire thing is invalid,
        // Otherwise we just have a valid label+destination.

        boolean sameLine = !whitespaceAfterDest.contains("\n");

        char openingChar = lexerIterator.next();
        if (openingChar != '"' && openingChar != '\'' && openingChar != '(') {
            // Not a valid start of title
            return invalidTitle(sameLine, label, destination, lexer, endOfDestination);
        }

        char closingChar = switch(openingChar) {
            case '"' -> '"';
            case '\'' -> '\'';
            case '(' -> ')';
            default -> throw new RuntimeException("This is literally impossible, but the compiler doesn't know");
        };

        StringBuilder titleBuilder = new StringBuilder();
        boolean foundEndOfTitle = false;
        while (lexerIterator.hasNext()) {
            char next = lexerIterator.next();
            if (next == closingChar) {
                foundEndOfTitle = true;
                break;
            } else if (openingChar == '(' && next == '(') {
                // Unescaped `(`
                return invalidTitle(sameLine, label, destination, lexer, endOfDestination);
            }
            handleNextChar(titleBuilder, next, lexer, lexerIterator);
        }

        if (!foundEndOfTitle) {
            // End of file
            return invalidTitle(sameLine, label, destination, lexer, endOfDestination);
        }

        String rawTitle = titleBuilder.toString();
        if (hasBlankLine(rawTitle)) {
            // Title cannot contain new lines
            return invalidTitle(sameLine, label, destination, lexer, endOfDestination);
        }

        Position endOfTitle = lexerIterator.getCurrentPosition();

        // Make sure nothing but whitespace comes after the ending title line
        while (lexerIterator.hasNext()) {
            char next = lexerIterator.next();
            if (next == '\n') {
                break;
            } else if (next != ' ' && next != '\t') {
                return invalidTitle(sameLine, label, destination, lexer, endOfDestination);
            }
        }
        String[] titleLines = rawTitle.split("\n", -1);
        String trimmedTitle = Stream.of(titleLines)
                .map(String::stripLeading)
                .collect(Collectors.joining("\n"));


        lexer.skipToLine(endOfTitle.row() + 1);
        return Optional.of(new LinkReferenceDefinition(label, destination, trimmedTitle));
    }

    /**
     * Handles the necessary backslash escapes for the current iterator position.
     * @param sb StringBuilder result reference
     * @param c char at the current position
     * @param lexer lexer context
     * @param lexerIterator lexer iterator context
     */
    private void handleNextChar(StringBuilder sb, char c, Lexer lexer, Lexer.LexerIterator lexerIterator) {
        if (c == '\\') {
            if (lexer.isEndOfLine(lexerIterator.getCurrentPosition())) {
                sb.append('\\');
            } else if (lexerIterator.hasNext()) {
                if (BackslashEscapeLexer.isAsciiPunctuation(lexerIterator.previewNext())) {
                    // It can be escaped, so consume and add the next token.
                    sb.append(lexerIterator.next());
                } else {
                    // It can't be escaped, so just add a literal backslash
                    sb.append('\\');
                }
            }
        } else {
            // Normal character to add
            sb.append(c);
        }
    }

    private static boolean hasBlankLine(String s) {
        return REGEX_BLANK_LINES.matcher(s).find();
    }

    /**
     * Optionally return a LinkReferenceDefinition based on a known invalid title format.
     * <br>
     * If the title starts on the same line as the destination did, then the entire thing is invalid,
     * but if the title started on a different line, only the title is invalid (and will eventually be interpreted as a paragraph)
     * <br>
     * Mutates the state of the lexer in the event that a valid definition is read and returned
     */
    private static Optional<LinkReferenceDefinition> invalidTitle(boolean sameLineAsDestination, String label, String destination, Lexer lexer, Position endOfDestination) {
        if (sameLineAsDestination) {
            return Optional.empty();
        } else {
            lexer.skipToLine(endOfDestination.row() + 1);
            return Optional.of(new LinkReferenceDefinition(label, destination));
        }
    }

    public static Optional<LinkReferenceDefinition> withoutTitle(String label, String destination, Lexer lexer, Position endOfDestination) {
        lexer.skipToLine(endOfDestination.row() + 1);
        return Optional.of(new LinkReferenceDefinition(label, destination));
    }
}
