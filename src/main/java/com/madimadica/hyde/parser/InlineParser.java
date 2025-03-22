package com.madimadica.hyde.parser;

import com.madimadica.hyde.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InlineParser {
    private static final char C_BACKTICK = '`';

    private final ParserOptions options;
    private final LinkRefMap linkRefMap;
    private final NodeStack<InlineDelimiter> delimiters = new NodeStack<>();
    private final NodeStack<InlineBracket> brackets = new NodeStack<>();

    private InlineLeafBlockNode block;
    private String input;
    private int length;
    private int pos;

    public InlineParser(LinkRefMap linkRefMap) {
        this(linkRefMap, ParserOptions.getDefaults());
    }

    public InlineParser(LinkRefMap linkRefMap, ParserOptions options) {
        this.linkRefMap = linkRefMap;
        this.options = options;
        this.pos = 0;
    }

    public static void parse(InlineLeafBlockNode node, LinkRefMap linkRefMap) {
        var inlineParser = new InlineParser(linkRefMap);
        inlineParser.parse(node);
    }

    public void parse(InlineLeafBlockNode node) {
        // Reset/initialize any member states in-case this is called multiple times
        this.delimiters.clear();
        this.brackets.clear();
        this.block = node;
        this.input = ParserUtils.stripAsciiWhitespace(node.getLiteral());
        this.length = input.length();
        this.pos = 0;

        while (pos < length) {
            parseNextToken();
        }

        if (options.gcOriginalInlines()) {
            block.setLiteral(null);
        }
        processEmphasis(null);
    }

    private void parseNextToken() {
        int codepoint = peek();
        boolean matched = switch (codepoint) {
            case '\n' -> parseNewline();
            case '\\' -> parseBackslash();
            case '`' -> parseBackticks();
            case '[' -> parseOpeningBracket();
            case ']' -> parseClosingBracket();
            case '!' -> parseBang();
            case '&' -> parseHtmlEntity();
            case '<' -> parseAutolink() || parseHtmlTag();
            case '*', '_' -> processDelimiter();
            case '\'', '"' -> options.smartQuotes() && processDelimiter();
            default -> parseString();
        };
        if (!matched) {
            pos++;
            block.appendChild(new InlineTextNode(Character.toString(codepoint)));
        }
    }

    private int peek() {
        return ParserUtils.peek(input, pos);
    }

    private boolean parseNewline() {
        pos++;
        if (block.getLastChild() instanceof InlineTextNode inlineText) {
            LineParser prevLine = new LineParser(inlineText.getLiteral());
            int trailingSpaces = prevLine.stripTrailing(' ');
            inlineText.setLiteral(prevLine.substring());
            if (trailingSpaces >= 2) {
                block.appendChild(new InlineHardBreakNode());
            } else {
                block.appendChild(new InlineSoftBreakNode());
            }
        } else {
            block.appendChild(new InlineSoftBreakNode());
        }
        consumeChars(' ');
        return true;
    }

    private boolean parseBackslash() {
        pos++; // Move to next char
        int next = peek();
        if (next == '\n') {
            pos++;
            block.appendChild(new InlineHardBreakNode());
        } else if (ParserUtils.isEscapable(next)) {
            pos++;
            block.appendChild(new InlineTextNode(Character.toString(next)));
        } else {
            block.appendChild(new InlineTextNode("\\"));
        }
        return true;
    }

    private boolean parseBackticks() {
        final int openingTicks = consumeChars(C_BACKTICK);
        final int startPos = pos;
        // Consume backtick runs until we find a group of closing ticks of equal size
        // Otherwise, return a text-node of the opening ticks
        while (true) {
            if (!consumeUntil(C_BACKTICK)) {
                // Consumed everything without any closing ticks that match
                this.pos = startPos; // Reset to start (after opening ticks)
                block.appendChild(new InlineTextNode("`".repeat(openingTicks)));
                return true;
            }
            int numberOfTicks = consumeChars(C_BACKTICK);
            if (numberOfTicks != openingTicks) {
                continue; // start/end ticks don't match, keep searching
            }

            int endPos = pos - numberOfTicks;
            String preNormalized = input.substring(startPos, endPos);

            // Convert line endings to spaces
            String normalized = preNormalized.replace('\n', ' ');

            /*
             * If it starts with and ends with a space, and has any non-space chars,
             * a single space is removed from both the front and back
             */
            if (normalized.startsWith(" ") && normalized.endsWith(" ") && ParserUtils.hasNonSpaces(normalized)) {
                normalized = normalized.substring(1, normalized.length() - 1);
            }

            block.appendChild(new InlineCodeNode(normalized));
            return true;
        }
    }

    private boolean parseAutolink() {
        return matchAutolink(PatternMatcher::findAutolinkUri, "")
            || matchAutolink(PatternMatcher::findAutolinkEmail, "mailto:");
    }

    private boolean matchAutolink(BiFunction<String, Integer, String> matcherFunction, String linkPrefix) {
        String match = tryConsumeMatch(matcherFunction);
        if (match == null) {
            return false;
        }
        String destination = match.substring(1, match.length() - 1);
        var linkNode = new InlineLinkNode(linkPrefix + destination);
        linkNode.setAutolink(true);
        linkNode.appendChild(new InlineTextNode(destination));
        block.appendChild(linkNode);
        return true;
    }

    private boolean parseHtmlTag() {
        String match = tryConsumeMatch(HtmlParserUtils::parseInlineHtmlTag);
        if (match == null) {
            return false;
        }
        var node = new InlineHTMLNode(match);
        block.appendChild(node);
        return true;
    }

    private boolean parseHtmlEntity() {
        String match = tryConsumeMatch(HtmlParserUtils::parseHtmlEntity);
        if (match == null) {
            return false;
        }
        String text = HtmlEntities.decode(match);
        if (text == null) {
            text = match; // Keep invalid entity as literal text
        }
        var node = new InlineTextNode(text);
        block.appendChild(node);
        return true;
    }

    private boolean parseOpeningBracket() {
        int bracketPos = pos++;
        var node = new InlineTextNode("[");
        block.appendChild(node);
        pushBracket(node, bracketPos, false);
        return true;
    }

    private boolean parseBang() {
        int bangPos = pos++;
        if (peek() == '[') {
            pos++;
            var node = new InlineTextNode("![");
            block.appendChild(node);
            pushBracket(node, bangPos + 1, true);
        } else {
            block.appendChild(new InlineTextNode("!"));
        }
        return true;
    }

    private InlineDelimiter scanDelimiters() {
        char ch = (char) peek();
        int startPos = pos;
        int runLength;

        assert "*_'\"".indexOf(ch) >= 0;

        if (ch == '*' || ch == '_') {
            runLength = consumeChars(ch);
        } else { // quotes
            runLength = 1;
            pos++;
        }

        assert runLength != 0;

        int before = ParserUtils.peekOrElse(input, startPos - 1, ' ');
        int after  = ParserUtils.peekOrElse(input, pos, ' ');

        boolean whitespaceBefore = ParserUtils.isAsciiWhitespaceOrNbsp(before);
        boolean whitespaceAfter  = ParserUtils.isAsciiWhitespaceOrNbsp(after);
        boolean puncBefore = ParserUtils.isPunctuation(before);
        boolean puncAfter  = ParserUtils.isPunctuation(after);

        boolean flankingLeft  = isFlanking(whitespaceBefore, puncBefore, whitespaceAfter,  puncAfter);
        boolean flankingRight = isFlanking(whitespaceAfter,  puncAfter,  whitespaceBefore, puncBefore);

        boolean potentialOpener, potentialCloser;
        if (ch == '_') {
            potentialOpener  = flankingLeft  && (!flankingRight || puncBefore);
            potentialCloser = flankingRight && (!flankingLeft  || puncAfter);
        } else if (ch == '*') {
            potentialOpener  = flankingLeft;
            potentialCloser = flankingRight;
        } else { // quotes
            potentialOpener  = flankingLeft && !flankingRight;
            potentialCloser = flankingRight;
        }

        pos = startPos; // Revert back, after looking-ahead

        return new InlineDelimiter(ch, runLength, potentialOpener, potentialCloser);
    }

    private static boolean isFlanking(boolean sameWhitespace, boolean samePunc, boolean diffWhitespace, boolean diffPunc) {
        return !diffWhitespace && (!diffPunc || sameWhitespace || samePunc);
    }

    private boolean processDelimiter() {
        int ch = (char) peek();
        int startPos = pos;

        assert "*_'\"".indexOf(ch) >= 0;

        var delims = scanDelimiters();

        pos += delims.currentRun;

        String text = switch (ch) {
            case '\'' -> "’";
            case '"' -> "“";
            default -> input.substring(startPos, pos); // run of '_' or '*'
        };

        var node = new InlineTextNode(text);
        block.appendChild(node);

        if (delims.hasPotential()) {
            delims.textNode = node;
            delimiters.push(delims);
        }
        return true;
    }

    private boolean parseClosingBracket() {
        lookForLinkOrImageProcedure();
        return true;
    }

    private boolean parseString() {
        int startPos = pos;
        while (pos < length && !isTokenStartChar(peek())) {
            pos++;
        }
        assert pos > startPos;
        // Text cannot be empty because the only way to invoke parseString() is to have a non-special char
        String text = input.substring(startPos, pos);
        // TODO smart replacements
        block.appendChild(new InlineTextNode(text));
        return true;
    }

    private static boolean isTokenStartChar(int codepoint) {
        if (codepoint > 96) { // Backtick is the maximum start-token ascii at 96
            return false;
        }
        char c = (char) codepoint;
        return c == '\n'
            || c == '!'
            || c == '"'
            || c == '&'
            || c == '\''
            || c == '*'
            || c == '<'
            || c == '['
            || c == ']'
            || c == '\\'
            || c == '_'
            || c == '`';
    }

    /**
     * As defined by
     * <a href="https://spec.commonmark.org/0.31.2/#look-for-link-or-image">CommonMark Spec</a>
     */
    private void lookForLinkOrImageProcedure() {
        pos++; // Advance past the "]"
        int startPos = pos;

        if (brackets.isEmpty()) {
            block.appendChild(new InlineTextNode("]"));
            return;
        }
        InlineBracket opener = brackets.peek().value;
        if (!opener.active) {
            // If the next brack is inactive, we pop it and use a text node
            block.appendChild(new InlineTextNode("]"));
            brackets.pop();
            return;
        }

        boolean matched = false;

        // If we find an active bracket, we look ahead to see if it's
        // an inline/reference/collapsed/shortcut link/image

        LinkData linkData = null;
        // Check: inline link [...](...)
        if (peek() == '(') {
            var result = InlineLinkParser.parse(this.input, this.pos);
            if (result != null) {
                linkData = LinkData.ofDestinationAndTitle(result.destination(), result.title());
                matched = true;
                pos = result.closeParenIndex() + 1;
            }
        }

        // Check: Link Reference (Full [...][...] , Collapsed [...][], Shortcut [...])
        CHECK_LINK_REF:
        if (!matched) {
            ReferenceLinkType type;
            String label = null;
            String lookahead = ParserUtils.peek(input, pos, 2);
            int endPos = pos;
            if (lookahead.equals("[]")) {
                type = ReferenceLinkType.COLLAPSED;
                endPos = pos + 2;
            } else if (!lookahead.startsWith("[")) {
                type = ReferenceLinkType.SHORTCUT;
            } else {
                var labelResult = InlineLinkParser.parseLabel(input, pos);
                if (labelResult == null) {
                    break CHECK_LINK_REF;
                }
                label = labelResult.label();
                endPos = labelResult.closeBracketIndex() + 1;
                type = ReferenceLinkType.FULL;
            }
            if (type != ReferenceLinkType.FULL && opener.bracketAfter) {
                break CHECK_LINK_REF;
            }
            if (type != ReferenceLinkType.FULL) {
                // Use implicit label from link-text (Shortcut/Collapsed)
                label = input.substring(opener.index + 1, startPos - 1); // Trim '[' and ']'
            }

            var linkRef = linkRefMap.get(label);
            if (linkRef == null) {
                break CHECK_LINK_REF;
            }
            linkData = LinkData.ofDestinationAndTitle(linkRef.getLinkDestination(), linkRef.getLinkTitle());
            pos = endPos;
            matched = true;
        }

        // If we didn't find a link/image, pop the bracket and return a literal bracket
        if (!matched) {
            brackets.pop();
            pos = startPos;
            block.appendChild(new InlineTextNode("]"));
            return;
        }

        InlineNode node = opener.isImage
                ? new InlineImageNode(linkData.destination(), linkData.title())
                : new InlineLinkNode(linkData.destination(), linkData.title());

        Node next;
        var tempNode = opener.textNode.getNext();
        while (tempNode != null) {
            next = tempNode.getNext();
            tempNode.unlink();
            node.appendChild(tempNode);
            tempNode = next;
        }
        block.appendChild(node);
        processEmphasis(opener.prevDelim);
        brackets.pop();
        opener.textNode.unlink();

        // Deactivate other links - prevents nested links
        if (opener.isLink()) {
            var openerNode = brackets.peek();
            while (openerNode != null) {
                if (openerNode.value.isLink()) {
                    openerNode.value.active = false;
                }
                openerNode = openerNode.prev;
            }
        }
    }

    /**
     * As defined by
     * <a href="https://spec.commonmark.org/0.31.2/#process-emphasis">CommonMark Spec</a>
     */
    private void processEmphasis(NodeStack.Node<InlineDelimiter> stackBottom) {
        NodeStack.Node<InlineDelimiter> openersBottomSingleQuote = stackBottom;
        NodeStack.Node<InlineDelimiter> openersBottomDoubleQuote = stackBottom;
        List<NodeStack.Node<InlineDelimiter>> openersBottomAsterisks = new ArrayList<>(6);
        List<NodeStack.Node<InlineDelimiter>> openersBottomUnderscores = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            openersBottomAsterisks.add(stackBottom);
            openersBottomUnderscores.add(stackBottom);
        }

        // same as current_position
        var closer = stackBottom == null
                ? delimiters.peekBottom()
                : stackBottom.next;


        while (closer != null) {
            // Move forward until we find the first potential closer
            if (!closer.value.potentialCloser) {
                closer = closer.next;
                continue;
            }

            // Found closer, now lookbehind for the matching opener
            int originalType = closer.value.type;
            int originalHash = 0;
            NodeStack.Node<InlineDelimiter> openersBottom = switch(originalType) {
                case '\'' -> openersBottomSingleQuote;
                case '"' -> openersBottomDoubleQuote;
                case '*' -> openersBottomAsterisks.get(originalHash = closer.value.customHash);
                case '_' -> openersBottomUnderscores.get(originalHash = closer.value.customHash);
                default -> throw new RuntimeException("Unexpected delimiter type '" + originalType + "'");
            };

            // Look back in the stack for the matching potential opener
            var opener = closer.prev;
            boolean foundOpener = false;
            while (opener != null && opener != stackBottom && opener != openersBottom) {
                var openV = opener.value;
                var closerV = closer.value;
                boolean oddMatch = (closerV.potentialOpener || openV.potentialCloser)
                        && (closerV.originalRunMod3 != 0)
                        && ((openV.originalRun + closerV.originalRun) % 3 == 0);
                if (openV.type == closerV.type && openV.potentialOpener && !oddMatch) {
                    foundOpener = true;
                    break;
                }
                opener = opener.prev;
            }

            var oldCloser = closer;

            var type = closer.value.type;
            IF_ELSE:
            if (type == '\'') {
                closer.value.textNode.setLiteral(foundOpener ? "‘" : "’");
                closer = closer.next;
            } else if (type == '"') {
                closer.value.textNode.setLiteral(foundOpener ? "“" : "”");
                closer = closer.next;
            } else {
                if (!foundOpener) {
                    closer = closer.next;
                    break IF_ELSE;
                }
                int toConsume = closer.value.currentRun >= 2 && opener.value.currentRun >= 2
                        ? 2
                        : 1;

                var openerTextNode = opener.value.textNode;
                var closerTextNode = closer.value.textNode;

                opener.value.currentRun -= toConsume;
                closer.value.currentRun -= toConsume;
                openerTextNode.setLiteral(Str.trimEnd(openerTextNode.getLiteral(), toConsume));
                closerTextNode.setLiteral(Str.trimEnd(closerTextNode.getLiteral(), toConsume));

                InlineContainerNode newNode = toConsume == 1
                        ? new InlineItalicNode()
                        : new InlineBoldNode();

                Node it = openerTextNode.getNext();
                while (it != null && it != closerTextNode) {
                    Node next = it.getNext();
                    it.unlink();
                    newNode.appendChild(it);
                    it = next;
                }

                openerTextNode.insertAfter(newNode);

                delimiters.removeBetween(opener, closer);

                if (opener.value.currentRun == 0) {
                    openerTextNode.unlink();
                    delimiters.remove(opener);
                }

                if (closer.value.currentRun == 0) {
                    closerTextNode.unlink();
                    var temp = closer.next;
                    delimiters.remove(closer);
                    closer = temp;
                }
            }

            if (!foundOpener) {
                switch(originalType) {
                    case '\'' -> openersBottomSingleQuote = oldCloser.prev;
                    case '"' -> openersBottomDoubleQuote = oldCloser.prev;
                    case '*' -> openersBottomAsterisks.set(originalHash, oldCloser.prev);
                    case '_' -> openersBottomUnderscores.set(originalHash, oldCloser.prev);
                }
                if (!oldCloser.value.potentialOpener) {
                    delimiters.remove(oldCloser);
                }
            }

        }

        // Remove everything above stackBottom lowerbound on the stack
        if (stackBottom == null) {
            delimiters.clear();
        } else {
            while (delimiters.peek() != stackBottom) {
                delimiters.pop();
            }
        }
    }

    /**
     * Attempt to match a regex pattern at the current position,
     * advancing the position to the end of the match, if present.
     * @return The match, or {@code null}
     */
    private String tryConsumeMatch(BiFunction<String, Integer, String> matcherFunction) {
        String result = matcherFunction.apply(input, pos);
        if (result == null) {
            return null;
        } else {
            pos += result.length();
            return result;
        }
    }

    /**
     * Repeatedly advance the current position while
     * the current char matches the {@code target}.
     * @param target char to consume
     * @return The number of chars consumed
     */
    private int consumeChars(char target) {
        int before = pos;
        while (peek() == target) {
            pos++;
        }
        return pos - before;
    }

    /**
     * Repeatedly advance the current position while
     * the current char <i>does not</i> match the {@code target}.
     * If present, this stops <i>at</i> the target, but does not consume it.
     * @return {@code true} if we found the {@code target},
     * and {@code false} if we failed to find a match before exhausting the input
     */
    private boolean consumeUntil(char target) {
        while (pos < length && peek() != target) {
            pos++;
        }
        return pos != length;
    }

    /**
     * Add a bracket with the given metadata to the stack
     * @param node TextNode representing this bracket
     * @param index input position
     * @param isImage whether this is an image bracket (i.e. {@code ![})
     */
    void pushBracket(InlineTextNode node, int index, boolean isImage) {
        if (brackets.isNotEmpty()) {
            brackets.peek().value.bracketAfter = true;
        }
        var bracket = new InlineBracket();
        bracket.textNode = node;
        bracket.prevDelim = delimiters.peek();
        bracket.index = index;
        bracket.isImage = isImage;
        bracket.active = true;
        brackets.push(bracket);
    }

}
