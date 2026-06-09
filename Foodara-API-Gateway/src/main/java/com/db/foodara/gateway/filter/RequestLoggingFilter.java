package com.db.foodara.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway Global Filter — Request Logging
 *
 * Logs every request passing through the gateway:
 * - HTTP method + path
 * - Response status code
 * - Duration in milliseconds
 * - User ID (from JWT, if authenticated)
 */
@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String clientIp = getClientIp(request);

        String fullPath = query != null ? path + "?" + query : path;

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;

            String userId = request.getHeaders().getFirst("X-User-Id");
            String userInfo = (userId != null && !userId.isBlank()) ? " user=" + userId : "";

            if (statusCode >= 400) {
                log.warn("→ {} {} | {} | {}ms | ip={}{}", method, fullPath, statusCode, duration, clientIp, userInfo);
            } else {
                log.info("→ {} {} | {} | {}ms | ip={}{}", method, fullPath, statusCode, duration, clientIp, userInfo);
            }
        }));
    }

    @Override
    public int getOrder() {
        // Run after JwtValidationFilter (-100), but before routing
        return -90;
    }

    private String getClientIp(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }
}
