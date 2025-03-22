package com.madimadica.hyde.parser;

import com.madimadica.hyde.ast.LinkReferenceDefinitionNode;
import com.madimadica.hyde.ast.ParagraphNode;

import java.util.ArrayList;
import java.util.List;

import static com.madimadica.hyde.parser.ParserUtils.escapeChar;
import static com.madimadica.hyde.parser.ParserUtils.escapeCharLen;

public class LinkReferenceDefinitionParser {

    public record Result(LinkReferenceDefinitionNode node, int endingIndex) {
        public static Result of(int endingIndex, String label, String destination) {
            return new Result(new LinkReferenceDefinitionNode(label, destination), endingIndex);
        }

        public static Result of(int endingIndex, String label, String destination, String title) {
            return new Result(new LinkReferenceDefinitionNode(label, destination, title), endingIndex);
        }
    }

    /**
     * Parse out all the leading link reference definitions from a paragraph node
     * and return the list of those nodes with all the link definition data AND their
     * correct source positions. The AST node relations are not modified.
     * The position of the input node is also modified if necessary, as well as the content.
     * Trailing newlines will also be trimmed off.
     * @param paragraphNode input node
     * @return list of parsed nodes
     */
    public static List<LinkReferenceDefinitionNode> extractLinks(ParagraphNode paragraphNode) {
        SourcePositions initialLocation = paragraphNode.getPositions();
        int currentStartLine = initialLocation.start.line();
        final int startCol = initialLocation.start.column();

        List<LinkReferenceDefinitionNode> linkDefinitionNodes = new ArrayList<>();
        StringBuilder contentBuilder = new StringBuilder(paragraphNode.getLiteral());
        int offset = 0;
        while (true) {
            var result = LinkReferenceDefinitionParser.extractNextLink(contentBuilder, offset);
            if (result == null)
                break;
            // Exclusive ending
            int scanEnd = contentBuilder.charAt(result.endingIndex - 1) == '\n'
                    ? result.endingIndex - 1
                    : result.endingIndex;

            int lines = 1; // At least one line
            int lastLineLen = 0; // Length of the last line
            for (int i = offset; i < scanEnd; ++i) {
                if (contentBuilder.charAt(i) == '\n') {
                    lines++;
                    lastLineLen = 0;
                } else {
                    lastLineLen++;
                }
            }

            var linkRefNode = result.node();
            int endLine = currentStartLine + lines - 1;
            int endCol = startCol + lastLineLen - 1;
            linkRefNode.setPositions(new SourcePositions(
                    currentStartLine,
                    startCol,
                    endLine,
                    endCol
            ));

            linkDefinitionNodes.add(linkRefNode);
            offset = result.endingIndex();
            currentStartLine = endLine + 1;
        }
        if (!linkDefinitionNodes.isEmpty()) {
            // Remove the raw link definitions' content from this paragraph node
            String updatedRawContent = contentBuilder.substring(offset);
            paragraphNode.setLiteral(updatedRawContent);
            // Update the start position to the expected line
            paragraphNode.getPositions().setStart(currentStartLine, startCol);
        }
        return linkDefinitionNodes;
    }

    public static Result extractNextLink(StringBuilder sb, final int offset) {
        final int len = sb.length();
        if (offset >= len) {
            return null;
        }
        if (sb.charAt(offset) != '[')
            return null;

        boolean foundLabel = false;

        int index = offset + 1;
        int labelStart = index;
        int labelEnd = -1;
        while (index < len) {
            char c = sb.charAt(index);
            if (c == ']') {
                foundLabel = true;
                labelEnd = index;
                break;
            } else if (c == '[') {
                return null; // Cannot contain any unescaped square brackets
            }
            index += 1 + escapeCharLen(sb, c, index, len);
        }

        // Never found the end of the label
        if (!foundLabel) {
            return null;
        }

        // Move index past the closing ']'
        index++;

        String label = sb.substring(labelStart, labelEnd);

        // Between these brackets there must be at least one character that is not a space, tab, or line ending.
        // A link label can have at most 999 characters inside the square brackets.
        // Must have another character followed by a colon (:)
        if (label.isBlank() || label.length() > 999 || index >= len || sb.charAt(index) != ':') {
            return null;
        }

        // Cannot have a blank line, which should be impossible by paragraph construction

        // Move past the ':'
        index++;

        // Optional spaces or tabs (including up to one line ending)
        int newLines = 0;
        while (index < len) {
            char c = sb.charAt(index);
            if (c == '\n') {
                newLines++;
            } else if (c != ' ' && c != '\t') {
                break;
            }
            index++;
        }

        if (newLines > 1 || index == len) {
            return null;
        }

        // Begin parsing the destination
        boolean foundDestination = false;
        StringBuilder destBuilder = new StringBuilder();

        if (sb.charAt(index) == '<') {
            index++;
            /*
             * A sequence of zero or more characters between
             * an opening < and a closing > that contains no line endings
             * or unescaped < or > characters
             */
            while (index < len) {
                char ch = sb.charAt(index);
                if (ch == '>') {
                    foundDestination = true;
                    index++; // Move cursor to after the closing rangle
                    break;
                } else if (ch == '<' || ch == '\n') {
                    return null;
                }
                index += 1 + escapeChar(sb, destBuilder, ch, index, len);
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
            // Go until we hit the end or a whitespace
            while (index < len) {
                char ch = sb.charAt(index);
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
                        return null;
                    }
                }
                index += 1 + escapeChar(sb, destBuilder, ch, index, len);
            }
            if (depth != 0) {
                return null;
            }
            foundDestination = true;
        }

        if (!foundDestination) {
            return null;
        }

        String destination = destBuilder.toString();

        /*
         * At this point we have a label and destination,
         * which may be enough on its own, but we still need
         * to check for a title, and depending on if it is invalid
         * we might not have a result at all.
         */

        newLines = 0;
        int nextLineStart = -1;
        int whitespace = 0;
        while (index < len) {
            char c = sb.charAt(index);
            if (c == '\n') {
                newLines++;
                nextLineStart = index + 1;
            } else if (c != ' ' && c != '\t') {
                break;
            }
            whitespace++;
            index++;
        }

        boolean sameLine = newLines == 0;

        if (newLines > 1) {
            // No title, realistically the input data should never produce this specific way as blank lines would be separare paragraphs
            throw new IllegalStateException("Somehow a paragraph contains a blank line");
        }
        if (index >= len) {
            // End of input, only whitespace after destination
            return Result.of(index, label, destination);
        }
        if (whitespace == 0) {
            // Same line, no whitespace
            return null;
        }

        char openingChar = sb.charAt(index);
        if (openingChar != '"' && openingChar != '\'' && openingChar != '(') {
            // Not a valid start of title, but it depends if this occurs on the same line
            return withBadTitle(sameLine, nextLineStart, label, destination);
        }

        char closingChar = switch(openingChar) {
            case '"' -> '"';
            case '\'' -> '\'';
            case '(' -> ')';
            default -> throw new RuntimeException("This is literally impossible, but the compiler doesn't know");
        };

        // If the title starts on the same line and is malformed, the entire thing is ruined,
        // otherwise just the label and destination are used

        // Now we have the possibility of a link title on the same or next line.
        // If it is invalid on the same line then the entire thing is invalid,
        // Otherwise we just have a valid label+destination.

        StringBuilder titleBuilder = new StringBuilder();
        boolean foundTitle = false;
        index++; // skip past the opening char
        while (index < len) {
            char ch = sb.charAt(index);
            if (ch == closingChar) {
                foundTitle = true;
                break;
            } else if (openingChar == '(' && ch == '(') {
                return withBadTitle(sameLine, nextLineStart, label, destination);
            }
            index += 1 + escapeChar(sb, titleBuilder, ch, index, len);
        }

        if (!foundTitle) {
            return withBadTitle(sameLine, nextLineStart, label, destination);
        }

        int endingIndex = len;
        index++; // move past the closing symbol
        // Make sure nothing but whitespace comes after the ending title line
        while (index < len) {
            char ch = sb.charAt(index);
            if (ch == '\n') {
                endingIndex = index + 1; // start of next line
                break;
            } else if (ch != ' ' && ch != '\t') {
                return withBadTitle(sameLine, nextLineStart, label, destination);
            }
            index++;
        }

        String trimmedTitle = LinkParserUtils.normalizeMultilineTitle(titleBuilder);
        return Result.of(endingIndex, label, destination, trimmedTitle);
    }

    private static Result withBadTitle(boolean sameLine, int nextLineStart, String label, String destination) {
        if (sameLine) {
            return null;
        } else {
            return Result.of(nextLineStart, label, destination);
        }
    }

}
