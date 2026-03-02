package com.example.autowebgenerator.ai.model;

/**
 * Result object for multi-file generation mode.
 *
 * The AI produces three separate files:
 *   - index.html  (structure)
 *   - style.css   (presentation)
 *   - script.js   (behaviour)
 *
 * LangChain4j serialises this class's schema into a JSON instruction that is
 * automatically appended to the system prompt at runtime.
 */
public class MultiFileCodeResult {

    /** HTML markup — references style.css via <link> and script.js via <script>. */
    private String htmlCode;

    /** All CSS rules for the page. */
    private String cssCode;

    /** All JavaScript logic for the page. */
    private String jsCode;

    /** A brief human-readable description of what was generated. */
    private String description;

    public String getHtmlCode() { return htmlCode; }
    public void setHtmlCode(String htmlCode) { this.htmlCode = htmlCode; }

    public String getCssCode() { return cssCode; }
    public void setCssCode(String cssCode) { this.cssCode = cssCode; }

    public String getJsCode() { return jsCode; }
    public void setJsCode(String jsCode) { this.jsCode = jsCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "MultiFileCodeResult{description='" + description + "'}";
    }
}
