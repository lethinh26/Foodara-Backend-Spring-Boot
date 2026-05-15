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
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Set;

/**
 * Gateway Global Filter — Centralized JWT Validation
 *
 * Flow:
 * 1. Check if the request path is public → skip validation
 * 2. Extract JWT from cookie "accessToken" or Authorization header
 * 3. Validate JWT signature and expiration
 * 4. Extract userId, email, roles from JWT claims
 * 5. Propagate as headers: X-User-Id, X-User-Email, X-User-Roles
 * 6. Forward to downstream service
 *
 * Downstream services read these headers to set SecurityContext and check roles.
 */
@Component
@Slf4j
public class JwtValidationFilter implements GlobalFilter, Ordered {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // Public endpoints that don't require JWT — must match SecurityConfig patterns
    private static final Set<String> PUBLIC_EXACT_PATHS = Set.of(
            "/api/v1/auth/register",
            "/api/v1/auth/register/check",
            "/api/v1/auth/link-role",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/user-role",
            "/api/v1/merchant/login",
            "/api/v1/merchant/register",
            "/api/v1/payment/sepay/ipn",
            "/api/v1/payment/sepay/callback"
    );

    private static final List<String> PUBLIC_PREFIX_PATHS = List.of(
            "/api/v1/home/",
            "/api/v1/search/",
            "/api/v1/stores/",
            "/api/v1/menu-items/",
            "/api/v1/locations/",
            "/api/v1/store-categories/",
            "/api/v1/users/check-merchant/",
            "/api/payments/webhook",
            "/api/ws/",
            "/actuator/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // Skip JWT validation for public endpoints
        if (isPublicEndpoint(path, method)) {
            return chain.filter(exchange);
        }

        // Extract JWT token
        String token = extractToken(request);

        if (token == null || token.isBlank()) {
            log.warn("No JWT token found for request: {} {}", method, path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Validate and extract claims
        try {
            Claims claims = parseToken(token);

            // Check token type — only access tokens are allowed
            Object tokenType = claims.get("type");
            if (tokenType != null && !"access".equalsIgnoreCase(tokenType.toString())) {
                log.warn("Non-access token used for request: {} {}", method, path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
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
                    // Remove original Authorization header to prevent downstream re-validation
                    // But keep the cookie for services that still need it (e.g., refresh-token)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (JwtException e) {
            log.warn("Invalid JWT token for request: {} {} — {}", method, path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        // Run early in the filter chain
        return -100;
    }

    private boolean isPublicEndpoint(String path, String method) {
        // Exact matches
        if (PUBLIC_EXACT_PATHS.contains(path)) {
            return true;
        }

        // Prefix matches — only for GET requests (browse endpoints)
        for (String prefix : PUBLIC_PREFIX_PATHS) {
            if (path.startsWith(prefix)) {
                // Home, search, stores, menu-items, locations, store-categories are GET-only public
                if (prefix.equals("/api/payments/webhook") || prefix.equals("/actuator/")) {
                    return true; // These are public for all methods
                }
                return "GET".equalsIgnoreCase(method);
            }
        }

        return false;
    }

    private String extractToken(ServerHttpRequest request) {
        // 1. Try cookie first (matches frontend behavior)
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
