package com.madimadica.hyde.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HtmlParserUtilsTest {

    @Test
    void parseClosingTag() {
        assertNotNull(HtmlParserUtils.parseClosingTag("</foo >", 0));
        assertNotNull(HtmlParserUtils.parseClosingTag("</foo>", 0));
        assertNotNull(HtmlParserUtils.parseClosingTag("</foo\t>", 0));
        assertNotNull(HtmlParserUtils.parseClosingTag("</foo \n >", 0));
        assertNotNull(HtmlParserUtils.parseClosingTag("</foo\n>", 0));
        assertNull(HtmlParserUtils.parseClosingTag("<foo>", 0));
        assertNull(HtmlParserUtils.parseClosingTag("</ foo>", 0));
        assertNull(HtmlParserUtils.parseClosingTag("</>", 0));
        assertNull(HtmlParserUtils.parseClosingTag("</ >", 0));
        assertNull(HtmlParserUtils.parseClosingTag("</foo\n\n>", 0));
        assertNull(HtmlParserUtils.parseClosingTag("</-foo>", 0));
        assertNull(HtmlParserUtils.parseClosingTag("</foo_bar>", 0));
    }

    void assertValidOpening(String input) {
        String result = HtmlParserUtils.parseOpeningTag(input, 0);
        assertEquals(input, result);
    }

    void assertInvalidOpening(String input) {
        String result = HtmlParserUtils.parseOpeningTag(input, 0);
        assertNull(result);
    }

    @Test
    void parseOpeningTag() {
        // Just tag name
        assertValidOpening("<foo>");
        assertValidOpening("<foo >");
        assertValidOpening("<foo\t>");
        assertValidOpening("<foo\n>");
        assertValidOpening("<foo \n>");
        assertValidOpening("<foo\n >");
        assertValidOpening("<foo/>");
        assertValidOpening("<foo />");
        assertValidOpening("<foo\t/>");
        assertValidOpening("<foo\n/>");
        assertValidOpening("<foo\n />");
        assertValidOpening("<foo \n/>");
        assertValidOpening("<foo>");
        assertValidOpening("<foo1>");
        assertValidOpening("<foo-1>");

        assertInvalidOpening("<>");
        assertInvalidOpening("</>");
        assertInvalidOpening("< foo>");
        assertInvalidOpening("<\tfoo>");
        assertInvalidOpening("<\nfoo>");
        assertInvalidOpening("<foo/ >");
        assertInvalidOpening("<foo / >");
        assertInvalidOpening("<foo /");
        assertInvalidOpening("<foo ");
        assertInvalidOpening("<foo");

        // Invalid tag name chars
        assertInvalidOpening("<-foo>");
        assertInvalidOpening("<foo:>");
        assertInvalidOpening("<foo_bar>");
        assertInvalidOpening("<foo.bar>");

        for (String type : List.of("", "'", "\"")) {
            for (int i = 0; i < 4; ++i) {
                for (int j = 0; j < 4; ++j) {
                    for (int k = 0; k < 4; ++k) {
                        String lSpaces = " ".repeat(i);
                        String rSpaces = " ".repeat(j);
                        String endSpaces = " ".repeat(k);
                        // one attribute value spec
                        assertValidOpening("<foo bar" + lSpaces + "=" + rSpaces + type + "baz" + type + endSpaces + ">");
                        assertValidOpening("<foo bar" + lSpaces + "=" + rSpaces + type + "baz" + type + endSpaces + "/>");
                        // one attribute value spec followed by single
                        assertValidOpening("<foo bar" + lSpaces + "=" + rSpaces + type + "baz" + type + endSpaces + " other>");
                        assertValidOpening("<foo bar" + lSpaces + "=" + rSpaces + type + "baz" + type + endSpaces + " other >");
                        assertValidOpening("<foo bar" + lSpaces + "=" + rSpaces + type + "baz" + type + endSpaces + " other/>");
                        assertValidOpening("<foo bar" + lSpaces + "=" + rSpaces + type + "baz" + type + endSpaces + " other />");
                        // one single attr followed by attr value spec
                        assertValidOpening("<foo other bar" + lSpaces + "=" + rSpaces + type + "baz" + type + endSpaces + ">");
                        assertValidOpening("<foo other bar" + lSpaces + "=" + rSpaces + type + "baz" + type + endSpaces + "/>");
                    }
                }
            }
        }
        assertInvalidOpening("<foo ");
        assertInvalidOpening("<foo bar");
        assertInvalidOpening("<foo bar=baz");
        assertInvalidOpening("<foo bar=baz /");
        assertInvalidOpening("<foo bar=ba<z>");
        assertInvalidOpening("<foo bar=ba<z >");
        assertInvalidOpening("<foo bar=ba<z />");
    }
}