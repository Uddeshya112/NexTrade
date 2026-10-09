package com.nextrade.contracts.auth;

public record VerifyEmailRequest(String token, String code) {
}
