package com.madimadica.hyde.parsing;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Lexer implements Iterable<Character> {
    /**
     * Original input Strings
     */
    private final String input;
    /**
     * Original line data without line endings
     */
    private final List<String> lines;
    /**
     * Total lines in {@code this.lines}, for fast access.
     */
    private final int totalLines;
    /**
     * The line number (0 based) the Lexer
     * is currently positioned at.
     */
    private int lineNumber;
    /**
     * Ths column number (0 based) the Lexer
     * is currently positioned at.
     */
    private int columnNumber;
    /**
     * The line number (0 based) the look-ahead
     * is currently positioned at.
     */
    private int readLineNumber;
    /**
     * Ths column number (0 based) the look-ahead
     * is currently positioned at.
     */
    private int readColumnNumber;
    /**
     * Precompute and cache the indentation amounts for each line.<br>
     * Indexed the same as <code>lines</code>.
     */
    private final int[] indentations;

    /**
     * Regex pattern to split by line endings, including a standalone {@code '\r'}
     */
    private static final Pattern REGEX_LINE_END = Pattern.compile("\\r?\\n|\\r");

    public Lexer(String input) {
        this.input = input;
        this.lines = List.of(REGEX_LINE_END.split(input));
        this.totalLines = this.lines.size();
        this.indentations = new int[totalLines];
        for (int i = 0; i < totalLines; ++i) {
            indentations[i] = Lexer.computeIndentation(lines.get(i));
        }
    }

    public String getInput() {
        return input;
    }

    public List<String> getLines() {
        return lines;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public int getReadLineNumber() {
        return readLineNumber;
    }

    public int getReadColumnNumber() {
        return readColumnNumber;
    }

    public Optional<String> getLine(int lineNumber) {
        if (0 <= lineNumber && lineNumber < lines.size()) {
            return Optional.of(lines.get(lineNumber));
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> getUnindentedLine(int lineNumber) {
        if (0 <= lineNumber && lineNumber < lines.size()) {
            if (this.isBlankLine(lineNumber)) {
                return Optional.of("");
            } else {
                return Optional.of(lines.get(lineNumber).substring(indentations[lineNumber]));
            }
        } else {
            return Optional.empty();
        }
    }

    public Optional<Character> getCharacter(int lineNumber, int column) {
        var optionalLine = getLine(lineNumber);
        if (optionalLine.isEmpty()) {
            return Optional.empty();
        }
        final String line = optionalLine.get();
        if (column < line.length()) {
            return Optional.of(line.charAt(column));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Check if the character at the given position (line:col)
     * of the input equals the target.
     * <br>
     * Returns false if index out-of-bounds or target doesn't match.
     * @param lineNumber - line to look at (0 based)
     * @param columnNumber - column to look at (0 based)
     * @return true if the input at the given position matches the target
     */
    private boolean charEquals(int lineNumber, int columnNumber, char target) {
        var charResult = this.getCharacter(lineNumber, columnNumber);
        if (charResult.isPresent()) {
            return charResult.get() == target;
        }
        return false;
    }

    /**
     * Reset the lookahead back to the last known
     * location of the lexer.
     */
    private void resetLookahead() {
        this.readLineNumber = this.lineNumber;
        this.readColumnNumber = this.columnNumber;
    }

    public String getCurrentLine() {
        return this.lines.get(this.lineNumber);
    }

    /**
     * Get the number of lines left to process, including the current line.
     * <br>
     * This will return 0 if all lines are exhausted.
     * @return number of lines remaining to process
     */
    public int getRemainingLines() {
        return this.totalLines - this.lineNumber;
    }

    /**
     * Find a line relative to the current line.
     * @param offset - integer offset, either positive or negative (or zero).
     * @return an optional string if the line exists.
     */
    public Optional<String> getLineWithOffset(int offset) {
        return getLine(this.lineNumber + offset);
    }

    /**
     * Move the lexer to the next line.
     * Resets the lookahead to the start of this line.
     */
    public void skipLine() {
        skipLines(1);
    }

    public void skipLines(int amountToSkip) {
        this.lineNumber += amountToSkip;
        this.columnNumber = 0;
        this.resetLookahead();
    }

    public static int countLeadingSpaces(String input) {
        final char[] chars = input.toCharArray();
        final int len = chars.length;
        int i = 0;
        while (i < len && chars[i] == ' ') {
            i++;
        }
        return i;
    }

    /**
     * Count the number of leading spaces, up to the limit (inclusive)
     * and return early if the limit is reached
     * @param input - input String to analyze
     * @param limit - maximum number of whitespaces to count
     * @return number of leading spaces found, up to the limit
     */
    public static int countLeadingSpaces(String input, int limit) {
        final char[] chars = input.toCharArray();
        final int len = chars.length;
        int i = 0;
        while (i < len && chars[i] == ' ') {
            i++;
            if (i == limit) return limit;
        }
        return i;
    }

    /**
     * Verify if the given input contains less than or equal to the limit of leading spaces.
     * @param input - input to check
     * @param limit - inclusive upper bound of the allowed number of leading spaces.
     * @return true if the number of leading spaces is less than the given limit
     */
    public static boolean checkLeadingSpacesLEQ(String input, int limit) {
        return countLeadingSpaces(input, limit + 1) <= limit;
    }

    public void skipToLine(int lineNumber) {
        this.lineNumber = lineNumber;
        this.columnNumber = 0;
        this.resetLookahead();
    }

    /**
     * Get the indentation of the current line.
     * Returns -1 if the line is blank. (0 is non-blank with no indent).
     * @param lineNumber int 0 based index of the line to check.
     * @return the indentation
     * @see Lexer#computeIndentation(String, int) 
     */
    public int getLineIndentation(int lineNumber) {
        return this.indentations[lineNumber];
    }

    /**
     * Check if the given line number is blank (all whitespaces or empty)
     * @param lineNumber int 0 based index of the line to check.
     * @return true if the link is blank.
     */
    public boolean isBlankLine(int lineNumber) {
        return this.indentations[lineNumber] == -1;
    }

    public boolean hasLine(int lineNumber) {
        return 0 <= lineNumber && lineNumber < totalLines;
    }

    public boolean hasStandardIndent(int lineNumber) {
        return !isBlankLine(lineNumber) && getLineIndentation(lineNumber) <= 3;
    }

    /**
     *
     * @param s String to check indentation on
     * @return the integer count of indentation.
     * @see Lexer#computeIndentation(String, int) 
     */
    public static int computeIndentation(String s) {
        return computeIndentation(s, 4);
    }

    /**
     * Compute how much whitespace of indentation there is between tabs and spaces.
     * <p>
     *     If the string is blank (all whitespace or empty) then return 0, because nothing is being indented.
     * </p>
     * For example, with a tabWidth=4:
     * <ul>
     *     <li>space + tab = 4</li>
     *     <li>space + space + tab = 4</li>
     *     <li>space + space + space + tab = 4</li>
     *     <li>space + space + space + space + tab = 8</li>
     *     <li>tab = 4</li>
     *     <li>tab + space = 5</li>
     *     <li>tab + space + tab = 8</li>
     * </ul>
     * @param s String to check indentation on
     * @param tabWidth tab character width
     * @return the integer count of indentation, -1 if the line is blank.
     */
    public static int computeIndentation(String s, final int tabWidth) {
        int indent = 0;
        // Track how much a tab is *currently* worth, in terms of spaces.
        // Improves efficiency by preventing modulus/division
        int currentTabAmount = tabWidth;
        // Track if the line is blank, in which case the indent should be zero
        boolean isBlank = true;
        for (char c : s.toCharArray()) {
            if (c == '\t') {
                indent += currentTabAmount;
                currentTabAmount = tabWidth;
            } else if (c == ' ') {
                indent++;
                if (--currentTabAmount == 0) {
                    currentTabAmount = tabWidth;
                }
            } else {
                isBlank = false;
                break;
            }
        }
        return isBlank ? -1 : indent;
    }

    public static int countLeading(String s, char c) {
        final int len = s.length();
        int count = 0;
        for (int i = 0; i < len; ++i) {
            if (s.charAt(i) == c) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    public boolean lineMatches(int lineNumber, Pattern pattern) {
        if (lineNumber < 0 || lineNumber >= this.totalLines) {
            return false;
        }
        return pattern.matcher(this.lines.get(lineNumber)).matches();
    }

    public boolean lineContains(int lineNumber, String substr) {
        if (lineNumber < 0 || lineNumber >= this.totalLines) {
            return false;
        }
        return this.lines.get(lineNumber).contains(substr);
    }

    public boolean lineStartsWith(int lineNumber, String substr, int maxIndent) {
        if (lineNumber < 0 || lineNumber >= this.totalLines) {
            return false;
        }
        int indent = indentations[lineNumber];
        if (indent > maxIndent) return false;
        return this.lines.get(lineNumber).startsWith(substr, Math.max(indent, 0));
    }

    public boolean inBounds(int row, int col) {
        return (0 <= row && row < totalLines)
            && (0 <= col && col < lines.get(row).length());
    }

    public boolean inBounds(int row) {
        return (0 <= row && row < totalLines);
    }

    /**
     * Return true if it's the last character on a given line
     */
    public boolean isEndOfLine(Position pos) {
        return lines.get(pos.row()).length() - 1 == pos.column();
    }

    public boolean isLastCharacter(int row, int col) {
        return row == totalLines - 1
            && col == lines.get(row).length() - 1;
    }

    @Override
    public LexerIterator iterator() {
        return new LexerIterator(lineNumber, 0);
    }

    public LexerIterator iterator(Position startingPosition) {
        return new LexerIterator(startingPosition.row(), startingPosition.column());
    }

    public class LexerIterator implements Iterator<Character> {
        /**
         * Line of the next token
         */
        private int cursorRow;
        /**
         * Character of the next token
         */
        private int cursorCol;


        LexerIterator(int cursorRow, int cursorCol) {
            this.cursorRow = cursorRow;
            this.cursorCol = cursorCol;
        }

        /**
         * @return true if the current position is in bounds,
         *         or if we are at the end of a line and another line exists
         *         (otherwise we're at EOF)
         */
        @Override
        public boolean hasNext() {
            return inBounds(cursorRow, cursorCol)
                || inBounds(cursorRow + 1);
        }

        public char previewNext() {
            return getCharacter(cursorRow, cursorCol).orElse('\n');
        }

        public Optional<Character> previewNextSafe() {
            if (!hasNext()) {
                return Optional.empty();
            }
            return Optional.of(previewNext());
        }

        /**
         * Preview the next character and see if it equals the input
         * @param target character to match on in the next token.
         * @return true if a match, false if there is no next token or it doesn't match
         */
        public boolean nextEqualsSafe(char target) {
            if (hasNext()) {
                return previewNext() == target;
            } else {
                return false;
            }
        }

        @Override
        public Character next() {
            Optional<Character> $char = getCharacter(cursorRow, cursorCol);
            cursorCol++;
            // Strictly greater than because == length is \n
            if (cursorCol > lines.get(cursorRow).length()) {
                cursorCol = 0;
                cursorRow++;
            }
            return $char.orElse('\n');
        }

        public Position getCurrentPosition() {
            return new Position(cursorRow, cursorCol);
        }

        public String consumeWhitespace() {
            StringBuilder whitespace = new StringBuilder();
            while (hasNext()) {
                char next = previewNext();
                if (next == ' ' || next == '\t' || next == '\n') {
                    whitespace.append(next());
                } else {
                    break;
                }
            }
            return whitespace.toString();
        }
    }
}
