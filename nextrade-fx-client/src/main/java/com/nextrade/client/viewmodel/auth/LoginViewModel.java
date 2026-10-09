package com.nextrade.client.viewmodel.auth;

import com.nextrade.client.core.NavigationManager;
import com.nextrade.client.security.AuthContext;
import com.nextrade.client.security.RateLimiter;
import com.nextrade.client.service.ApiClient;
import com.nextrade.contracts.auth.AuthResponse;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;

public class LoginViewModel {
    private final ApiClient apiClient;
    private final AuthContext authContext = AuthContext.getInstance();
    private final RateLimiter rateLimiter = RateLimiter.getInstance();

    private final StringProperty emailOrUsername = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final BooleanProperty rememberMe = new SimpleBooleanProperty(false);
    private final BooleanProperty showPassword = new SimpleBooleanProperty(false);
    private final StringProperty emailError = new SimpleStringProperty("");
    private final StringProperty passwordError = new SimpleStringProperty("");
    private final BooleanProperty loggingIn = new SimpleBooleanProperty(false);
    private final StringProperty rateLimitError = new SimpleStringProperty("");

    public LoginViewModel() {
        this.apiClient = new ApiClient("http://localhost:8080");
        setupValidation();
    }

    private void setupValidation() {
        emailOrUsername.addListener((obs, old, val) -> {
            if (val == null || val.trim().isEmpty()) emailError.set("Email or username is required");
            else if (val.length() < 3) emailError.set("Must be at least 3 characters");
            else emailError.set("");
        });
        password.addListener((obs, old, val) -> {
            if (val == null || val.isEmpty()) passwordError.set("Password is required");
            else if (val.length() < 12) passwordError.set("Password must be at least 12 characters");
            else passwordError.set("");
        });
    }

    public void login() {
        clearError();
        if (!validateForm()) return;
        String rateKey = "login:" + emailOrUsername.get().toLowerCase();
        if (!RateLimiter.getInstance().tryAcquire(rateKey, 5, 15)) {
            rateLimitError.set("Too many attempts. Try again later.");
            return;
        }
        loggingIn.set(true);
        Task<AuthResponse> task = new Task<>() {
            @Override protected AuthResponse call() throws Exception {
                return apiClient.login(emailOrUsername.get(), password.get(), rememberMe.get()).get();
            }
        };
        task.setOnSucceeded(e -> {
            loggingIn.set(false);
            AuthResponse result = task.getValue();
            if (result instanceof AuthResponse.Success success) {
                rateLimiter.reset("login:" + emailOrUsername.get().toLowerCase());
                try {
                    authContext.setAuthenticated(success.accessToken(), success.refreshToken(),
                            com.nextrade.common.identifier.UserId.parse(success.userId()),
                            emailOrUsername.get(), com.nextrade.common.enumtype.UserRole.valueOf(success.role()),
                            "", "ACTIVE", rememberMe.get());
                    NavigationManager.getInstance().navigateTo("dashboard");
                } catch (RuntimeException ex) {
                    handleLoginFailure("Login response is incomplete: " + ex.getMessage());
                }
            } else if (result instanceof AuthResponse.Failure failure) {
                handleLoginFailure(failure.code() + ": " + failure.message());
            } else if (result instanceof AuthResponse.PendingVerification pending) {
                handleLoginFailure("EMAIL_NOT_VERIFIED: " + pending.message());
            } else {
                handleLoginFailure("Unexpected authentication response");
            }
        });
        task.setOnFailed(e -> {
            loggingIn.set(false);
            Throwable failure = task.getException();
            handleLoginFailure(failure == null || failure.getMessage() == null ? "Login failed" : failure.getMessage());
        });
        new Thread(task).start();
    }

    private void clearError() {
        emailError.set("");
        passwordError.set("");
        rateLimitError.set("");
    }

    private boolean validateForm() {
        boolean valid = true;
        if (emailOrUsername.get().trim().isEmpty()) { emailError.set("Email or username is required"); valid = false; }
        if (password.get().isEmpty()) { passwordError.set("Password is required"); valid = false; }
        else if (password.get().length() < 12) { passwordError.set("Password must be at least 12 characters"); valid = false; }
        return valid;
    }

    private void handleLoginFailure(String message) {
        if (message.contains("ACCOUNT_LOCKED")) rateLimitError.set(message);
        else if (message.contains("EMAIL_NOT_VERIFIED")) rateLimitError.set("Please verify your email first.");
        else if (message.contains("ACCOUNT_SUSPENDED")) rateLimitError.set("Account suspended. Contact support.");
        else rateLimitError.set("Invalid credentials. Please try again.");
    }

    public void forgotPassword() { NavigationManager.getInstance().navigateTo("forgot-password"); }
    public void navigateToRegister() { NavigationManager.getInstance().navigateTo("register"); }

    public StringProperty emailOrUsernameProperty() { return emailOrUsername; }
    public StringProperty passwordProperty() { return password; }
    public BooleanProperty rememberMeProperty() { return rememberMe; }
    public BooleanProperty showPasswordProperty() { return showPassword; }
    public StringProperty emailErrorProperty() { return emailError; }
    public StringProperty passwordErrorProperty() { return passwordError; }
    public BooleanProperty loggingInProperty() { return loggingIn; }
    public StringProperty rateLimitErrorProperty() { return rateLimitError; }
}
