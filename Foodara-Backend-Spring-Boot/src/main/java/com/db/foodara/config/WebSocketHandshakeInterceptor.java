package com.db.foodara.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.db.foodara.security.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves the authenticated user during the WebSocket HTTP upgrade.
 *
 * Token resolution order:
 *   1. {@code accessToken} cookie (preferred — matches HTTP cookie auth flow)
 *   2. {@code Authorization: Bearer ...} header
 *   3. {@code ?access_token=} query parameter (fallback for environments where cookies cannot be sent)
 *
 * On success, an {@code Authentication} object is placed in the WebSocket session attributes
 * under the key {@link #USER_AUTH_ATTRIBUTE}. {@link WebSocketAuthInterceptor} picks it up
 * during the STOMP {@code CONNECT} frame so subscribers are recognised as authenticated users.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_AUTH_ATTRIBUTE = "user-auth";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            log.debug("WebSocket handshake: no token found, allowing anonymous connection");
            return true;
        }

        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isAccessToken(token)) {
            log.debug("WebSocket handshake: token invalid, allowing anonymous connection");
            return true;
        }

        String userId = jwtTokenProvider.getUserIdFromToken(token);
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);
        List<SimpleGrantedAuthority> authorities = roles == null
                ? List.of()
                : roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
        attributes.put(USER_AUTH_ATTRIBUTE, auth);
        log.debug("WebSocket handshake authenticated user={} roles={}", userId, roles);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }

    private String extractToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            // 1. Cookie
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                        return cookie.getValue();
                    }
                }
            }
        }

        // 2. Authorization header
        HttpHeaders headers = request.getHeaders();
        String bearer = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        // 3. Query parameter (?access_token=...)
        String query = request.getURI().getQuery();
        if (query != null && !query.isBlank()) {
            for (String pair : query.split("&")) {
                int eq = pair.indexOf('=');
                if (eq <= 0) continue;
                String key = pair.substring(0, eq);
                String value = pair.substring(eq + 1);
                if ("access_token".equals(key) && !value.isBlank()) {
                    return value;
                }
            }
        }

        return null;
    }
}
