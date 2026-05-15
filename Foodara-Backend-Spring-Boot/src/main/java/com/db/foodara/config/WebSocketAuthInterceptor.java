package com.db.foodara.config;

import com.db.foodara.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract Authorization header from STOMP headers
            List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");
            
            if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
                String bearerToken = authorizationHeaders.get(0);
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    String token = bearerToken.substring(7);
                    
                    if (jwtTokenProvider.validateToken(token) && jwtTokenProvider.isAccessToken(token)) {
                        String userId = jwtTokenProvider.getUserIdFromToken(token);
                        List<String> roles = jwtTokenProvider.getRolesFromToken(token);
                        
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                                
                        UsernamePasswordAuthenticationToken auth = 
                                new UsernamePasswordAuthenticationToken(userId, null, authorities);
                        
                        // Set the user in the accessor so Spring WebSocket knows who is connected
                        accessor.setUser(auth);
                        log.debug("WebSocket connection authenticated for user: {}", userId);
                        return message;
                    }
                }
            }
            log.warn("WebSocket connection rejected: Invalid or missing token");
            // If we throw exception, the connection is rejected
            throw new IllegalArgumentException("Invalid JWT token for WebSocket");
        }
        
        return message;
    }
}
