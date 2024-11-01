package com.madimadica.hyde.parsing;


import java.util.LinkedHashMap;
import java.util.Map;

public class HtmlTagAttributeLexer {

    private final String rawAttributes;
    private final int size;
    private int position;
    private int readPosition;
    private char $char = '\0';

    public HtmlTagAttributeLexer(String rawAttributes) {
        this.rawAttributes = rawAttributes;
        this.size = rawAttributes.length();
        readChar();
    }

    public Map<String, String> parse() throws LexicalAnalysisException {
        Map<String, String> attributes = new LinkedHashMap<>();
        while (position < size) {
            consumeWhitespace();
            if (atEnd()) {
                break;
            }
            String name = readAttributeName();
            consumeWhitespace();
            if ($char == '=') {
                readChar();
                consumeWhitespace();
                if (atEnd()) {
                    throw new LexicalAnalysisException("Unexpected end of attribute value specification for attribute '" + name + "'");
                }
                String value = readAttributeValue();
                attributes.put(name, value);
            } else {
                attributes.put(name, null);
            }
        }
        return attributes;
    }

    private void readChar() {
        if (this.readPosition >= this.size) {
            $char = '\0';
        } else {
            $char = rawAttributes.charAt(this.readPosition);
        }
        this.position = this.readPosition++;
    }

    private String readAttributeName() {
        int start = position;
        // Valid start character?
        if (!Character.isLetter($char) && $char != '_' && $char != ':') {
            throw new LexicalAnalysisException("Unexpected character '" + $char + "' at start of attribute name");
        }
        readChar();
        while (Character.isLetterOrDigit($char) || $char == '_' || $char == '.' || $char == ':' || $char == '-') {
            readChar();
        }
        if (Character.isWhitespace($char) || $char == '\0' || $char == '=') {
            return rawAttributes.substring(start, this.position);
        } else {
            throw new LexicalAnalysisException("Unexpected character '" + $char + "' found in attribute name");
        }
    }

    private String readAttributeValue() {
        int start = position;
        boolean singleQuotes = $char == '\'';
        boolean doubleQuotes = $char == '"';
        if (singleQuotes || doubleQuotes) {
            char matchingQuote = $char;
            readChar();
            boolean found = false;
            while (position < size) {
                if ($char == matchingQuote) {
                    found = true;
                    readChar(); // Skip the closing quote
                    break;
                }
                readChar();
            }
            if (!found) {
                throw new LexicalAnalysisException("Unexpected end of value");
            }
            return rawAttributes.substring(start + 1, this.position - 1);
        } else {
            // Unquoted Attribute Value
            while (position < size && !Character.isWhitespace($char) && $char != '\"' && $char != '\'' && $char != '=' && $char != '<' && $char != '>' && $char != '`') {
                readChar();
            }
            if (!atEnd() && !Character.isWhitespace($char)) {
                throw new LexicalAnalysisException("Unexpected character '" + $char + "' in unquoted attribute value");
            }
            return rawAttributes.substring(start, this.position);
        }
    }

    private void consumeWhitespace() {
        while (Character.isWhitespace($char)) {
            readChar();
        }
    }

    private boolean atEnd() {
        return position >= this.size;
    }

}
