package com.nextrade.service.email;

import org.springframework.stereotype.Component;

@Component
public class EmailService {

    public void sendVerificationEmail(String email, String code, String username) {
        // Implementation - send via SMTP or log for dev
        System.out.println("VERIFICATION EMAIL to " + email + ": code=" + code);
    }

    public void sendPasswordResetEmail(String email, String token, String username) {
        System.out.println("PASSWORD RESET EMAIL to " + email + ": token=" + token);
    }

    public void sendLoginAlertEmail(String email, String ip, String userAgent) {
        System.out.println("LOGIN ALERT to " + email + ": ip=" + ip + ", agent=" + userAgent);
    }

    public void sendWelcomeEmail(String email, String username) {
        System.out.println("WELCOME EMAIL to " + email);
    }

    public void sendSecurityAlertEmail(String email, String event, String details) {
        System.out.println("SECURITY ALERT to " + email + ": " + event + " - " + details);
    }
}
