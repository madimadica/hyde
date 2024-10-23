package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import com.madimadica.hyde.syntax.SetextHeading;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * <a href="https://spec.commonmark.org/0.31.2/#setext-headings">CommonMark Spec - Setext Headings</a>
 * <br>
 * Parser for handling Setext headings.
 * <br>
 * Defined as:
 * <br>
 * <p>
 * A setext heading consists of one or more lines of text, not interrupted by a blank line, of which the first line does not have more than 3 spaces of indentation, followed by a setext heading underline. The lines of text must be such that, were they not followed by the setext heading underline, they would be interpreted as a paragraph: they cannot be interpretable as a code fence, ATX heading, block quote, thematic break, list item, or HTML block.
 * </p>
 * <p>
 * A setext heading underline is a sequence of = characters or a sequence of - characters, with no more than 3 spaces of indentation and any number of trailing spaces or tabs.
 * </p>
 * <p>
 * The heading is a level 1 heading if = characters are used in the setext heading underline, and a level 2 heading if - characters are used. The contents of the heading are the result of parsing the preceding lines of text as CommonMark inline content.
 * </p>
 * <p>
 * In general, a setext heading need not be preceded or followed by a blank line. However, it cannot interrupt a paragraph, so when a setext heading comes after a paragraph, a blank line is needed between them.
 * </p>
 * <h2>Examples</h2>
 * <pre>
 * Foo *bar*
 * =========
 *
 * Foo *bar*
 * ---------
 * </pre>
 * <p>Which would be rendered as</p>
 * <pre>
 * &lt;h1&gt;Foo &lt;em&gt;bar&lt;/em&gt;&lt;/h1&gt;
 * &lt;h2&gt;Foo &lt;em&gt;bar&lt;/em&gt;&lt;/h2&gt;
 * </pre>
 *
 * <h2>Notes</h2>
 * Remember this should run *before* resolving to a paragraph parser.
 *
 * @see SetextHeading
 */
public class SetextHeadingParser implements Parser<SetextHeading> {

    // TODO write this after Paragraph parsing, but run it before paragraph parsing
    // and write the other parsers before doing paragraph parsers, which are basically last

    /**
     * Matches 0 to 3 (inclusive) spaces, followed by a sequence of '=' or '-',
     * and ending with an optional series of tabs and spaces
     */
    public static final Pattern REGEX_SETEXT_HEADING_UNDERLINE = Pattern.compile("^[ ]{0,3}(=+|-+)[ \\t]*$");

    @Override
    public Optional<SetextHeading> parse(Lexer lexer) {
        // This is assumed to be run almost dead last so we can assume
        // Nothing else matched and just check where this should end.
        // Need to decide if this should consume/skip over the leading/trailing newlines
        // for this section
        // assume HTML blocks are already handled
        // TODO handle leading blank/empty lines


        // Need at least two lines, quick return
        if (lexer.getRemainingLines() < 2) {
            return Optional.empty();
        }

        final int startingLineNumber = lexer.getLineNumber();
        // Must have 3 or less leading spaces
        if (lexer.isBlankLine(startingLineNumber) || lexer.getLineIndentation(startingLineNumber) > 3) {
            return Optional.empty();
        }

        // TODO handle "  " trailing spaces/breaks?

        // Check the following lines until an underline or end-of-paragraph
        List<String> lines = lexer.getLines();
        for (int i = startingLineNumber + 1, len = lines.size(); i < len; ++i) {
            String lookaheadLine = lines.get(i);
            // Leading whitespaces don't matter after the initial paragraph.
            if (lookaheadLine.isBlank()) {
                // Paragraph ended before setext underline
                return Optional.empty();
            }
            // Check if it is an underline
            if (REGEX_SETEXT_HEADING_UNDERLINE.matcher(lookaheadLine).matches()) {
                // Level 1: '='    Level 2: '-'
                int headingLevel = lookaheadLine.contains("=") ? 1 : 2;
                // Get the header lines excluding the underline
                // The heading’s raw content is formed by concatenating the lines and removing initial and final spaces or tabs.
                List<String> mutableLinesDeepCopy = new ArrayList<>(lines.subList(startingLineNumber, i));
                mutableLinesDeepCopy.set(0, mutableLinesDeepCopy.get(0).stripLeading());
                int lastIndex = mutableLinesDeepCopy.size() - 1;
                mutableLinesDeepCopy.set(lastIndex, mutableLinesDeepCopy.get(lastIndex).stripTrailing());

                // Mutate the lexer state to go beyond the current line
                lexer.skipToLine(i + 1);
                return Optional.of(new SetextHeading(mutableLinesDeepCopy, headingLevel));
            }
        }

        return Optional.empty();
    }
}
