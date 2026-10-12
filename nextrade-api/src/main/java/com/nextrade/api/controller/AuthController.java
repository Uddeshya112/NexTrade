package com.nextrade.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.nextrade.api.security.SupabaseAuthGateway;
import com.nextrade.api.security.SupabaseTokenVerifier;
import com.nextrade.common.identifier.UserId;
import com.nextrade.contracts.auth.AuthResponse;
import com.nextrade.domain.repository.UserRepository;
import com.nextrade.domain.user.User;
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
@Tag(name = "Authentication", description = "Supabase Auth endpoints")
public class AuthController {
    private final SupabaseAuthGateway supabase;
    private final SupabaseTokenVerifier tokenVerifier;
    private final UserRepository users;

    public AuthController(SupabaseAuthGateway supabase, SupabaseTokenVerifier tokenVerifier, UserRepository users) {
        this.supabase = supabase;
        this.tokenVerifier = tokenVerifier;
        this.users = users;
    }

    @PostMapping("/register")
    @Operation(summary = "Create a Supabase Auth account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthResponse.RegisterRequest request) {
        try {
            supabase.signup(request.email(), request.password(), request.username());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new AuthResponse.PendingVerification("", "", true,
                    "Check your email and confirm the link before signing in."));
        } catch (org.springframework.web.client.RestClientResponseException rejected) {
            return ResponseEntity.badRequest().body(new AuthResponse.Failure("SIGNUP_FAILED",
                    "Could not create the account. Check the email and password, or sign in if you already have an account."));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Sign in through Supabase Auth")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthResponse.LoginRequest request) {
        try {
            JsonNode session = supabase.login(request.username(), request.password());
            AuthResponse result = success(session);
            return result instanceof AuthResponse.Failure ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result) : ResponseEntity.ok(result);
        } catch (org.springframework.web.client.RestClientResponseException rejected) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse.Failure("INVALID_CREDENTIALS",
                    "Sign in failed. Check your email and password, and confirm your email address."));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody AuthResponse.RefreshRequest request) {
        try {
            AuthResponse result = success(supabase.refresh(request.refreshToken()));
            return result instanceof AuthResponse.Failure ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result) : ResponseEntity.ok(result);
        } catch (org.springframework.web.client.RestClientResponseException rejected) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse.Failure("INVALID_REFRESH_TOKEN", "Your session expired. Sign in again."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) supabase.logout(authorization.substring(7).trim(), false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody AuthResponse.ForgotPasswordRequest request) {
        supabase.recover(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<AuthResponse> resendVerification(@Valid @RequestBody AuthResponse.ResendVerificationRequest request) {
        supabase.resendSignup(request.email());
        return ResponseEntity.accepted().body(new AuthResponse.PendingVerification("", "", true, "If the account needs confirmation, Supabase will send a new email."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail() { return ResponseEntity.status(HttpStatus.GONE).build(); }

    @PostMapping("/verify-reset-token")
    public ResponseEntity<AuthResponse.TokenVerificationResponse> verifyResetToken() {
        return ResponseEntity.status(HttpStatus.GONE).body(new AuthResponse.TokenVerificationResponse.Invalid(false,
                "Password recovery is verified by Supabase on the reset page."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword() { return ResponseEntity.status(HttpStatus.GONE).build(); }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestHeader("Authorization") String authorization,
            @Valid @RequestBody AuthResponse.ChangePasswordRequest request) {
        supabase.updatePassword(bearer(authorization), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@RequestHeader("Authorization") String authorization) {
        supabase.logout(bearer(authorization), true);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current NexTrade account profile")
    public ResponseEntity<UserProfile> getProfile(@AuthenticationPrincipal String principal) {
        User user = users.findById(UserId.parse(principal)).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new UserProfile(user.id().asString(), user.username().value(), user.email().value(),
                user.role().name(), user.status().name(), user.emailVerified()));
    }

    private AuthResponse success(JsonNode session) {
        String accessToken = session == null ? "" : session.path("access_token").asText("");
        String refreshToken = session == null ? "" : session.path("refresh_token").asText("");
        if (accessToken.isBlank() || refreshToken.isBlank()) {
            return new AuthResponse.Failure("EMAIL_CONFIRMATION_REQUIRED", "Check your email and confirm your address before signing in.");
        }
        SupabaseTokenVerifier.VerifiedUser identity = tokenVerifier.verify(accessToken);
        if (identity == null) return new AuthResponse.Failure("EMAIL_CONFIRMATION_REQUIRED", "Confirm your email before signing in.");
        User user = users.findById(UserId.parse(identity.nextradeUserId())).orElse(null);
        if (user == null) return new AuthResponse.Failure("ACCOUNT_NOT_FOUND", "Your NexTrade profile could not be loaded.");
        return new AuthResponse.Success(accessToken, refreshToken, identity.nextradeUserId(), user.role().name(), UUID.randomUUID().toString());
    }

    private static String bearer(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() <= 7) {
            throw new IllegalArgumentException("Bearer token required");
        }
        return authorization.substring(7).trim();
    }

    public record UserProfile(String id, String username, String email, String role, String status, boolean emailVerified) { }
}
