package com.nextrade.domain.user;

public record Email(String value) {
    public Email {
        if (value == null || !value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Invalid email");
        }
        value = value.toLowerCase(java.util.Locale.ROOT);
    }
}
