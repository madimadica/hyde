package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.HtmlTagAttributeLexer;
import com.madimadica.hyde.parsing.LexicalAnalysisException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ParserUtils {

    /**
     * Regex to check if an input is a valid tag name. <br>
     * A tag name consists of an ASCII letter followed by zero or more ASCII letters, digits, or hyphens (-).
     */
    public static final Pattern REGEX_TAG_NAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9-]*$");

    public static boolean isOpeningTag(String s) {
        if (!s.startsWith("<") || !s.endsWith(">")) {
            return false;
        }
        String details = s.substring(
                1,
                s.length() - (s.endsWith("/>") ? 2 : 1)
        );

        int indexOfFirstWhitespace = indexOfFirstWhitespace(details);
        String tagName = indexOfFirstWhitespace == -1
                ? details
                : details.substring(0, indexOfFirstWhitespace);

        if (!REGEX_TAG_NAME.matcher(tagName).matches()) {
            return false;
        }

        if (indexOfFirstWhitespace == -1) return true;

        String attributes = details.substring(indexOfFirstWhitespace);

        try {
            new HtmlTagAttributeLexer(attributes).parse();
            return true;
        } catch (LexicalAnalysisException e) {
            return false;
        }
    }

    public static boolean isClosingTag(String s) {
        if (!s.startsWith("</") || !s.endsWith(">")) {
            return false;
        }
        String details = s.substring(2, s.length() - 1);

        int indexOfFirstWhitespace = indexOfFirstWhitespace(details);
        String tagName = indexOfFirstWhitespace == -1
                ? details
                : details.substring(0, indexOfFirstWhitespace);

        return REGEX_TAG_NAME.matcher(tagName).matches();
    }



    public static int indexOfFirstWhitespace(String s) {
        for (int i = 0, len = s.length(); i < len; ++i) {
            char c = s.charAt(i);
            if (c == ' ' || c == '\t') {
                return i;
            }
        }
        return -1;
    }
}
