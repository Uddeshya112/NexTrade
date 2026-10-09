package com.nextrade.service.ports;

import com.nextrade.common.identifier.SessionId;
import com.nextrade.common.identifier.UserId;

/** Application boundary for issuing an already-authenticated access token. */
public interface AccessTokenIssuer {
    String issue(UserId userId, String role, SessionId sessionId);
}
