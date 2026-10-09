package com.nextrade.service;

import com.nextrade.common.exception.TokenException;
import com.nextrade.common.identifier.SessionId;
import com.nextrade.common.identifier.UserId;
import com.nextrade.common.enumtype.SessionStatus;
import com.nextrade.common.util.Hashing;
import com.nextrade.common.util.RandomIds;
import com.nextrade.domain.repository.UserRepository;
import com.nextrade.domain.user.User;
import com.nextrade.service.ports.AccessTokenIssuer;
import com.nextrade.service.ports.AuthChallengeRepository;
import com.nextrade.service.ports.EventOutbox;
import com.nextrade.service.ports.PasswordHasher;
import com.nextrade.service.ports.RefreshTokenFactory;
import com.nextrade.service.ports.SessionRepository;
import com.nextrade.service.ports.UserClock;

import java.time.Instant;
import java.util.Optional;

public final class AuthService {
    private final UserRepository users;
    private final SessionRepository sessions;
    private final AuthChallengeRepository challenges;
    private final PasswordHasher passwords;
    private final RefreshTokenFactory refreshTokens;
    private final EventOutbox outbox;
    private final AccessTokenIssuer accessTokens;
    private final UserClock clock;
    private final LoginPolicy loginPolicy;
    private final TokenPolicy tokenPolicy;

    public AuthService(UserRepository users, SessionRepository sessions, AuthChallengeRepository challenges,
                       PasswordHasher passwords, RefreshTokenFactory refreshTokens, EventOutbox outbox,
                       AccessTokenIssuer accessTokens, UserClock clock, LoginPolicy loginPolicy, TokenPolicy tokenPolicy) {
        this.users = users;
        this.sessions = sessions;
        this.challenges = challenges;
        this.passwords = passwords;
        this.refreshTokens = refreshTokens;
        this.outbox = outbox;
        this.accessTokens = accessTokens;
        this.clock = clock;
        this.loginPolicy = loginPolicy;
        this.tokenPolicy = tokenPolicy;
    }

    public AuthResult register(String username, String email, String rawPassword, String role) {
        if (users.existsByUsername(username) || users.existsByEmail(email)) {
            return new AuthResult.Failure("ACCOUNT_EXISTS", "Username or email already exists");
        }
        User user = User.register(username, email, passwords.hash(rawPassword), com.nextrade.common.enumtype.UserRole.valueOf(role));
        users.save(user);
        String selector = RandomIds.numericCode(8);
        String code = RandomIds.numericCode(6);
        challenges.createVerification(user.id(), selector, Hashing.sha256(code),
                clock.now().plus(tokenPolicy.verificationTtl()), 5);
        outbox.appendAll(user.drainEvents());
        return new AuthResult.Pending(user.id(), "Verification challenge created; email delivery must be configured");
    }

    public AuthResult login(String login, String rawPassword) {
        Optional<User> found = users.findByUsername(login).or(() -> users.findByEmail(login));
        if (found.isEmpty()) return new AuthResult.Failure("INVALID_CREDENTIALS", "Invalid credentials");
        User user = found.get();
        Instant now = clock.now();
        if (!user.emailVerified()) return new AuthResult.Failure("EMAIL_NOT_VERIFIED", "Verify your email before signing in");
        if (!user.loginAllowed(now)) return new AuthResult.Failure(user.status().name(), "Account cannot log in");
        if (!passwords.matches(rawPassword, user.passwordHash())) {
            user.recordFailedLogin(loginPolicy.maxAttempts(), loginPolicy.lockout(), now);
            users.save(user);
            outbox.appendAll(user.drainEvents());
            return new AuthResult.Failure("INVALID_CREDENTIALS", "Invalid credentials");
        }
        user.recordSuccessfulLogin();
        users.save(user);
        outbox.appendAll(user.drainEvents());
        SessionId sessionId = SessionId.generate();
        String refresh = refreshTokens.issue(user.id(), sessionId);
        sessions.create(new SessionRepository.SessionRecord(sessionId, user.id(), Hashing.sha256(refresh),
                SessionStatus.ACTIVE, now.plus(tokenPolicy.refreshTtl())));
        return new AuthResult.Success(accessTokens.issue(user.id(), user.role().name(), sessionId), refresh,
                user.id(), user.role().name(), sessionId);
    }

    public AuthResult refresh(String refreshToken) {
        RefreshTokenFactory.TokenClaims claims;
        try {
            claims = refreshTokens.parse(refreshToken);
        } catch (RuntimeException e) {
            return new AuthResult.Failure("INVALID_REFRESH_TOKEN", "Invalid refresh token");
        }
        SessionId nextSession = SessionId.generate();
        if (!sessions.rotate(claims.sessionId(), nextSession, Hashing.sha256(refreshToken))) {
            return new AuthResult.Failure("INVALID_REFRESH_TOKEN", "Refresh token already used or revoked");
        }
        Optional<User> found = users.findById(claims.userId());
        if (found.isEmpty() || !found.get().loginAllowed(clock.now())) {
            return new AuthResult.Failure("USER_INACTIVE", "User inactive");
        }
        User user = found.get();
        String nextRefresh = refreshTokens.issue(claims.userId(), nextSession);
        sessions.create(new SessionRepository.SessionRecord(nextSession, claims.userId(), Hashing.sha256(nextRefresh),
                SessionStatus.ACTIVE, clock.now().plus(tokenPolicy.refreshTtl())));
        return new AuthResult.Success(accessTokens.issue(claims.userId(), user.role().name(), nextSession), nextRefresh,
                claims.userId(), user.role().name(), nextSession);
    }

    public void logout(SessionId sessionId) { sessions.revoke(sessionId); }
    public void logoutRefresh(String refreshToken) {
        try { sessions.revoke(refreshTokens.parse(refreshToken).sessionId()); } catch (RuntimeException ignored) { }
    }
    public void logoutAll(UserId userId) { sessions.revokeAll(userId); }

    public void startPasswordReset(String email) {
        users.findByEmail(email).ifPresent(user -> {
            String token = java.util.UUID.randomUUID().toString();
            challenges.createReset(user.id(), Hashing.sha256(token), clock.now().plus(tokenPolicy.resetTtl()));
            // Delivery is intentionally not faked: configure an email provider to deliver this challenge.
        });
    }

    public boolean verifyResetToken(String token) {
        return token != null && challenges.isResetValid(Hashing.sha256(token), clock.now());
    }

    public void resetPassword(String token, String newHash) {
        UserId userId = challenges.consumeReset(Hashing.sha256(token), clock.now());
        if (userId == null) throw new TokenException("Invalid or expired reset token");
        User user = users.findById(userId).orElseThrow(() -> new TokenException("User not found"));
        user.changePassword(newHash);
        users.save(user);
        sessions.revokeAll(userId);
        outbox.appendAll(user.drainEvents());
    }

    public void resetPasswordRaw(String token, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 12) throw new IllegalArgumentException("Password must contain at least 12 characters");
        resetPassword(token, passwords.hash(rawPassword));
    }

    public void changePassword(UserId userId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 12) throw new IllegalArgumentException("Password must contain at least 12 characters");
        User user = users.findById(userId).orElseThrow(() -> new TokenException("User not found"));
        if (!passwords.matches(currentPassword, user.passwordHash())) throw new TokenException("Current password is incorrect");
        user.changePassword(passwords.hash(newPassword));
        users.save(user);
        sessions.revokeAll(userId);
        outbox.appendAll(user.drainEvents());
    }

    public boolean verifyEmail(String selector, String code) {
        UserId userId = challenges.consumeVerification(selector, Hashing.sha256(code), clock.now(), 5);
        if (userId == null) return false;
        User user = users.findById(userId).orElse(null);
        if (user == null) return false;
        user.verifyEmail();
        users.save(user);
        outbox.appendAll(user.drainEvents());
        return true;
    }
}
