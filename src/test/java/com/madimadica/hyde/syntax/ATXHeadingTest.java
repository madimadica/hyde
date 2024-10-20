package com.madimadica.hyde.syntax;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ATXHeadingTest {

    @Test
    void isATXHeading() {
        // Headings with no content & no trailing whitespace
        assertTrue(ATXHeading.isATXHeading("#"));
        assertTrue(ATXHeading.isATXHeading("##"));
        assertTrue(ATXHeading.isATXHeading("###"));
        assertTrue(ATXHeading.isATXHeading("####"));
        assertTrue(ATXHeading.isATXHeading("#####"));
        assertTrue(ATXHeading.isATXHeading("######"));
        assertTrue(ATXHeading.isATXHeading(" #"));
        assertTrue(ATXHeading.isATXHeading(" ##"));
        assertTrue(ATXHeading.isATXHeading(" ###"));
        assertTrue(ATXHeading.isATXHeading(" ####"));
        assertTrue(ATXHeading.isATXHeading(" #####"));
        assertTrue(ATXHeading.isATXHeading(" ######"));
        assertTrue(ATXHeading.isATXHeading("  #"));
        assertTrue(ATXHeading.isATXHeading("  ##"));
        assertTrue(ATXHeading.isATXHeading("  ###"));
        assertTrue(ATXHeading.isATXHeading("  ####"));
        assertTrue(ATXHeading.isATXHeading("  #####"));
        assertTrue(ATXHeading.isATXHeading("  ######"));
        assertTrue(ATXHeading.isATXHeading("   #"));
        assertTrue(ATXHeading.isATXHeading("   ##"));
        assertTrue(ATXHeading.isATXHeading("   ###"));
        assertTrue(ATXHeading.isATXHeading("   ####"));
        assertTrue(ATXHeading.isATXHeading("   #####"));
        assertTrue(ATXHeading.isATXHeading("   ######"));
        // Headings with no content & trailing whitespace
        assertTrue(ATXHeading.isATXHeading("# "));
        assertTrue(ATXHeading.isATXHeading("## "));
        assertTrue(ATXHeading.isATXHeading("### "));
        assertTrue(ATXHeading.isATXHeading("#### "));
        assertTrue(ATXHeading.isATXHeading("##### "));
        assertTrue(ATXHeading.isATXHeading("###### "));
        assertTrue(ATXHeading.isATXHeading(" # "));
        assertTrue(ATXHeading.isATXHeading(" ## "));
        assertTrue(ATXHeading.isATXHeading(" ### "));
        assertTrue(ATXHeading.isATXHeading(" #### "));
        assertTrue(ATXHeading.isATXHeading(" ##### "));
        assertTrue(ATXHeading.isATXHeading(" ###### "));
        assertTrue(ATXHeading.isATXHeading("  # "));
        assertTrue(ATXHeading.isATXHeading("  ## "));
        assertTrue(ATXHeading.isATXHeading("  ### "));
        assertTrue(ATXHeading.isATXHeading("  #### "));
        assertTrue(ATXHeading.isATXHeading("  ##### "));
        assertTrue(ATXHeading.isATXHeading("  ###### "));
        assertTrue(ATXHeading.isATXHeading("   # "));
        assertTrue(ATXHeading.isATXHeading("   ## "));
        assertTrue(ATXHeading.isATXHeading("   ### "));
        assertTrue(ATXHeading.isATXHeading("   #### "));
        assertTrue(ATXHeading.isATXHeading("   ##### "));
        assertTrue(ATXHeading.isATXHeading("   ###### "));

        // Headers with content and +0 indent
        assertTrue(ATXHeading.isATXHeading("# One"));
        assertTrue(ATXHeading.isATXHeading("## Two"));
        assertTrue(ATXHeading.isATXHeading("### Three"));
        assertTrue(ATXHeading.isATXHeading("#### Four"));
        assertTrue(ATXHeading.isATXHeading("##### Five"));
        assertTrue(ATXHeading.isATXHeading("###### Six"));
        // Headers with content and +1 indent
        assertTrue(ATXHeading.isATXHeading(" # One"));
        assertTrue(ATXHeading.isATXHeading(" ## Two"));
        assertTrue(ATXHeading.isATXHeading(" ### Three"));
        assertTrue(ATXHeading.isATXHeading(" #### Four"));
        assertTrue(ATXHeading.isATXHeading(" ##### Five"));
        assertTrue(ATXHeading.isATXHeading(" ###### Six"));
        // Headers with content and +2 indents
        assertTrue(ATXHeading.isATXHeading("  # One"));
        assertTrue(ATXHeading.isATXHeading("  ## Two"));
        assertTrue(ATXHeading.isATXHeading("  ### Three"));
        assertTrue(ATXHeading.isATXHeading("  #### Four"));
        assertTrue(ATXHeading.isATXHeading("  ##### Five"));
        assertTrue(ATXHeading.isATXHeading("  ###### Six"));
        // Headers with content and +3 indents
        assertTrue(ATXHeading.isATXHeading("   # One"));
        assertTrue(ATXHeading.isATXHeading("   ## Two"));
        assertTrue(ATXHeading.isATXHeading("   ### Three"));
        assertTrue(ATXHeading.isATXHeading("   #### Four"));
        assertTrue(ATXHeading.isATXHeading("   ##### Five"));
        assertTrue(ATXHeading.isATXHeading("   ###### Six"));

        // Headers with tabs before content
        assertTrue(ATXHeading.isATXHeading("#\tOne"));
        assertTrue(ATXHeading.isATXHeading("##\tTwo"));
        assertTrue(ATXHeading.isATXHeading("###\tThree"));
        assertTrue(ATXHeading.isATXHeading("####\tFour"));
        assertTrue(ATXHeading.isATXHeading("#####\tFive"));
        assertTrue(ATXHeading.isATXHeading("######\tSix"));



        // -------------- Invalid Inputs -----------
        assertFalse(ATXHeading.isATXHeading(""));
        assertFalse(ATXHeading.isATXHeading(" "));
        assertFalse(ATXHeading.isATXHeading("  "));
        assertFalse(ATXHeading.isATXHeading("   "));
        assertFalse(ATXHeading.isATXHeading("hi"));
        assertFalse(ATXHeading.isATXHeading(" hi"));
        assertFalse(ATXHeading.isATXHeading("  hi"));
        assertFalse(ATXHeading.isATXHeading("   hi"));
        // 7 deep
        assertFalse(ATXHeading.isATXHeading("#######"));
        assertFalse(ATXHeading.isATXHeading(" #######"));
        assertFalse(ATXHeading.isATXHeading("  #######"));
        assertFalse(ATXHeading.isATXHeading("   #######"));
        assertFalse(ATXHeading.isATXHeading("####### "));
        assertFalse(ATXHeading.isATXHeading(" ####### "));
        assertFalse(ATXHeading.isATXHeading("  ####### "));
        assertFalse(ATXHeading.isATXHeading("   ####### "));
        // Too many indents (4)
        assertFalse(ATXHeading.isATXHeading("    # One"));
        assertFalse(ATXHeading.isATXHeading("    ## Two"));
        assertFalse(ATXHeading.isATXHeading("    ### Three"));
        assertFalse(ATXHeading.isATXHeading("    #### Four"));
        assertFalse(ATXHeading.isATXHeading("    ##### Five"));
        assertFalse(ATXHeading.isATXHeading("    ###### Six"));
        // No whitespace before content
        assertFalse(ATXHeading.isATXHeading("#One"));
        assertFalse(ATXHeading.isATXHeading("##Two"));
        assertFalse(ATXHeading.isATXHeading("###Three"));
        assertFalse(ATXHeading.isATXHeading("####Four"));
        assertFalse(ATXHeading.isATXHeading("#####Five"));
        assertFalse(ATXHeading.isATXHeading("######Six"));
        // Tabs are invalid indents
        assertFalse(ATXHeading.isATXHeading("\t#"));
        assertFalse(ATXHeading.isATXHeading("\t##"));
        assertFalse(ATXHeading.isATXHeading("\t###"));
        assertFalse(ATXHeading.isATXHeading("\t####"));
        assertFalse(ATXHeading.isATXHeading("\t#####"));
        assertFalse(ATXHeading.isATXHeading("\t######"));
    }
}