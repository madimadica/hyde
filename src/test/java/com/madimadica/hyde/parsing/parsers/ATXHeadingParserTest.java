package com.madimadica.hyde.parsing.parsers;

import com.madimadica.hyde.parsing.Lexer;
import com.madimadica.hyde.syntax.ATXHeading;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ATXHeadingParserTest {

    private void validCase(String input, int expectedLevel, String expectedRawContent) {
        Lexer lexer = new Lexer(input);
        ATXHeadingParser parser = new ATXHeadingParser();
        Optional<ATXHeading> result = parser.parse(lexer);
        assertTrue(result.isPresent());
        ATXHeading heading = result.get();
        assertEquals(expectedLevel, heading.getLevel());
        assertEquals(expectedRawContent, heading.getContent());
    }

    private void invalidCase(String input) {
        Lexer lexer = new Lexer(input);
        ATXHeadingParser parser = new ATXHeadingParser();
        Optional<ATXHeading> result = parser.parse(lexer);
        assertTrue(result.isEmpty());
    }

    @Test
    void noContentAndValidIndentsAndLinefeed() {
        validCase("#", 1, "");
        validCase("##", 2, "");
        validCase("###", 3, "");
        validCase("####", 4, "");
        validCase("#####", 5, "");
        validCase("######", 6, "");
        validCase(" #", 1, "");
        validCase(" ##", 2, "");
        validCase(" ###", 3, "");
        validCase(" ####", 4, "");
        validCase(" #####", 5, "");
        validCase(" ######", 6, "");
        validCase("  #", 1, "");
        validCase("  ##", 2, "");
        validCase("  ###", 3, "");
        validCase("  ####", 4, "");
        validCase("  #####", 5, "");
        validCase("  ######", 6, "");
        validCase("   #", 1, "");
        validCase("   ##", 2, "");
        validCase("   ###", 3, "");
        validCase("   ####", 4, "");
        validCase("   #####", 5, "");
        validCase("   ######", 6, "");
    }

    @Test
    void noContentAndValidIndentsAndValidTrailingSpace() {
        validCase("# ", 1, "");
        validCase("## ", 2, "");
        validCase("### ", 3, "");
        validCase("#### ", 4, "");
        validCase("##### ", 5, "");
        validCase("###### ", 6, "");
        validCase(" # ", 1, "");
        validCase(" ## ", 2, "");
        validCase(" ### ", 3, "");
        validCase(" #### ", 4, "");
        validCase(" ##### ", 5, "");
        validCase(" ###### ", 6, "");
        validCase("  # ", 1, "");
        validCase("  ## ", 2, "");
        validCase("  ### ", 3, "");
        validCase("  #### ", 4, "");
        validCase("  ##### ", 5, "");
        validCase("  ###### ", 6, "");
        validCase("   # ", 1, "");
        validCase("   ## ", 2, "");
        validCase("   ### ", 3, "");
        validCase("   #### ", 4, "");
        validCase("   ##### ", 5, "");
        validCase("   ###### ", 6, "");
    }

    @Test
    void noContentAndValidIndentsAndValidTrailingTab() {
        validCase("#\t", 1, "");
        validCase("##\t", 2, "");
        validCase("###\t", 3, "");
        validCase("####\t", 4, "");
        validCase("#####\t", 5, "");
        validCase("######\t", 6, "");
        validCase(" #\t", 1, "");
        validCase(" ##\t", 2, "");
        validCase(" ###\t", 3, "");
        validCase(" ####\t", 4, "");
        validCase(" #####\t", 5, "");
        validCase(" ######\t", 6, "");
        validCase("  #\t", 1, "");
        validCase("  ##\t", 2, "");
        validCase("  ###\t", 3, "");
        validCase("  ####\t", 4, "");
        validCase("  #####\t", 5, "");
        validCase("  ######\t", 6, "");
        validCase("   #\t", 1, "");
        validCase("   ##\t", 2, "");
        validCase("   ###\t", 3, "");
        validCase("   ####\t", 4, "");
        validCase("   #####\t", 5, "");
        validCase("   ######\t", 6, "");
    }

    @Test
    void simpleContentAndValidIndents() {
        validCase("# Test", 1, "Test");
        validCase("## Test", 2, "Test");
        validCase("### Test", 3, "Test");
        validCase("#### Test", 4, "Test");
        validCase("##### Test", 5, "Test");
        validCase("###### Test", 6, "Test");
        validCase(" # Test", 1, "Test");
        validCase(" ## Test", 2, "Test");
        validCase(" ### Test", 3, "Test");
        validCase(" #### Test", 4, "Test");
        validCase(" ##### Test", 5, "Test");
        validCase(" ###### Test", 6, "Test");
        validCase("  # Test", 1, "Test");
        validCase("  ## Test", 2, "Test");
        validCase("  ### Test", 3, "Test");
        validCase("  #### Test", 4, "Test");
        validCase("  ##### Test", 5, "Test");
        validCase("  ###### Test", 6, "Test");
        validCase("   # Test", 1, "Test");
        validCase("   ## Test", 2, "Test");
        validCase("   ### Test", 3, "Test");
        validCase("   #### Test", 4, "Test");
        validCase("   ##### Test", 5, "Test");
        validCase("   ###### Test", 6, "Test");
    }

    @Test
    void simpleContentWithRedundantSpacesAtStart() {
        validCase("#      Test", 1, "Test");
        validCase("##     Test", 2, "Test");
        validCase("###    Test", 3, "Test");
        validCase("####   Test", 4, "Test");
        validCase("#####  Test", 5, "Test");
        validCase("###### Test", 6, "Test");
        validCase(" #      Test", 1, "Test");
        validCase(" ##     Test", 2, "Test");
        validCase(" ###    Test", 3, "Test");
        validCase(" ####   Test", 4, "Test");
        validCase(" #####  Test", 5, "Test");
        validCase(" ###### Test", 6, "Test");
        validCase("  #      Test", 1, "Test");
        validCase("  ##     Test", 2, "Test");
        validCase("  ###    Test", 3, "Test");
        validCase("  ####   Test", 4, "Test");
        validCase("  #####  Test", 5, "Test");
        validCase("  ###### Test", 6, "Test");
        validCase("   #      Test", 1, "Test");
        validCase("   ##     Test", 2, "Test");
        validCase("   ###    Test", 3, "Test");
        validCase("   ####   Test", 4, "Test");
        validCase("   #####  Test", 5, "Test");
        validCase("   ###### Test", 6, "Test");
    }

    @Test
    void simpleContentWithTabsAtStart() {
        validCase("#\tTest", 1, "Test");
        validCase("##\tTest", 2, "Test");
        validCase("###\tTest", 3, "Test");
        validCase("####\tTest", 4, "Test");
        validCase("#####\tTest", 5, "Test");
        validCase("######\tTest", 6, "Test");
        validCase(" #\tTest", 1, "Test");
        validCase(" ##\tTest", 2, "Test");
        validCase(" ###\tTest", 3, "Test");
        validCase(" ####\tTest", 4, "Test");
        validCase(" #####\tTest", 5, "Test");
        validCase(" ######\tTest", 6, "Test");
        validCase("  #\tTest", 1, "Test");
        validCase("  ##\tTest", 2, "Test");
        validCase("  ###\tTest", 3, "Test");
        validCase("  ####\tTest", 4, "Test");
        validCase("  #####\tTest", 5, "Test");
        validCase("  ######\tTest", 6, "Test");
        validCase("   #\tTest", 1, "Test");
        validCase("   ##\tTest", 2, "Test");
        validCase("   ###\tTest", 3, "Test");
        validCase("   ####\tTest", 4, "Test");
        validCase("   #####\tTest", 5, "Test");
        validCase("   ######\tTest", 6, "Test");
    }

    @Test
    void simpleContentWithWhitespacesAtStart() {
        validCase("#      \tTest", 1, "Test");
        validCase("##     \tTest", 2, "Test");
        validCase("###    \tTest", 3, "Test");
        validCase("####   \tTest", 4, "Test");
        validCase("#####  \tTest", 5, "Test");
        validCase("###### \tTest", 6, "Test");
        validCase(" #      \tTest", 1, "Test");
        validCase(" ##     \tTest", 2, "Test");
        validCase(" ###    \tTest", 3, "Test");
        validCase(" ####   \tTest", 4, "Test");
        validCase(" #####  \tTest", 5, "Test");
        validCase(" ###### \tTest", 6, "Test");
        validCase("  #      \tTest", 1, "Test");
        validCase("  ##     \tTest", 2, "Test");
        validCase("  ###    \tTest", 3, "Test");
        validCase("  ####   \tTest", 4, "Test");
        validCase("  #####  \tTest", 5, "Test");
        validCase("  ###### \tTest", 6, "Test");
        validCase("   #      \tTest", 1, "Test");
        validCase("   ##     \tTest", 2, "Test");
        validCase("   ###    \tTest", 3, "Test");
        validCase("   ####   \tTest", 4, "Test");
        validCase("   #####  \tTest", 5, "Test");
        validCase("   ###### \tTest", 6, "Test");
    }

    @Test
    void simpleContentWithWhitespacesAtStartAndEnd() {
        validCase("#      Test  \t  \t ", 1, "Test");
        validCase("##     Test  \t  \t ", 2, "Test");
        validCase("###    Test  \t  \t ", 3, "Test");
        validCase("####   Test  \t  \t ", 4, "Test");
        validCase("#####  Test  \t  \t ", 5, "Test");
        validCase("###### Test  \t  \t ", 6, "Test");
        validCase(" #      Test  \t  \t ", 1, "Test");
        validCase(" ##     Test  \t  \t ", 2, "Test");
        validCase(" ###    Test  \t  \t ", 3, "Test");
        validCase(" ####   Test  \t  \t ", 4, "Test");
        validCase(" #####  Test  \t  \t ", 5, "Test");
        validCase(" ###### Test  \t  \t ", 6, "Test");
        validCase("  #      Test  \t  \t ", 1, "Test");
        validCase("  ##     Test  \t  \t ", 2, "Test");
        validCase("  ###    Test  \t  \t ", 3, "Test");
        validCase("  ####   Test  \t  \t ", 4, "Test");
        validCase("  #####  Test  \t  \t ", 5, "Test");
        validCase("  ###### Test  \t  \t ", 6, "Test");
        validCase("   #      Test  \t  \t ", 1, "Test");
        validCase("   ##     Test  \t  \t ", 2, "Test");
        validCase("   ###    Test  \t  \t ", 3, "Test");
        validCase("   ####   Test  \t  \t ", 4, "Test");
        validCase("   #####  Test  \t  \t ", 5, "Test");
        validCase("   ###### Test  \t  \t ", 6, "Test");
    }

    @Test
    void longerContent() {
        validCase("# Testing testing I'm just suggesting, HTML might not be the best thing", 1, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase("## Testing testing I'm just suggesting, HTML might not be the best thing", 2, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase("### Testing testing I'm just suggesting, HTML might not be the best thing", 3, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase("#### Testing testing I'm just suggesting, HTML might not be the best thing", 4, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase("##### Testing testing I'm just suggesting, HTML might not be the best thing", 5, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase("###### Testing testing I'm just suggesting, HTML might not be the best thing", 6, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase(" # Testing testing I'm just suggesting, HTML might not be the best thing", 1, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase(" ## Testing testing I'm just suggesting, HTML might not be the best thing", 2, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase(" ### Testing testing I'm just suggesting, HTML might not be the best thing", 3, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase(" #### Testing testing I'm just suggesting, HTML might not be the best thing", 4, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase(" ##### Testing testing I'm just suggesting, HTML might not be the best thing", 5, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase(" ###### Testing testing I'm just suggesting, HTML might not be the best thing", 6, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase("#      Testing testing I'm just suggesting, HTML might not be the best thing", 1, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase("##     Testing testing I'm just suggesting, HTML might not be the best thing", 2, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase("###    Testing testing I'm just suggesting, HTML might not be the best thing", 3, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase("####   Testing testing I'm just suggesting, HTML might not be the best thing", 4, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase("#####  Testing testing I'm just suggesting, HTML might not be the best thing", 5, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase("###### Testing testing I'm just suggesting, HTML might not be the best thing", 6, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase(" #      Testing testing I'm just suggesting, HTML might not be the best thing", 1, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase(" ##     Testing testing I'm just suggesting, HTML might not be the best thing", 2, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase(" ###    Testing testing I'm just suggesting, HTML might not be the best thing", 3, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase(" ####   Testing testing I'm just suggesting, HTML might not be the best thing", 4, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase(" #####  Testing testing I'm just suggesting, HTML might not be the best thing", 5, "Testing testing I'm just suggesting, HTML might not be the best thing");
        validCase(" ###### Testing testing I'm just suggesting, HTML might not be the best thing", 6, "Testing testing I'm just suggesting, HTML might not be the best thing");
    }

    @Test
    void testOptionalEndingsEmptyContent() {
        // No ending space
        validCase("# #", 1, "");
        validCase("# ##", 1, "");
        validCase("# ###", 1, "");
        validCase("#  #", 1, "");
        validCase("#  ##", 1, "");
        validCase("#  ###", 1, "");
        validCase("# \t#", 1, "");
        validCase("# \t##", 1, "");
        validCase("# \t###", 1, "");
        validCase("#  \t#", 1, "");
        validCase("#  \t##", 1, "");
        validCase("#  \t###", 1, "");
        // Ending space
        validCase("# # ", 1, "");
        validCase("# ## ", 1, "");
        validCase("# ### ", 1, "");
        validCase("#  # ", 1, "");
        validCase("#  ## ", 1, "");
        validCase("#  ### ", 1, "");
        validCase("# \t# ", 1, "");
        validCase("# \t## ", 1, "");
        validCase("# \t### ", 1, "");
        validCase("#  \t# ", 1, "");
        validCase("#  \t## ", 1, "");
        validCase("#  \t### ", 1, "");
        // Ending space and tab
        validCase("# # \t", 1, "");
        validCase("# ## \t", 1, "");
        validCase("# ### \t", 1, "");
        validCase("#  # \t", 1, "");
        validCase("#  ## \t", 1, "");
        validCase("#  ### \t", 1, "");
        validCase("# \t# \t", 1, "");
        validCase("# \t## \t", 1, "");
        validCase("# \t### \t", 1, "");
        validCase("#  \t# \t", 1, "");
        validCase("#  \t## \t", 1, "");
        validCase("#  \t### \t", 1, "");
        // Ending tab
        validCase("# #\t", 1, "");
        validCase("# ##\t", 1, "");
        validCase("# ###\t", 1, "");
        validCase("#  #\t", 1, "");
        validCase("#  ##\t", 1, "");
        validCase("#  ###\t", 1, "");
        validCase("# \t#\t", 1, "");
        validCase("# \t##\t", 1, "");
        validCase("# \t###\t", 1, "");
        validCase("#  \t#\t", 1, "");
        validCase("#  \t##\t", 1, "");
        validCase("#  \t###\t", 1, "");
        // Ending tab and space
        validCase("# #\t ", 1, "");
        validCase("# ##\t ", 1, "");
        validCase("# ###\t ", 1, "");
        validCase("#  #\t ", 1, "");
        validCase("#  ##\t ", 1, "");
        validCase("#  ###\t ", 1, "");
        validCase("# \t#\t ", 1, "");
        validCase("# \t##\t ", 1, "");
        validCase("# \t###\t ", 1, "");
        validCase("#  \t#\t ", 1, "");
        validCase("#  \t##\t ", 1, "");
        validCase("#  \t###\t ", 1, "");
    }

    @Test
    void testOptionalEndingsSimpleContent() {
        validCase("# Test #", 1, "Test");
        validCase("# Test ##", 1, "Test");
        validCase("# Test ###", 1, "Test");
        validCase("# Test  #", 1, "Test");
        validCase("# Test  ##", 1, "Test");
        validCase("# Test  ###", 1, "Test");
        validCase("# Test\t#", 1, "Test");
        validCase("# Test\t##", 1, "Test");
        validCase("# Test\t###", 1, "Test");
        validCase("# Test \t#", 1, "Test");
        validCase("# Test \t##", 1, "Test");
        validCase("# Test \t###", 1, "Test");
    }

    @Test
    void improperOptionalEnding() {
        validCase("# Test#", 1, "Test#");
        validCase("# Test##", 1, "Test##");
        validCase("# Test###", 1, "Test###");
        validCase("# Test Case#", 1, "Test Case#");
        validCase("# Test Case##", 1, "Test Case##");
        validCase("# Test Case###", 1, "Test Case###");

    }

    @Test
    void mixedOctothorpes() {
        validCase("# Test# #", 1, "Test#");
        validCase("# Test## #", 1, "Test##");
        validCase("# Test## #", 1, "Test##");
        validCase("# Test## ##", 1, "Test##");
        validCase("# Test # #", 1, "Test #");
        validCase("# Test # # #", 1, "Test # #");
        validCase("# Test # # ###", 1, "Test # #");
        validCase("# Test # #  ", 1, "Test #");
        validCase("# Test # # #  ", 1, "Test # #");
        validCase("# Test # # ###  ", 1, "Test # #");
    }

    @Test
    void noHeadings() {
        invalidCase("");
        invalidCase(" ");
        invalidCase("  ");
        invalidCase("   ");
        invalidCase("Test");
        invalidCase(" Test");
        invalidCase("  Test");
        invalidCase("   Test");
    }

    @Test
    void tooDeepHeadings() {
        invalidCase("#######");
        invalidCase(" #######");
        invalidCase("  #######");
        invalidCase("   #######");
        invalidCase("####### ");
        invalidCase(" ####### ");
        invalidCase("  ####### ");
        invalidCase("   ####### ");
        invalidCase("#######  #");
        invalidCase(" #######  #");
        invalidCase("  #######  #");
        invalidCase("   #######  #");
        invalidCase("#######  # \t");
        invalidCase(" #######  # \t");
        invalidCase("  #######  # \t");
        invalidCase("   #######  # \t");
    }

    @Test
    void tooManyIndents() {
        invalidCase("    #");
        invalidCase("    ##");
        invalidCase("    ###");
        invalidCase("    ####");
        invalidCase("    #####");
        invalidCase("    ######");
        invalidCase("    # Test");
        invalidCase("    ## Test");
        invalidCase("    ### Test");
        invalidCase("    #### Test");
        invalidCase("    ##### Test");
        invalidCase("    ###### Test");
        invalidCase("    # Test ###");
        invalidCase("    ## Test ###");
        invalidCase("    ### Test ###");
        invalidCase("    #### Test ###");
        invalidCase("    ##### Test ###");
        invalidCase("    ###### Test ###");
    }

    @Test
    void noWhitespaceAfterHeadings() {
        invalidCase("#Test");
        invalidCase("##Test");
        invalidCase("###Test");
        invalidCase("####Test");
        invalidCase("#####Test");
        invalidCase("######Test");
        invalidCase(" #Test");
        invalidCase(" ##Test");
        invalidCase(" ###Test");
        invalidCase(" ####Test");
        invalidCase(" #####Test");
        invalidCase(" ######Test");
        invalidCase("#Test Case");
        invalidCase("##Test Case");
        invalidCase("###Test Case");
        invalidCase("####Test Case");
        invalidCase("#####Test Case");
        invalidCase("######Test Case");
    }

    @Test
    void tabsAtStartNoContent() {
        invalidCase("\t#");
        invalidCase("\t##");
        invalidCase("\t###");
        invalidCase("\t####");
        invalidCase("\t#####");
        invalidCase("\t######");
        invalidCase(" \t#");
        invalidCase(" \t##");
        invalidCase(" \t###");
        invalidCase(" \t####");
        invalidCase(" \t#####");
        invalidCase(" \t######");
        invalidCase("\t #");
        invalidCase("\t ##");
        invalidCase("\t ###");
        invalidCase("\t ####");
        invalidCase("\t #####");
        invalidCase("\t ######");
        invalidCase(" \t #");
        invalidCase(" \t ##");
        invalidCase(" \t ###");
        invalidCase(" \t ####");
        invalidCase(" \t #####");
        invalidCase(" \t ######");
    }

    @Test
    void tabsAtStartWithContent() {
        invalidCase("\t# Test");
        invalidCase("\t## Test");
        invalidCase("\t### Test");
        invalidCase("\t#### Test");
        invalidCase("\t##### Test");
        invalidCase("\t###### Test");
        invalidCase(" \t# Test");
        invalidCase(" \t## Test");
        invalidCase(" \t### Test");
        invalidCase(" \t#### Test");
        invalidCase(" \t##### Test");
        invalidCase(" \t###### Test");
        invalidCase("\t # Test");
        invalidCase("\t ## Test");
        invalidCase("\t ### Test");
        invalidCase("\t #### Test");
        invalidCase("\t ##### Test");
        invalidCase("\t ###### Test");
        invalidCase(" \t # Test");
        invalidCase(" \t ## Test");
        invalidCase(" \t ### Test");
        invalidCase(" \t #### Test");
        invalidCase(" \t ##### Test");
        invalidCase(" \t ###### Test");
    }

}