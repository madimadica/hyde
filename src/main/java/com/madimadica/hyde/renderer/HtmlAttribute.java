package com.madimadica.hyde.renderer;

public record HtmlAttribute(String key, String value) {
    public static HtmlAttribute of(String key) {
        return new HtmlAttribute(key, null);
    }

    public static HtmlAttribute of(String key, String value) {
        return new HtmlAttribute(key, value);
    }

    public static HtmlAttribute ofEscaped(String key, String value) {
        return new HtmlAttribute(key, HtmlAstRenderer.escapeHtml(value));
    }
}
