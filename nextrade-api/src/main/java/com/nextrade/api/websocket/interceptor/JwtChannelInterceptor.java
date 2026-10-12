package com.nextrade.api.websocket.interceptor;

import com.nextrade.api.security.SupabaseTokenVerifier;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Authenticates STOMP CONNECT frames and authorizes subsequent inbound frames. */
@Component
public final class JwtChannelInterceptor implements ChannelInterceptor {
    private static final long MAX_STOMP_CONTENT_LENGTH = 1024L * 1024L;

    private final SupabaseTokenVerifier tokenService;

    public JwtChannelInterceptor(SupabaseTokenVerifier tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT) {
            accessor.setUser(authenticate(accessor));
        } else if (command == StompCommand.SEND || command == StompCommand.SUBSCRIBE) {
            Authentication authentication = requireAuthentication(accessor);
            if (command == StompCommand.SEND) {
                authorizeSend(accessor);
                validateContentLength(accessor, message.getPayload());
            } else {
                authorizeSubscription(accessor, authentication);
            }
        }
        return message;
    }

    private Authentication authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ") || header.length() <= 7) {
            throw new MessageDeliveryException("Authentication required: send a Bearer access token");
        }

        try {
            SupabaseTokenVerifier.VerifiedUser user = tokenService.verify(header.substring(7).trim());
            if (user == null) throw new MessageDeliveryException("Valid, confirmed Supabase access token required");
            return new UsernamePasswordAuthenticationToken(
                    user.nextradeUserId(), null, List.of(new SimpleGrantedAuthority("ROLE_" + user.role())));
        } catch (MessageDeliveryException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new MessageDeliveryException(null, "Invalid, expired, or unconfirmed Supabase access token");
        }
    }

    private Authentication requireAuthentication(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof Authentication authentication) || !authentication.isAuthenticated()) {
            throw new MessageDeliveryException("Unauthorized STOMP frame");
        }
        return authentication;
    }

    private void authorizeSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/app/")) {
            throw new MessageDeliveryException("STOMP SEND is only allowed to /app destinations");
        }
    }

    private void authorizeSubscription(StompHeaderAccessor accessor, Authentication authentication) {
        String destination = accessor.getDestination();
        if (destination == null || destination.isBlank()) {
            throw new MessageDeliveryException("Subscription destination is required");
        }
        if (destination.startsWith("/topic/admin") && authentication.getAuthorities().stream()
                .noneMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            throw new MessageDeliveryException("Administrator role is required for this topic");
        }
        if (destination.startsWith("/user/")) {
            String[] pieces = destination.split("/");
            // Spring's standard user destination (/user/queue/...) is already scoped to the authenticated Principal.
            // Explicit UUID destinations are accepted only for the authenticated user's UUID.
            if (pieces.length > 2) {
                try {
                    UUID requestedUser = UUID.fromString(pieces[2]);
                    UUID authenticatedUser = UUID.fromString(authentication.getName());
                    if (!requestedUser.equals(authenticatedUser)) {
                        throw new MessageDeliveryException("Cannot subscribe to another user's destination");
                    }
                } catch (IllegalArgumentException ignored) {
                    // Non-UUID segment (e.g. /user/queue) is a normal Spring user destination.
                }
            }
            return;
        }
        if (!destination.startsWith("/topic/")) {
            throw new MessageDeliveryException("Unsupported STOMP subscription destination");
        }
    }

    private void validateContentLength(StompHeaderAccessor accessor, Object payload) {
        long actualLength;
        if (payload instanceof byte[] bytes) {
            actualLength = bytes.length;
        } else if (payload instanceof String text) {
            actualLength = text.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        } else {
            actualLength = payload == null ? 0 : payload.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        }
        if (actualLength > MAX_STOMP_CONTENT_LENGTH) {
            throw new MessageDeliveryException("STOMP message payload exceeds 1 MiB");
        }
        String value = accessor.getFirstNativeHeader("content-length");
        if (value == null) return;
        try {
            long declaredLength = Long.parseLong(value);
            if (declaredLength < 0 || declaredLength > MAX_STOMP_CONTENT_LENGTH) {
                throw new MessageDeliveryException("STOMP message content length is invalid or exceeds 1 MiB");
            }
        } catch (NumberFormatException e) {
            throw new MessageDeliveryException(null, "Invalid STOMP content-length header: " + e.getMessage());
        }
    }
}
