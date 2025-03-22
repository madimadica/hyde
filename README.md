# Overview
Hyde is a markdown AST parser written for modern Java (21+).
It currently supports rendering the AST as HTML.

It does not have any runtime dependencies, and runs on Java 21 or higher.

Hyde follows the [CommonMark Spec](https://spec.commonmark.org/0.31.2/).

## Getting Started
**Java Version**: 21 or higher

### Adding Dependency
#### Maven
```xml
  <dependency>
    <groupId>com.madimadica</groupId>
    <artifactId>hyde</artifactId>
    <version>0.0.1</version>
  </dependency>
```

#### Gradle
```groovy
implementation 'com.madimadica:hyde:0.0.1'
```

## Quick Start Tutorial
This is a quick intro to simply convert Markdown to HTML.

These are some important fully qualified class names:
* `com.madimadica.hyde.ast.AST`
* `com.madimadica.hyde.parser.Parser`
* `com.madimadica.hyde.parser.ParserOptions`
* `com.madimadica.hyde.renderer.HtmlAstRenderer`

### Minimal Example
```java
String markdown = "# My First Document";
AST ast = Parser.parse(markdown);
var renderer = new HtmlAstRenderer();
String html = renderer.render(ast);
```

## Documentation
### Parsing
The entry point to begin parsing a markdown String is with `com.madimadica.hyde.parser.Parser`.
This class has two methods: `parse(String)` and `parse(String, ParserOptions)`. 
If you do not provide any options argument, then the defaults are used. In either case, an `AST` is returned.

### ParserOptions
You can customize a few options on how things are parsed/rendered.

| Option                      | Default                | Description                                                                                                                          |
|-----------------------------|------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `boolean smartQuotes`       | `false`                | Auto convert matching `'` and `"` into smart quotes like `’` and `“`                                                                 |
| `boolean smartSymbols`      | `false`                | Auto convert `...` into `…` and `--`/`---` into `–`/`—`                                                                              |
| `boolean gcOriginalInlines` | `false`                | Garbage Collect raw inline string values after parsing. Can clean up extra memory usage if `true`                                    |
| `boolean safeMode`          | `false`                | Enable HTML rendering safe mode for rendering raw HTML or raw HTML attributes. Set this to `true` if the user can control the input. |
| `String safeModeText`       | `"<!-- SAFE MODE -->"` | What to render in-place of raw HTML, when safeMode is `true`.                                                                        |
| `String codeInfoPrefix`     | `"language-"`          | The CSS class prefix to use with a fenced code block's info string. Set this to `""` to remove any css class prefixes.               |
| `String softBreak` | "\n" | The literal character to use for a soft linebreak in HTML.                                                                           |

#### Builder
You can customize/override the defaults by using `ParserOptions.builder()` to construct a builder option (containing the defaults), and then `set<attribute>`, then `.build()`.

### Rendering
To render as HTML, there is a single public method on `HtmlAstRenderer`: `String render(AST ast)`.
When you construct the renderer, you can provide an options argument, otherwise the defaults are used.


## Future Enhancements / Goals
* Add custom block elements
* Add custom inline elements
* Support GFM