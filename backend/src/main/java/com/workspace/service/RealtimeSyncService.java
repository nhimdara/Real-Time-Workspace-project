package com.workspace.service;

import com.workspace.dto.RealtimeEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeSyncService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastPageEvent(UUID pageId, RealtimeEventDTO event) {
        String destination = "/topic/page/" + pageId;
        log.debug("Broadcasting event {} to {}", event.getType(), destination);
        messagingTemplate.convertAndSend(destination, event);
    }
}
