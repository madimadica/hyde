package com.madimadica.hyde.parser;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HtmlTagAttributeLexerTest {

    private void _assertThrows(String input) {
        assertThrows(LexicalAnalysisException.class, () -> new HtmlTagAttributeLexer(input).parse());
    }

    private void assertFlag(Map<String, String> attributes, String name) {
        assertTrue(attributes.containsKey(name));
        assertNull(attributes.get(name));
    }

    @Test
    void testFlag() {
        List<String> inputs = List.of(
                " foo",
                " foo ",
                " foo",
                " foo ",
                " foo\t",
                "\tfoo",
                "\tfoo\t"
        );
        for (String input : inputs) {
            var result = new HtmlTagAttributeLexer(input).parse();
            assertEquals(1, result.size());
            assertFlag(result, "foo");
        }
    }


    @Test
    void testFlags() {
        List<String> inputs = List.of(
                " foo bar",
                " foo  bar",
                " foo\tbar",
                " foo bar"
        );
        for (String input : inputs) {
            List<String> subinputs = List.of(
                    input,
                    " " + input,
                    input + " ",
                    " " + input + " ",
                    "\t" + input,
                    input + "\t",
                    "\t" + input + "\t"
            );
            for (String subinput : subinputs) {
                var result = new HtmlTagAttributeLexer(subinput).parse();
                assertEquals(2, result.size());
                assertFlag(result, "foo");
                assertFlag(result, "bar");
            }
        }
    }

    @Test
    void testValue() {
        List<String> inputs = List.of(
                " foo=bar",
                " foo=bar",
                " foo=bar ",
                " foo=bar ",
                " foo =bar",
                " foo =bar",
                " foo =bar ",
                " foo =bar ",
                " foo= bar",
                " foo= bar",
                " foo= bar ",
                " foo= bar ",
                " foo = bar",
                " foo = bar",
                " foo = bar ",
                " foo = bar "
        );
        for (String input : inputs) {
            var unquotedResult = new HtmlTagAttributeLexer(input).parse();
            assertEquals(1, unquotedResult.size());
            assertEquals("bar", unquotedResult.get("foo"));

            var singleQuotedResult = new HtmlTagAttributeLexer(input.replace("bar", "'bar'")).parse();
            assertEquals(1, singleQuotedResult.size());
            assertEquals("bar", singleQuotedResult.get("foo"));

            var doubleQuotedResult = new HtmlTagAttributeLexer(input.replace("bar", "\"bar\"")).parse();
            assertEquals(1, doubleQuotedResult.size());
            assertEquals("bar", doubleQuotedResult.get("foo"));
        }
    }


    @Test
    void testValueAndFlag() {
        List<String> inputs = List.of(
                " foo=bar",
                " foo=bar",
                " foo=bar ",
                " foo=bar ",
                " foo =bar",
                " foo =bar",
                " foo =bar ",
                " foo =bar ",
                " foo= bar",
                " foo= bar",
                " foo= bar ",
                " foo= bar ",
                " foo = bar",
                " foo = bar",
                " foo = bar ",
                " foo = bar "
        );
        for (String input : inputs) {
            List<String> subinputs = List.of(
                    input + " baz",
                    " baz " + input,
                    input + " baz ",
                    " baz " + input
            );
            for (String subinput : subinputs) {
                var unquotedResult = new HtmlTagAttributeLexer(subinput).parse();
                assertEquals(2, unquotedResult.size());
                assertEquals("bar", unquotedResult.get("foo"));
                assertFlag(unquotedResult, "baz");

                var singleQuotedResult = new HtmlTagAttributeLexer(subinput.replace("bar", "'bar'")).parse();
                assertEquals(2, singleQuotedResult.size());
                assertEquals("bar", singleQuotedResult.get("foo"));
                assertFlag(singleQuotedResult, "baz");

                var doubleQuotedResult = new HtmlTagAttributeLexer(subinput.replace("bar", "\"bar\"")).parse();
                assertEquals(2, doubleQuotedResult.size());
                assertEquals("bar", doubleQuotedResult.get("foo"));
                assertFlag(doubleQuotedResult, "baz");
            }
        }
    }

    @Test
    void invalidNames() {
        _assertThrows("%foo=bar");
        _assertThrows("*foo=bar");
        _assertThrows("(foo=bar");
        _assertThrows(".foo=bar");
        _assertThrows("-foo=bar");

        _assertThrows("fo&o=bar");
        _assertThrows("fo#o=bar");
        _assertThrows("fo!o=bar");

        _assertThrows("foo&=bar");
        _assertThrows("foo#=bar");
        _assertThrows("foo!=bar");
    }

    @Test
    void invalidUnquotedValues() {
        _assertThrows("foo=ba\"r");
        _assertThrows("foo=ba'r");
        _assertThrows("foo=ba=r");
        _assertThrows("foo=ba<r");
        _assertThrows("foo=ba>r");
        _assertThrows("foo=ba`r");
    }

    @Test
    void unclosedSingleQuoteValue() {
        _assertThrows("foo='bar");
        _assertThrows("foo='bar ");
        _assertThrows("foo='bar baz");
    }

    @Test
    void unclosedDoubleQuoteValue() {
        _assertThrows("foo=\"bar");
        _assertThrows("foo=\"bar ");
        _assertThrows("foo=\"bar baz");
    }

    @Test
    void noValue() {
        _assertThrows("foo=");
        _assertThrows("foo =");
        _assertThrows("foo = ");
        _assertThrows(" foo = ");
    }
}