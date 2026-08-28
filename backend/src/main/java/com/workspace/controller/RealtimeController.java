package com.workspace.controller;

import com.workspace.dto.RealtimeEventDTO;
import com.workspace.security.UserDetailsServiceImpl;
import com.workspace.service.RealtimeSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RealtimeController {

    private final RealtimeSyncService realtimeSyncService;

    /**
     * Handles real-time page updates, block modifications, and user cursor movements.
     * Broadcasts incoming events to topic /topic/page/{pageId}
     */
    @MessageMapping("/page/{pageId}/update")
    public void handlePageEvent(
            @DestinationVariable UUID pageId,
            @Payload RealtimeEventDTO event,
            Principal principal) {

        log.debug("Received realtime event on page {}: {}", pageId, event.getType());

        // Ensure pageId in the payload matches path
        event.setPageId(pageId);

        // Enrich with sender details if principal is present
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof UserDetailsServiceImpl.SecurityUser secUser) {
            if (event.getSenderId() == null) {
                event.setSenderId(secUser.getId());
            }
            if (event.getSenderName() == null) {
                event.setSenderName(secUser.getName());
            }
            if (event.getSenderAvatar() == null) {
                event.setSenderAvatar(secUser.getUser().getAvatarUrl());
            }
        }

        // Broadcast to all subscribers of /topic/page/{pageId}
        realtimeSyncService.broadcastPageEvent(pageId, event);
    }
}
