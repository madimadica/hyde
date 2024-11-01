package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HTMLBlockParserTest {

    /**
     * @param input input textblock
     * @param expectedLineCount from the input, the number of lines that should match
     */
    private void validCase(String input, int expectedLineCount) {
        List<String> inputLines = Arrays.asList(input.split("\n"));
        Lexer lexer = new Lexer(input);
        var parser = new HTMLBlockParser();
        var result = parser.parse(lexer);
        assertTrue(result.isPresent());
        var htmlBlock = result.get();
        List<String> outputLines = htmlBlock.getRawLines();
        assertEquals(expectedLineCount, outputLines.size());
        for (int i = 0; i < expectedLineCount; ++i) {
            assertEquals(inputLines.get(i), outputLines.get(i));
        }
    }

    private void invalidCase(String input) {
        Lexer lexer = new Lexer(input);
        var parser = new HTMLBlockParser();
        var result = parser.parse(lexer);
        assertTrue(result.isEmpty());
    }

    @Test
    void example148() {
        validCase("""
                <table><tr><td>
                <pre>
                **Hello**,
                
                _world_.\
                """,
                3
        );
    }

    @Test
    void example149() {
        validCase("""
                <table>
                  <tr>
                    <td>
                           hi
                    </td>
                  </tr>
                </table>
                
                okay.\
                """,
                7
        );
    }

    @Test
    void example150() {
        validCase("""
                 <div>
                  *hello*
                         <foo><a>\
                """,
                3
        );
    }

    @Test
    void example151() {
        validCase("""
                </div>
                *foo*\
                """,
                2
        );
    }

    @Test
    void example152() {
        validCase("""
                <DIV CLASS="foo">
                
                *Markdown*
                """,
                1
        );
    }

    @Test
    void example153() {
        validCase("""
                <div id="foo"
                  class="bar">
                </div>\
                """,
                3
        );
    }

    @Test
    void example154() {
        validCase("""
                <div id="foo" class="bar
                  baz">
                </div>\
                """,
                3
        );
    }

    @Test
    void example155() {
        validCase("""
                <div>
                *foo*
                
                *bar*
                """,
                2
        );
    }

    @Test
    void example156() {
        validCase("""
                <div id="foo"
                *hi*\
                """,
                2
        );
    }

    @Test
    void example157() {
        validCase("""
                <div class
                foo\
                """,
                2
        );
    }

    @Test
    void example158() {
        validCase("""
                <div *???-&&&-<---
                *foo*\
                """,
                2
        );
    }

    @Test
    void example159() {
        validCase("<div><a href=\"bar\">*foo*</a></div>", 1);
    }

    @Test
    void example160() {
        validCase("""
                <table><tr><td>
                foo
                </td></tr></table>\
                """,
                3
        );
    }

    @Test
    void example161() {
        validCase("""
                <div></div>
                ``` c
                int x = 33;
                ```\
                """,
                4
        );
    }

    @Test
    void example162() {
        validCase("""
                <a href="foo">
                *bar*
                </a>\
                """,
                3
        );
    }

    @Test
    void example163() {
        validCase("""
                <Warning>
                *bar*
                </Warning>\
                """,
                3
        );
    }

    @Test
    void example164() {
        validCase("""
                <i class="foo">
                *bar*
                </i>\
                """,
                3
        );
    }

    @Test
    void example165() {
        validCase("""
                </ins>
                *bar*\
                """,
                2
        );
    }

    @Test
    void example166() {
        validCase("""
                <del>
                *foo*
                </del>\
                """,
                3
        );
    }

    @Test
    void example167() {
        validCase("""
                <del>
                
                *foo*
                
                </del>\
                """, 1);
    }

    @Test
    void example168() {
        invalidCase("<del>*foo*</del>");
    }

    @Test
    void example169() {
        validCase("""
                <pre language="haskell"><code>
                import Text.HTML.TagSoup
                                
                main :: IO ()
                main = print $ parseTags tags
                </code></pre>
                okay\
                """, 6);
    }

    @Test
    void example170() {
        validCase("""
                <script type="text/javascript">
                // JavaScript example
                
                document.getElementById("demo").innerHTML = "Hello JavaScript!";
                </script>
                okay\
                """, 5);
    }

    @Test
    void example171() {
        validCase("""
                <textarea>
                
                *foo*
                
                _bar_
                
                </textarea>\
                """, 7);
    }

    @Test
    void example172() {
        validCase("""
                <style
                  type="text/css">
                h1 {color:red;}
                
                p {color:blue;}
                </style>
                okay
                """, 6);
    }

    @Test
    void example173() {
        validCase("""
                <style
                  type="text/css">
                
                foo\
                """, 4);
    }

    @Test
    void example176() {
        validCase("""
                <style>p{color:red;}</style>
                *foo*\
                """, 1);
    }

    @Test
    void example177() {
        validCase("""
                <!-- foo -->*bar*
                *baz*\
                """, 1);
    }

    @Test
    void example178() {
        validCase("""
                <script>
                foo
                </script>1. *bar*\
                """, 3);
    }

    @Test
    void example179() {
        validCase("""
                <!-- Foo
                
                bar
                   baz -->
                okay\
                """, 4);
    }

    @Test
    void example180() {
        validCase("""
                <?php
                
                  echo '>';
                
                ?>
                okay\
                """, 5);
    }

    @Test
    void example181() {
        validCase("<!DOCTYPE html>", 1);
    }

    @Test
    void example182() {
        validCase("""
                <![CDATA[
                function matchwo(a,b)
                {
                  if (a < b && a < 0) then {
                    return 1;
                
                  } else {
                
                    return 0;
                  }
                }
                ]]>
                okay\
                """, 12);
    }

    @Test
    void example183() {
        validCase("  <!-- foo -->", 1);
        invalidCase("    <!-- foo -->");
        invalidCase("\t<!-- foo -->");
    }


    @Test
    void example184() {
        validCase("  <div>", 1);
        invalidCase("    <div>");
        invalidCase("\t<div>");
    }

    @Test
    void example186() {
        validCase("""
                <div>
                bar
                </div>
                *foo*\
                """, 4);
    }

    @Test
    void example188() {
        validCase("""
                <div>
                
                *Emphasized* text.
                
                </div>
                """, 1);
    }

    @Test
    void example189() {
        validCase("""
                <div>
                *Emphasized* text.
                </div>\
                """, 3);
    }


}
