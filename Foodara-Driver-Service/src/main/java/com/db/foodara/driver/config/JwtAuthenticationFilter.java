package com.db.foodara.driver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter — Gateway-aware
 *
 * Priority:
 * 1. If X-User-Id header is present (from API Gateway) → trust gateway headers, set SecurityContext
 * 2. If no gateway headers → fallback to JWT validation (for local dev without gateway)
 *
 * Gateway propagates: X-User-Id, X-User-Email, X-User-Roles
 * Downstream services use @PreAuthorize / SecurityConfig to check roles.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Priority 1: Check gateway headers
        String gatewayUserId = request.getHeader("X-User-Id");

        if (StringUtils.hasText(gatewayUserId)) {
            // Trusted headers from API Gateway — no need to re-validate JWT
            String rolesHeader = request.getHeader("X-User-Roles");
            List<SimpleGrantedAuthority> authorities = parseRolesHeader(rolesHeader);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(gatewayUserId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } else {
            // Priority 2: Fallback — validate JWT directly (local dev without gateway)
            String token = getTokenFromRequest(request);

            if (StringUtils.hasText(token)
                    && jwtTokenProvider.validateToken(token)
                    && jwtTokenProvider.isAccessToken(token)) {
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                List<String> roles = jwtTokenProvider.getRolesFromToken(token);

                List<SimpleGrantedAuthority> authorities = roles == null
                        ? List.of()
                        : roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Parse X-User-Roles header (comma-separated) into Spring Security authorities.
     * Example: "CUSTOMER,DRIVER" → [ROLE_CUSTOMER, ROLE_DRIVER]
     */
    private List<SimpleGrantedAuthority> parseRolesHeader(String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return List.of();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
