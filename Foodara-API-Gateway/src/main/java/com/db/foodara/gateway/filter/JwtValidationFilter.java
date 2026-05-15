package com.db.foodara.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;

/**
 * Gateway Global Filter — JWT Validation & User Info Propagation
 *
 * Behavior:
 * - Token present & valid   → extract claims, add X-User-* headers, forward
 * - Token present & invalid → return 401 (bad/expired token)
 * - No token                → forward as-is (let downstream SecurityConfig decide)
 *
 * The gateway does NOT enforce authentication.
 * Downstream services use their own SecurityConfig + @PreAuthorize to check roles.
 */
@Component
@Slf4j
public class JwtValidationFilter implements GlobalFilter, Ordered {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Always skip CORS preflight requests
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // Extract JWT token
        String token = extractToken(request);

        // No token → forward as-is, let downstream handle auth
        if (token == null || token.isBlank()) {
            return chain.filter(exchange);
        }

        // Token present → validate and propagate
        try {
            Claims claims = parseToken(token);

            // Only access tokens are allowed for API requests
            Object tokenType = claims.get("type");
            if (tokenType != null && !"access".equalsIgnoreCase(tokenType.toString())) {
                // Not an access token — forward without user headers
                // (e.g., refresh token calls should reach downstream as-is)
                return chain.filter(exchange);
            }

            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            String rolesHeader = roles != null ? String.join(",", roles) : "";

            // Propagate user info to downstream services via headers
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId != null ? userId : "")
                    .header("X-User-Email", email != null ? email : "")
                    .header("X-User-Roles", rolesHeader)
                    .build();

            log.debug("JWT validated for user={} roles={} path={}", userId, rolesHeader, request.getURI().getPath());
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (JwtException e) {
            // Invalid/expired token → 401
            log.warn("Invalid JWT for {} {} — {}", request.getMethod(), request.getURI().getPath(), e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private String extractToken(ServerHttpRequest request) {
        // 1. Try cookie first
        HttpCookie accessTokenCookie = request.getCookies().getFirst("accessToken");
        if (accessTokenCookie != null && !accessTokenCookie.getValue().isBlank()) {
            return accessTokenCookie.getValue();
        }

        // 2. Fall back to Authorization header
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
