package com.madimadica.hyde.renderer;

import com.madimadica.hyde.ast.*;
import com.madimadica.hyde.parser.ParserOptions;
import com.madimadica.hyde.parser.ParserUtils;
import com.madimadica.hyde.parser.Str;

import java.util.ArrayList;
import java.util.List;

public class HtmlAstRenderer {

    protected final ParserOptions options;
    protected StringBuilder doc;
    protected char last;
    protected int altTags; // Disable inside image alt text

    public HtmlAstRenderer() {
        this(ParserOptions.getDefaults());
    }
    
    public HtmlAstRenderer(ParserOptions options) {
        this.options = options;
    }

    public String render(AST ast) {
        doc = new StringBuilder();
        last = '\n';
        altTags = 0;
        for (var event : ast) {
            boolean entering = event.isEntering();
            switch (event.node()) {
                case HeadingNode node -> render(node, entering);
                case BlankLineNode node -> render(node);
                case BlockQuoteNode node -> render(node, entering);
                case DocumentNode node -> render(node, entering);
                case FencedCodeBlockNode node -> render(node);
                case HTMLBlockNode node -> render(node);
                case IndentedCodeBlockNode node -> render(node);
                case InlineBoldNode node -> render(node, entering);
                case InlineCodeNode node -> render(node);
                case InlineHardBreakNode node -> render(node);
                case InlineHTMLNode node -> render(node);
                case InlineImageNode node -> render(node, entering);
                case InlineItalicNode node -> render(node, entering);
                case InlineLinkNode node -> render(node, entering);
                case InlineSoftBreakNode node -> render(node);
                case InlineTextNode node -> render(node);
                case LinkReferenceDefinitionNode node -> render(node);
                case ListNode node -> render(node, entering);
                case ListItemNode node -> render(node, entering);
                case ParagraphNode node -> render(node, entering);
                case ThematicBreakNode node -> render(node);
            }
        }
        return doc.toString();
    }

    protected void addAttributes(List<HtmlAttribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        for (var attr : attributes) {
            doc.append(' ')
                    .append(attr.key())
                    .append('=')
                    .append('"')
                    .append(attr.value())
                    .append('"');
        }
    }

    protected void voidTag(String tag, List<HtmlAttribute> attributes) {
        if (altTags > 0) return;
        doc.append('<').append(tag);
        addAttributes(attributes);
        doc.append(" />");
        last = '>';
    }

    protected void openTag(String tag) {
        if (altTags > 0) return;
        doc.append('<').append(tag);
        doc.append('>');
        last = '>';
    }

    protected void openTag(String tag, List<HtmlAttribute> attributes) {
        if (altTags > 0) return;
        doc.append('<').append(tag);
        addAttributes(attributes);
        doc.append('>');
        last = '>';
    }

    protected void closeTag(String tag) {
        if (altTags > 0) return;
        doc.append("</").append(tag).append('>');
        last = '>';
    }

    protected void tag(String tag, boolean opening) {
        if (opening) {
            openTag(tag);
        } else {
            closeTag(tag);
        }
    }

    protected void literal(String s) {
        doc.append(s);
        last = Str.lastCharOrElse(s, '\n');
    }

    protected void newline() {
        if (last != '\n') {
            doc.append('\n');
            last = '\n';
        }
    }

    protected void escaped(String s) {
        literal(escapeHtml(s));
    }

    public static String escapeHtml(String s) {
        // Special or replace unsafe
        final int len = s.length();

        if (len == 0) {
            return s;
        }

        boolean hasSpecial = false;
        int i = 0;
        for (; i < len; ++i) {
            final char c = s.charAt(i);
            boolean isSpecial = (c == '&' || c == '<' || c == '>' || c == '"');
            if (isSpecial) {
                hasSpecial = true;
                break;
            }
        }

        if (!hasSpecial) {
            return s;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(s, 0, i);
        for (; i < len; ++i) {
            final char c = s.charAt(i);
            boolean isSpecial = (c == '&' || c == '<' || c == '>' || c == '"');
            if (isSpecial) {
                String escaped = switch (c) {
                    case '&' -> "&amp;";
                    case '"' -> "&quot;";
                    case '<' -> "&lt;";
                    case '>' -> "&gt;";
                    default -> throw new IllegalStateException("Impossible");
                };
                sb.append(escaped);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    protected void render(HeadingNode node, boolean entering) {
        // Optimized tag conversion to prevent str concatenation
        assert node.getLevel() <= 6 : "expected [1, 6] Instead found " + node.getLevel();
        String tag = String.valueOf(new char[] {
                'h', (char) (node.getLevel() + 48)
        });
        if (entering) {
            newline();
            openTag(tag);
        } else {
            closeTag(tag);
            newline();
        }
    }

    protected void render(BlankLineNode node) {
        newline();
    }

    protected void render(BlockQuoteNode node, boolean entering) {
        newline();
        if (entering) {
            openTag("blockquote");
        } else {
            closeTag("blockquote");
        }
        newline();
    }

    protected void render(DocumentNode node, boolean entering) {
        return; // Don't render <html> tags
    }

    protected void render(FencedCodeBlockNode node) {
        List<HtmlAttribute> attrs = new ArrayList<>();
        if (node.hasInfoString()) {
            String fullInfo = node.getInfoString();
            String firstWord = fullInfo.split("[ \t]")[0];
            String escaped = escapeHtml(ParserUtils.unescapeString(firstWord));
            if (!escaped.startsWith(options.codeInfoPrefix())) {
                escaped = options.codeInfoPrefix() + escaped;
            }
            attrs.add(HtmlAttribute.of("class", escaped));
        }
        newline();
        openTag("pre");
        openTag("code", attrs);
        escaped(node.getLiteral());
        closeTag("code");
        closeTag("pre");
        newline();
    }

    protected void render(HTMLBlockNode node) {
        newline();
        if (options.safeMode()) {
            literal(options.safeModeText());
        } else {
            literal(node.getLiteral());
        }
        newline();
    }

    protected void render(IndentedCodeBlockNode node) {
        newline();
        openTag("pre");
        openTag("code");
        escaped(node.getLiteral());
        closeTag("code");
        closeTag("pre");
        newline();
    }

    protected void render(InlineBoldNode node, boolean entering) {
        tag("strong", entering);
    }

    protected void render(InlineCodeNode node) {
        openTag("code");
        escaped(node.getLiteral());
        closeTag("code");
    }

    protected void render(InlineHardBreakNode node) {
        voidTag("br", null);
        newline();
    }

    protected void render(InlineHTMLNode node) {
        if (options.safeMode()) {
            literal(options.safeModeText());
        } else {
            literal(node.getLiteral());
        }
    }

    protected void render(InlineImageNode node, boolean entering) {
        if (entering) {
            if (altTags++ == 0) {
                String src = ParserUtils.uriEncode(ParserUtils.unescapeString(node.getDestination()));
                if (options.safeMode() && !SafeMode.isSafeHref(src)) {
                    literal("<img src=\"\" alt=\"");
                } else {
                    literal("<img src=\"" + escapeHtml(src) + "\" alt=\"");
                }
            }
        } else {
            if (--altTags == 0) {
                String title = ParserUtils.unescapeString(node.getDescription());
                if (title != null && !title.isEmpty()) {
                    literal("\" title=\"" + escapeHtml(title));
                }
                literal("\" />");
            }
        }
    }

    protected void render(InlineItalicNode node, boolean entering) {
        tag("em", entering);
    }

    protected void render(InlineLinkNode node, boolean entering) {
        if (!entering) {
            closeTag("a");
            return;
        }
        List<HtmlAttribute> attrs = new ArrayList<>();
        String href = ParserUtils.uriEncode(node.isAutolink() ? node.getDestination() : ParserUtils.unescapeString(node.getDestination()));
        if (!options.safeMode() || SafeMode.isSafeHref(href)) {
            attrs.add(HtmlAttribute.ofEscaped("href", href));
        }

        String title = ParserUtils.unescapeString(node.getTitle());
        if (title != null && !title.isEmpty()) {
            attrs.add(HtmlAttribute.ofEscaped("title", title));
        }

        openTag("a", attrs);
    }

    protected void render(InlineSoftBreakNode node) {
        literal(options.softBreak());
    }

    protected void render(InlineTextNode node) {
        escaped(node.getLiteral());
    }

    protected void render(LinkReferenceDefinitionNode node) {
        return;
    }

    protected void render(ListNode node, boolean entering) {
        List<HtmlAttribute> attrs = null;
        String tag = switch (node.getListData().getType()) {
            case ORDERED -> {
                var start = node.getListData().getOrderedStart();
                if (entering && start != 1) {
                    attrs = List.of(HtmlAttribute.of("start", String.valueOf(start)));
                }
                yield "ol";
            }
            case UNORDERED -> "ul";
        };
        newline();
        if (entering) {
            openTag(tag, attrs);
        } else {
            closeTag(tag);
        }
        newline();
    }

    protected void render(ListItemNode node, boolean entering) {
        if (entering) {
            openTag("li");
        } else {
            closeTag("li");
            newline();
        }
    }

    protected void render(ParagraphNode node, boolean entering) {
        if (node.getParent().getParent() instanceof ListNode listNode && listNode.getListData().isTight()) {
            return; // Tight list text does not get wrapped in paragraphs
        }
        if (entering) {
            newline();
            openTag("p");
        } else {
            closeTag("p");
            newline();
        }
    }

    protected void render(ThematicBreakNode node) {
        newline();
        voidTag("hr", null);
        newline();
    }

}
