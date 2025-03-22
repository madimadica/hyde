package com.madimadica.hyde.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ManualTest {

    @Test
    void htmlDecode() {
        Assertions.assertEquals("💀", HtmlEntities.decode("&#x1F480;"));
        Assertions.assertEquals("💀", HtmlEntities.decode("&#128128;"));
        Assertions.assertEquals("💯", HtmlEntities.decode("&#x01F4AF;"));
        Assertions.assertEquals("💯", HtmlEntities.decode("&#128175;"));
    }

}
