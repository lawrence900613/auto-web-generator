package com.example.autowebgenerator.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CodeGenTypeEnum — no Spring context required.
 */
class CodeGenTypeEnumTest {

    @Test
    void fromValue_returnsHtmlForHtmlString() {
        assertEquals(CodeGenTypeEnum.HTML, CodeGenTypeEnum.fromValue("html"));
    }

    @Test
    void fromValue_returnsMultiFileForMultiFileString() {
        assertEquals(CodeGenTypeEnum.MULTI_FILE, CodeGenTypeEnum.fromValue("multi_file"));
    }

    @Test
    void fromValue_isCaseInsensitive() {
        assertEquals(CodeGenTypeEnum.HTML,       CodeGenTypeEnum.fromValue("HTML"));
        assertEquals(CodeGenTypeEnum.MULTI_FILE, CodeGenTypeEnum.fromValue("MULTI_FILE"));
    }

    @Test
    void fromValue_returnsNullForUnknownType() {
        assertNull(CodeGenTypeEnum.fromValue("vue_project"));
        assertNull(CodeGenTypeEnum.fromValue("unknown"));
    }

    @Test
    void fromValue_returnsNullForNullOrBlankInput() {
        assertNull(CodeGenTypeEnum.fromValue(null));
        assertNull(CodeGenTypeEnum.fromValue(""));
        assertNull(CodeGenTypeEnum.fromValue("   "));
    }

    @Test
    void getValueAndLabel_areCorrect() {
        assertEquals("html",       CodeGenTypeEnum.HTML.getValue());
        assertEquals("multi_file", CodeGenTypeEnum.MULTI_FILE.getValue());

        assertEquals("Single-file HTML",            CodeGenTypeEnum.HTML.getLabel());
        assertEquals("Multi-file (HTML + CSS + JS)", CodeGenTypeEnum.MULTI_FILE.getLabel());
    }
}
