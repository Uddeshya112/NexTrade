package com.nextrade.contracts.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Transport DTOs shared by the REST API and JavaFX client. */
public sealed interface AuthResponse permits AuthResponse.Success, AuthResponse.PendingVerification, AuthResponse.Failure {
    record Success(String accessToken, String refreshToken, String userId, String role, String sessionId)
            implements AuthResponse { }

    record PendingVerification(String userId, String selector, boolean deliveryConfigured, String message) implements AuthResponse { }

    record Failure(String code, String message) implements AuthResponse { }

    record RegisterRequest(String username, @NotBlank @Email String email, @NotBlank @Size(min = 8) String password, String role) { }
    record LoginRequest(@NotBlank @Email String username, @NotBlank String password) { }
    record RefreshRequest(String refreshToken) { }
    record ForgotPasswordRequest(@NotBlank @Email String email) { }
    record VerifyResetTokenRequest(String token) { }
    record ResetPasswordRequest(String token, String newPassword) { }
    record ChangePasswordRequest(String currentPassword, String newPassword) { }
    record VerifyEmailRequest(String selector, String code) { }
    record ResendVerificationRequest(String email) { }

    sealed interface TokenVerificationResponse permits TokenVerificationResponse.Valid, TokenVerificationResponse.Invalid {
        record Valid(boolean valid, String message) implements TokenVerificationResponse { }
        record Invalid(boolean valid, String message) implements TokenVerificationResponse { }
    }
}
