package com.madimadica.hyde;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.madimadica.hyde.ast.AST;
import com.madimadica.hyde.parser.Parser;
import com.madimadica.hyde.parser.ParserOptions;
import com.madimadica.hyde.renderer.HtmlAstRenderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MarkdownToHtmlTests {

    private final ParserOptions options = ParserOptions.getDefaults();
    private final HtmlAstRenderer renderer = new HtmlAstRenderer(options);

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TestCase(
            String markdown,
            String html,
            int example,
            String section
    ) {}

    @Test
    void runAll() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<TestCase> testCases = objectMapper.readValue(
                new File("C:/Code/spec-tests.json"),
                new TypeReference<>() {}
        );

        int totalTests = testCases.size();
        int passed = 0;

        for (TestCase testCase : testCases) {
            try {
                test(testCase.markdown, testCase.html);
                passed++;
            } catch (AssertionFailedError err) {
                System.err.println("Failed test " + testCase.example + ":");
            } catch (Exception other) {
                System.err.println("Failed test " + testCase.example + ":");
                other.printStackTrace();
            }
        }
        System.out.printf("Passed %d/%d tests (%.2f%%)%n", passed, totalTests, (double) passed / totalTests * 100);
        Assertions.assertEquals(totalTests, passed);
    }

    void test(String markdown, String expectedHtml) {
        AST ast = Parser.parse(markdown, options);
        String html = renderer.render(ast);
        expectedHtml = expectedHtml.replaceAll("\r\n", "\n");
        Assertions.assertEquals(expectedHtml, html, "For input\n" + markdown);
    }


}
