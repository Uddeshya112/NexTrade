package com.nextrade.tests;

import com.nextrade.common.enumtype.UserRole;
import com.nextrade.domain.repository.EventOutbox;
import com.nextrade.domain.repository.UserRepository;
import com.nextrade.domain.user.User;
import com.nextrade.service.AuthResult;
import com.nextrade.service.AuthService;
import com.nextrade.service.LoginPolicy;
import com.nextrade.service.TokenPolicy;
import com.nextrade.service.ports.AccessTokenIssuer;
import com.nextrade.service.ports.AuthChallengeRepository;
import com.nextrade.service.ports.PasswordHasher;
import com.nextrade.service.ports.RefreshTokenFactory;
import com.nextrade.service.ports.SessionRepository;
import com.nextrade.service.ports.UserClock;
import com.nextrade.service.ports.VerificationMailer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.ArgumentMatchers.anyString;

class AuthServiceLoginTest {
    @Test
    void trimsLoginIdentifierAndLooksUpEmailOrUsernameWithoutCaseSensitivity() {
        UserRepository users = mock(UserRepository.class);
        SessionRepository sessions = mock(SessionRepository.class);
        AuthChallengeRepository challenges = mock(AuthChallengeRepository.class);
        PasswordHasher passwords = mock(PasswordHasher.class);
        RefreshTokenFactory refreshTokens = mock(RefreshTokenFactory.class);
        EventOutbox outbox = mock(EventOutbox.class);
        AccessTokenIssuer accessTokens = mock(AccessTokenIssuer.class);
        UserClock clock = () -> Instant.parse("2026-10-12T00:00:00Z");

        User user = User.register("trader", "trader@example.com", "hash", UserRole.TRADER);
        user.verifyEmail();
        when(users.findByUsernameOrEmail("Trader@Example.com")).thenReturn(Optional.of(user));
        when(passwords.matches("correct-password", "hash")).thenReturn(true);
        when(refreshTokens.issue(any(), any())).thenReturn("refresh-token");
        when(accessTokens.issue(any(), any(), any())).thenReturn("access-token");

        AuthService auth = new AuthService(users, sessions, challenges, passwords, refreshTokens,
                outbox, accessTokens, clock, LoginPolicy.defaults(), TokenPolicy.defaults(), mock(VerificationMailer.class));

        AuthResult result = auth.login("  Trader@Example.com  ", "correct-password");

        assertInstanceOf(AuthResult.Success.class, result);
        verify(users).findByUsernameOrEmail("Trader@Example.com");
        verify(sessions).create(any(SessionRepository.SessionRecord.class));
    }

    @Test
    void missingCredentialsFailWithoutRepositoryLookup() {
        UserRepository users = mock(UserRepository.class);
        AuthService auth = new AuthService(users, mock(SessionRepository.class), mock(AuthChallengeRepository.class),
                mock(PasswordHasher.class), mock(RefreshTokenFactory.class), mock(EventOutbox.class),
                mock(AccessTokenIssuer.class), Instant::now, LoginPolicy.defaults(), TokenPolicy.defaults(), mock(VerificationMailer.class));

        AuthResult result = auth.login("  ", "password");

        assertEquals("INVALID_CREDENTIALS", assertInstanceOf(AuthResult.Failure.class, result).code());
        verify(users, org.mockito.Mockito.never()).findByUsernameOrEmail(any());
    }

    @Test
    void resendCreatesAndEmailsVerificationChallengeForPendingAccount() {
        UserRepository users = mock(UserRepository.class);
        VerificationMailer mailer = mock(VerificationMailer.class);
        User pendingUser = User.register("trader", "trader@example.com", "hash", UserRole.TRADER);
        when(users.findByEmail("trader@example.com")).thenReturn(Optional.of(pendingUser));
        when(mailer.isConfigured()).thenReturn(true);

        AuthService auth = new AuthService(users, mock(SessionRepository.class), mock(AuthChallengeRepository.class),
                mock(PasswordHasher.class), mock(RefreshTokenFactory.class), mock(EventOutbox.class),
                mock(AccessTokenIssuer.class), Instant::now, LoginPolicy.defaults(), TokenPolicy.defaults(), mailer);

        AuthResult.Pending result = assertInstanceOf(AuthResult.Pending.class, auth.resendVerification(" Trader@Example.com "));

        assertEquals(true, result.deliveryConfigured());
        org.junit.jupiter.api.Assertions.assertFalse(result.selector().isBlank());
        verify(mailer).sendVerification(eq("trader@example.com"), eq("trader"), anyString(), matches("[0-9]{6}"));
    }
}
