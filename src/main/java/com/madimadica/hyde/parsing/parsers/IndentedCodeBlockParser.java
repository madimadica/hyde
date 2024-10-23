package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import com.madimadica.hyde.syntax.IndentedCodeBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * <a href="https://spec.commonmark.org/0.31.2/#indented-code-blocks">CommonMark Spec - Indented Code Blocks</a>
 * <br>
 * Summarized as:
 * Starts with a non-blank line with at least 4 wide indentation (tabs or spaces)
 * This goes until a non-blank line with less than 4 spaces of indentation.
 * The blank lines before and after are consumed/trimmed.
 *
 */
public class IndentedCodeBlockParser implements Parser<IndentedCodeBlock> {

    public static final int REQUIRED_INDENT = 4;

    @Override
    public Optional<IndentedCodeBlock> parse(Lexer lexer) {
        // TODO revisit blank leading line parsing. Assume there arent any?
        List<String> allLines = lexer.getLines();
        int lineNumber = lexer.getLineNumber();
        if (lexer.getLineIndentation(lineNumber) < REQUIRED_INDENT) {
            return Optional.empty(); // Blank or not enough indent
        }
        List<String> codeBlockLines = new ArrayList<>();
        codeBlockLines.add(allLines.get(lineNumber));

        // Add lines until the block ends
        final int totalLines = lexer.getTotalLines();
        for (int i = lineNumber + 1; i < totalLines; ++i) {
            // If the line has content but not enough indentation, the code block has ended
            if (!lexer.isBlankLine(i) && lexer.getLineIndentation(i) < REQUIRED_INDENT) {
                break;
            }
            codeBlockLines.add(allLines.get(i));
        }

        // Number of lines consumed by this code block.
        // Computing before we trim lines, as the trailing blank lines are consumed but ignored.
        final int linesConsumed = codeBlockLines.size();

        // Clear any blank trailing lines
        for (int i = codeBlockLines.size(); i --> 0 && lexer.isBlankLine(i); ) {
            codeBlockLines.remove(i);
        }
        // Remove the leading indentation from the lines we kept
        codeBlockLines.replaceAll(IndentedCodeBlockParser::trimIndentation);

        // Advance the position of the lexer to the end of the codeblock (start of the line after)
        lexer.skipLines(linesConsumed);

        return Optional.of(new IndentedCodeBlock(codeBlockLines));
    }

    /**
     * Remove the first tab width of indentation (e.g. "space + tab", or "tab", or "space + space + space + space"
     * @param line
     * @return
     */
    public static String trimIndentation(String line) {
        int removed = 0;
        int i = 0;
        int len = line.length();

        while (i < len && removed < 4) {
            char c = line.charAt(i);
            if (c == '\t') {
                removed = 4; // first tab
            } else if (c == ' ') {
                removed++;
            } else {
                break;
            }
            i++;
        }
        return line.substring(i);
    }
}
