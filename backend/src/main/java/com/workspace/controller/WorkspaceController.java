package com.workspace.controller;

import com.workspace.dto.WorkspaceDTOs;
import com.workspace.entity.User;
import com.workspace.security.SecurityUtils;
import com.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping
    public ResponseEntity<List<WorkspaceDTOs.WorkspaceDTO>> getWorkspaces() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(workspaceService.getUserWorkspaces(userId));
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceDTOs.WorkspaceDTO> getWorkspace(@PathVariable UUID workspaceId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(workspaceService.getWorkspaceById(workspaceId, userId));
    }

    @PostMapping
    public ResponseEntity<WorkspaceDTOs.WorkspaceDTO> createWorkspace(@Valid @RequestBody WorkspaceDTOs.CreateWorkspaceRequest request) {
        User user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(workspaceService.createWorkspace(request, user));
    }

    @PutMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceDTOs.WorkspaceDTO> updateWorkspace(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody WorkspaceDTOs.CreateWorkspaceRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(workspaceService.updateWorkspace(workspaceId, request, userId));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable UUID workspaceId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        workspaceService.deleteWorkspace(workspaceId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<List<WorkspaceDTOs.WorkspaceMemberDTO>> getMembers(@PathVariable UUID workspaceId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(workspaceService.getWorkspaceMembers(workspaceId, userId));
    }

    @PostMapping("/{workspaceId}/invite")
    public ResponseEntity<WorkspaceDTOs.WorkspaceMemberDTO> inviteMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody WorkspaceDTOs.InviteMemberRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(workspaceService.inviteMember(workspaceId, request, userId));
    }
}
