package com.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class WorkspaceDTOs {

    @Data
    public static class CreateWorkspaceRequest {
        @NotBlank(message = "Workspace name is required")
        @Size(max = 150)
        private String name;

        @NotBlank(message = "Workspace slug is required")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase alphanumeric characters and hyphens")
        @Size(max = 150)
        private String slug;
    }

    @Data
    public static class InviteMemberRequest {
        @NotBlank(message = "Email is required")
        private String email;

        private String role = "MEMBER";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkspaceDTO {
        private UUID id;
        private String name;
        private String slug;
        private UUID ownerId;
        private String role; // Current user's role in this workspace
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkspaceMemberDTO {
        private UUID id;
        private UUID workspaceId;
        private UserDTO user;
        private String role;
        private Instant createdAt;
    }
}
