package com.nextrade.service;

import com.nextrade.common.identifier.SessionId;
import com.nextrade.common.identifier.UserId;

public sealed interface AuthResult permits AuthResult.Success, AuthResult.Pending, AuthResult.Failure {
    record Success(String accessToken, String refreshToken, UserId userId, String role, SessionId sessionId) implements AuthResult {}
    record Pending(UserId userId, String message) implements AuthResult {}
    record Failure(String code, String message) implements AuthResult {}
}
