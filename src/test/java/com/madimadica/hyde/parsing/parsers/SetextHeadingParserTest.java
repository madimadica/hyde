package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class SetextHeadingParserTest {

    private void validCase(int level, String input, List<String> expectedLines) {
        Lexer lexer = new Lexer(input);
        var parser = new SetextHeadingParser();
        var result = parser.parse(lexer);
        assertTrue(result.isPresent());
        var heading = result.get();
        assertEquals(level, heading.getLevel());
        assertEquals(expectedLines, heading.getRawContent());
    }

    private void invalidCase(String input) {
        Lexer lexer = new Lexer(input);
        var parser = new SetextHeadingParser();
        var result = parser.parse(lexer);
        assertTrue(result.isEmpty());
    }

    @Test
    void invalid() {
        invalidCase("");
        invalidCase(" ");
        invalidCase("  ");
        invalidCase("   ");
        invalidCase("\t");

        invalidCase("\n===");
        invalidCase(" \n===");
        invalidCase("  \n===");
        invalidCase("   \n===");
        invalidCase("\t\n===");
    }

    @Test
    void invalid_tooMuchIndentOnFirstLine() {
        invalidCase("    Test\n===");
        invalidCase("     Test\n===");
        invalidCase("\tTest\n===");
        invalidCase("\t\tTest\n===");
        invalidCase("\t\t\tTest\n===");
        invalidCase("\t\t\t\tTest\n===");
    }

    private static String randomSpacesAndTabs(int length) {
        char[] result = new char[length];
        for (int i = 0; i < length; ++i) {
            result[i] = ThreadLocalRandom.current().nextBoolean() ? ' ' : '\t';
        }
        return new String(result);
    }

    @Test
    void valid_setextUnderlines_oneLineHeading() {
        String line1 = "foo";
        List<String> expected = List.of(line1);
        for (int level = 1; level <= 2; ++level) {
            for (int leading = 0; leading <= 3; ++leading) {
                for (int trailing = 0; trailing < 10; ++trailing) {
                    for (int length = 1; length <= 10; ++length) {
                        String ch = level == 1 ? "=" : "-";
                        String withSpacesAtEnd = " ".repeat(leading) + ch.repeat(length) + " ".repeat(trailing);
                        String withTabsAtEnd = " ".repeat(leading) + ch.repeat(length) + "\t".repeat(trailing);
                        String withMiscEnd = " ".repeat(leading) + ch.repeat(length) + randomSpacesAndTabs(trailing);
                        String testString1 = line1 + "\n" + withSpacesAtEnd;
                        String testString2 = line1 + "\n" + withTabsAtEnd;
                        String testString3 = line1 + "\n" + withMiscEnd;
                        validCase(level, testString1, expected);
                        validCase(level, testString2, expected);
                        validCase(level, testString3, expected);
                    }
                }
            }
        }
    }

    @Test
    void valid_setextUnderlines_twoLineHeading() {
        String contentLines = "foo\nbar\nbaz";
        List<String> expected = List.of("foo", "bar", "baz");
        for (int level = 1; level <= 2; ++level) {
            for (int leading = 0; leading <= 3; ++leading) {
                for (int trailing = 0; trailing < 10; ++trailing) {
                    for (int length = 1; length <= 10; ++length) {
                        String ch = level == 1 ? "=" : "-";
                        String withSpacesAtEnd = " ".repeat(leading) + ch.repeat(length) + " ".repeat(trailing);
                        String withTabsAtEnd = " ".repeat(leading) + ch.repeat(length) + "\t".repeat(trailing);
                        String withMiscEnd = " ".repeat(leading) + ch.repeat(length) + randomSpacesAndTabs(trailing);
                        String testString1 = contentLines + "\n" + withSpacesAtEnd;
                        String testString2 = contentLines + "\n" + withTabsAtEnd;
                        String testString3 = contentLines + "\n" + withMiscEnd;
                        validCase(level, testString1, expected);
                        validCase(level, testString2, expected);
                        validCase(level, testString3, expected);
                    }
                }
            }
        }
    }

    @Test
    void invalid_setextUnderlines_tooManySpacesIndented() {
        String line1 = "foo";
        for (int level = 1; level <= 2; ++level) {
            for (int leading = 4; leading <= 10; ++leading) {
                for (int trailing = 0; trailing < 10; ++trailing) {
                    for (int length = 1; length <= 10; ++length) {
                        String ch = level == 1 ? "=" : "-";
                        String withSpacesAtEnd = " ".repeat(leading) + ch.repeat(length) + " ".repeat(trailing);
                        String withTabsAtEnd = " ".repeat(leading) + ch.repeat(length) + "\t".repeat(trailing);
                        String withMiscEnd = " ".repeat(leading) + ch.repeat(length) + randomSpacesAndTabs(trailing);
                        String testString1 = line1 + "\n" + withSpacesAtEnd;
                        String testString2 = line1 + "\n" + withTabsAtEnd;
                        String testString3 = line1 + "\n" + withMiscEnd;
                        invalidCase(testString1);
                        invalidCase(testString2);
                        invalidCase(testString3);
                    }
                }
            }
        }
    }

    @Test
    void invalid_setextUnderlines_tooManyTabsIndented() {
        String line1 = "foo";
        for (int level = 1; level <= 2; ++level) {
            for (int leading = 1; leading <= 10; ++leading) {
                for (int trailing = 0; trailing < 10; ++trailing) {
                    for (int length = 1; length <= 10; ++length) {
                        String ch = level == 1 ? "=" : "-";
                        String withSpacesAtEnd = "\t".repeat(leading) + ch.repeat(length) + " ".repeat(trailing);
                        String withTabsAtEnd = "\t".repeat(leading) + ch.repeat(length) + "\t".repeat(trailing);
                        String withMiscEnd = "\t".repeat(leading) + ch.repeat(length) + randomSpacesAndTabs(trailing);
                        String testString1 = line1 + "\n" + withSpacesAtEnd;
                        String testString2 = line1 + "\n" + withTabsAtEnd;
                        String testString3 = line1 + "\n" + withMiscEnd;
                        invalidCase(testString1);
                        invalidCase(testString2);
                        invalidCase(testString3);
                    }
                }
            }
        }
    }


    @Test
    void invalid_setextUnderlines_gaps() {
        for (int level = 1; level <= 2; ++level) {
            List<String> patterns = List.of(
                    "x x",
                    " x x ",
                    "x  x",
                    "x\tx",
                    "x x x",
                    "xx xx",
                    "xx\txx",
                    "xxxx x",
                    "xxxxx x",
                    "xxxxxx x",
                    "xxxxxxx x",
                    "xxxxxxxx x",
                    "xxxxxxxxx x",
                    "xxxxxxxxxx x",
                    "xxxxxxxxxxx x",
                    "xxxxx x",
                    "xxxxx x x",
                    "xxxxx xx x",
                    "xxxxx xxx x",
                    "xxxxx xxxx x",
                    "xxxxx xxxxx x",
                    "xxxxx xxxxxx x"
            );
            for (String pattern : patterns) {
                char replacement = level == 1 ? '=' : '-';
                String line2 = pattern.replace('x', replacement);
                invalidCase("foo\n" + line2);
                invalidCase("foo\nbar\n" + line2);
            }
        }
    }


    @Test
    void invalid_setextUnderline_mixturePermutations() {
        /*
         * Test things like -===- -=- -=, --=, etc.
         * Using binary and converting 0s and 1s to the given corresponding - or =
         * O(2^n)
         */
        int n = 15; // 2^{n} combinations and underline length (excluding indent)
        for (int length = 2; length < n; ++length) {
            int start = 1;                      // Exclude all zeros, so start from 1
            int end = (1 << length) - 2;        // Exclude all ones, which is (2^length) - 1
            for (int i = start; i <= end; i++) {
                String binaryString = Integer.toBinaryString(i);
                String binaryUnderline = String.format("%" + length + "s", binaryString)
                        .replace(' ', '=')
                        .replace('0', '=')
                        .replace('1', '-');
                for (int leading = 0; leading <= 3; ++leading) {
                    String underline = " ".repeat(leading) + binaryUnderline;
                    invalidCase("foo\n" + underline);
                    invalidCase("foo\nbar\n" + underline);
                    invalidCase("foo\nbar\nbaz\n" + underline);
                }
            }
        }
    }

    @Test
    void example80() {
        validCase(1, "Foo *bar*\n=========", List.of("Foo *bar*"));
        validCase(2, "Foo *bar*\n---------", List.of("Foo *bar*"));
    }

    @Test
    void example81() {
        validCase(1, "Foo *bar\nbaz*\n====", List.of("Foo *bar", "baz*"));
    }

    @Test
    void example82() {
        validCase(1, "  Foo *bar\nbaz*\t\n====", List.of("Foo *bar", "baz*"));
    }

    @Test
    void example83() {
        validCase(2, "Foo\n-------------------------", List.of("Foo"));
        validCase(1, "Foo\n=", List.of("Foo"));
    }

    @Test
    void example84() {
        validCase(2, "   Foo\n---", List.of("Foo"));
        validCase(2, "  Foo\n-----", List.of("Foo"));
        validCase(1, "  Foo\n  ===", List.of("Foo"));
    }

    @Test
    void example85() {
        invalidCase("    Foo\n    ---");
        invalidCase("    Foo\n---");
    }

    @Test
    void example86() {
        validCase(2, "Foo\n   ---      ", List.of("Foo"));
    }

    @Test
    void example87() {
        invalidCase("Foo\n    ---");
    }

    @Test
    void example88() {
        invalidCase("Foo\n= =");
        invalidCase("Foo\n--- -");
    }

    @Test
    void example89() {
        validCase(2, "Foo  \n-----", List.of("Foo"));
    }

    @Test
    void example90() {
        validCase(2, "Foo\\\n----", List.of("Foo\\"));
    }

    @Test
    void example91() {
        validCase(2, "`Foo\n----\n`", List.of("`Foo"));
        validCase(2, "<a title=\"a lot\n---\nof dashes\"/>", List.of("<a title=\"a lot"));
    }

    // Example 92-94 won't exist here because that combines blockquotes, which go first

    @Test
    void example95() {
        validCase(2, "Foo\nBar\n---", List.of("Foo", "Bar"));
    }

    @Test
    void example97() {
        invalidCase("\n====");
    }

    // Example 98 cant be done perfectly because we assume we parse thematic breaks first,
    // so "---\n---" wouldn't ever be an input.

    @Test
    void example102() {
        validCase(2, "\\> foo\n------", List.of("\\> foo"));
    }

}