package com.madimadica.hyde.parser;

public class LineParser {
    // "foo" left=0, right=3
    final String input;
    int right; // Exclusive end index
    int left;

    public LineParser(String input) {
        this.input = input;
        left = 0;
        right = input.length();
    }

    public LineParser(String input, int offset) {
        this.input = input;
        left = offset;
        right = input.length();
    }

    public String substring() {
        return input.substring(left, right);
    }

    public void appendSubstring(StringBuilder sb) {
        sb.append(input, left, right);
    }

    public boolean inBounds() {
        return left < right;
    }

    public boolean outOfBounds() {
        return left >= right;
    }

    /**
     * Consume as many spaces or tabs
     * as possible. End at the index of the next
     * non-whitespace, or out of bounds.
     * @return number of characters consumed
     */
    public int stripLeading() {
        final int startIndex = left;
        while (left < right) {
            char c = input.charAt(left);
            if (c == ' ' || c == '\t') {
                left++;
            } else {
                break;
            }
        }
        return left - startIndex;
    }


    /**
     * Consume as many of the target character
     * as possible. End at the index of the next
     * non-target char, or out of bounds.
     * @return number of characters consumed
     */
    public int consumeChar(char target) {
        final int startIndex = left;
        while (left < right) {
            char c = input.charAt(left);
            if (c == target) {
                left++;
            } else {
                break;
            }
        }
        return left - startIndex;
    }

    /**
     * Consume up to the limit of the target character. End at the index of the next
     * non-target char, or out of bounds.
     * @return number of characters consumed
     */
    public int consumeChar(char target, int limit) {
        int consumed = 0;
        while (left < right && consumed < limit) {
            char c = input.charAt(left);
            if (c == target) {
                consumed++;
                left++;
            } else {
                break;
            }
        }
        return consumed;
    }

    public int stripTrailing() {
        final int originalRight = right;
        // 'x' left=0, right=1
        while (left < right) {
            char c = input.charAt(right - 1);
            if (c == ' ' || c == '\t') {
                right--;
            } else {
                break;
            }
        }
        return originalRight - right;
    }

    public int stripTrailing(char target) {
        final int originalRight = right;
        // 'x' left=0, right=1
        while (left < right) {
            char c = input.charAt(right - 1);
            if (c == target) {
                right--;
            } else {
                break;
            }
        }
        return originalRight - right;
    }

    public void strip() {
        stripLeading();
        stripTrailing();
    }

    public boolean startsWith(char ch) {
        return inBounds() && input.charAt(left) == ch;
    }

    public boolean endsWith(char ch) {
        return inBounds() && input.charAt(right - 1) == ch;
    }

    public boolean endsWith(String s) {
        int sLen = s.length();
        if (sLen == 0) {
            return true;
        }
        if (size() < s.length()) {
            return false;
        }
        int myStart = right - sLen;
        for (int i = 0; i < sLen; ++i) {
            if (input.charAt(myStart + i) != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return left == right;
    }

    public int size() {
        return right - left;
    }

    /**
     * @return {@code true} if this substring only contains spaces or is empty
     */
    public boolean hasNonSpaces() {
        for (int i = left; i < right; ++i) {
            if (input.charAt(i) != ' ') {
                return true;
            }
        }
        return false;
    }

    public int indexOfWhitespaceFromEnd() {
        for (int i = right; i --> left;) {
            char c = input.charAt(i);
            if (c == ' ' || c == '\t') {
                return i;
            }
        }
        return -1;
    }

    public boolean matchAll(char c, int start, int end) {
        for (int i = start; i < end; ++i) {
            if (input.charAt(i) != c) {
                return false;
            }
        }
        return true;
    }

    public boolean matchAll(char c) {
        return matchAll(c, this.left, this.right);
    }

    @Override
    public String toString() {
        return "LineParser{" + substring() + "}";
    }
}
