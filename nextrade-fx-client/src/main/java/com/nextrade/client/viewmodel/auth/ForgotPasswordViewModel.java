package com.nextrade.client.viewmodel.auth;

import com.nextrade.client.core.NavigationManager;
import com.nextrade.client.service.ApiClient;
import com.nextrade.client.security.RateLimiter;
import com.nextrade.contracts.auth.AuthResponse;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import java.util.concurrent.TimeUnit;

public class ForgotPasswordViewModel {
    private final ApiClient apiClient;
    private final RateLimiter rateLimiter = RateLimiter.getInstance();

    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty emailError = new SimpleStringProperty("");
    private final BooleanProperty sending = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final ObjectProperty<Step> currentStep = new SimpleObjectProperty<>(Step.ENTER_EMAIL);
    private String resetToken;

    public enum Step { ENTER_EMAIL, CODE_SENT, VERIFY_CODE, RESET_PASSWORD }

    public ForgotPasswordViewModel() {
        this.apiClient = new ApiClient("http://localhost:8080");
        setupValidation();
    }

    private void setupValidation() {
        email.addListener((obs, old, val) -> {
            if (val == null || val.trim().isEmpty()) emailError.set("Email is required");
            else if (!val.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) emailError.set("Please enter a valid email address");
            else emailError.set("");
        });
    }

    public void sendResetCode() {
        if (!validateEmail()) return;
        String rateKey = "forgot:" + email.get().toLowerCase();
        if (!RateLimiter.getInstance().tryAcquire(rateKey, 3, 60)) {
            long remaining = RateLimiter.getInstance().getRemainingTime(rateKey, 60);
            emailError.set("Too many requests. Try again in " + TimeUnit.SECONDS.toMinutes(remaining) + 1 + " minutes");
            return;
        }
        sending.set(true); statusMessage.set("Sending reset token...");
        Task<Void> task = new Task<>() { @Override protected Void call() { apiClient.forgotPassword(email.get()).get(); return null; } };
        task.setOnSucceeded(e -> { sending.set(false); currentStep.set(Step.VERIFY_CODE); statusMessage.set("Reset token sent to " + maskEmail(email.get())); });
        task.setOnFailed(e -> { sending.set(false); statusMessage.set("Failed to send token"); emailError.set("Email not found or error occurred"); });
        new Thread(task).start();
    }

    public void verifyToken(String token) {
        if (token == null || token.length() < 32) { emailError.set("Please enter the full reset token from email"); return; }
        sending.set(true); statusMessage.set("Verifying token...");
        Task<AuthResponse.TokenVerificationResponse> task = new Task<>() {
            @Override protected AuthResponse.TokenVerificationResponse call() { return apiClient.verifyResetToken(token).get(); }
        };
        task.setOnSucceeded(e -> {
            sending.set(false);
            AuthResponse.TokenVerificationResponse result = task.getValue();
            if (result instanceof AuthResponse.TokenVerificationResponse.Valid valid) {
                resetToken = token;
                currentStep.set(Step.RESET_PASSWORD); statusMessage.set("Token verified. Set your new password.");
            } else { emailError.set("Invalid or expired token"); }
        });
        task.setOnFailed(e -> { sending.set(false); emailError.set("Verification failed"); });
        new Thread(task).start();
    }

    public void resetPassword(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) { emailError.set("Passwords do not match"); return; }
        if (newPassword.length() < 12) { emailError.set("Password must be at least 12 characters"); return; }
        sending.set(true); statusMessage.set("Resetting password...");
        Task<Void> task = new Task<>() { @Override protected Void call() { apiClient.resetPassword(resetToken, newPassword).get(); return null; } };
        task.setOnSucceeded(e -> { sending.set(false); statusMessage.set("Password reset successful. Redirecting to login..."); Platform.runLater(() -> NavigationManager.getInstance().navigateTo("login")); });
        task.setOnFailed(e -> {
            sending.set(false);
            Throwable failure = task.getException();
            emailError.set("Reset failed: " + (failure == null || failure.getMessage() == null ? "unknown error" : failure.getMessage()));
        });
        new Thread(task).start();
    }

    private boolean validateEmail() {
        if (email.get().trim().isEmpty()) { emailError.set("Email is required"); return false; }
        if (!email.get().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) { emailError.set("Please enter a valid email address"); return false; }
        return true;
    }

    private String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) return email;
        String name = parts[0];
        return name.substring(0, Math.min(2, name.length())) + "***" + (name.length() > 1 ? name.substring(name.length() - 1) : "") + "@" + parts[1];
    }

    public StringProperty emailProperty() { return email; }
    public StringProperty emailErrorProperty() { return emailError; }
    public BooleanProperty sendingProperty() { return sending; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public ObjectProperty<Step> currentStepProperty() { return currentStep; }
}
