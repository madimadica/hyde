package com.madimadica.hyde.parser;

import com.madimadica.hyde.ast.LinkReferenceDefinitionNode;
import com.madimadica.hyde.ast.ParagraphNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkParserTest {

    public static class TestBuilder {
        private String label;
        private String destination;
        private String title;

        public TestBuilder label(String label) {
            this.label = label;
            return this;
        }

        public TestBuilder destination(String destination) {
            this.destination = destination;
            return this;
        }

        public TestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public LinkReferenceDefinitionNode build() {
            var node = new LinkReferenceDefinitionNode(label, destination, title);
            node.setPositions(new SourcePositions());
            return node;
        }
    }

    public static TestBuilder label(String label) {
        var builder = new TestBuilder();
        return builder.label(label);
    }

    public static void test(String input, TestBuilder... expectedResults) {
        var node = new ParagraphNode();
        node.setPositions(new SourcePositions());
        node.setLiteral(input);
        var actual = LinkReferenceDefinitionParser.extractLinks(node);

        assertEquals(expectedResults.length, actual.size(), "Result size is incorrect");

        for (int i = 0; i < expectedResults.length; ++i) {
            var expectedNode = expectedResults[i].build();
            var actualNode = actual.get(i);
            assertTrue(expectedNode.equalsIgnorePosition(actualNode));
        }
    }

    public static void test(String input, String remainder, TestBuilder... expectedResults) {
        var node = new ParagraphNode();
        node.setPositions(new SourcePositions());
        node.setLiteral(input);
        var actual = LinkReferenceDefinitionParser.extractLinks(node);

        assertEquals(expectedResults.length, actual.size(), "Result size is incorrect");
        assertEquals(remainder, node.getLiteral());

        for (int i = 0; i < expectedResults.length; ++i) {
            var expectedNode = expectedResults[i].build();
            var actualNode = actual.get(i);
            assertTrue(expectedNode.equalsIgnorePosition(actualNode));
        }
    }

    @Test
    void testBlank() {
        test("");
        test("  ");
        test("\n");
        test("\t");
    }

    @Test
    void testSimpleInvalid() {
        test("foo");
    }

    @Test
    void testMissingLabel() {
        test("foo");
        test("[");
        test("[  ");
        test("[  \\]");
        test("[s");
        test("[ s");
    }

    @Test
    void testEmptyLabel() {
        test("[]: /bar 'baz'");
        test("[ ]: /bar 'baz'");
        test("[\t]: /bar 'baz'");
        test("[ \t ]: /bar 'baz'");
        test("[\n]: /bar 'baz'");
        test("[ \n]: /bar 'baz'");
        test("[ \n ]: /bar 'baz'");
    }

    @Test
    void testEscapedLabel() {
        test("[fo\\[o]: /bar 'baz'", label("fo\\[o").destination("/bar").title("baz"));
        test("[f\\[o\\[o]: /bar 'baz'", label("f\\[o\\[o").destination("/bar").title("baz"));
        test("[f\\]o\\[o]: /bar 'baz'", label("f\\]o\\[o").destination("/bar").title("baz"));
        test("[f\\]o\\]o]: /bar 'baz'", label("f\\]o\\]o").destination("/bar").title("baz"));
        test("[f\\(o\\]o]: /bar 'baz'", label("f\\(o\\]o").destination("/bar").title("baz"));
        test("[fo\\o]: /bar 'baz'", label("fo\\o").destination("/bar").title("baz"));
        test("[fo\\]: /bar 'baz'");
        test("[fo\\]: /bar]: 'baz'", label("fo\\]: /bar").destination("'baz'"));
    }

    @Test
    void onlyLabel() {
        test("[foo]");
        test("[foo]:");
        test("[foo]: ");
        test("[foo]:  ");
        test("[foo]:\t");
        test("[foo]:\t ");
        test("[foo]:\n ");
        test("[foo]: \n ");
    }

    @Test
    void labelTooBig() {
        // Max
        test("[" + ("x".repeat(999)) + "]: /bar 'baz'", label("x".repeat(999)).destination("/bar").title("baz"));
        // Exceeds
        test("[" + ("x".repeat(1000)) + "]: /bar 'baz'");
    }

    @Test
    void testBadColon() {
        test("[foo] : /bar 'baz'");
        test("[foo]\t: /bar 'baz'");
        test("[foo]\n: /bar 'baz'");
        test("[foo]x: /bar 'baz'");
    }

    @Test
    void testOptionalSpacesAfterColon() {
        test("[foo]:/bar 'baz'", label("foo").destination("/bar").title("baz"));
        test("[foo]: /bar 'baz'", label("foo").destination("/bar").title("baz"));
        test("[foo]:  /bar 'baz'", label("foo").destination("/bar").title("baz"));
        test("[foo]:\t/bar 'baz'", label("foo").destination("/bar").title("baz"));
        test("[foo]:\t /bar 'baz'", label("foo").destination("/bar").title("baz"));
        test("[foo]:\n/bar 'baz'", label("foo").destination("/bar").title("baz"));
        test("[foo]:\n /bar 'baz'", label("foo").destination("/bar").title("baz"));
        test("[foo]: \n /bar 'baz'", label("foo").destination("/bar").title("baz"));

        // Too many blank lines
        test("[foo]: \n\n /bar 'baz'");
        test("[foo]:\n\n/bar 'baz'");
        test("[foo]:\n \n/bar 'baz'");
        test("[foo]:\n \n /bar 'baz'");
        test("[foo]: \n \n/bar 'baz'");
        test("[foo]: \n \n /bar 'baz'");
    }

    @Test
    void testLangleAndRangleDestination() {
        // Various leading whitespaces
        test("[foo]:<bar>", label("foo").destination("bar"));
        test("[foo]: <bar>", label("foo").destination("bar"));
        test("[foo]:\t<bar>", label("foo").destination("bar"));
        test("[foo]:\n<bar>", label("foo").destination("bar"));
        test("[foo]:\n <bar>", label("foo").destination("bar"));
        test("[foo]: \n <bar>", label("foo").destination("bar"));
        // Trailing whitespaces
        test("[foo]:<bar> ", label("foo").destination("bar"));
        test("[foo]: <bar> ", label("foo").destination("bar"));
        test("[foo]:\t<bar> ", label("foo").destination("bar"));
        test("[foo]:\n<bar> ", label("foo").destination("bar"));
        test("[foo]:\n <bar> ", label("foo").destination("bar"));
        test("[foo]: \n <bar> ", label("foo").destination("bar"));

        // Cannot have line endings
        test("[foo]: <\nbar>");
        test("[foo]: <bar\n>");
        test("[foo]: <ba\nr>");

        // Cannot have unescaped <>
        test("[foo]: <bar>>");
        test("[foo]: <bar>baz>");
        test("[foo]: <bar<baz>");
        test("[foo]: <<barbaz>");
        test("[foo]: <barbaz<>");

        // Has escaped <>
        test("[foo]: <bar\\>>", label("foo").destination("bar>"));
        test("[foo]: <bar\\<>", label("foo").destination("bar<"));
        test("[foo]: <\\>bar>", label("foo").destination(">bar"));
        test("[foo]: <\\<bar>", label("foo").destination("<bar"));
        test("[foo]: <b\\>ar>", label("foo").destination("b>ar"));
        test("[foo]: <b\\<ar>", label("foo").destination("b<ar"));

        // Other escaped chars
        test("[foo]: <bar\\(>", label("foo").destination("bar("));
        test("[foo]: <bar\\)>", label("foo").destination("bar)"));
        test("[foo]: <ba\\(r>", label("foo").destination("ba(r"));
        test("[foo]: <ba\\)r>", label("foo").destination("ba)r"));
        test("[foo]: <\\(bar>", label("foo").destination("(bar"));
        test("[foo]: <\\)bar>", label("foo").destination(")bar"));

        // Can have spaces
        test("[foo]: <bar baz>", label("foo").destination("bar baz"));
    }

    @Test
    void testPlainDestination() {
        // Basic cases
        test("[foo]:bar", label("foo").destination("bar"));
        test("[foo]: bar", label("foo").destination("bar"));
        test("[foo]:\tbar", label("foo").destination("bar"));
        test("[foo]:\nbar", label("foo").destination("bar"));

        // Basic cases, trailing
        test("[foo]: bar ", label("foo").destination("bar"));
        test("[foo]: bar\t", label("foo").destination("bar"));
        test("[foo]: bar\n", label("foo").destination("bar"));
        test("[foo]: bar\nasdf", "asdf", label("foo").destination("bar"));

        // Valid parenthesis
        test("[foo]: (bar)", label("foo").destination("(bar)"));
        test("[foo]: ((bar))", label("foo").destination("((bar))"));
        test("[foo]: (x(bar)x)", label("foo").destination("(x(bar)x)"));
        test("[foo]: b(a)r", label("foo").destination("b(a)r"));
        test("[foo]: b()a()r", label("foo").destination("b()a()r"));
        test("[foo]: b((a))r", label("foo").destination("b((a))r"));

        // Invalid parenthesis
        test("[foo]: (bar");
        test("[foo]: bar)");
        test("[foo]: )bar(");
        test("[foo]: b)a(r");
        test("[foo]: a)(z");
        test("[foo]: a))((z");

        // Test excaped parenthesis
        test("[foo]: \\(bar", label("foo").destination("(bar"));
        test("[foo]: \\)bar", label("foo").destination(")bar"));
        test("[foo]: bar\\(", label("foo").destination("bar("));
        test("[foo]: bar\\)", label("foo").destination("bar)"));
        test("[foo]: a\\(z", label("foo").destination("a(z"));
        test("[foo]: a\\)z", label("foo").destination("a)z"));
    }

    @Test
    void testTitles() {
        // Bad title
        test("[foo]: bar baz");
        test("[foo]: <bar> baz");

        // Must have at least one space - bad
        test("[foo]: <bar>'baz'");

        // Must have at least one space - good
        test("[foo]: <bar> 'baz'", label("foo").destination("bar").title("baz"));
        test("[foo]: <bar>\t'baz'", label("foo").destination("bar").title("baz"));
        test("[foo]: <bar>\n'baz'", label("foo").destination("bar").title("baz"));

        // All valid pairs
        test("[foo]: <bar> 'baz'", label("foo").destination("bar").title("baz"));
        test("[foo]: <bar> \"baz\"", label("foo").destination("bar").title("baz"));
        test("[foo]: <bar> (baz)", label("foo").destination("bar").title("baz"));

        // Incorrect pairs
        test("[foo]: <bar> 'baz\"");
        test("[foo]: <bar> 'baz)");
        test("[foo]: <bar> (baz\"");
        test("[foo]: <bar> (baz'");
        test("[foo]: <bar> \"baz'");
        test("[foo]: <bar> \"baz)");

        // Opening '(' with unescaped '(' is invalid
        test("[foo]: <bar> ((baz)");
        test("[foo]: <bar> (b(az)");
        test("[foo]: <bar> (baz()");
        // Opening '(' with escaped '(' is valid
        test("[foo]: <bar> (\\(baz)", label("foo").destination("bar").title("(baz"));
        test("[foo]: <bar> (b\\(az)", label("foo").destination("bar").title("b(az"));
        test("[foo]: <bar> (ba\\(z)", label("foo").destination("bar").title("ba(z"));
        test("[foo]: <bar> (baz\\()", label("foo").destination("bar").title("baz("));
        // Escaped closing
        test("[foo]: <bar> (\\)baz)", label("foo").destination("bar").title(")baz"));
        test("[foo]: <bar> (b\\)az)", label("foo").destination("bar").title("b)az"));
        test("[foo]: <bar> (ba\\)z)", label("foo").destination("bar").title("ba)z"));
        test("[foo]: <bar> (baz\\))", label("foo").destination("bar").title("baz)"));

        // Incorrect pairs diff lines
        test("[foo]: <bar>\n'baz\"", "'baz\"", label("foo").destination("bar"));
        test("[foo]: <bar>\n'baz)", "'baz)",label("foo").destination("bar"));
        test("[foo]: <bar>\n(baz\"", "(baz\"",label("foo").destination("bar"));
        test("[foo]: <bar>\n(baz'", "(baz'",label("foo").destination("bar"));
        test("[foo]: <bar>\n\"baz'", "\"baz'",label("foo").destination("bar"));
        test("[foo]: <bar>\n\"baz)", "\"baz)",label("foo").destination("bar"));

        // No title
        test("[foo]: <bar>\nbaz", label("foo").destination("bar"));

        // Invalid from trailing nonspace chars
        test("[foo]: <bar> 'baz'x");
        test("[foo]: <bar> 'baz' x");
        test("[foo]: <bar> 'baz' x ");
        // Invalid title but on a different line
        test("[foo]: <bar>\n'baz'x", "'baz'x", label("foo").destination("bar"));
        test("[foo]: <bar>\n'baz' x", "'baz' x", label("foo").destination("bar"));
        test("[foo]: <bar>\n'baz' x ", "'baz' x ", label("foo").destination("bar"));
    }

    @Test
    void multilineTitle() {
        test("[foo]: <bar> '\nbaz'", label("foo").destination("bar").title("\nbaz"));
        test("[foo]: <bar> 'b\naz'", label("foo").destination("bar").title("b\naz"));
        test("[foo]: <bar> 'ba\nz'", label("foo").destination("bar").title("ba\nz"));
        test("[foo]: <bar> 'baz\n'", label("foo").destination("bar").title("baz\n"));
        test("[foo]: <bar> '\nbaz\n'", label("foo").destination("bar").title("\nbaz\n"));
        test("[foo]: <bar> '\nb\naz'", label("foo").destination("bar").title("\nb\naz"));
        test("[foo]: <bar> '\nb\na\nz'", label("foo").destination("bar").title("\nb\na\nz"));
        test("[foo]: <bar> '\nb\na\nz\n'", label("foo").destination("bar").title("\nb\na\nz\n"));

    }

    @Test
    void parseMultipleLinks() {
        test("""
                [foo]: /bar 'baz'
                [fizz]: /buzz "fizzbuzz"\
                """,
                label("foo").destination("/bar").title("baz"),
                label("fizz").destination("/buzz").title("fizzbuzz")
        );

        test("""
                [foo0]: /bar0 'baz0'
                [foo1]: /bar1 'baz1'
                [foo2]: /bar2 'baz2'
                [foo3]: /bar3 'baz3'
                [foo4]: /bar4 'baz4'
                [foo5]: /bar5 'baz5'
                [foo6]: /bar6 'baz6'
                remaining paragraph
                """,
                label("foo0").destination("/bar0").title("baz0"),
                label("foo1").destination("/bar1").title("baz1"),
                label("foo2").destination("/bar2").title("baz2"),
                label("foo3").destination("/bar3").title("baz3"),
                label("foo4").destination("/bar4").title("baz4"),
                label("foo5").destination("/bar5").title("baz5"),
                label("foo6").destination("/bar6").title("baz6")
        );
        test("""
                [foo0]:
                /bar0
                'baz0'
                [foo1]: /bar1 'baz1'
                [foo2]: /bar2
                'baz2'
                [foo3]:
                /bar3 'baz3'
                [foo4]: /bar4 'baz4'
                [foo5]: /bar5
                asdf
                [foo6]: /bar6 'baz6'
                foo
                """,
                label("foo0").destination("/bar0").title("baz0"),
                label("foo1").destination("/bar1").title("baz1"),
                label("foo2").destination("/bar2").title("baz2"),
                label("foo3").destination("/bar3").title("baz3"),
                label("foo4").destination("/bar4").title("baz4"),
                label("foo5").destination("/bar5")
        );
    }

    @Test
    void testRemainingParagraph() {
        test("""
                [foo0]: /bar0
                'baz0'
                [foo1]: /bar1 'baz1'
                [foo2]: /bar2 'baz2'
                remaining paragraph1
                remaining paragraph2
                """,
                "remaining paragraph1\nremaining paragraph2",
                label("foo0").destination("/bar0").title("baz0"),
                label("foo1").destination("/bar1").title("baz1"),
                label("foo2").destination("/bar2").title("baz2")
        );
    }

}