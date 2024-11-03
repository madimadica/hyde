package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import com.madimadica.hyde.syntax.LinkReferenceDefinition;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class LinkReferenceDefinitionParserTest {

    private static class TestCase {
        private String input;
        private String label;
        private String destination;
        private String title;
        private int lexerCursorLine = -1;
        private boolean _title = false;

        public static TestCase of(String input) {
            TestCase testCase = new TestCase();
            testCase.input = input;
            return testCase;
        }

        public TestCase label(String expectedLabel) {
            this.label = expectedLabel;
            return this;
        }

        public TestCase destination(String expectedDestination) {
            this.destination = expectedDestination;
            return this;
        }

        public TestCase title(String expectedTitle) {
            this.title = expectedTitle;
            this._title = true;
            return this;
        }

        public TestCase cursor(int expectedLineNumber) {
            this.lexerCursorLine = expectedLineNumber;
            return this;
        }

        public void runInvalid() {
            Lexer lexer = new Lexer(input);
            var parser = new LinkReferenceDefinitionParser();
            var result = parser.parse(lexer);
            assertTrue(result.isEmpty());
        }

        public void runValid() {
            Lexer lexer = new Lexer(input);
            var parser = new LinkReferenceDefinitionParser();
            var result = parser.parse(lexer);
            assertTrue(result.isPresent());
            var linkReferenceDefinition = result.get();
            assertEquals(label, linkReferenceDefinition.getLinkLabel());
            assertEquals(destination, linkReferenceDefinition.getLinkDestination());
            if (_title) {
                assertEquals(title, linkReferenceDefinition.getLinkTitle().orElseThrow());
            } else {
                assertTrue(linkReferenceDefinition.getLinkTitle().isEmpty());
            }
            assertEquals(lexerCursorLine, lexer.getLineNumber());
        }
    }

    @Test
    void example192() {
        TestCase.of("[foo]: /url \"title\"")
                .label("foo")
                .destination("/url")
                .title("title")
                .cursor(1)
                .runValid();
    }

    @Test
    void example193() {
        TestCase.of("""
                   [foo]:\s
                      /url \s
                           'the title' \s
                """)
                .label("foo")
                .destination("/url")
                .title("the title")
                .cursor(3)
                .runValid();
    }

    @Test
    void example194() {
        TestCase.of("[Foo*bar\\]]:my_(url) 'title (with parens)'")
                .label("Foo*bar]")
                .destination("my_(url)")
                .title("title (with parens)")
                .cursor(1)
                .runValid();
    }

    @Test
    void example195() {
        TestCase.of("""
                [Foo bar]:
                <my url>
                'title'
                """)
                .label("Foo bar")
                .destination("my url")
                .title("title")
                .cursor(3)
                .runValid();
    }

    @Test
    void example196() {
        TestCase.of("""
                [foo]: /url '
                title
                line1
                line2
                '
                """)
                .label("foo")
                .destination("/url")
                .title("\ntitle\nline1\nline2\n")
                .cursor(5)
                .runValid();
    }

    @Test
    void example197() {
        TestCase.of("""
                [foo]: /url 'title
                                
                with blank line'
                """)
                .cursor(1)
                .runInvalid();
    }

    @Test
    void example198() {
        TestCase.of("""
                [foo]:
                /url
                """)
                .label("foo")
                .destination("/url")
                .cursor(2)
                .runValid();
    }

    @Test
    void example199() {
        TestCase.of("[foo]:\n").runInvalid();
    }

    @Test
    void example200() {
        TestCase.of("[foo]: <>\n")
                .label("foo")
                .destination("")
                .cursor(1)
                .runValid();
    }

    @Test
    void example201() {
        TestCase.of("[foo]: <bar>(baz)").runInvalid();
    }

    @Test
    void example202() {
        TestCase.of("""
                [foo]: /url\\bar\\*baz "foo\\"bar\\baz"
                """)
                .label("foo")
                .destination("/url\\bar*baz")
                .title("foo\"bar\\baz")
                .cursor(1)
                .runValid();
    }

    @Test
    void example203() {
        TestCase.of("[foo]: url")
                .label("foo")
                .destination("url")
                .cursor(1)
                .runValid();
    }

    @Test
    void example208() {
        TestCase.of("""
                [
                foo
                ]: /url
                bar
                """)
                .label("\nfoo\n")
                .destination("/url")
                .cursor(3)
                .runValid();
    }

    @Test
    void example209() {
        TestCase.of("[foo]: /url \"title\" ok")
                .runInvalid();
    }

    @Test
    void example210() {
        TestCase.of("""
                [foo]: /url
                "title" ok
                """)
                .label("foo")
                .destination("/url")
                .cursor(1)
                .runValid();
    }

    @Test
    void example211() {
        TestCase.of("""
                    [foo]: /url "title"
                """)
                .runInvalid();
    }

    @Test
    void example215() {
        TestCase.of("""
                [foo]: /url
                bar
                ===
                [foo]
                """)
                .label("foo")
                .destination("/url")
                .cursor(1)
                .runValid();
    }

    @Test
    void example216() {
        TestCase.of("""
                [foo]: /url
                ===
                [foo]
                """)
                .label("foo")
                .destination("/url")
                .cursor(1)
                .runValid();
    }

    @Test
    void test1_tooMuchIndent() {
        TestCase.of("    [foo]: /url").runInvalid();
        TestCase.of("    [foo]: /url 'title'").runInvalid();
        TestCase.of("\t[foo]: /url").runInvalid();
        TestCase.of("\t[foo]: /url 'title'").runInvalid();
        TestCase.of(" \t[foo]: /url").runInvalid();
        TestCase.of(" \t[foo]: /url 'title'").runInvalid();
    }

    @Test
    void test2_correctIndent() {
        for (int i = 0; i <= 3; ++i) {
            TestCase.of(" ".repeat(i) + "[foo]: /bar 'baz'")
                    .label("foo")
                    .destination("/bar")
                    .title("baz")
                    .cursor(1)
                    .runValid();
        }
    }

    @Test
    void test3_noLeftBracket() {
        TestCase.of("{foo}: /url").runInvalid();
        TestCase.of("(foo): /url").runInvalid();
        TestCase.of("\\[foo]: /url").runInvalid();
        TestCase.of(" {foo}: /url").runInvalid();
        TestCase.of(" (foo): /url").runInvalid();
        TestCase.of(" \\[foo]: /url").runInvalid();
    }

    @Test
    void test4_emptyLabel() {
        TestCase.of("[]: /url").runInvalid();
        TestCase.of("[ ]: /url").runInvalid();
        TestCase.of("[\n]: /url").runInvalid();
        TestCase.of("[ \n]: /url").runInvalid();
        TestCase.of("[\t]: /url").runInvalid();
        TestCase.of("[\n\n]: /url").runInvalid();
        TestCase.of("[\n \n]: /url").runInvalid();
    }

    @Test
    void test5_labelBlankLine() {
        TestCase.of("[foo\n\nbar]: /url").runInvalid();
        TestCase.of("[foo\n \nbar]: /url").runInvalid();
        TestCase.of("[foo\n\t\nbar]: /url").runInvalid();
        TestCase.of("[foo\n \t \nbar]: /url").runInvalid();
        TestCase.of("[\nfoo\n \t \nbar\n]: /url").runInvalid();
    }

    @Test
    void test6_labelAtMax() {
        String maxLabel = "x".repeat(999);
        TestCase.of("[%s]: /url".formatted(maxLabel))
                .label(maxLabel)
                .destination("/url")
                .cursor(1)
                .runValid();
    }

    @Test
    void test7_labelOverMax() {
        String maxLabel = "x".repeat(1000);
        TestCase.of("[%s]: /url".formatted(maxLabel))
                .runInvalid();
    }

    @Test
    void test8_whitespaceBeforeColon() {
        TestCase.of("[foo] : /url").runInvalid();
        TestCase.of("[foo]\t: /url").runInvalid();
        TestCase.of("[foo]\n: /url").runInvalid();
    }

    @Test
    void test9_optionalWhitespaceAfterColon() {
        TestCase.of("[foo]:bar").label("foo").destination("bar").cursor(1).runValid();
        TestCase.of("[foo]:<bar>").label("foo").destination("bar").cursor(1).runValid();
    }

    @Test
    void test10_blankLineAfterColon() {
        TestCase.of("""
                [foo]:
                
                /bar
                """).runInvalid();
    }

    @Test
    void test11_noDestination() {
        TestCase.of("[foo]:").runInvalid();
        TestCase.of("[foo]:\n").runInvalid();
        TestCase.of("[foo]:\n\n").runInvalid();
    }

    @Test
    void test12_destinationWithAngles() {
        TestCase.of("[foo]: <bar>").label("foo").destination("bar").cursor(1).runValid();
    }

    @Test
    void test13_destinationWithAngles_noUnescapedAngles() {
        TestCase.of("[foo]: <b<ar>").runInvalid();
        TestCase.of("[foo]: <b>ar>").runInvalid();
    }

    @Test
    void test14_destinationWithAngles_noLineEndings() {
        TestCase.of("[foo]: <\nbar>").runInvalid();
        TestCase.of("[foo]: <ba\nr>").runInvalid();
        TestCase.of("[foo]: <bar\n>").runInvalid();
    }

    @Test
    void test15_destinationAngles_withEscapedAngles() {
        TestCase.of("[foo]: <b\\<ar>").label("foo").destination("b<ar").cursor(1).runValid();
        TestCase.of("[foo]: <b\\>ar>").label("foo").destination("b>ar").cursor(1).runValid();
    }

    @Test
    void test16_destinationAngles_withAlmostEscapedAngles() {
        TestCase.of("[foo]: <b\\\\<ar>").runInvalid();
        TestCase.of("[foo]: <b\\\\>ar>").runInvalid();
    }

    @Test
    void test17_unclosedLabel() {
        TestCase.of("[foo").runInvalid();
        TestCase.of("[foo\n").runInvalid();
    }

    @Test
    void test18_unclosedAngleDestination() {
        TestCase.of("[foo]: <bar").runInvalid();
    }

    @Test
    void test19_labelEscapes() {
        TestCase.of("[[foo]: bar").label("[foo").destination("bar").cursor(1).runValid();
        TestCase.of("[foo\\]]: bar").label("foo]").destination("bar").cursor(1).runValid();
        TestCase.of("[foo\\\\]]: bar").runInvalid();
        TestCase.of("[foo]]: bar").runInvalid();
    }

    @Test
    void test20_destinationParensValid() {
        TestCase.of("[foo]: bar()baz").label("foo").destination("bar()baz").cursor(1).runValid();
        TestCase.of("[foo]: ()").label("foo").destination("()").cursor(1).runValid();
        TestCase.of("[foo]: (bar)").label("foo").destination("(bar)").cursor(1).runValid();
        TestCase.of("[foo]: bar\\(baz").label("foo").destination("bar(baz").cursor(1).runValid();
        TestCase.of("[foo]: bar\\)baz").label("foo").destination("bar)baz").cursor(1).runValid();
    }

    @Test
    void test21_destinationParensInvalid() {
        TestCase.of("[foo]: bar(baz").runInvalid();
        TestCase.of("[foo]: (").runInvalid();
        TestCase.of("[foo]: )").runInvalid();
        TestCase.of("[foo]: )(").runInvalid();
        TestCase.of("[foo]: bar)baz").runInvalid();
        TestCase.of("[foo]: (bar").runInvalid();
        TestCase.of("[foo]: bar)").runInvalid();
        TestCase.of("[foo]: bar)").runInvalid();
        TestCase.of("[foo]: ((bar)").runInvalid();
        TestCase.of("[foo]: (((bar))").runInvalid();
        TestCase.of("[foo]: ((((bar)))").runInvalid();
        TestCase.of("[foo]: (((((bar))))").runInvalid();
        TestCase.of("[foo]: bar)").runInvalid();
        TestCase.of("[foo]: (bar))").runInvalid();
        TestCase.of("[foo]: ((bar)))").runInvalid();
        TestCase.of("[foo]: (((bar))))").runInvalid();
        TestCase.of("[foo]: ((((bar)))))").runInvalid();
        TestCase.of("[foo]: bar)(baz").runInvalid();
        TestCase.of("[foo]: bar())(baz").runInvalid();
    }

    @Test
    void test22_unclosedAngleDestination() {
        TestCase.of("[foo]: <bar").runInvalid();
    }

    @Test
    void test23_destinationBlankLines() {
        TestCase.of("[foo]: <bar\n\nbaz>").runInvalid();
    }

    @Test
    void test24_blanklineAfterDestination() {
        TestCase.of("[foo]: bar\n\nbaz").label("foo").destination("bar").cursor(1).runValid();
        TestCase.of("[foo]: \nbar\n\nbaz").label("foo").destination("bar").cursor(2).runValid();
    }

    @Test
    void test25_noWhitespaceAfterDestinationButHasTitle() {
        TestCase.of("[foo]: <bar>baz").runInvalid();
    }

    @Test
    void test26_titleTypesValid() {
        TestCase.of("[foo]: bar \"baz\"").label("foo").destination("bar").title("baz").cursor(1).runValid();
        TestCase.of("[foo]: bar 'baz'").label("foo").destination("bar").title("baz").cursor(1).runValid();
        TestCase.of("[foo]: bar (baz)").label("foo").destination("bar").title("baz").cursor(1).runValid();
    }

    @Test
    void test27_titleTypesInvalid_sameLine() {
        TestCase.of("[foo]: bar baz").runInvalid();
        TestCase.of("[foo]: bar [baz]").runInvalid();
        TestCase.of("[foo]: bar <baz>").runInvalid();
        TestCase.of("[foo]: bar {baz}").runInvalid();
    }

    @Test
    void test28_titleTypesInvalid_diffLine() {
        TestCase.of("[foo]: bar \nbaz").label("foo").destination("bar").cursor(1).runValid();
        TestCase.of("[foo]: bar \n[baz]").label("foo").destination("bar").cursor(1).runValid();
        TestCase.of("[foo]: bar \n<baz>").label("foo").destination("bar").cursor(1).runValid();
        TestCase.of("[foo]: bar \n{baz}").label("foo").destination("bar").cursor(1).runValid();
    }

    @Test
    void test29_titleTypesNonmatching_sameLine() {
        TestCase.of("[foo]: bar \"baz'").runInvalid();
        TestCase.of("[foo]: bar \"baz)").runInvalid();
        TestCase.of("[foo]: bar 'baz\"").runInvalid();
        TestCase.of("[foo]: bar 'baz)").runInvalid();
        TestCase.of("[foo]: bar (baz'").runInvalid();
        TestCase.of("[foo]: bar (baz\"").runInvalid();
    }

    @Test
    void test30_titleTypesNonmatching_diffLine() {
        TestCase.of("[foo]: bar \n\"baz'").label("foo").destination("bar").cursor(1).runValid();
        TestCase.of("[foo]: bar \n\"baz)").label("foo").destination("bar").cursor(1).runValid();
        TestCase.of("[foo]: bar \n'baz\"").label("foo").destination("bar").cursor(1).runValid();
        TestCase.of("[foo]: bar \n'baz)").label("foo").destination("bar").cursor(1).runValid();
        TestCase.of("[foo]: bar \n(baz'").label("foo").destination("bar").cursor(1).runValid();
        TestCase.of("[foo]: bar \n(baz\"").label("foo").destination("bar").cursor(1).runValid();
    }

    @Test
    void test31_titleTypesValid_sameEcapedInMiddle() {
        TestCase.of("[foo]: bar \"b\\\"az\"").label("foo").destination("bar").title("b\"az").cursor(1).runValid();
        TestCase.of("[foo]: bar \"\\\"baz\"").label("foo").destination("bar").title("\"baz").cursor(1).runValid();

        TestCase.of("[foo]: bar 'b\\'az'").label("foo").destination("bar").title("b'az").cursor(1).runValid();
        TestCase.of("[foo]: bar '\\'baz'").label("foo").destination("bar").title("'baz").cursor(1).runValid();

        TestCase.of("[foo]: bar (b\\(az)").label("foo").destination("bar").title("b(az").cursor(1).runValid();
        TestCase.of("[foo]: bar (\\(baz)").label("foo").destination("bar").title("(baz").cursor(1).runValid();
        TestCase.of("[foo]: bar (b\\)az)").label("foo").destination("bar").title("b)az").cursor(1).runValid();
        TestCase.of("[foo]: bar (\\)baz)").label("foo").destination("bar").title(")baz").cursor(1).runValid();
    }

    @Test
    void test32_titleParensUnescaped_sameLine() {
        TestCase.of("[foo]: bar (ba)z)").runInvalid();
        TestCase.of("[foo]: bar (ba(z)").runInvalid();
   }

    @Test
    void test33_titleParensUnescaped_diffLine() {
        TestCase.of("[foo]: bar \n(ba)z)").label("foo").destination("bar").cursor(1).runValid();
        TestCase.of("[foo]: bar \n(ba(z)").label("foo").destination("bar").cursor(1).runValid();
    }

    @Test
    void test34_unclosedTitle() {
        TestCase.of("[foo]: bar (baz").runInvalid();
        TestCase.of("[foo]: bar \"baz").runInvalid();
        TestCase.of("[foo]: bar 'baz").runInvalid();
    }

    @Test
    void test35_titleWithBlankLine() {
        TestCase.of("[foo]: bar 'b\n\naz'").runInvalid();
        TestCase.of("[foo]: bar \n'b\n\naz'").label("foo").destination("bar").cursor(1).runValid();
    }

    @Test
    void test36_titlesTrailingTabsAndSpaces() {
        TestCase.of("[foo]: bar \"baz\" ").label("foo").destination("bar").title("baz").cursor(1).runValid();
        TestCase.of("[foo]: bar 'baz' ").label("foo").destination("bar").title("baz").cursor(1).runValid();
        TestCase.of("[foo]: bar (baz) ").label("foo").destination("bar").title("baz").cursor(1).runValid();
        TestCase.of("[foo]: bar \"baz\"\t").label("foo").destination("bar").title("baz").cursor(1).runValid();
        TestCase.of("[foo]: bar 'baz'\t").label("foo").destination("bar").title("baz").cursor(1).runValid();
        TestCase.of("[foo]: bar (baz)\t").label("foo").destination("bar").title("baz").cursor(1).runValid();
        TestCase.of("[foo]: bar \"baz\" \t").label("foo").destination("bar").title("baz").cursor(1).runValid();
        TestCase.of("[foo]: bar 'baz' \t").label("foo").destination("bar").title("baz").cursor(1).runValid();
        TestCase.of("[foo]: bar (baz) \t").label("foo").destination("bar").title("baz").cursor(1).runValid();
    }

    @Test
    void test37_titleWithNonWhitespaceAfter() {
        TestCase.of("[foo]: bar 'baz' \nfizz").label("foo").destination("bar").title("baz").cursor(1).runValid();
        TestCase.of("[foo]: bar 'baz' fizz").runInvalid();
    }

    @Test
    void example206() {
        assertEquals("αγω", new LinkReferenceDefinition("ΑΓΩ", "bar").getNormalizedLabel());
    }

    @Test
    void test38_labelNormalization() {
        assertEquals("foo", new LinkReferenceDefinition("Foo", "bar").getNormalizedLabel());
        assertEquals("foo", new LinkReferenceDefinition("FOO", "bar").getNormalizedLabel());
        assertEquals("foo bar", new LinkReferenceDefinition("foo\nbar", "bar").getNormalizedLabel());
        assertEquals("foo bar", new LinkReferenceDefinition("foo  bar", "bar").getNormalizedLabel());
        assertEquals("foo bar", new LinkReferenceDefinition("foo \nbar", "bar").getNormalizedLabel());
        assertEquals("foo bar", new LinkReferenceDefinition("foo \n bar", "bar").getNormalizedLabel());
        assertEquals("foo bar", new LinkReferenceDefinition("foo \n\t bar", "bar").getNormalizedLabel());
        assertEquals("foo bar", new LinkReferenceDefinition(" foo \n\t bar", "bar").getNormalizedLabel());
        assertEquals("foo bar", new LinkReferenceDefinition("\t foo \n\t bar", "bar").getNormalizedLabel());
        assertEquals("foo bar", new LinkReferenceDefinition("\t foo \n\t bar ", "bar").getNormalizedLabel());
        assertEquals("foo bar", new LinkReferenceDefinition("\t foo \n\t bar \t", "bar").getNormalizedLabel());
    }

}