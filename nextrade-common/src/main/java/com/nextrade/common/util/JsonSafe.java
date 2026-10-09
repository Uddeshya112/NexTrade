package com.nextrade.common.util;

public final class JsonSafe {
    private JsonSafe() {
    }

    public static String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\""
                + value.replace("\\", "\\\\").replace("\"", "\\\"")
                + "\"";
    }
}
