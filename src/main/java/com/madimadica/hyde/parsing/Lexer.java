package com.madimadica.hyde.parsing;

import java.nio.file.Files;

public class Lexer {
    private final String input;
    private final int size;
    private int position;
    private int readPosition;

    public Lexer(String input) {
        this.input = input;
        this.size = input.length();
    }

    public String getInput() {
        return input;
    }

    public int getSize() {
        return size;
    }

    public int getPosition() {
        return position;
    }

    public int getReadPosition() {
        return readPosition;
    }

    public void setReadPosition(int readPosition) {
        this.readPosition = readPosition;
    }

    void setPosition(int position) {
        this.position = position;
    }

    /**
     * Check if the character at the given index
     * of the input equals the target.
     * <br>
     * Returns false if index out-of-bounds or target doesn't match.
     * @param index - position to look in the input string
     * @return true if the input matches the target
     */
    private boolean charEquals(int index, char target) {
        if (index < size) {
            return this.input.charAt(index) == target;
        }
        return false;
    }

    private void resetLookahead() {
        readPosition = position;
    }

    /**
     * Find to the end of the current line (LF/CRLF/CR)
     * and then return up to but excluding the line ending.
     * <br>
     * Updates the readPosition to the start of the next line.
     * @return the current line, excluding the line ending.
     */
    public String previewLine() {
        if (position >= size) return "";

        int endingWidth = 0;
        while (readPosition < size) {
            char currentChar = input.charAt(readPosition);

            if (currentChar == '\n') {
                endingWidth = 1;
                readPosition++; // LF
                break;
            } else if (currentChar == '\r') {
                // CR found, check for LF
                if (charEquals(readPosition + 1, '\n')) {
                    endingWidth = 2;
                    readPosition += 2; // CRLF
                } else {
                    endingWidth = 1;
                    readPosition++; // CR
                }
                break;
            } else {
                readPosition++;
            }
        }
        // EOF
        if (endingWidth == 0) {
            return input.substring(position);
        }
        return input.substring(position, readPosition - endingWidth + 1);
    }

    public void skipLine() {
        if (position >= size) {
            System.out.println("WARN - Skipping line at EOF");
            return;
        }
        while (readPosition < size) {
            char currentChar = input.charAt(readPosition);
            if (currentChar == '\n') {
                readPosition++; // LF
                break;
            } else if (currentChar == '\r') {
                // CR found, check for LF
                if (charEquals(readPosition + 1, '\n')) {
                    readPosition += 2; // CRLF
                } else {
                    readPosition++; // CR
                }
                break;
            } else {
                readPosition++;
            }
        }
        position = readPosition;
    }
}
