package com.example.autowebgenerator.model.enums;

public enum MessageTypeEnum {

    USER("user"),
    AI("ai");

    private final String value;

    MessageTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MessageTypeEnum fromValue(String value) {
        if (value == null || value.isBlank()) return null;
        for (MessageTypeEnum e : values()) {
            if (e.value.equals(value)) return e;
        }
        return null;
    }
}
