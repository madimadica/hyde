package com.madimadica.hyde.parser;

public class HtmlParserUtils {

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

        if (!isValidTagName(tagName)) {
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
        boolean hasWhitespace = indexOfFirstWhitespace != -1;
        String tagName = hasWhitespace
                ? details.substring(0, indexOfFirstWhitespace)
                : details;
        if (!isValidTagName(tagName)) {
            return false;
        }
        return !hasWhitespace || details.substring(indexOfFirstWhitespace).isBlank();
    }

    /**
     * A tag name consists of an ASCII letter followed by zero or more ASCII letters, digits, or hyphens (-).
     */
    private static boolean isValidTagName(String input) {
        final int len = input.length();
        if (len == 0) {
            return false;
        }
        char first = input.charAt(0);
        if (('A' <= first && first <= 'Z') || ('a' <= first && first <= 'z')) {
            for (int i = 1; i < len; ++i) {
                char c = input.charAt(i);
                if (!(c == '-' || ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9'))) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
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

    public static String parseHtmlEntity(String s, final int offset) {
        // Insensitive "&(?:#x[a-f0-9]{1,6}|#[0-9]{1,7}|[a-z][a-z0-9]{1,31});"
        final int len = s.length();
        final int effectiveLength = len - offset;
        if (effectiveLength < 3 || !s.startsWith("&", offset)) {
            return null;
        }
        char afterAmpersand = s.charAt(offset + 1);
        if (afterAmpersand == '#') {
            if (effectiveLength < 4) {
                return null;
            }
            char afterPound = s.charAt(offset + 2);

            int size = 0;
            if (afterPound == 'x' || afterPound == 'X') {
                // #x[a-f0-9]{1,6}
                int curPos = offset + 3;
                while (curPos < len && size <= 6) {
                    char c = s.charAt(curPos++);
                    if (PatternMatcher.isHexDigit(c)) {
                        size++;
                    } else if (c == ';') {
                        if (size < 1) {
                            return null;
                        }
                        return s.substring(offset, curPos);
                    } else {
                        return null;
                    }
                }
            } else {
                // #[0-9]{1,7}
                int curPos = offset + 2;
                while (curPos < len && size <= 7) {
                    char c = s.charAt(curPos++);
                    if (PatternMatcher.isDigit(c)) {
                        size++;
                    } else if (c == ';') {
                        if (size < 1) {
                            return null;
                        }
                        return s.substring(offset, curPos);
                    } else {
                        return null;
                    }
                }
            }
        } else {
            if (!PatternMatcher.isAsciiLetter(afterAmpersand)) {
                return null;
            }
            // [a-z][a-z0-9]{1,31}
            int curPos = offset + 2;
            int size = 0;
            while (curPos < len && size <= 31) {
                char c = s.charAt(curPos++);
                if (PatternMatcher.isAsciiLetterOrDigit(c)) {
                    size++;
                } else if (c == ';') {
                    if (size < 1) {
                        return null;
                    }
                    return s.substring(offset, curPos);
                } else {
                    return null;
                }
            }
        }
        return null; // End of input or too long
    }

    public static String parseInlineHtmlTag(String s, final int offset) {
        // Run parseOpening last, as it's the most intensive algorithm, though probably the most common
        String result;
        if ((result = parseComment(s, offset)) != null) {
            return result;
        } else if ((result = parseProcessingInstruction(s, offset)) != null) {
            return result;
        } else if ((result = parseCDATA(s, offset)) != null) {
            return result;
        } else if ((result = parseDeclaration(s, offset)) != null) {
            return result;
        } else if ((result = parseClosingTag(s, offset)) != null) {
            return result;
        } else if ((result = parseOpeningTag(s, offset)) != null) {
            return result;
        } else {
            return null;
        }
    }

    static String parseOpeningTag(String s, final int offset) {
        final int len = s.length();
        // Starts with "</[A-Za-z]
        if (len < (3 + offset) || !s.startsWith("<", offset) || !PatternMatcher.isAsciiLetter(s.charAt(offset + 1))) {
            return null;
        }
        int currentPos = offset + 2;

        while (currentPos < len) {
            char c = s.charAt(currentPos++);
            if (c == '>') {
                return s.substring(offset, currentPos);
            } else if (!PatternMatcher.isAsciiLetterDigitOrHyphen(c)) {
                currentPos--; // Backup and follow whitespace logic instead
                break;
            }
        }

        // currentPos points to the first position after a valid tag name ended

        // Note: Based on how/when this method is called we know it cannot have blank lines (multiple whitespaces)

        int count = 0;
        ATTR_LOOP:
        while (currentPos < len) {
            char c = s.charAt(currentPos++);
            if (PatternMatcher.isSimpleWhitespace(c)) {
                count++;
            } else if (c == '>') {
                return s.substring(offset, currentPos);
            } else if (c == '/') {
                if ('>' == ParserUtils.peek(s, currentPos++)) {
                    return s.substring(offset, currentPos);
                } else {
                    return null;
                }
            } else {
                if (count == 0) {
                    return null; // Must have at least one space before attr
                }
                count = 0;
                // Parse attribute name
                if (!PatternMatcher.isAsciiLetter(c) && c != '_' && c != ':') {
                    return null; // Starts with Letter, _, or :
                }

                while (currentPos < len) {
                    char nameC = s.charAt(currentPos);
                    if (!PatternMatcher.isAsciiLetterDigitOrHyphen(nameC) && nameC != '.' && nameC != '_' && nameC != ':') {
                        // End of valid name
                        break;
                    }
                    currentPos++;
                }

                // Consume whitespaces after name, possibly before > or =
                int whitespaces = 0;
                while (currentPos < len && PatternMatcher.isSimpleWhitespace(s.charAt(currentPos))) {
                    currentPos++;
                    whitespaces++;
                }
                if (currentPos == len) return null;

                char afterWhitespace = s.charAt(currentPos);
                if (afterWhitespace != '=') {
                    count = whitespaces;
                    continue ATTR_LOOP;
                }
                // Parse attribute value info
                currentPos++; // Skip past the =

                // Consume whitespaces
                while (currentPos < len && PatternMatcher.isSimpleWhitespace(s.charAt(currentPos))) {
                    currentPos++;
                }
                if (currentPos == len) return null;
                // currentPos points to the start of the 'attribute value'

                // Unquoted, single, or double
                final char attrValueStart = s.charAt(currentPos);

                if (attrValueStart != '\'' && attrValueStart != '\"') {
                    if (attrValueStart == '>') {
                        return null; // Unquoted and empty "<div x=>"
                    }
                    // Parse unquoted attribute value
                    while (currentPos < len) {
                        char curr = s.charAt(currentPos);
                        switch (curr) {
                            case ' ', '\t', '\n', '>' -> {
                                // End of attribute value, safe, look for more
                                continue ATTR_LOOP;
                            }
                            case '"', '\'', '`', '<', '=' -> {
                                // End of attribute value, failed
                                return null;
                            }
                            default -> {}
                        }
                        currentPos++;
                    }
                } else {
                    currentPos++; // Skip opening quote
                    while (currentPos < len) {
                        char curr = s.charAt(currentPos++);
                        if (curr == attrValueStart) {
                            continue  ATTR_LOOP;
                        }
                    }
                }
                return null; // Ran out of input
            }
        }
        return null; // Ran out of input
    }

    static String parseClosingTag(String s, final int offset) {
        final int len = s.length();
        // Starts with "</[A-Za-z]
        if (len < (4 + offset) || !s.startsWith("</", offset) || !PatternMatcher.isAsciiLetter(s.charAt(offset + 2))) {
            return null;
        }
        int currentPos = offset + 3;
        // Followed by any number of ASCII letters/digits/hyphens
        while (currentPos < len) {
            char c = s.charAt(currentPos++);
            if (!PatternMatcher.isAsciiLetterDigitOrHyphen(c)) {
                currentPos--; // Backup and follow "\\s*>" logic instead
                break;
            }
        }

        // Consume optional whitespaces, including up to one line ending, ends with '>'
        int newLines = 0;
        while (currentPos < len) {
            char c = s.charAt(currentPos++);
            if (c == '>') {
                return s.substring(offset, currentPos);
            } else if (c == '\n') {
                if (++newLines > 1) {
                    return null;
                }
            } else if (!ParserUtils.isSpaceOrTab(c)) {
                return null;
            }
        }

        return null;
    }

    static String parseComment(String s, final int offset) {
        // We can share the '--' with start and end
        return matchBetween(s, offset, 2, "<!--", "-->");
    }

    static String parseProcessingInstruction(String s, final int offset) {
        // Cannot share a '?' with start and end
        return matchBetween(s, offset, 2, "<?", "?>");
    }

    static String parseCDATA(String s, final int offset) {
        return matchBetween(s, offset, 9, "<![CDATA[", "]]>");
    }

    static String parseDeclaration(String s, final int offset) {
        // Match "<![A-Za-z].*?>"
        if (s.length() - offset < 4 || !PatternMatcher.isAsciiLetter(s.charAt(offset + 2))) {
            return null;
        }
        return matchBetween(s, offset, 3, "<!", ">");
    }

    /**
     * Non-greedy matching between a starting string and an ending string
     * @param s String to search
     * @param offset search start offset
     * @param secondaryOffset offset to begin looking for {@code end}, relative to {@code offset}
     * @param start required starting string
     * @param end required ending string
     * @return The string matched between, and including, the start and end, or {@code null} if no matches.
     */
    static String matchBetween(String s, final int offset, final int secondaryOffset, String start, String end) {
        if (!s.startsWith(start, offset)) {
            return null;
        }
        int index = s.indexOf(end, offset + secondaryOffset);
        if (index == -1) {
            return null;
        }
        return s.substring(offset, index + end.length());
    }
}
