package com.nextrade.api.security;

import com.nextrade.common.identifier.SessionId;
import com.nextrade.service.ports.SessionRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/** Authenticates signed access tokens and checks the backing session on every request. */
public final class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AccessTokenService tokens;
    private final SessionRepository sessions;

    public JwtAuthenticationFilter(AccessTokenService tokens, SessionRepository sessions) {
        this.tokens = tokens;
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") && header.length() > 7) {
            try {
                Claims claims = tokens.parse(header.substring(7).trim()).getPayload();
                String type = claims.get("token_type", String.class);
                String role = claims.get("role", String.class);
                String subject = claims.getSubject();
                String sessionClaim = claims.getId();
                if ("access".equals(type) && role != null && !role.isBlank()
                        && subject != null && !subject.isBlank()
                        && sessionClaim != null && !sessionClaim.isBlank()) {
                    SessionId sessionId = SessionId.parse(sessionClaim);
                    var activeSession = sessions.findActive(sessionId).orElse(null);
                    if (activeSession != null
                            && activeSession.userId().value().toString().equals(subject)
                            && activeSession.expiresAt().isAfter(Instant.now())) {
                        var auth = new UsernamePasswordAuthenticationToken(subject, null,
                                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (RuntimeException ignored) {
                // Invalid, expired, revoked, or malformed tokens remain unauthenticated.
            }
        }
        try {
            chain.doFilter(req, res);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
