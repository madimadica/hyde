package com.madimadica.hyde.renderer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HtmlAstRendererTest {

    @Test
    void escapeHtml() {
        assertEquals("title &amp;quot;", HtmlAstRenderer.escapeHtml("title &quot;"));
    }
}