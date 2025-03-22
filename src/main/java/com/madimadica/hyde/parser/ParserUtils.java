package com.madimadica.hyde.parser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.madimadica.hyde.parser.PatternMatcher.isAsciiLetterOrDigit;
import static com.madimadica.hyde.parser.PatternMatcher.isHexDigit;

public class ParserUtils {
    public static int peek(String text, int pos) {
        if (pos < text.length() && pos >= 0) {
            return text.codePointAt(pos);
        } else {
            return -1;
        }
    }

    public static int peekOrElse(String text, int pos, int fallback) {
        if (pos < text.length() && pos >= 0) {
            return text.codePointAt(pos);
        } else {
            return fallback;
        }
    }

    public static String peek(String text, int pos, int amount) {
        int strlen = text.length();
        int current = pos;
        if (pos > strlen) {
            return "";
        }
        return text.substring(pos, Math.min(strlen, pos + amount));
    }

    public static boolean isSpaceOrTab(char c) {
        return c == ' ' || c == '\t';
    }

    public static boolean isSpaceOrTab(int codepoint) {
        return codepoint == ' ' || codepoint == '\t';
    }

    public static boolean isAsciiWhitespace(int codepoint) {
        return codepoint == ' ' || codepoint == '\t' || codepoint == '\n' || codepoint == '\r';
    }

    public static boolean isAsciiWhitespaceOrNbsp(int codepoint) {
        return codepoint == ' ' || codepoint == '\t' || codepoint == '\n' || codepoint == '\r' || codepoint == '\u00A0';
    }

    /**
     * Check if the given unicode codepoint is a symbol or punctuation
     * @param codepoint input to check
     * @return {@code true} if a symbol or punctuation
     */
    public static boolean isPunctuation(int codepoint) {
        // This relies on the 7 punctuation and 4 symbol classes' type IDs never changing
        final int type = Character.getType(codepoint);
        return 20 <= type && type <= 30;
    }

    public static String stripAsciiWhitespace(String input) {
        int left = 0;
        final int len = input.length();
        while (left < len && isAsciiWhitespace(input.charAt(left))) {
            left++;
        }
        if (left == len) {
            return "";
        }
        int right = len;
        while (right > 0 && isAsciiWhitespace(input.charAt(right - 1))) {
            right--;
        }
        return input.substring(left, right);
    }

    public static boolean isEscapable(int c) {
        // !"#$%&'()*+,-./   :;<=>?@   [\]^_`   {|}~
        return 0x21 <= c && c <= 0x2F
            || 0x3A <= c && c <= 0x40
            || 0x5B <= c && c <= 0x60
            || 0x7B <= c && c <= 0x7E;
    }

    public static int escapeCharLen(CharSequence source, char ch, int index, int len) {
        if (ch == '\\') {
            int j = index + 1;
            if (j < len && ParserUtils.isEscapable(source.charAt(j))) {
                return 1;
            }
        }
        return 0;
    }

    public static int escapeChar(CharSequence source, StringBuilder output, char ch, int index, int len) {
        if (ch == '\\') {
            int j = index + 1;
            char next;
            if (j < len && ParserUtils.isEscapable(next = source.charAt(j))) {
                // If the next char is escapable, escape it, consuming the backslash and advancing the offset
                output.append(next);
                return 1;
            } else {
                // Otherwise append a literal backslash
                output.append('\\');
            }
        } else {
            output.append(ch);
        }
        return 0;
    }

    public static boolean hasAtLeast(CharSequence input, int offset, int minimum) {
        int remaining = input.length() - offset;
        return remaining >= minimum;
    }

    public static int countCharFrom(String input, char target, int offset) {
        int count = 0;
        final int len = input.length();
        for (int i = offset; i < len; ++i) {
            if (input.charAt(i) == target) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    public static boolean hasOptionalTrailingWhitespace(String input, int offset) {
        final int len = input.length();
        for (int i = offset; i < len; ++i) {
            if (!isSpaceOrTab(input.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBlank(String s, int start) {
        final int len = s.length();
        for (int i = start; i < len; ++i) {
            if (!isSpaceOrTab(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String unescapeString(String s) {
        if (s == null) return null;
        final int len = s.length();
        boolean special = false;
        for (int i = 0; i < len; ++i) {
            char ch = s.charAt(i);
            if (ch == '\\' || ch == '&') {
                special = true;
                break;
            }
        }
        if (!special) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len;) {
            char ch = s.charAt(i);
            if (ch == '&') {
                String entity = HtmlParserUtils.parseHtmlEntity(s, i);
                if (entity != null) {
                    i += entity.length();
                    String decoded = HtmlEntities.decode(entity);
                    sb.append(decoded == null ? entity : decoded);
                    continue;
                }
            }
            i += 1 + escapeChar(s, sb, ch, i, len);
        }
        return sb.toString();
    }


    private static final String[] URI_ENCODE_CACHE = new String[128];

    static {
        String excluded = ";/?:@&=+$,-_.!~*'()#";
        for (int i = 0; i < 128; ++i) {
            char ch = (char) i;
            String entry;
            if (isAsciiLetterOrDigit(ch) || excluded.indexOf(ch) >= 0) {
                entry = Character.toString(ch);
            } else {
                String s = Integer.toString(i, 16).toUpperCase();
                if (s.length() == 1) {
                    s = "%0" + s;
                } else {
                    s = "%" + s;
                }
                entry = s;
            }
            URI_ENCODE_CACHE[i] = entry;
        }
    }

    public static String uriEncode(String s) {
        StringBuilder sb = new StringBuilder();
        final int len = s.length();

        for (int i = 0; i < len; ++i) {
            char ch = s.charAt(i);
            // Keep pre-escaped values (e.g. "foo%20bar" does not get double encoded)
            if (ch == '%' && i + 2 < len && isHexDigit(s.charAt(i + 1)) && isHexDigit(s.charAt(i + 2))) {
                sb.append(s, i, i + 3);
                i += 2;
                continue;
            }

            if (ch < 128) {
                sb.append(URI_ENCODE_CACHE[ch]);
                continue;
            }

            if (Character.isHighSurrogate(ch) && i + 1 < len) {
                char next = s.charAt(i + 1);
                if (Character.isLowSurrogate(next)) {
                    String nonBMP = String.valueOf(new char[] {ch, next});
                    sb.append(URLEncoder.encode(nonBMP, StandardCharsets.UTF_8));
                    i++;
                    continue;
                }
            }
            sb.append(URLEncoder.encode(Character.toString(ch), StandardCharsets.UTF_8));
        }

        return sb.toString();
    }

    public static String backslashEscape(String s) {
        final int len = s.length();
        StringBuilder output = new StringBuilder();
        int i = 0;
        while (i < len) {
            char ch = s.charAt(i);
            i += 1 + escapeChar(s, output, ch, i, len);
        }
        return output.toString();
    }

    public static boolean hasNonSpaces(String s) {
        for (int i = 0, LUB = s.length(); i < LUB; ++i) {
            if (s.charAt(i) != ' ') {
                return true;
            }
        }
        return false;
    }
}
