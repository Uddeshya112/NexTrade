package com.nextrade.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JwtKidResolver {
    private static final Pattern KID = Pattern.compile("\\\"kid\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

    private JwtKidResolver() {
    }

    public static String resolve(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Malformed token");
        }
        String json = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        Matcher matcher = KID.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("kid missing");
        }
        return matcher.group(1);
    }
}
