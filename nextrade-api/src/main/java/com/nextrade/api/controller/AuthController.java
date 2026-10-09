package com.nextrade.api.controller;

import com.nextrade.api.security.AccessTokenService;
import com.nextrade.common.identifier.SessionId;
import com.nextrade.common.identifier.UserId;
import com.nextrade.contracts.auth.AuthResponse;
import com.nextrade.domain.repository.UserRepository;
import com.nextrade.domain.user.User;
import com.nextrade.service.AuthResult;
import com.nextrade.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    private final AuthService authService;
    private final AccessTokenService accessTokens;
    private final UserRepository users;

    public AuthController(AuthService authService, AccessTokenService accessTokens, UserRepository users) {
        this.authService = authService;
        this.accessTokens = accessTokens;
        this.users = users;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthResponse.RegisterRequest request) {
        String role = request.role() == null || request.role().isBlank() ? "TRADER" : request.role();
        return ResponseEntity.ok(map(authService.register(request.username(), request.email(), request.password(), role)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthResponse.LoginRequest request) {
        AuthResponse result = map(authService.login(request.username(), request.password()));
        return result instanceof AuthResponse.Failure ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result) : ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody AuthResponse.RefreshRequest request) {
        AuthResponse result = map(authService.refresh(request.refreshToken()));
        return result instanceof AuthResponse.Failure ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result) : ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current session")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        Jws<Claims> jwt = accessTokens.parse(bearerValue(authorization));
        authService.logout(SessionId.parse(jwt.getPayload().getId()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout all sessions")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal String principal) {
        authService.logoutAll(UserId.parse(principal));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody AuthResponse.ForgotPasswordRequest request) {
        authService.startPasswordReset(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/verify-reset-token")
    @Operation(summary = "Verify password reset token")
    public ResponseEntity<AuthResponse.TokenVerificationResponse> verifyResetToken(
            @Valid @RequestBody AuthResponse.VerifyResetTokenRequest request) {
        boolean valid = authService.verifyResetToken(request.token());
        AuthResponse.TokenVerificationResponse response = valid
                ? new AuthResponse.TokenVerificationResponse.Valid(true, "Token valid")
                : new AuthResponse.TokenVerificationResponse.Invalid(false, "Invalid or expired token");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody AuthResponse.ResetPasswordRequest request) {
        authService.resetPasswordRaw(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal String principal,
                                               @Valid @RequestBody AuthResponse.ChangePasswordRequest request) {
        authService.changePassword(UserId.parse(principal), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with selector and code")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody AuthResponse.VerifyEmailRequest request) {
        return authService.verifyEmail(request.selector(), request.code())
                ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email")
    public ResponseEntity<AuthResponse> resendVerification(@AuthenticationPrincipal String principal) {
        // The current development setup has no email transport configured; do not pretend a message was sent.
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new AuthResponse.Failure("EMAIL_DELIVERY_NOT_CONFIGURED", "Configure an email provider to resend verification messages"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfile> getProfile(@AuthenticationPrincipal String principal) {
        User user = users.findById(UserId.parse(principal)).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new UserProfile(user.id().asString(), user.username().value(), user.email().value(),
                user.role().name(), user.status().name(), user.emailVerified()));
    }

    private static AuthResponse map(AuthResult result) {
        if (result instanceof AuthResult.Success success) {
            return new AuthResponse.Success(success.accessToken(), success.refreshToken(), success.userId().asString(),
                    success.role(), success.sessionId().asString());
        }
        if (result instanceof AuthResult.Pending pending) {
            return new AuthResponse.PendingVerification(pending.userId().asString(), pending.message());
        }
        AuthResult.Failure failure = (AuthResult.Failure) result;
        return new AuthResponse.Failure(failure.code(), failure.message());
    }

    private static String bearerValue(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() <= 7) {
            throw new IllegalArgumentException("Bearer token required");
        }
        return authorization.substring(7).trim();
    }

    public record UserProfile(String id, String username, String email, String role, String status, boolean emailVerified) { }
}
