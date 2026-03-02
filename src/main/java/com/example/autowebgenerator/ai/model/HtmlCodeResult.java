package com.example.autowebgenerator.ai.model;

/**
 * Result object for single-file HTML generation mode.
 *
 * LangChain4j uses this class's field names to build a JSON schema that is
 * automatically appended to the prompt, instructing the AI to respond in the
 * matching JSON structure.
 */
public class HtmlCodeResult {

    /** Complete, self-contained HTML page code (includes inline CSS and JS). */
    private String htmlCode;

    /** A brief human-readable description of what was generated. */
    private String description;

    public String getHtmlCode() { return htmlCode; }
    public void setHtmlCode(String htmlCode) { this.htmlCode = htmlCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "HtmlCodeResult{description='" + description + "', htmlCode.length=" +
                (htmlCode != null ? htmlCode.length() : 0) + "}";
    }
}
