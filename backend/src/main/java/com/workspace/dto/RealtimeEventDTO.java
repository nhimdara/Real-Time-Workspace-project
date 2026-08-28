package com.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeEventDTO {
    private RealtimeEventType type;
    private UUID pageId;
    private UUID senderId;
    private String senderName;
    private String senderAvatar;
    private Object payload;
    
    @Builder.Default
    private Instant timestamp = Instant.now();
}
