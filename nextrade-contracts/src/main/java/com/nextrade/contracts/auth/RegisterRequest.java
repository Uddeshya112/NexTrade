package com.nextrade.contracts.auth;

public record RegisterRequest(String username, String email, String password) {
}
