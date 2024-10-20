package com.madimadica.hyde.syntax;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThematicBreakTest {

    @Test
    void isThematicBreak() {
        // Happiest case
        assertTrue(ThematicBreak.isThematicBreak("---"));
        assertTrue(ThematicBreak.isThematicBreak("___"));
        assertTrue(ThematicBreak.isThematicBreak("***"));

        // Leading spaces 1
        assertTrue(ThematicBreak.isThematicBreak(" ---"));
        assertTrue(ThematicBreak.isThematicBreak(" ___"));
        assertTrue(ThematicBreak.isThematicBreak(" ***"));

        // Leading spaces 2
        assertTrue(ThematicBreak.isThematicBreak("  ---"));
        assertTrue(ThematicBreak.isThematicBreak("  ___"));
        assertTrue(ThematicBreak.isThematicBreak("  ***"));

        // Leading spaces 3
        assertTrue(ThematicBreak.isThematicBreak("   ---"));
        assertTrue(ThematicBreak.isThematicBreak("   ___"));
        assertTrue(ThematicBreak.isThematicBreak("   ***"));

        // Trailing spaces 1
        assertTrue(ThematicBreak.isThematicBreak("--- "));
        assertTrue(ThematicBreak.isThematicBreak("___ "));
        assertTrue(ThematicBreak.isThematicBreak("*** "));

        // Trailing tabs 1
        assertTrue(ThematicBreak.isThematicBreak("---\t"));
        assertTrue(ThematicBreak.isThematicBreak("___\t"));
        assertTrue(ThematicBreak.isThematicBreak("***\t"));

        // Trailing spaces/tabs 2
        assertTrue(ThematicBreak.isThematicBreak("--- \t"));
        assertTrue(ThematicBreak.isThematicBreak("___ \t"));
        assertTrue(ThematicBreak.isThematicBreak("*** \t"));

        // Leading and Trailing spaces 1:1
        assertTrue(ThematicBreak.isThematicBreak(" --- "));
        assertTrue(ThematicBreak.isThematicBreak(" ___ "));
        assertTrue(ThematicBreak.isThematicBreak(" *** "));

        // Leading and Trailing spaces 2:1
        assertTrue(ThematicBreak.isThematicBreak("  --- "));
        assertTrue(ThematicBreak.isThematicBreak("  ___ "));
        assertTrue(ThematicBreak.isThematicBreak("  *** "));

        // Leading and Trailing spaces 2:2
        assertTrue(ThematicBreak.isThematicBreak("  ---  "));
        assertTrue(ThematicBreak.isThematicBreak("  ___  "));
        assertTrue(ThematicBreak.isThematicBreak("  ***  "));

        // Leading and Trailing spaces 3:1
        assertTrue(ThematicBreak.isThematicBreak("   --- "));
        assertTrue(ThematicBreak.isThematicBreak("   ___ "));
        assertTrue(ThematicBreak.isThematicBreak("   *** "));

        // Leading and Trailing spaces 3:2
        assertTrue(ThematicBreak.isThematicBreak("   ---  "));
        assertTrue(ThematicBreak.isThematicBreak("   ___  "));
        assertTrue(ThematicBreak.isThematicBreak("   ***  "));

        // Leading and Trailing spaces 3:3
        assertTrue(ThematicBreak.isThematicBreak("   ---   "));
        assertTrue(ThematicBreak.isThematicBreak("   ___   "));
        assertTrue(ThematicBreak.isThematicBreak("   ***   "));

        // 4+ delimeters
        assertTrue(ThematicBreak.isThematicBreak("----"));
        assertTrue(ThematicBreak.isThematicBreak("____"));
        assertTrue(ThematicBreak.isThematicBreak("****"));
        assertTrue(ThematicBreak.isThematicBreak(" ----"));
        assertTrue(ThematicBreak.isThematicBreak(" ____"));
        assertTrue(ThematicBreak.isThematicBreak(" ****"));
        assertTrue(ThematicBreak.isThematicBreak("  ----"));
        assertTrue(ThematicBreak.isThematicBreak("  ____"));
        assertTrue(ThematicBreak.isThematicBreak("  ****"));
        assertTrue(ThematicBreak.isThematicBreak("   ----"));
        assertTrue(ThematicBreak.isThematicBreak("   ____"));
        assertTrue(ThematicBreak.isThematicBreak("   ****"));
        assertTrue(ThematicBreak.isThematicBreak(" ---- "));
        assertTrue(ThematicBreak.isThematicBreak(" ____ "));
        assertTrue(ThematicBreak.isThematicBreak(" **** "));
        assertTrue(ThematicBreak.isThematicBreak("  ---- "));
        assertTrue(ThematicBreak.isThematicBreak("  ____ "));
        assertTrue(ThematicBreak.isThematicBreak("  **** "));
        assertTrue(ThematicBreak.isThematicBreak("   ---- "));
        assertTrue(ThematicBreak.isThematicBreak("   ____ "));
        assertTrue(ThematicBreak.isThematicBreak("   **** "));

        assertTrue(ThematicBreak.isThematicBreak("----"));
        assertTrue(ThematicBreak.isThematicBreak("____                                               "));
        assertTrue(ThematicBreak.isThematicBreak("****                                               "));
        assertTrue(ThematicBreak.isThematicBreak(" ----                                               "));
        assertTrue(ThematicBreak.isThematicBreak(" ____                                               "));
        assertTrue(ThematicBreak.isThematicBreak(" ****                                               "));
        assertTrue(ThematicBreak.isThematicBreak("  ----                                               "));
        assertTrue(ThematicBreak.isThematicBreak("  ____                                               "));
        assertTrue(ThematicBreak.isThematicBreak("  ****                                               "));
        assertTrue(ThematicBreak.isThematicBreak("   ----                                               "));
        assertTrue(ThematicBreak.isThematicBreak("   ____                                               "));
        assertTrue(ThematicBreak.isThematicBreak("   ****                                               "));
        assertTrue(ThematicBreak.isThematicBreak(" ----                                                "));
        assertTrue(ThematicBreak.isThematicBreak(" ____                                                "));
        assertTrue(ThematicBreak.isThematicBreak(" ****                                                "));
        assertTrue(ThematicBreak.isThematicBreak("  ----                                                "));
        assertTrue(ThematicBreak.isThematicBreak("  ____                                                "));
        assertTrue(ThematicBreak.isThematicBreak("  ****                                                "));
        assertTrue(ThematicBreak.isThematicBreak("   ----                                                "));
        assertTrue(ThematicBreak.isThematicBreak("   ____                                                "));
        assertTrue(ThematicBreak.isThematicBreak("   ****                                                "));

        // ------------ False/Invalid/Edge Cases --------
        assertFalse(ThematicBreak.isThematicBreak(""));
        assertFalse(ThematicBreak.isThematicBreak(" "));
        assertFalse(ThematicBreak.isThematicBreak("  "));
        assertFalse(ThematicBreak.isThematicBreak("   "));
        assertFalse(ThematicBreak.isThematicBreak("-"));
        assertFalse(ThematicBreak.isThematicBreak("--"));
        assertFalse(ThematicBreak.isThematicBreak(" --"));
        assertFalse(ThematicBreak.isThematicBreak("  --"));
        assertFalse(ThematicBreak.isThematicBreak("   --"));
        assertFalse(ThematicBreak.isThematicBreak("    ---"));
        assertFalse(ThematicBreak.isThematicBreak("     ---"));
        assertFalse(ThematicBreak.isThematicBreak("     ___"));
        assertFalse(ThematicBreak.isThematicBreak("     ***"));
        assertFalse(ThematicBreak.isThematicBreak("     --- "));
        assertFalse(ThematicBreak.isThematicBreak("     ___ "));
        assertFalse(ThematicBreak.isThematicBreak("     *** "));
        assertFalse(ThematicBreak.isThematicBreak("-- "));
        assertFalse(ThematicBreak.isThematicBreak("---x"));
        assertFalse(ThematicBreak.isThematicBreak("___x"));
        assertFalse(ThematicBreak.isThematicBreak("***x"));
        assertFalse(ThematicBreak.isThematicBreak("--- x"));
        assertFalse(ThematicBreak.isThematicBreak("___ x"));
        assertFalse(ThematicBreak.isThematicBreak("*** x"));
        assertFalse(ThematicBreak.isThematicBreak("-_-"));
        assertFalse(ThematicBreak.isThematicBreak("-__"));
        assertFalse(ThematicBreak.isThematicBreak("--_"));
        assertFalse(ThematicBreak.isThematicBreak("-_*"));
        assertFalse(ThematicBreak.isThematicBreak(" -_-"));
        assertFalse(ThematicBreak.isThematicBreak(" -__"));
        assertFalse(ThematicBreak.isThematicBreak(" --_"));
        assertFalse(ThematicBreak.isThematicBreak(" -_*"));
        assertFalse(ThematicBreak.isThematicBreak("  -_-"));
        assertFalse(ThematicBreak.isThematicBreak("  -__"));
        assertFalse(ThematicBreak.isThematicBreak("  --_"));
        assertFalse(ThematicBreak.isThematicBreak("  -_*"));
        assertFalse(ThematicBreak.isThematicBreak("   -_-"));
        assertFalse(ThematicBreak.isThematicBreak("   -__"));
        assertFalse(ThematicBreak.isThematicBreak("   --_"));
        assertFalse(ThematicBreak.isThematicBreak("   -_*"));
        assertFalse(ThematicBreak.isThematicBreak("- - -"));
        assertFalse(ThematicBreak.isThematicBreak("-- -"));
        assertFalse(ThematicBreak.isThematicBreak("- --"));
        // Tabs invalid indentation
        assertFalse(ThematicBreak.isThematicBreak("\t---"));
        assertFalse(ThematicBreak.isThematicBreak("\t___"));
        assertFalse(ThematicBreak.isThematicBreak("\t***"));
        assertFalse(ThematicBreak.isThematicBreak(" \t---"));
        assertFalse(ThematicBreak.isThematicBreak(" \t___"));
        assertFalse(ThematicBreak.isThematicBreak(" \t***"));
        assertFalse(ThematicBreak.isThematicBreak("\t--- "));
        assertFalse(ThematicBreak.isThematicBreak("\t___ "));
        assertFalse(ThematicBreak.isThematicBreak("\t*** "));
    }


}