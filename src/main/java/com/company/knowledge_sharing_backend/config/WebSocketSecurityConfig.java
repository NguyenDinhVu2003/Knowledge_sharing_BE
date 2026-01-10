package com.company.knowledge_sharing_backend.config;

import com.company.knowledge_sharing_backend.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Security Configuration
 * Intercept WebSocket connections to validate JWT tokens from query parameters
 */
@Configuration
@Slf4j
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Try to get token from native headers (query parameter)
                    String token = null;

                    // Check Authorization header first
                    if (accessor.getNativeHeader("Authorization") != null) {
                        String authHeader = accessor.getFirstNativeHeader("Authorization");
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            token = authHeader.substring(7);
                        }
                    }

                    // If no token in header, check query parameter (SockJS sends it as native header)
                    if (token == null && accessor.getNativeHeader("token") != null) {
                        token = accessor.getFirstNativeHeader("token");
                    }

                    if (token != null) {
                        try {
                            String username = jwtUtil.extractUsername(token);

                            if (username != null && jwtUtil.validateToken(token)) {
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                                UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                    );
                                accessor.setUser(authentication);
                                log.debug("WebSocket authenticated: {}", username);
                            }
                        } catch (Exception e) {
                            log.warn("WebSocket authentication failed: {}", e.getMessage());
                        }
                    }
                }

                return message;
            }
        });
    }
}

