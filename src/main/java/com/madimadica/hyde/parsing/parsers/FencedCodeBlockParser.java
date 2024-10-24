package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import com.madimadica.hyde.syntax.FencedCodeBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <a href="https://spec.commonmark.org/0.31.2/#fenced-code-blocks">CommonMark Spec - Fenced Code Blocks</a>
 *
 * <br>
 *
 * @see FencedCodeBlock
 */
public class FencedCodeBlockParser implements Parser<FencedCodeBlock> {

    public static final int MIN_FENCE_SIZE = 3;
    public static final int MAX_INDENT = 3;

    @Override
    public Optional<FencedCodeBlock> parse(Lexer lexer) {
        var allLines = lexer.getLines();
        int lineNumber = lexer.getLineNumber();

        /* A fenced code block begins with a code fence, preceded by up to three spaces of indentation. */
        int indentation = lexer.getLineIndentation(lineNumber);
        if (lexer.isBlankLine(lineNumber) || indentation > 3) {
            return Optional.empty();
        }

        /* A code fence is a sequence of at least three consecutive backtick characters (`) or tildes (~). */
        String unindentedStart = allLines.get(lineNumber).substring(indentation);
        if (!unindentedStart.startsWith("```") && !unindentedStart.startsWith("~~~")) {
            return Optional.empty();
        }

        /*
         * The line with the opening code fence may optionally contain
         * some text following the code fence; this is trimmed of
         * leading and trailing spaces or tabs and called the info string
         */
        final char fenceChar = unindentedStart.charAt(0);
        final int openingFenceLength = Lexer.countLeading(unindentedStart, fenceChar);
        final String infoString = unindentedStart.substring(openingFenceLength).strip();

        /* If the info string comes after a backtick fence, it may not contain any backtick characters */
        if (fenceChar == '`' && infoString.contains("`")) {
            return Optional.empty();
        }

        /*
         * The content of the code block consists of all subsequent lines,
         * until a closing code fence of the same type as the code block began with (backticks or tildes),
         * and with at least as many backticks or tildes as the opening code fence.
         *
         * If the end of the containing block (or document) is reached
         * and no closing code fence has been found, the code block
         * contains all the lines after the opening code fence
         * until the end of the containing block (or document).
         */
        int closingFenceLineNumber = lexer.getTotalLines();
        for (int currentLineNumber = lineNumber + 1; currentLineNumber < lexer.getTotalLines(); ++currentLineNumber) {
            String currentLine = allLines.get(currentLineNumber);
            int currentIndent = lexer.getLineIndentation(currentLineNumber);
            /* The closing code fence may be preceded by up to three spaces of indentation */
            if (currentIndent > MAX_INDENT || lexer.isBlankLine(currentLineNumber)) {
                continue;
            }
            /* with at least as many backticks or tildes as the opening code fence. */
            String unindentedCurrentLine = currentLine.substring(currentIndent);
            int fenceLength = Lexer.countLeading(unindentedCurrentLine, fenceChar);
            if (fenceLength < openingFenceLength) {
                continue;
            }
            /* [The closing code fence] may be followed only by spaces or tabs, which are ignored. */
            if (!unindentedCurrentLine.substring(fenceLength).isBlank()) {
                continue;
            }

            // I believe we have a line ending
            closingFenceLineNumber = currentLineNumber;
            break;
        }

        List<String> content = new ArrayList<>(closingFenceLineNumber - lineNumber);
        for (int i = lineNumber + 1; i < closingFenceLineNumber; ++i) {
            String currentLine = allLines.get(i);
            /*
             * If the leading code fence is preceded by N spaces of indentation,
             * then up to N spaces of indentation are removed from each line of the content (if present).
             * (If a content line is not indented, it is preserved unchanged.
             * If it is indented N spaces or less, all the indentation is removed.)
             */
            if (indentation > 0) {
                currentLine = reduceIndent(currentLine, indentation);
            }
            content.add(currentLine);
        }

        return Optional.of(new FencedCodeBlock(infoString, content));
    }


    public static String reduceIndent(String s, int amount) {
        final int len = s.length();
        int amountRemoved = 0;
        int nextTabMarker = 4;
        int i = 0;
        for (; i < len && amountRemoved < amount; ++i) {
            char c = s.charAt(i);
            if (c == '\t') {
                amountRemoved = nextTabMarker;
            } else if (c == ' ') {
                amountRemoved++;
            } else {
                break;
            }
            if (amountRemoved == nextTabMarker) {
                nextTabMarker += 4;
            }
        }
        String result = s.substring(i);
        // If we removed too much, add spaces back
        int overage = amountRemoved - amount;
        if (overage > 0) {
            result = " ".repeat(overage) + result;
        }
        return result;
    }

}
