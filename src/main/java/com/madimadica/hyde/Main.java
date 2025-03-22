package com.madimadica.hyde;

import com.madimadica.hyde.ast.AST;
import com.madimadica.hyde.parser.Parser;
import com.madimadica.hyde.parser.ParserOptions;
import com.madimadica.hyde.renderer.HtmlAstRenderer;


public class Main {

    public static void main(String[] args) {
        var options = ParserOptions.getDefaults();

        AST ast = Parser.parse(
                "# Hello world!"
                , options
        );

        HtmlAstRenderer renderer = new HtmlAstRenderer(options);
        String html = renderer.render(ast);

        System.out.println(html);
        System.out.println(ast.toTree());
    }

}
