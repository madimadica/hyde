package com.madimadica.hyde.parser;


public class PatternMatcher {

    public static boolean isAsciiLetter(char c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z');
    }

    public static boolean isDigit(char c) {
        return ('0' <= c && c <= '9');
    }

    public static boolean isAsciiLetterOrDigit(char c) {
        return isAsciiLetter(c) || isDigit(c);
    }

    public static boolean isAsciiLetterDigitOrHyphen(char c) {
        return isAsciiLetter(c) || isDigit(c) || c == '-';
    }

    public static boolean isSimpleWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n';
    }

    public static boolean isHexDigit(char c) {
        return ('0' <= c && c <= '9') || ('A' <= c && c <= 'F') || ('a' <= c && c <= 'f');
    }

    public static String findAutolinkUri(String input, int offset) {
        // Manual impl of "^<[A-Za-z][A-Za-z0-9.+-]{1,31}:[^<>\\x00-\\x20]*>"
        int i = offset;
        final int len = input.length();
        if (len - offset < 5 || input.charAt(i++) != '<') {
            return null;
        }
        if (!isAsciiLetter(input.charAt(i++))) {
            return null;
        }

        // now at [A-Za-z0-9.+-]{1,31}:
        int count = 0;
        while (i < len) {
            char c = input.charAt(i++);
            if (c == ':') {
                break;
            }
            if ((!isAsciiLetterOrDigit(c) && c != '.' && c != '+' && c != '-') || ++count > 31) {
                return null;
            }
        }
        if (i <= 3) {
            return null; // Schema must be at least 2 characters (3 with opening '<')
        }

        // now at [^<>\\x00-\\x20]*>
        while (i < len) {
            char c = input.charAt(i++);
            if (c == '>') {
                // Matched
                return input.substring(offset, i);
            } else if (c == '<' || c <= 0x20) {
                return null;
            }
        }

        return null;
    }

    static boolean isEmailCharBeforeAtSign(char c) {
        return isAsciiLetterOrDigit(c)
            || c == '!'
            || ('#' <= c && c <= '\'')
            || c == '*'
            || c == '+'
            || ('-' <= c && c <= '/')
            || c == '='
            || c == '?'
            || ('^' <= c && c <= '`')
            || ('{' <= c && c <= '~');
    }


    public static String findAutolinkEmail(String input, int offset) {
        // Manual impl of "^<([a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*)>"
        int i = offset;
        final int len = input.length();
        if (len - offset < 5 || input.charAt(i++) != '<') {
            return null;
        }

        // [a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@
        int count = 0;
        while (i < len) {
            char c = input.charAt(i++);
            if (c == '@') {
                break;
            }
            if (!isEmailCharBeforeAtSign(c)) {
                return null;
            }
            count++;
        }
        // Require something before at-sign
        if (count == 0) {
            return null;
        }

        // [a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])
        // repeating with '.' joining
        // until '>'
        count = 0;
        while (i < len) {
            char c = input.charAt(i++);
            if (count == 0) {
                // at [a-zA-Z0-9]
                if (!isAsciiLetterOrDigit(c)) {
                    return null;
                }
                count++;
            } else {
                // at ([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?
                if (c == '>' || c == '.') {
                    if (input.charAt(i - 2) == '-') {
                        return null; // Section must end with [a-zA-Z0-9]
                    }
                    if (c == '>') {
                        // Successfully matched to the very end
                        return input.substring(offset, i);
                    }
                    // '.' restarts infinite of these sections
                    count = 0;
                } else if (!isAsciiLetterDigitOrHyphen(c) || ++count > 63) {
                    return null;
                }
            }
        }
        // Ran out of input before matching
        return null;
    }

}
