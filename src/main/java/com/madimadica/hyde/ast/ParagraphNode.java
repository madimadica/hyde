package com.madimadica.hyde.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ParagraphNode extends InlineLeafBlockNode implements AcceptsLines {

    private final List<String> rawLines = new ArrayList<>();

    @Override
    public String getLiteral() {
        return String.join("\n", rawLines);
    }

    @Override
    public void setLiteral(String literal) {
        rawLines.clear();
        rawLines.addAll(Arrays.asList(literal.split("\n")));
    }

    @Override
    public void acceptLine(String line) {
        rawLines.add(line);
    }

    public List<String> getRawLines() {
        return rawLines;
    }

    /**
     * Check if the converted content is only whitespaces,
     * technically excluding newlines because paragraphs shouldn't have blank lines
     */
    public boolean isBlank() {
        return rawLines.isEmpty() || (rawLines.size() == 1 && rawLines.get(0).isBlank());
    }

}
