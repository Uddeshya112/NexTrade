package com.nextrade.client.viewmodel.auth;

import com.nextrade.client.core.NavigationManager;
import com.nextrade.client.service.ApiClient;
import com.nextrade.client.security.RateLimiter;
import com.nextrade.contracts.auth.AuthResponse;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
        Task<Void> task = new Task<>() { @Override protected Void call() {
            var response = await(apiClient.forgotPassword(email.get()));
            if (response.statusCode() < 200 || response.statusCode() >= 300) throw new IllegalStateException("Could not request a password reset");
            return null;
        } };
        task.setOnSucceeded(e -> { sending.set(false); currentStep.set(Step.CODE_SENT); statusMessage.set("If the account exists, a reset link was sent to " + maskEmail(email.get()) + ". Open it in your browser to set a new password, then return here to sign in."); });
        task.setOnFailed(e -> { sending.set(false); statusMessage.set("Failed to send token"); emailError.set("Email not found or error occurred"); });
        new Thread(task).start();
    }

    public void verifyToken(String token) {
        emailError.set("Open the Supabase password recovery link in your browser and finish resetting there.");
    }

    public void resetPassword(String newPassword, String confirmPassword) {
        emailError.set("Open the Supabase password recovery link in your browser and finish resetting there.");
    }

    private boolean validateEmail() {
        if (email.get().trim().isEmpty()) { emailError.set("Email is required"); return false; }
        if (!email.get().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) { emailError.set("Please enter a valid email address"); return false; }
        return true;
    }

    private static <T> T await(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Request interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Request failed", e.getCause());
        }
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
