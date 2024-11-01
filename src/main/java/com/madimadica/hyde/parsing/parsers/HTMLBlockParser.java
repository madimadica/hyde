package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import com.madimadica.hyde.syntax.HTMLBlock;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * <a href="https://spec.commonmark.org/0.31.2/#html-blocks">CommonMark Spec - HTML Blocks</a>
 * <br>
 * Parses HTML blocks.
 * <br>
 * Defined by:
 * <p>
 *     An HTML block is a group of lines that is treated as raw HTML (and will not be escaped in HTML output).
 * </p>
 *
 */
public class HTMLBlockParser implements Parser<HTMLBlock> {

    private static final List<HTMLBlockParserType> SUBPARSERS = List.of(
            new HTMLBlockParserType1(),
            new HTMLBlockParserType2(),
            new HTMLBlockParserType3(),
            new HTMLBlockParserType4(),
            new HTMLBlockParserType5(),
            new HTMLBlockParserType6(),
            new HTMLBlockParserType7()
    );

    @Override
    public Optional<HTMLBlock> parse(Lexer lexer) {
        List<String> allLines = lexer.getLines();
        int lineNumber = lexer.getLineNumber();
        // Do a quick check
        if (lexer.getLineIndentation(lineNumber) > 3 || lexer.isBlankLine(lineNumber)) {
            return Optional.empty();
        }
        for (HTMLBlockParserType subparser : SUBPARSERS) {
            if (!subparser.hasStartCondition(lexer, lineNumber)) {
                continue;
            }
            int endingLineNumber = Math.max(0, lexer.getTotalLines() - 1); // Last line
            for (int currentLineNumber = lineNumber; currentLineNumber < lexer.getTotalLines(); ++currentLineNumber) {
                if (subparser.hasEndCondition(lexer, currentLineNumber)) {
                    endingLineNumber = currentLineNumber;
                    break;
                }
            }
            List<String> shallowCopy = allLines.subList(lineNumber, endingLineNumber + 1);
            lexer.skipLines(shallowCopy.size());
            return Optional.of(new HTMLBlock(shallowCopy));
        }
        return Optional.empty();
    }

    public sealed interface HTMLBlockParserType permits HTMLBlockParserType1, HTMLBlockParserType2, HTMLBlockParserType3, HTMLBlockParserType4, HTMLBlockParserType5, HTMLBlockParserType6, HTMLBlockParserType7 {
        boolean hasStartCondition(Lexer lexer, int lineNumber);
        boolean hasEndCondition(Lexer lexer, int lineNumber);
    }

    public static final class HTMLBlockParserType1 implements HTMLBlockParserType {
        public static final Pattern REGEX_START_CONDITION = Pattern.compile("(?i)^\\s{0,3}<(pre|script|style|textarea)(\\s|\\t|>|$)(.*)");
        public static final Pattern REGEX_END_CONDITION   = Pattern.compile("(?i).*</(pre|script|style|textarea)>.*");

        @Override
        public boolean hasStartCondition(Lexer lexer, int lineNumber) {
            return lexer.lineMatches(lineNumber, REGEX_START_CONDITION);
        }

        @Override
        public boolean hasEndCondition(Lexer lexer, int lineNumber) {
            return lexer.lineMatches(lineNumber, REGEX_END_CONDITION);
        }
    }

    public static final class HTMLBlockParserType2 implements HTMLBlockParserType {
        @Override
        public boolean hasStartCondition(Lexer lexer, int lineNumber) {
            return lexer.lineStartsWith(lineNumber, "<!--", 3);
        }

        @Override
        public boolean hasEndCondition(Lexer lexer, int lineNumber) {
            return lexer.lineContains(lineNumber, "-->");
        }
    }

    public static final class HTMLBlockParserType3 implements HTMLBlockParserType {
        @Override
        public boolean hasStartCondition(Lexer lexer, int lineNumber) {
            return lexer.lineStartsWith(lineNumber, "<?", 3);
        }

        @Override
        public boolean hasEndCondition(Lexer lexer, int lineNumber) {
            return lexer.lineContains(lineNumber, "?>");
        }
    }

    public static final class HTMLBlockParserType4 implements HTMLBlockParserType {
        public static final Pattern REGEX_START_CONDITION = Pattern.compile("^\\s{0,3}<![A-Za-z].*");

        @Override
        public boolean hasStartCondition(Lexer lexer, int lineNumber) {
            return lexer.lineMatches(lineNumber, REGEX_START_CONDITION);
        }

        @Override
        public boolean hasEndCondition(Lexer lexer, int lineNumber) {
            return lexer.lineContains(lineNumber, ">");
        }
    }

    public static final class HTMLBlockParserType5 implements HTMLBlockParserType {
        @Override
        public boolean hasStartCondition(Lexer lexer, int lineNumber) {
            return lexer.lineStartsWith(lineNumber, "<![CDATA[", 3);
        }

        @Override
        public boolean hasEndCondition(Lexer lexer, int lineNumber) {
            return lexer.lineContains(lineNumber, "]]>");
        }
    }

    public static final class HTMLBlockParserType6 implements HTMLBlockParserType {
        public static final Pattern REGEX_START_CONDITION = Pattern.compile("(?i)^\\s{0,3}</?(address|article|aside|base|basefont|blockquote|body|caption|center|col|colgroup|dd|details|dialog|dir|div|dl|dt|fieldset|figcaption|figure|footer|form|frame|frameset|h1|h2|h3|h4|h5|h6|head|header|hr|html|iframe|legend|li|link|main|menu|menuitem|nav|noframes|ol|optgroup|option|p|param|search|section|summary|table|tbody|td|tfoot|th|thead|title|tr|track|ul)(\\s|\\t|>|/>|$)(.*)");
        @Override
        public boolean hasStartCondition(Lexer lexer, int lineNumber) {
            return lexer.lineMatches(lineNumber, REGEX_START_CONDITION);
        }

        @Override
        public boolean hasEndCondition(Lexer lexer, int lineNumber) {
            return !lexer.hasLine(lineNumber + 1) || lexer.isBlankLine(lineNumber + 1);
        }
    }

    public static final class HTMLBlockParserType7 implements HTMLBlockParserType {
        @Override
        public boolean hasStartCondition(Lexer lexer, int lineNumber) {
            int indent = lexer.getLineIndentation(lineNumber);
            if (indent == -1 || indent > 3) {
                return false;
            }
            String afterIndent = lexer.getLine(lineNumber).get().strip();

            return ParserUtils.isClosingTag(afterIndent) || ParserUtils.isOpeningTag(afterIndent);
        }

        @Override
        public boolean hasEndCondition(Lexer lexer, int lineNumber) {
            return !lexer.hasLine(lineNumber + 1) || lexer.isBlankLine(lineNumber + 1);
        }
    }


}
