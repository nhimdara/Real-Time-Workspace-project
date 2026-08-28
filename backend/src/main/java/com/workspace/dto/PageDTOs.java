package com.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

public class PageDTOs {

    @Data
    public static class CreatePageRequest {
        @NotBlank(message = "Title is required")
        private String title;
        private UUID parentPageId;
        private String icon = "📄";
        private boolean isKanban = false;
    }

    @Data
    public static class UpdatePageRequest {
        private String title;
        private UUID parentPageId;
        private String icon;
        private Boolean isKanban;
        private Integer position;
    }

    @Data
    public static class ReorderPageRequest {
        private UUID pageId;
        private int newPosition;
        private UUID newParentPageId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageDTO {
        private UUID id;
        private UUID workspaceId;
        private UUID parentPageId;
        private String title;
        private String icon;
        private boolean isKanban;
        private int position;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
