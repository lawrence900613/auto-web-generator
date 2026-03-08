package com.example.autowebgenerator.model.enums;

/**
 * Supported code generation modes.
 *
 * NOTE: For now only VUE_PROJECT is enabled in runtime validation.
 * HTML and MULTI_FILE are kept as legacy enum values and are temporarily disabled.
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
        // Temporarily support only vue_project mode.
        if (VUE_PROJECT.value.equalsIgnoreCase(value)) return VUE_PROJECT;
        // Legacy modes (temporarily disabled):
        // if (HTML.value.equalsIgnoreCase(value)) return HTML;
        // if (MULTI_FILE.value.equalsIgnoreCase(value)) return MULTI_FILE;
        return null;
    }
}
