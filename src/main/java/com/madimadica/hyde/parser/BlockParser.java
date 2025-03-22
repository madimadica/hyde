package com.madimadica.hyde.parser;

import com.madimadica.hyde.ast.*;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.madimadica.hyde.parser.ParserUtils.isSpaceOrTab;

public class BlockParser {
    private static final Pattern REGEX_LINE_END = Pattern.compile("\\r?\\n|\\r");
    private static final int INDENTED_CODE_BLOCK_INDENT = 4;

    private AST ast;
    private BlockNode lastOpenBlock;
    private BlockNode prevOpenBlock;
    private BlockNode lastMatchedContainer;
    private String currentLine;
    private List<String> lines;
    private int lineNumber;
    private int offset;
    private int column;
    private int nextNonspace;
    private int nextNonspaceColumn;
    private int indent;
    private boolean indented;
    private boolean blank;
    private boolean partiallyConsumedTab;
    private boolean hasUnclosedBlocks;
    private LinkRefMap linkRefMap;

    public BlockParser() {}

    public record Output(AST ast, LinkRefMap linkRefMap) {}

    /**
     * Parse a string of Markdown text into an AST
     * @param input Markdown to parse
     * @return {@link Output} abstract syntax tree of the input
     */
    public Output parse(String input) {
        initialize();
        if (input.isBlank()) {
            return new Output(this.ast, this.linkRefMap);
        }

        // Security/safety
        input = input.replace('\0', '\uFFFD');

        lines = List.of(REGEX_LINE_END.split(input));
        final int totalLines = lines.size();
        for (int i = 0; i < totalLines; i++) {
            this.processLine(lines.get(i));
        }
        while (lastOpenBlock != null) {
            closeBlock(lastOpenBlock, totalLines);
        }

        return new Output(this.ast, this.linkRefMap);
    }

    /**
     * Initialize the parser state to the beginning of a parse operation.
     */
    private void initialize() {
        ast = new AST();
        DocumentNode root = this.ast.getRoot();
        lastOpenBlock = root;
        prevOpenBlock = root;
        lastMatchedContainer = root;
        currentLine = "";
        lineNumber = 0;
        offset = 0;
        column = 0;
        nextNonspace = 0;
        nextNonspaceColumn = 0;
        indent = 0;
        indented = false;
        blank = false;
        partiallyConsumedTab = false;
        hasUnclosedBlocks = false;
        linkRefMap = new LinkRefMap();
    }

    /**
     * Check if an AST node may contain another node.
     * @param parent Node to check if a child can be added
     * @param child Child to check if it can be added
     * @return {@code true} if the {@code child} is allowed to go inside the {@code parent}.
     */
    private static boolean canContain(BlockNode parent, BlockNode child) {
        return switch (parent) {
            case DocumentNode ignored -> !(child instanceof ListItemNode);
            case BlockQuoteNode ignored -> !(child instanceof ListItemNode);
            case ListItemNode ignored -> !(child instanceof ListItemNode);
            case ListNode ignored -> child instanceof ListItemNode;
            default -> false;
        };
    }

    /**
     * The state of if a block may continue to exist
     */
    enum ContinuationResult {
        MATCHED,
        NOT_MATCHED,
        CONSUMED;

        static ContinuationResult require(boolean matched) {
            return matched ? MATCHED : NOT_MATCHED;
        }
    }

    /**
     * Check a character on the current line at the current index.
     * @param pos index to check
     * @return -1 if invalid position, else the codepoint at the index.
     * @see ParserUtils#peek(String, int)
     */
    private int peek(int pos) {
        return ParserUtils.peek(this.currentLine, pos);
    }

    /**
     * Check if a block can continue for more columns/lines
     * and advance the cursor if necessary.
     */
    private ContinuationResult canContinue(BlockNode blockNode) {
        return switch (blockNode) {
            case DocumentNode      ignored -> ContinuationResult.MATCHED;
            case ListNode          ignored -> ContinuationResult.MATCHED;
            case ThematicBreakNode ignored -> ContinuationResult.NOT_MATCHED;
            case ATXHeadingNode    ignored -> ContinuationResult.NOT_MATCHED;
            case SetextHeadingNode ignored -> ContinuationResult.NOT_MATCHED;
            case LinkReferenceDefinitionNode ignored -> ContinuationResult.NOT_MATCHED;
            case ParagraphNode     ignored -> ContinuationResult.require(!blank);
            case HTMLBlockNode htmlBlock   -> ContinuationResult.require(!blank || htmlBlock.allowsBlankLines());
            case BlankLineNode ignored     -> throw new IllegalArgumentException("Blank lines shouldn't appear here");

            case BlockQuoteNode ignored -> {
                if (!indented && peek(nextNonspace) == '>') {
                    consumeWhitespaces();
                    consumeChars(1);
                    consumeWhitespace(1);
                    yield ContinuationResult.MATCHED;
                } else {
                    yield ContinuationResult.NOT_MATCHED;
                }
            }

            case ListItemNode li -> {
                if (blank) {
                    if (li.hasFirstChild()) {
                        consumeWhitespaces();
                        yield ContinuationResult.MATCHED;
                    } else {
                        yield ContinuationResult.NOT_MATCHED;
                    }
                } else {
                    int minIndent = li.getListData().getMinimumIndent();
                    if (indent < minIndent) {
                        yield ContinuationResult.NOT_MATCHED;
                    } else {
                        consumeColumns(minIndent);
                        yield ContinuationResult.MATCHED;
                    }
                }
            }

            case FencedCodeBlockNode fencedCodeBlock -> {
                final char fenceChar = fencedCodeBlock.getFenceType();

                if (indent < 4 && nextNonspaceChar() == fenceChar) {
                    // if we also match the regex then update and return consumed
                    int openingFenceLength = fencedCodeBlock.getFenceLength();
                    int closingFenceLength = ParserUtils.countCharFrom(currentLine, fenceChar, nextNonspace);
                    if (closingFenceLength >= openingFenceLength) {
                        if (ParserUtils.hasOptionalTrailingWhitespace(currentLine, nextNonspace + closingFenceLength)) {
                            closeBlock(fencedCodeBlock, lineNumber);
                            yield ContinuationResult.CONSUMED;
                        }
                    }
                }
                consumeWhitespace(fencedCodeBlock.getFenceOffset());
                yield ContinuationResult.MATCHED;
            }

            case IndentedCodeBlockNode ignored -> {
                if (indent >= INDENTED_CODE_BLOCK_INDENT) {
                    consumeColumns(INDENTED_CODE_BLOCK_INDENT);
                    yield ContinuationResult.MATCHED;
                } else if (blank) {
                    consumeWhitespaces();
                    yield ContinuationResult.MATCHED;
                } else {
                    yield ContinuationResult.NOT_MATCHED;
                }
            }
        };
    }

    /**
     * Close a block and update its ending source position
     * @param blockNode node to close
     * @param lineNumber line number that it ended at
     */
    private void closeBlock(BlockNode blockNode, int lineNumber) {
        var parent = blockNode.getParent();
        blockNode.close();
        int lineLength = lines.get(lineNumber - 1).length();
        blockNode.getPositions().setEnd(lineNumber, lineLength);
        closeBlock(blockNode);
        lastOpenBlock = parent;
    }

    private static boolean hasLinesBetween(Node first, Node second) {
        return first.getPositions().getEnd().line() + 1 != second.getPositions().getStart().line();
    }


    /**
     * Close the given block and finish determining its internal state
     */
    private void closeBlock(BlockNode blockNode) {
        switch (blockNode) {
            case DocumentNode root -> extractLinkReferenceDefinitions(root);
            case ListNode listNode -> {
                // Check if it is loose or tight
                var li = listNode.getFirstChild();
                boolean isTight = true;
                OUTER:
                while (li != null) {
                    if (li.hasNext() && hasLinesBetween(li, li.getNext())) {
                        isTight = false;
                        break;
                    }

                    // Check if li directly contains two nodes with any lines between them
                    var directChild = li.getFirstChild();
                    var lastDirectChild = li.getLastChild();
                    while (directChild != lastDirectChild) {
                        if (hasLinesBetween(directChild, directChild.getNext())) {
                            isTight = false;
                            break OUTER;
                        }
                        directChild = directChild.getNext();
                    }
                    li = li.getNext();
                }
                listNode.getPositions().setEnd(listNode.getLastChild().getPositions().getEnd());
                listNode.getListData().setTight(isTight);
            }
            case ListItemNode li -> {
                if (li.hasLastChild()) {
                    li.getPositions().setEnd(
                            li.getLastChild().getPositions().getEnd()
                    );
                } else {
                    li.getPositions().setEnd(
                            li.getPositions().getStart().line(),
                            li.getListData().getMinimumIndent()
                    );
                }
            }
            case FencedCodeBlockNode codeBlock -> {
                var rawLines = codeBlock.getRawLines();
                var totalLines = rawLines.size();
                StringJoiner contentJoiner = new StringJoiner("\n");
                // Skip the info-string line
                for (int i = 1; i < totalLines; ++i) {
                    contentJoiner.add(rawLines.get(i));
                }
                String literal = contentJoiner.toString();
                if (!literal.isEmpty()) {
                    literal += "\n";
                }
                codeBlock.setLiteral(literal);
            }
            case IndentedCodeBlockNode codeBlock -> {
                var rawLines = codeBlock.getRawLines();
                int lastContentLine = rawLines.size() - 1;
                // Trim empty trailing lines
                // we know we can safely do this as an indented code block has at least one non-blank line
                while (rawLines.get(lastContentLine).isBlank()) {
                    lastContentLine--;
                }

                StringJoiner contentJoiner = new StringJoiner("\n", "", "\n");
                for (int i = 0; i <= lastContentLine; ++i) {
                    contentJoiner.add(rawLines.get(i));
                }
                codeBlock.setLiteral(contentJoiner.toString());
            }
            case HTMLBlockNode htmlBlock ->
                htmlBlock.setLiteral(String.join("\n", htmlBlock.getRawLines()));
            default -> {}
        }
    }

    private void processLine(final String line) {
        lineNumber++;
        prevOpenBlock = lastOpenBlock;
        offset = column = 0;
        blank = partiallyConsumedTab = false;
        currentLine = line;

        BlockNode lastChild;
        BlockNode container = this.ast.getRoot();
        // Match as many block containers as possible, otherwise we end at the last matched
        LOOP:
        while ((lastChild = (BlockNode) container.getLastChild()) != null && lastChild.isOpen()) {
            container = lastChild;
            findNextNonspace();
            switch (canContinue(container)) {
                case MATCHED -> {}
                case NOT_MATCHED -> {
                    container = container.getParent();
                    break LOOP;
                }
                case CONSUMED -> {
                    return;
                }
            }
        }

        hasUnclosedBlocks = (container != prevOpenBlock);
        lastMatchedContainer = container;

        // Can the current container be interrupted by a new block start?
        // E.g. A paragraph can be interrupted by # Example-Header
        boolean isInterruptable = !(container instanceof RawLeafBlockNode);

        LOOP:
        while (isInterruptable) {
            findNextNonspace();
            // Try to start a new block
            switch (startNextBlock(container)) {
                case NONE -> {
                    consumeWhitespaces();
                    break LOOP;
                }
                case LEAF -> {
                    container = lastOpenBlock;
                    break LOOP;
                }
                case CONTAINER -> container = lastOpenBlock;
            }
        }

        if (hasUnclosedBlocks && !blank && lastOpenBlock instanceof ParagraphNode) {
            acceptLine(); // Lazy paragraph continuation
        } else {
            closeUnmatchedBlocks();
            if (container instanceof AcceptsLines) {
                acceptLine();
                if (container instanceof HTMLBlockNode htmlBlock && htmlBlock.allowsBlankLines()) {
                    if (BlockParser.tryMatchHTMLEndType(line, offset, htmlBlock.getTypeId())) {
                        closeBlock(htmlBlock, lineNumber);
                    }
                }
            } else if (offset < line.length() && !blank) {
                ParagraphNode node = new ParagraphNode();
                appendChild(node, offset);
                consumeWhitespaces();
                acceptLine();
            }
        }
    }

    private int currentTabAmount() {
        // Manually optimized incase Java compiler doesn't
        return 4 - (column & 3);
    }

    private int currentTabAmount(int col) {
        // Manually optimized incase Java compiler doesn't
        return 4 - (col & 3);
    }

    private int nextNonspaceChar() {
        return ParserUtils.peek(currentLine, nextNonspace);
    }

    private void consumeWhitespace(int limit) {
        for (int i = 0; i < limit && isSpaceOrTab(peek(offset)); i++) {
            consumeColumns(1);
        }
    }

    /**
     * Consume the whitespace to the next non-whitespace column
     */
    private void consumeWhitespaces() {
        offset = nextNonspace;
        column = nextNonspaceColumn;
        partiallyConsumedTab = false;
    }

    /**
     * Consume a specified number of columns, where tabs have a width of 4 columns
     * @param count number of columns to consume
     */
    private void consumeColumns(final int count) {
        final int len = currentLine.length();
        int remaining = count;
        while (0 < remaining && offset < len) {
            char c = currentLine.charAt(offset);
            if (c == '\t') {
                int tabAmount = currentTabAmount();
                if (remaining < tabAmount) {
                    this.column += remaining;
                    this.partiallyConsumedTab = true;
                    break;
                } else {
                    this.column += tabAmount;
                    this.offset++;
                    remaining -= tabAmount;
                }
            } else {
                column++;
                offset++;
                remaining--;
            }
            this.partiallyConsumedTab = false;
        }
    }

    /**
     * Consume a specified number of characters, where tabs count as 1 character.
     * @param count number of characters to consume
     */
    private void consumeChars(final int count) {
        final int len = currentLine.length();
        int remaining = count;
        while (0 < remaining && offset < len) {
            column += '\t' == currentLine.charAt(offset)
                    ? currentTabAmount()
                    : 1;
            offset++;
            remaining--;
        }
        partiallyConsumedTab = false;
    }

    private void findNextNonspace() {
        int currentCol = column;

        // Assume blank until we find a non-space.
        // If we hit EOL this will remain true
        boolean isBlank = true;

        int i = offset;
        final int len = currentLine.length();
        LOOP:
        while (i < len) {
            char ch = currentLine.charAt(i);
            switch (ch) {
                case ' '  -> currentCol++;
                case '\t' -> currentCol += currentTabAmount(currentCol);
                default -> {
                    isBlank = false;
                    break LOOP;
                }
            }
            i++;
        }

        this.blank = isBlank;
        this.nextNonspace = i;
        this.nextNonspaceColumn = currentCol;
        this.indent = currentCol - column;
        this.indented = this.indent >= 4;
    }

    /**
     * Add the offset content of the current line to the {@code lastOpenBlock}'s content.
     */
    private void acceptLine() {
        if (lastOpenBlock instanceof AcceptsLines linesBlock) {
            StringBuilder sb = new StringBuilder();
            if (partiallyConsumedTab) {
                offset++;
                sb.repeat(' ', currentTabAmount());
            }
            sb.append(currentLine, offset, currentLine.length());
            linesBlock.acceptLine(sb.toString());
        } else {
            throw new IllegalStateException("Expected instanceof AcceptsLines, instead found " + lastOpenBlock.getClass().getName());
        }
    }

    /**
     * Append a child to the deepest block willing to accept it, closing anything
     * deeper that cannot accept it.
     * @param newBlockNode Child to add to a container block
     * @param offset Source offset to start this child at
     * @return a reference to the given {@code newBlockNode}
     * @param <T> type of the new node
     */
    private <T extends BlockNode> T appendChild(T newBlockNode, int offset) {
        while (!canContain(lastOpenBlock, newBlockNode)) {
            closeBlock(lastOpenBlock, lineNumber - 1);
        }
        newBlockNode.getPositions().setStart(lineNumber, offset + 1);
        lastOpenBlock.appendChild(newBlockNode);
        lastOpenBlock = newBlockNode;
        return newBlockNode;
    }


    /**
     * Close any blocks containers that we backed out of, such as when exiting a block-quote
     */
    void closeUnmatchedBlocks() {
        if (hasUnclosedBlocks) {
            while (prevOpenBlock != lastMatchedContainer) {
                var parent = prevOpenBlock.getParent();
                closeBlock(prevOpenBlock, lineNumber - 1);
                prevOpenBlock = parent;
            }
            hasUnclosedBlocks = false;
        }
    }

    /**
     * Advance the cursor over the remaining characters on the current line
     */
    void consumeRemaining() {
        consumeChars(currentLine.length() - offset);
    }

    private enum StartType {
        NONE,
        CONTAINER,
        LEAF
    }

    /**
     * Try to start a block node
     * @param currentNode immediate parent node/containing node
     * @return the result of if it was able to find a match
     */
    private StartType startNextBlock(BlockNode currentNode) {
        List<Function<BlockNode, ? extends BlockNode>> startChecks = List.of(
                this::startBlockQuote,
                this::startATXHeading,
                this::startFencedCodeBlock,
                this::startHTMLBlock,
                this::startSetextHeading,
                this::startThematicBreak,
                this::startListItem,
                this::startIndentedCodeBlock
        );
        for (var startCheck : startChecks) {
            var result = startCheck.apply(currentNode);
            if (result != null) {
                return result instanceof ContainerBlockNode
                        ? StartType.CONTAINER
                        : StartType.LEAF;
            }
        }
        return StartType.NONE;
    }

    /**
     * Try to start a Block Quote
     * @return {@code null} if it cannot start, otherwise a Node
     */
    private BlockQuoteNode startBlockQuote(BlockNode currentNode) {
        if (indented || peek(nextNonspace) != '>') {
            return null;
        }
        consumeWhitespaces(); // indent
        consumeChars(1); // '>'
        consumeWhitespace(1); // optional ' ' after '>'

        closeUnmatchedBlocks();

        return appendChild(new BlockQuoteNode(), nextNonspace);
    }

    /**
     * Try to start an ATX Heading
     * @return {@code null} if it cannot start, otherwise a Node
     */
    private ATXHeadingNode startATXHeading(BlockNode currentNode) {
        if (indented) {
            return null;
        }

        // Starts with 1-6 unescaped '#'
        LineParser lineParser = new LineParser(currentLine, nextNonspace);
        int level = lineParser.consumeChar('#', 7);
        if (level < 1 || level > 6) {
            return null;
        }

        // Opening must be followed by spaces or tabs, or by the end of the line
        int leadingSpaces = lineParser.stripLeading();

        // Check for empty header
        if (lineParser.outOfBounds() || lineParser.matchAll('#')) {
            var emptyHeader = new ATXHeadingNode(level);
            consumeWhitespaces();
            consumeChars(level + leadingSpaces);
            closeUnmatchedBlocks();
            appendChild(emptyHeader, nextNonspace);
            consumeRemaining();
            return emptyHeader;
        }

        // Check for required whitespace
        if (leadingSpaces == 0) {
            return null;
        }

        // We have a valid header,
        // Now check if it has an optional closing sequence

        // Check for closing
        lineParser.stripTrailing();
        int lastWhitespaceIndex = lineParser.indexOfWhitespaceFromEnd();
        if (lastWhitespaceIndex != -1) {
            // We have a whitespace somewhere, check if the last group is all #'s
            boolean hasOptionalClosing = lineParser.matchAll('#', lastWhitespaceIndex + 1, lineParser.right);
            if (hasOptionalClosing) {
                // Chop off optional closing and trim content again
                lineParser.right = lastWhitespaceIndex;
                lineParser.stripTrailing();
            }
        }

        var header = new ATXHeadingNode(level);
        header.setLiteral(lineParser.substring());

        consumeWhitespaces();
        consumeChars(level + leadingSpaces);
        closeUnmatchedBlocks();
        appendChild(header, nextNonspace);
        consumeRemaining();
        return header;
    }

    /**
     * Try to start a Fenced Code Block
     * @return {@code null} if it cannot start, otherwise a Node
     */
    private FencedCodeBlockNode startFencedCodeBlock(BlockNode currentNode) {
        int nextCodepoint = peek(nextNonspace);
        if (indented || (nextCodepoint != '~' && nextCodepoint != '`')) {
            return null;
        }
        final char fenceType = (char) nextCodepoint;

        LineParser lineParser = new LineParser(currentLine, nextNonspace);
        int fenceLength = lineParser.consumeChar(fenceType);
        if (fenceLength < 3) {
            return null;
        }
        lineParser.strip();
        String infoString = ParserUtils.backslashEscape(lineParser.substring());

        // If the info string comes after a backtick fence, it may not contain any backtick characters
        if (fenceType == '`' && infoString.indexOf('`') >= 0) {
            return null;
        }

        closeUnmatchedBlocks();
        var codeBlock = new FencedCodeBlockNode(infoString, fenceType, fenceLength, indent);
        appendChild(codeBlock, nextNonspace);
        consumeWhitespaces();
        consumeChars(fenceLength);
        return codeBlock;
    }

    /**
     * Try to start an HTML Block
     * @return {@code null} if it cannot start, otherwise a Node
     */
    private HTMLBlockNode startHTMLBlock(BlockNode currentNode) {
        // Optimized prerequisite check, since all HTML types start with '<'
        if (indented || peek(nextNonspace) != '<') {
            return null;
        }
        String inputText = currentLine.substring(nextNonspace);
        int type = tryMatchHTMLStartType(inputText);
        if (type == -1) {
            return null;
        }
        if (type == 7) {
            // Type 7 cannot interrupt a paragraph, including lazy continuation check
            if (currentNode instanceof ParagraphNode ||
                    (hasUnclosedBlocks && !blank && lastOpenBlock instanceof ParagraphNode)) {
                return null;
            }
        }
        closeUnmatchedBlocks();
        return appendChild(new HTMLBlockNode(type), offset);
    }

    private static final Pattern RE_HTML_TYPE_1_START = Pattern.compile("(?i)^<(?:pre|script|style|textarea)(?:\\s|>|$)");
    private static final Pattern RE_HTML_TYPE_1_END = Pattern.compile("(?i)</(?:pre|script|style|textarea)>");
    private static final Pattern RE_HTML_TYPE_6_START = Pattern.compile("(?i)^</?(address|article|aside|base|basefont|blockquote|body|caption|center|col|colgroup|dd|details|dialog|dir|div|dl|dt|fieldset|figcaption|figure|footer|form|frame|frameset|h1|h2|h3|h4|h5|h6|head|header|hr|html|iframe|legend|li|link|main|menu|menuitem|nav|noframes|ol|optgroup|option|p|param|search|section|summary|table|tbody|td|tfoot|th|thead|title|tr|track|ul)(\\s|>|/>|$)");

    /**
     * Check if an HTML block can start based on the given input
     * @return the HTML block type, or -1 if no matches
     */
    private static int tryMatchHTMLStartType(String input) {
        // Type 2
        if (input.startsWith("<!--")) {
            return 2;
        }
        // Type 3
        if (input.startsWith("<?")) {
            return 3;
        }
        // Type 4
        if (input.length() >= 3 && input.startsWith("<!")) {
            char letter = input.charAt(2);
            if (('a' <= letter && letter <= 'z' ) || ('A' <= letter && letter <= 'Z')) {
                return 4;
            }
        }
        // Type 5
        if (input.startsWith("<![CDATA[")) {
            return 5;
        }
        // Type 1 (slower regex check)
        if (RE_HTML_TYPE_1_START.matcher(input).find()) {
            return 1;
        }
        // Type 6 (slower regex check)
        if (RE_HTML_TYPE_6_START.matcher(input).find()) {
            return 6;
        }
        // Type 7 (slower regex checks)
        var withoutTrailingWhitespace = input.stripTrailing();
        if (HtmlParserUtils.isClosingTag(withoutTrailingWhitespace) || HtmlParserUtils.isOpeningTag(withoutTrailingWhitespace)) {
            return 7;
        }
        return -1;
    }

    /**
     * Check if an HTML block's end condition is met, staring from the containing block's offset
     */
    private static boolean tryMatchHTMLEndType(String input, int offset, int htmlType) {
        return switch (htmlType) {
            case 1 -> RE_HTML_TYPE_1_END.matcher(input.substring(offset)).find();
            case 2 -> input.indexOf("-->", offset) >= 0;
            case 3 -> input.indexOf("?>", offset) >= 0;
            case 4 -> input.indexOf(">", offset) >= 0;
            case 5 -> input.indexOf("]]>", offset) >= 0;
            default -> throw new IllegalArgumentException("Illegal type " + htmlType + ", expected in range [1, 5]");
        };
    }

    /**
     * Try to start a Setext Heading
     * @return {@code null} if it cannot start, otherwise a Node
     */
    private SetextHeadingNode startSetextHeading(BlockNode currentNode) {
        if (indented || !(currentNode instanceof ParagraphNode paragraphNode)) {
            return null;
        }
        int headingCodepoint = nextNonspaceChar();
        if (headingCodepoint != '-' && headingCodepoint != '=') {
            return null;
        }
        char headingType = (char) headingCodepoint;
        final int len = currentLine.length();
        int firstSpace = len;
        for (int i = nextNonspace; i < len; ++i) {
            char ch = currentLine.charAt(i);
            if (isSpaceOrTab(ch)) {
                firstSpace = i;
                break;
            }
            if (ch != headingType) {
                return null; // Invalid character
            }
        }
        // Must only have trailing whitespaces now, if any
        for (int i = firstSpace + 1; i < len; ++i) {
            if (!isSpaceOrTab(currentLine.charAt(i))) {
                return null;
            }
        }

        closeUnmatchedBlocks();

        // Reached end of line with a valid setext heading underline
        // Now, is it a valid setext heading?
        // Need to extract links to find out
        var links = LinkReferenceDefinitionParser.extractLinks(paragraphNode);
        addLinks(links, paragraphNode);

        if (paragraphNode.isBlank()) {
            // This is a very rare, and fairly ambiguous case on how to handle an empty
            // paragraph now that could be a list/break/etc

            // All the links were removed from this paragraph and
            // all that remains is something containing at least one '-' or '='
            // which could be a list or thematic break.
            // Remove this empty paragraph
//            lastOpenBlock = links.getLast();
//            paragraphNode.unlink();
            // would also need to update container too
            // Maybe I could fix this oddness by stripping the paragraph and then throwing a RetryBlockException
            // To re-enable/check list/break parsing
            return null;
        }

        var heading = new SetextHeadingNode(headingType == '=' ? 1 : 2);
        heading.setPositions(paragraphNode.getPositions());
        heading.setLiteral(paragraphNode.getLiteral());
        paragraphNode.insertAfter(heading);
        paragraphNode.unlink(); // Remove/replace itself with the setext value
        lastOpenBlock = heading;
        consumeRemaining();
        return heading;
    }

    /**
     * Try to start a Thematic Break
     * @return {@code null} if it cannot start, otherwise a Node
     */
    private ThematicBreakNode startThematicBreak(BlockNode currentNode) {
        if (indented) {
            return null;
        }
        final int delimiterType = nextNonspaceChar();
        if (delimiterType != '*' && delimiterType != '-' && delimiterType != '_') {
            return null;
        }
        final int len = currentLine.length();
        // We already have 1/3
        int count = 1;
        for (int i = nextNonspace + 1; i < len; ++i) {
            // Only allow whitespaces and the chosen delimiter
            char c = currentLine.charAt(i);
            if (c == delimiterType) {
                count++;
            } else if (!isSpaceOrTab(c)) {
                return null;
            }
        }

        if (count < 3) {
            return null;
        }

        // We have a match
        closeUnmatchedBlocks();
        var node = new ThematicBreakNode();
        appendChild(node, nextNonspace);
        consumeRemaining();
        return node;
    }

    /**
     * Try to start a List-Item
     * @return {@code null} if it cannot start, otherwise a Node
     */
    private ListItemNode startListItem(BlockNode currentNode) {
        if (indented && !(currentNode instanceof ListNode)) {
            return null;
        }
        if (this.indent >= 4) {
            return null;
        }
        final int len = currentLine.length();

        ListData listData = new ListData();
        listData.setMarkerOffset(indent);

        boolean interruptingParagraph = currentNode instanceof ParagraphNode;
        int afterMarker = -1; // Index of first character after list item marker
        final int startOfMarker = nextNonspace; // Index of the first character in the marker

        int nextChar = nextNonspaceChar();
        if (nextChar == '-' || nextChar == '*' || nextChar == '+') {
            listData.setType(ListData.Type.UNORDERED);
            listData.setDelimiter((char) nextChar);
            afterMarker = nextNonspace + 1;
        } else {
            // Try to parse as ordered list
            int digits = 0;
            int startNumber = 0;
            char firstNonDigit = '\0';
            for (int i = nextNonspace; i < len; ++i) {
                char ch = currentLine.charAt(i);
                if ('0' <= ch && ch <= '9') {
                    digits++;
                    startNumber = (startNumber * 10) + (ch - 48); // atoi
                }
                else {
                    firstNonDigit = ch;
                    afterMarker = i + 1;
                    break;
                }
            }
            if (digits < 1 // needs at least one digit
                    || digits > 9 // allow up to 9 digits
                    || (firstNonDigit != '.' && firstNonDigit != ')') // Correct delimiter
                    || (interruptingParagraph && startNumber != 1) // Must start at 1 if interrupting
            ) {
                return null;
            }

            listData.setType(ListData.Type.ORDERED);
            listData.setOrderedStart(startNumber);
            listData.setDelimiter(firstNonDigit);
        }

        // Empty items cannot interrupt a paragraph
        if (interruptingParagraph && ParserUtils.isBlank(currentLine, afterMarker)) {
            return null;
        }

        // Must be followed by a space, tab, or newline
        int codepoint = peek(afterMarker);
        if (codepoint != -1 && codepoint != ' ' && codepoint != '\t') {
            return null;
        }

        // -- Valid list starts here -- //
        consumeWhitespaces();
        int markerWidth = afterMarker - startOfMarker;
        consumeColumns(markerWidth);

        int markerIndentStartCol = column;
        int markerIndentStartOffset = offset;
        do {
            consumeColumns(1);
            codepoint = peek(offset);
        } while (column - markerIndentStartCol < 5 && isSpaceOrTab(codepoint));

        boolean isBlankItem = peek(offset) == -1;
        int N = column - markerIndentStartCol;
        if (N < 1 || N > 4 || isBlankItem) {
            listData.setPadding(markerWidth + 1);
            column = markerIndentStartCol;
            offset = markerIndentStartOffset;
            consumeWhitespace(1);
        } else {
            listData.setPadding(markerWidth + N);
        }

        closeUnmatchedBlocks();

        // Check if it is a list node and if it accepts
        if (!(lastOpenBlock instanceof ListNode listNode) || !listNode.allows(listData)) {
            ListNode newListNode = new ListNode(listData);
            appendChild(newListNode, nextNonspace);
        }
        return appendChild(new ListItemNode(listData), nextNonspace);
    }

    /**
     * Try to start an Indented Code Block
     * @return {@code null} if it cannot start, otherwise a Node
     */
    private IndentedCodeBlockNode startIndentedCodeBlock(BlockNode currentNode) {
        // Cannot interrupt a paragraph or start on a blank line
        if (!indented || (lastOpenBlock instanceof ParagraphNode) || blank) {
            return null;
        }
        consumeColumns(INDENTED_CODE_BLOCK_INDENT);
        closeUnmatchedBlocks();
        return appendChild(new IndentedCodeBlockNode(), offset);
    }

    /**
     * Extract any LRDs from paragraphs and add them to the AST.
     * @param subtreeRoot The node to scan from
     */
    private void extractLinkReferenceDefinitions(BlockNode subtreeRoot) {
        var events = subtreeRoot.toList();
        for (var nodeEvent : events) {
            var node = nodeEvent.node();
            if (nodeEvent.isExiting() || !(node instanceof ParagraphNode paragraphNode)) {
                continue;
            }
            var links = LinkReferenceDefinitionParser.extractLinks(paragraphNode);
            addLinks(links, paragraphNode);

            // If the paragraph was only links, remove itself
            if (paragraphNode.isBlank()) {
                paragraphNode.unlink();
            }
        }
    }

    /**
     * Add link nodes to the AST before the given node, and update the linkRefMap
     * @param linkNodes nodes to add, in order
     * @param source node to add links before
     */
    private void addLinks(List<LinkReferenceDefinitionNode> linkNodes, BlockNode source) {
        for (var link : linkNodes) {
            if (!this.linkRefMap.put(link)) {
                System.err.println("WARNING: Duplicate link reference definition " + link);
            }
            source.insertBefore(link);
        }
    }


}
