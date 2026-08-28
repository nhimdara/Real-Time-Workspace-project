package com.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlockDTOs {

    @Data
    public static class CreateBlockRequest {
        private UUID id; // Optional pre-generated client UUID for optimistic UI
        private UUID parentId;
        @NotBlank(message = "Block type is required")
        private String type = "paragraph";
        private Map<String, Object> content;
        private Integer position;
    }

    @Data
    public static class UpdateBlockRequest {
        private UUID parentId;
        private String type;
        private Map<String, Object> content;
        private Integer position;
    }

    @Data
    public static class BlockReorderItem {
        private UUID id;
        private UUID parentId;
        private int position;
    }

    @Data
    public static class BatchBlockMoveRequest {
        private List<BlockReorderItem> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockDTO {
        private UUID id;
        private UUID pageId;
        private UUID parentId;
        private String type;
        private Map<String, Object> content;
        private int position;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
