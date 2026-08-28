package com.workspace.config;

import com.workspace.security.JwtService;
import com.workspace.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader("Authorization");
            String token = null;

            if (authorization != null && !authorization.isEmpty()) {
                String bearerToken = authorization.get(0);
                if (bearerToken.startsWith("Bearer ")) {
                    token = bearerToken.substring(7);
                }
            }

            // Also check query param or passcode if provided
            if (token == null) {
                token = accessor.getFirstNativeHeader("token");
            }

            if (token != null) {
                try {
                    String username = jwtService.extractUsername(token);
                    if (username != null && jwtService.isTokenValid(token)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(authentication);
                        log.info("WebSocket authenticated user: {}", username);
                    }
                } catch (Exception e) {
                    log.warn("WebSocket authentication failed: {}", e.getMessage());
                }
            }
        }

        return message;
    }
}
