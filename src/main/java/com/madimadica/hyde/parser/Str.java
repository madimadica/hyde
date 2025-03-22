package com.madimadica.hyde.parser;

public class Str {
    public static String trimEnd(String input, int amount) {
        return input.substring(0, input.length() - amount);
    }

    /**
     * @throws IndexOutOfBoundsException if empty string
     */
    public static char lastChar(String input) {
        return input.charAt(input.length() - 1);
    }

    public static char lastCharOrElse(String input, char fallback) {
        if (input.isEmpty()) {
            return fallback;
        }
        return input.charAt(input.length() - 1);
    }
}
