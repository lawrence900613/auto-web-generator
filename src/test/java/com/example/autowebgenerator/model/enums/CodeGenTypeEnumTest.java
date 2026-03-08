package com.example.autowebgenerator.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CodeGenTypeEnum — no Spring context required.
 */
class CodeGenTypeEnumTest {

    @Test
    void fromValue_returnsVueProjectForVueProjectString() {
        assertEquals(CodeGenTypeEnum.VUE_PROJECT, CodeGenTypeEnum.fromValue("vue_project"));
    }

    @Test
    void fromValue_returnsNullForDisabledLegacyModes() {
        assertNull(CodeGenTypeEnum.fromValue("html"));
        assertNull(CodeGenTypeEnum.fromValue("multi_file"));
    }

    @Test
    void fromValue_isCaseInsensitive() {
        assertEquals(CodeGenTypeEnum.VUE_PROJECT, CodeGenTypeEnum.fromValue("VUE_PROJECT"));
    }

    @Test
    void fromValue_returnsNullForNullOrBlankInput() {
        assertNull(CodeGenTypeEnum.fromValue(null));
        assertNull(CodeGenTypeEnum.fromValue(""));
        assertNull(CodeGenTypeEnum.fromValue("   "));
    }

    @Test
    void getValueAndLabel_areCorrect() {
        assertEquals("vue_project", CodeGenTypeEnum.VUE_PROJECT.getValue());
        assertEquals("Vue Project", CodeGenTypeEnum.VUE_PROJECT.getLabel());
    }
}
