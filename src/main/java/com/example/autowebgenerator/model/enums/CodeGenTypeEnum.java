package com.example.autowebgenerator.model.enums;

/**
 * Supported code generation modes.
 *
 * HTML        — everything in one self-contained .html file (easiest to preview)
 * MULTI_FILE  — separate index.html / style.css / script.js (cleaner structure)
 */
public enum CodeGenTypeEnum {

    HTML("Single-file HTML", "html"),
    MULTI_FILE("Multi-file (HTML + CSS + JS)", "multi_file"),
    VUE_PROJECT("Vue Project", "vue_project");

    private final String label;
    private final String value;

    CodeGenTypeEnum(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() { return label; }
    public String getValue() { return value; }

    /** Look up an enum constant by its string value, or return null if not found. */
    public static CodeGenTypeEnum fromValue(String value) {
        if (value == null || value.isBlank()) return null;
        for (CodeGenTypeEnum e : values()) {
            if (e.value.equalsIgnoreCase(value)) return e;
        }
        return null;
    }
}
