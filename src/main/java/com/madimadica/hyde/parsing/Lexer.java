package com.madimadica.hyde.parsing;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Lexer {
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
     * Regex pattern to split by line endings, including a standalone {@code '\r'}
     */
    private static final Pattern REGEX_LINE_END = Pattern.compile("\\r?\\n|\\r");

    public Lexer(String input) {
        this.input = input;
        this.lines = List.of(REGEX_LINE_END.split(input));
        this.totalLines = this.lines.size();
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
        this.lineNumber++;
        this.columnNumber++;
        this.resetLookahead();
    }
}
