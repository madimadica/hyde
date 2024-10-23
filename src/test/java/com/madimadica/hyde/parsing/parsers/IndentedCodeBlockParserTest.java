package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import com.madimadica.hyde.syntax.IndentedCodeBlock;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class IndentedCodeBlockParserTest {
    private void validCase(String input, List<String> expectedLines) {
        Lexer lexer = new Lexer(input);
        IndentedCodeBlockParser parser = new IndentedCodeBlockParser();
        Optional<IndentedCodeBlock> result = parser.parse(lexer);
        assertTrue(result.isPresent());
        IndentedCodeBlock heading = result.get();
        assertEquals(expectedLines, heading.getRawLines());
    }

    private void invalidCase(String input) {
        Lexer lexer = new Lexer(input);
        IndentedCodeBlockParser parser = new IndentedCodeBlockParser();
        Optional<IndentedCodeBlock> result = parser.parse(lexer);
        assertTrue(result.isEmpty());
    }

    @Test
    void trimIndentation() {
        assertEquals("", IndentedCodeBlockParser.trimIndentation(""));
        assertEquals("", IndentedCodeBlockParser.trimIndentation(" "));
        assertEquals("", IndentedCodeBlockParser.trimIndentation("  "));
        assertEquals("", IndentedCodeBlockParser.trimIndentation("   "));
        assertEquals("", IndentedCodeBlockParser.trimIndentation("    "));
        assertEquals(" ", IndentedCodeBlockParser.trimIndentation("     "));
        assertEquals("  ", IndentedCodeBlockParser.trimIndentation("      "));
        assertEquals("", IndentedCodeBlockParser.trimIndentation("\t"));
        assertEquals(" ", IndentedCodeBlockParser.trimIndentation("\t "));
        assertEquals("  ", IndentedCodeBlockParser.trimIndentation("\t  "));
        assertEquals("", IndentedCodeBlockParser.trimIndentation(" \t"));
        assertEquals("", IndentedCodeBlockParser.trimIndentation("  \t"));
        assertEquals("", IndentedCodeBlockParser.trimIndentation("   \t"));
        assertEquals("\t", IndentedCodeBlockParser.trimIndentation("    \t"));
        assertEquals("\t ", IndentedCodeBlockParser.trimIndentation("    \t "));
        assertEquals("\t\t", IndentedCodeBlockParser.trimIndentation("\t\t\t"));

        assertEquals("test", IndentedCodeBlockParser.trimIndentation("test"));
        assertEquals("test", IndentedCodeBlockParser.trimIndentation(" test"));
        assertEquals("test", IndentedCodeBlockParser.trimIndentation("  test"));
        assertEquals("test", IndentedCodeBlockParser.trimIndentation("   test"));
        assertEquals("test", IndentedCodeBlockParser.trimIndentation("    test"));
        assertEquals(" test", IndentedCodeBlockParser.trimIndentation("     test"));
        assertEquals("  test", IndentedCodeBlockParser.trimIndentation("      test"));
        assertEquals("test", IndentedCodeBlockParser.trimIndentation("\ttest"));
        assertEquals(" test", IndentedCodeBlockParser.trimIndentation("\t test"));
        assertEquals("  test", IndentedCodeBlockParser.trimIndentation("\t  test"));
        assertEquals("test", IndentedCodeBlockParser.trimIndentation(" \ttest"));
        assertEquals("test", IndentedCodeBlockParser.trimIndentation("  \ttest"));
        assertEquals("test", IndentedCodeBlockParser.trimIndentation("   \ttest"));
        assertEquals("\ttest", IndentedCodeBlockParser.trimIndentation("    \ttest"));
        assertEquals("\t test", IndentedCodeBlockParser.trimIndentation("    \t test"));
    }

    @Test
    void invalid_blank() {
        invalidCase("");
        invalidCase(" ");
        invalidCase("  ");
        invalidCase("   ");
        invalidCase("    ");
        invalidCase("     ");
        invalidCase("      ");
        invalidCase("\t\t");
        invalidCase("\t\t ");
        invalidCase("\t \t ");
        invalidCase(" \t \t ");
    }

    @Test
    void invalid_tooShort() {
        invalidCase("Test");
        invalidCase(" Test");
        invalidCase("  Test");
        invalidCase("   Test");
    }

    @Test
    void valid_simpleBaseIndent() {
        List<String> expected = List.of("foo");
        validCase("    foo", expected);
        validCase("   \tfoo", expected);
        validCase("  \tfoo", expected);
        validCase(" \tfoo", expected);
        validCase("\tfoo", expected);
    }

    @Test
    void valid_simpleBaseIndent_extraSpacesAtEnd() {
        List<String> expected = List.of("foo  ");
        validCase("    foo  ", expected);
        validCase("   \tfoo  ", expected);
        validCase("  \tfoo  ", expected);
        validCase(" \tfoo  ", expected);
        validCase("\tfoo  ", expected);
    }

    @Test
    void valid_simpleBaseIndent_extraSpacesAtStart() {
        List<String> expected = List.of("  foo");
        validCase("      foo", expected);
        validCase("   \t  foo", expected);
        validCase("  \t  foo", expected);
        validCase(" \t  foo", expected);
        validCase("\t  foo", expected);
    }

    @Test
    void valid_simpleBaseIndent_extraSpacesAtStartAndEnd() {
        List<String> expected = List.of("  foo  ");
        validCase("      foo  ", expected);
        validCase("   \t  foo  ", expected);
        validCase("  \t  foo  ", expected);
        validCase(" \t  foo  ", expected);
        validCase("\t  foo  ", expected);
    }

    @Test
    void valid_simpleBaseIndent_extraTabsAtEnd() {
        List<String> expected = List.of("foo\t\t");
        validCase("    foo\t\t", expected);
        validCase("   \tfoo\t\t", expected);
        validCase("  \tfoo\t\t", expected);
        validCase(" \tfoo\t\t", expected);
        validCase("\tfoo\t\t", expected);
    }

    @Test
    void valid_simpleBaseIndent_extraTabAtStart() {
        List<String> expected = List.of("\tfoo");
        validCase("    \tfoo", expected);
        validCase("   \t\tfoo", expected);
        validCase("  \t\tfoo", expected);
        validCase(" \t\tfoo", expected);
        validCase("\t\tfoo", expected);
    }

    @Test
    void valid_simpleRawText() {
        validCase("    *test*", List.of("*test*"));
        validCase("    # test #", List.of("# test #"));
        validCase("    ---", List.of("---"));
    }

    @Test
    void valid_multilineEOF() {
        // Testing standard lines
        validCase("""
                    Hello
                    World
                    
                    Foo
                    Bar
                  \
                """,
                List.of("Hello", "World", "", "Foo", "Bar")
        );
        // Testing trailing whitespaces and spaces in middle of lines
        validCase("""
                    Hello\s
                    World \t
                    \s\s
                    \t\s
                    \s\t
                    Foo
                    Bar
                       \
                """,
                List.of("Hello ", "World \t", "  ", "\t ", " \t", "Foo", "Bar")
        );
        // Testing extra lines at the end
        validCase("\tHello\n\tWorld\n\n\n\n\n", List.of("Hello", "World"));
    }

    @Test
    void valid_multilineEndOfBlock() {
        validCase("""
                    Hello
                    
                    World
                My Paragraph
                """,
                List.of("Hello", "", "World")
        );
        validCase("""
                    Hello
                    
                    World
                    
                        \s
                    
                    
                My Paragraph
                """,
                List.of("Hello", "", "World")
        );
    }

    @Test
    void valid_example107() {
        validCase("""
                    a simple
                      indented code block\
                """,
                List.of("a simple", "  indented code block")
        );
    }

    @Test
    void invalid_example108() {
        invalidCase("""
                  - foo
                
                    bar\
                """
        );
    }

    @Test
    void invalid_example109() {
        invalidCase("""
                1.  foo
                
                    - bar\
                """
        );
    }

    @Test
    void valid_example110() {
        validCase("""
                    <a/>
                    *hi*
                
                    - one\
                """,
                List.of("<a/>", "*hi*", "", "- one")
        );
    }

    @Test
    void valid_example111() {
        validCase("""
                    chunk1
                
                    chunk2
                  
                 
                 
                    chunk3\
                """,
                List.of("chunk1", "", "chunk2", "", "", "", "chunk3")
        );
    }

    @Test
    void valid_example112() {
        validCase("""
                    chunk1
                      \n\
                      chunk2\
                """,
                List.of("chunk1", "  ", "  chunk2")
        );
    }

    @Test
    void invalid_example113() {
        invalidCase("Foo\n    bar");
    }

    @Test
    void valid_example114() {
        validCase("    foo\nbar", List.of("foo"));
    }

    @Test
    void valid_example116() {
        validCase("        foo\n    bar", List.of("    foo", "bar"));
    }

    @Test
    void valid_example118() {
        validCase("    foo  ", List.of("foo  "));
    }
}