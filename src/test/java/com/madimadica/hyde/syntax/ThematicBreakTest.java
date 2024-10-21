package com.madimadica.hyde.syntax;

import com.madimadica.hyde.parsing.Lexer;
import com.madimadica.hyde.parsing.parsers.ThematicBreakParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThematicBreakTest {

    private void assertValid(String input) {
        Lexer lexer = new Lexer(input);
        ThematicBreakParser parser = new ThematicBreakParser();
        assertTrue(parser.parse(lexer).isPresent());
    }

    private void assertInvalid(String input) {
        Lexer lexer = new Lexer(input);
        ThematicBreakParser parser = new ThematicBreakParser();
        assertTrue(parser.parse(lexer).isEmpty());
    }

    @Test
    void standardVersions() {
        assertValid("---");
        assertValid("___");
        assertValid("***");
    }

    @Test
    void leadingSpaces1() {
        assertValid(" ---");
        assertValid(" ___");
        assertValid(" ***");
    }

    @Test
    void leadingSpaces2() {
        assertValid("  ---");
        assertValid("  ___");
        assertValid("  ***");
    }

    @Test
    void leadingSpaces3() {
        assertValid("   ---");
        assertValid("   ___");
        assertValid("   ***");
    }

    @Test
    void leadingSpaces4() {
        assertInvalid("    ---");
        assertInvalid("    ___");
        assertInvalid("    ***");
    }

    @Test
    void leadingSpaces5() {
        assertInvalid("     ---");
        assertInvalid("     ___");
        assertInvalid("     ***");
    }

    @Test
    void leadingTabs() {
        assertInvalid("\t---");
        assertInvalid("\t___");
        assertInvalid("\t***");
    }

    @Test
    void leadingTabsAndSpacesA() {
        assertInvalid(" \t---");
        assertInvalid(" \t___");
        assertInvalid(" \t***");
    }

    @Test
    void leadingTabsAndSpacesB() {
        assertInvalid("\t ---");
        assertInvalid("\t ___");
        assertInvalid("\t ***");
    }

    @Test
    void trailingSpaces1() {
        assertValid("--- ");
        assertValid("___ ");
        assertValid("*** ");
    }

    @Test
    void trailingSpaces2() {
        assertValid("---  ");
        assertValid("___  ");
        assertValid("***  ");
    }

    @Test
    void trailingSpaces3() {
        assertValid("---   ");
        assertValid("___   ");
        assertValid("***   ");
    }

    @Test
    void trailingTabs1() {
        assertValid("---\t");
        assertValid("___\t");
        assertValid("***\t");
    }

    @Test
    void trailingTabs2() {
        assertValid("---\t\t");
        assertValid("___\t\t");
        assertValid("***\t\t");
    }

    @Test
    void trailingTabsAndSpacesA() {
        assertValid("--- \t");
        assertValid("___ \t");
        assertValid("*** \t");
    }

    @Test
    void trailingTabsAndSpacesB() {
        assertValid("---\t ");
        assertValid("___\t ");
        assertValid("***\t ");
    }

    @Test
    void leadingAndTrailingSpaces1() {
        assertValid(" --- ");
        assertValid(" ___ ");
        assertValid(" *** ");
    }

    @Test
    void leadingAndTrailingSpaces2() {
        assertValid("  ---  ");
        assertValid("  ___  ");
        assertValid("  ***  ");
    }

    @Test
    void leadingAndTrailingSpaces3() {
        assertValid("   ---   ");
        assertValid("   ___   ");
        assertValid("   ***   ");
    }

    @Test
    void tooShort() {
        assertInvalid("");
        assertInvalid("-");
        assertInvalid("_");
        assertInvalid("*");
        assertInvalid("--");
        assertInvalid("__");
        assertInvalid("**");
        assertInvalid(" -");
        assertInvalid(" _");
        assertInvalid(" *");
        assertInvalid(" --");
        assertInvalid(" __");
        assertInvalid(" **");
        assertInvalid("  -");
        assertInvalid("  _");
        assertInvalid("  *");
        assertInvalid("  --");
        assertInvalid("  __");
        assertInvalid("  **");
        assertInvalid("   -");
        assertInvalid("   _");
        assertInvalid("   *");
        assertInvalid("   --");
        assertInvalid("   __");
        assertInvalid("   **");
    }

    @Test
    void tooLong() {
        assertValid("----");
        assertValid("____");
        assertValid("****");
        assertValid(" ----");
        assertValid(" ____");
        assertValid(" ****");
        assertValid("  ----");
        assertValid("  ____");
        assertValid("  ****");
        assertValid("   ----");
        assertValid("   ____");
        assertValid("   ****");

        assertValid("-----");
        assertValid("_____");
        assertValid("*****");
        assertValid(" -----");
        assertValid(" _____");
        assertValid(" *****");
        assertValid("  -----");
        assertValid("  _____");
        assertValid("  *****");
        assertValid("   -----");
        assertValid("   _____");
        assertValid("   *****");
    }

    // mixtures
    @Test
    void invalidMixtures() {
        assertInvalid("--_");
        assertInvalid("--*");
        assertInvalid("__-");
        assertInvalid("__*");
        assertInvalid("**-");
        assertInvalid("**_");
        assertInvalid("---_");
        assertInvalid("---*");
        assertInvalid("___-");
        assertInvalid("___*");
        assertInvalid("***-");
        assertInvalid("***_");
    }

    @Test
    void miscInvalid() {
        assertInvalid("---x");
        assertInvalid("___x");
        assertInvalid("***x");
        assertInvalid("--- x");
        assertInvalid("___ x");
        assertInvalid("*** x");
        assertInvalid("  ");
        assertInvalid("   ");
    }

    @Test
    void validWhitespaceSeparated() {
        assertValid("--- ---");
        assertValid("___ ___");
        assertValid("*** ***");
        assertValid("- - -");
        assertValid("_ _ _");
        assertValid("* * *");

        assertValid("-\t-\t-");
        assertValid("-\t- -");
        assertValid("- -\t-");
        assertValid("-  -\t\t-");
        assertValid("-\t -\t -");
        assertValid("- \t - \t -");
        assertValid("- \t - \t - \t \t - \t");
        assertValid("- \t - \t - \t \t - \t ----- ---");
        assertValid("- \t - \t - \t \t - \t --\t--- ---\t");
    }

}