package com.db.foodara.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.db.foodara.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves the principal for STOMP CONNECT frames.
 *
 * Resolution order:
 *   1. STOMP {@code Authorization: Bearer ...} header (when the JS client passes it explicitly)
 *   2. Authentication previously placed in session attributes by {@link WebSocketHandshakeInterceptor}
 *      (typical case — token came from the HTTP {@code accessToken} cookie at handshake time)
 *
 * The connection is allowed even when no authentication is found so that anonymous
 * subscriptions to public topics keep working. Endpoints that require authentication
 * should rely on the {@code @PreAuthorize} annotation in their controllers / message mappings.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        // 1. STOMP Authorization header (explicit Bearer token from client)
        UsernamePasswordAuthenticationToken auth = authFromStompHeader(accessor);

        // 2. Fallback to authentication captured at HTTP handshake from cookie / header
        if (auth == null) {
            auth = authFromHandshakeAttributes(accessor);
        }

        if (auth != null) {
            accessor.setUser(auth);
            log.debug("WebSocket connection authenticated for user: {}", auth.getName());
        } else {
            log.debug("WebSocket connection accepted as anonymous (no valid token)");
        }
        return message;
    }

    private UsernamePasswordAuthenticationToken authFromStompHeader(StompHeaderAccessor accessor) {
        List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");
        if (authorizationHeaders == null || authorizationHeaders.isEmpty()) {
            return null;
        }
        String bearer = authorizationHeaders.get(0);
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            return null;
        }
        String token = bearer.substring(7);
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isAccessToken(token)) {
            return null;
        }
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);
        List<SimpleGrantedAuthority> authorities = roles == null
                ? List.of()
                : roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }

    private UsernamePasswordAuthenticationToken authFromHandshakeAttributes(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
        if (sessionAttrs == null) {
            return null;
        }
        Object stored = sessionAttrs.get(WebSocketHandshakeInterceptor.USER_AUTH_ATTRIBUTE);
        return stored instanceof UsernamePasswordAuthenticationToken authentication ? authentication : null;
    }
}
