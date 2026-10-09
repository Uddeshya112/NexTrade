package com.nextrade.contracts.auth;

public record ResetPasswordRequest(String token, String newPassword) {
}
