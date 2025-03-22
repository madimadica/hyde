package com.madimadica.hyde.parser;

import java.util.Locale;

public class LinkParserUtils {

    public static String normalizeMultilineTitle(CharSequence input) {
        String[] titleLines = input.toString().split("\n", -1);
        for (int i = 1, lub = titleLines.length; i < lub; ++i) {
            titleLines[i] = titleLines[i].stripLeading();
        }
        return String.join("\n", titleLines);
    }

    public static String normalizeLabel(String label) {
        return label
                .strip()
                .toLowerCase(Locale.ROOT)
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }
}
