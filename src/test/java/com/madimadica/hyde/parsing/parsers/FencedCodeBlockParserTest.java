package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FencedCodeBlockParserTest {

    @Test
    void reduceIndent() {
        assertEquals(" Test", FencedCodeBlockParser.reduceIndent("    Test", 3));
        assertEquals("Test", FencedCodeBlockParser.reduceIndent("   Test", 3));
        assertEquals("Test", FencedCodeBlockParser.reduceIndent("  Test", 3));
        assertEquals("Test", FencedCodeBlockParser.reduceIndent(" Test", 3));
        assertEquals("Test", FencedCodeBlockParser.reduceIndent("Test", 3));
        assertEquals(" Test", FencedCodeBlockParser.reduceIndent("\tTest", 3));
        assertEquals("  Test", FencedCodeBlockParser.reduceIndent("\tTest", 2));
        assertEquals(" Test", FencedCodeBlockParser.reduceIndent("   Test", 2));
        assertEquals("Test", FencedCodeBlockParser.reduceIndent("  Test", 2));
        assertEquals("Test", FencedCodeBlockParser.reduceIndent(" Test", 2));
        assertEquals("Test", FencedCodeBlockParser.reduceIndent("Test", 2));
        assertEquals("  ", FencedCodeBlockParser.reduceIndent("    ", 2));
        assertEquals("  ", FencedCodeBlockParser.reduceIndent("\t", 2));
        assertEquals("   ", FencedCodeBlockParser.reduceIndent("\t ", 2));
    }

    private void assertValid(String input, List<String> content) {
        assertValid(input, content, "");
    }

    private void assertValid(String input, List<String> content, String infoString) {
        Lexer lexer = new Lexer(input);
        var parser = new FencedCodeBlockParser();
        var result = parser.parse(lexer);
        assertTrue(result.isPresent());
        var codeBlock = result.get();
        assertEquals(infoString, codeBlock.getInfoString());
        assertEquals(content, codeBlock.getLines());
    }

    private void assertInvalid(String input) {
        Lexer lexer = new Lexer(input);
        var parser = new SetextHeadingParser();
        var result = parser.parse(lexer);
        assertTrue(result.isEmpty());
    }

    @Test
    void example119() {
        assertValid("""
                ```
                <
                 >
                ```
                """,
                List.of("<", " >")
        );
    }

    @Test
    void example120() {
        assertValid("""
                ~~~
                <
                 >
                ~~~
                """,
                List.of("<", " >")
        );
    }

    @Test
    void example121() {
        assertInvalid("""
                ``
                foo
                ``
                """);
    }

    @Test
    void example122() {
        assertValid("""
                ```
                aaa
                ~~~
                ```
                """,
                List.of("aaa", "~~~")
        );
    }

    @Test
    void example123() {
        assertValid("""
                ~~~
                aaa
                ```
                ~~~
                """,
                List.of("aaa", "```")
        );
    }

    @Test
    void example124() {
        assertValid("""
                ````
                aaa
                ```
                ``````
                """,
                List.of("aaa", "```")
        );
    }

    @Test
    void example125() {
        assertValid("""
                ~~~~
                aaa
                ~~~
                ~~~~
                """,
                List.of("aaa", "~~~")
        );
    }

    @Test
    void example126() {
        assertValid("```", List.of());
    }

    @Test
    void example127() {
        assertValid("""
                `````
                
                ```
                aaa\
                """,
                List.of("", "```", "aaa")
        );
    }

    @Test
    void example129() {
        assertValid("""
                ```
                
                \s\s
                ```
                """,
                List.of("", "\s\s")
        );
    }

    @Test
    void example130() {
        assertValid("""
                ```
                ```
                """,
                List.of()
        );
    }

    @Test
    void example131() {
        assertValid("""
                 ```
                 aaa
                aaa
                ```
                """,
                List.of("aaa", "aaa")
        );
    }

    @Test
    void example132() {
        assertValid("""
                  ```
                aaa
                  aaa
                aaa
                  ```
                """,
                List.of("aaa", "aaa", "aaa")
        );
    }

    @Test
    void example133() {
        assertValid("""
                   ```
                   aaa
                    aaa
                  aaa
                   ```
                """,
                List.of("aaa", " aaa", "aaa")
        );
    }

    @Test
    void example135() {
        assertValid("""
                ```
                aaa
                  ```
                """,
                List.of("aaa")
        );
    }

    @Test
    void example136() {
        assertValid("""
                   ```
                aaa
                  ```
                """,
                List.of("aaa")
        );
    }

    @Test
    void example137() {
        assertValid("""
                ```
                aaa
                    ```
                """,
                List.of("aaa", "    ```")
        );
    }

    @Test
    void example138() {
        assertInvalid("""
                ``` ```
                aaa
                """);
    }

    @Test
    void example139() {
        assertValid("""
                ~~~~~~
                aaa
                ~~~ ~~
                """,
                List.of("aaa", "~~~ ~~")
        );
    }

    @Test
    void example142() {
        assertValid("""
                ```ruby
                def foo(x)
                  return 3
                end
                ```
                """,
                List.of("def foo(x)", "  return 3", "end"),
                "ruby"
        );
    }

    @Test
    void example143() {
        assertValid("""
                ```    ruby startline=3 $%@#$
                def foo(x)
                  return 3
                end
                ```
                """,
                List.of("def foo(x)", "  return 3", "end"),
                "ruby startline=3 $%@#$"
        );
    }

    @Test
    void example144() {
        assertValid("""
                ````;
                ````
                """,
                List.of(),
                ";"
        );
    }

    @Test
    void example145() {
        assertInvalid("``` aa ```\nfoo");
    }

    @Test
    void example146() {
        assertValid("""
                ~~~ aa ``` ~~~
                foo
                ~~~
                """,
                List.of("foo"),
                "aa ``` ~~~"
        );
    }

    @Test
    void example147() {
        assertValid("""
                ```
                ``` aaa
                ```
                """,
                List.of("``` aaa")
        );
    }
}
