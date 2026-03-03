package com.example.autowebgenerator.model.enums;

/**
 * User role enumeration.
 */
public enum UserRoleEnum {

    USER("user", "user"),
    ADMIN("admin", "admin");

    private final String text;
    private final String value;

    UserRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /** Look up an enum by its string value; returns null if not found. */
    public static UserRoleEnum getEnumByValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (UserRoleEnum item : UserRoleEnum.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}
