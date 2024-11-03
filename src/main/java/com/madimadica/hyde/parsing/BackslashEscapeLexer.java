package com.madimadica.hyde.parsing;

import java.util.Optional;

public class BackslashEscapeLexer {
    private final char[] escapes;

    public BackslashEscapeLexer() {
        this(new char[] {});
    }

    public BackslashEscapeLexer(char[] escapes) {
        this.escapes = escapes;
    }

    public BackslashEscapeLexer(String escapes) {
        this.escapes = escapes.toCharArray();
    }

    public final boolean needsEscaped(char c) {
        for (char escape : escapes) {
            if (escape == c) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAsciiPunctuation(char c) {
        return 0x21 <= c && c <= 0x2F
            || 0x3A <= c && c <= 0x40
            || 0x5B <= c && c <= 0x60
            || 0x7B <= c && c <= 0x7E;
    }

    public Optional<String> removeEscapes(String s) {
        return processEscapes(s, false);

    }

    public Optional<String> processEscapes(String s) {
        return processEscapes(s, true);
    }

    private Optional<String> processEscapes(String s, boolean keepEscapedChars) {
        if (s.isEmpty()) return Optional.of(s);
        StringBuilder sb = new StringBuilder();
        final int len = s.length();
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if (c != '\\') {
                // Since there is no backslash before this, must not be a required escape
                if (needsEscaped(c)) {
                    return Optional.empty();
                }
                sb.append(c);
            } else if (i == len - 1) {
                // Trailing backslash - keep it
                sb.append(c);
            } else {
                // is the next character an ascii punctuation?
                char next = s.charAt(i + 1);
                if (isAsciiPunctuation(next)) {
                    if (keepEscapedChars) {
                        sb.append(next);
                    }
                    i++; // Skip it
                } else {
                    sb.append(c);
                }
            }
        }
        return Optional.of(sb.toString());
    }

}
