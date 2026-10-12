package com.nextrade.service.ports;

public interface VerificationMailer {
    boolean isConfigured();
    void sendVerification(String email, String username, String selector, String code);
}
