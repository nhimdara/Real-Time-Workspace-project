package com.workspace.service;

import com.workspace.dto.UserDTO;
import com.workspace.dto.WorkspaceDTOs;
import com.workspace.entity.Page;
import com.workspace.entity.User;
import com.workspace.entity.Workspace;
import com.workspace.entity.WorkspaceMember;
import com.workspace.exception.Exceptions;
import com.workspace.repository.PageRepository;
import com.workspace.repository.UserRepository;
import com.workspace.repository.WorkspaceMemberRepository;
import com.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final PageRepository pageRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<WorkspaceDTOs.WorkspaceDTO> getUserWorkspaces(UUID userId) {
        List<Workspace> workspaces = workspaceRepository.findWorkspacesForUser(userId);
        return workspaces.stream()
                .map(w -> mapToDTO(w, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkspaceDTOs.WorkspaceDTO getWorkspaceById(UUID workspaceId, UUID userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Workspace not found"));

        validateUserAccess(workspaceId, userId);
        return mapToDTO(workspace, userId);
    }

    @Transactional
    public WorkspaceDTOs.WorkspaceDTO createWorkspace(WorkspaceDTOs.CreateWorkspaceRequest request, User owner) {
        if (workspaceRepository.existsBySlug(request.getSlug())) {
            throw new Exceptions.BadRequestException("Workspace slug already exists. Please choose another.");
        }

        Workspace workspace = Workspace.builder()
                .name(request.getName().trim())
                .slug(request.getSlug().trim().toLowerCase())
                .owner(owner)
                .build();
        workspace = workspaceRepository.save(workspace);

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(owner)
                .role("OWNER")
                .build();
        workspaceMemberRepository.save(member);

        // Create a default initial page
        Page defaultPage = Page.builder()
                .workspace(workspace)
                .title("Getting Started")
                .icon("👋")
                .isKanban(false)
                .position(0)
                .build();
        pageRepository.save(defaultPage);

        return mapToDTO(workspace, owner.getId());
    }

    @Transactional
    public WorkspaceDTOs.WorkspaceDTO updateWorkspace(UUID workspaceId, WorkspaceDTOs.CreateWorkspaceRequest request, UUID userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Workspace not found"));

        if (!workspace.getOwner().getId().equals(userId)) {
            throw new Exceptions.ForbiddenException("Only the workspace owner can update workspace details");
        }

        if (!workspace.getSlug().equalsIgnoreCase(request.getSlug()) && workspaceRepository.existsBySlug(request.getSlug())) {
            throw new Exceptions.BadRequestException("Workspace slug already in use");
        }

        workspace.setName(request.getName());
        workspace.setSlug(request.getSlug().toLowerCase());
        workspace = workspaceRepository.save(workspace);

        return mapToDTO(workspace, userId);
    }

    @Transactional
    public void deleteWorkspace(UUID workspaceId, UUID userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Workspace not found"));

        if (!workspace.getOwner().getId().equals(userId)) {
            throw new Exceptions.ForbiddenException("Only the workspace owner can delete this workspace");
        }

        workspaceRepository.delete(workspace);
    }

    @Transactional
    public WorkspaceDTOs.WorkspaceMemberDTO inviteMember(UUID workspaceId, WorkspaceDTOs.InviteMemberRequest request, UUID requestingUserId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Workspace not found"));

        validateUserAccess(workspaceId, requestingUserId);

        User invitee = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, invitee.getId())) {
            throw new Exceptions.BadRequestException("User is already a member of this workspace");
        }

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(invitee)
                .role(request.getRole() != null ? request.getRole().toUpperCase() : "MEMBER")
                .build();
        member = workspaceMemberRepository.save(member);

        return mapMemberToDTO(member);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceDTOs.WorkspaceMemberDTO> getWorkspaceMembers(UUID workspaceId, UUID userId) {
        validateUserAccess(workspaceId, userId);
        return workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapMemberToDTO)
                .collect(Collectors.toList());
    }

    public void validateUserAccess(UUID workspaceId, UUID userId) {
        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
        if (!isMember) {
            Workspace workspace = workspaceRepository.findById(workspaceId).orElse(null);
            if (workspace == null || !workspace.getOwner().getId().equals(userId)) {
                throw new Exceptions.ForbiddenException("You do not have access to this workspace");
            }
        }
    }

    public WorkspaceDTOs.WorkspaceDTO mapToDTO(Workspace workspace, UUID currentUserId) {
        String role = "MEMBER";
        if (workspace.getOwner().getId().equals(currentUserId)) {
            role = "OWNER";
        } else {
            role = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspace.getId(), currentUserId)
                    .map(WorkspaceMember::getRole)
                    .orElse("MEMBER");
        }

        return WorkspaceDTOs.WorkspaceDTO.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .slug(workspace.getSlug())
                .ownerId(workspace.getOwner().getId())
                .role(role)
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }

    private WorkspaceDTOs.WorkspaceMemberDTO mapMemberToDTO(WorkspaceMember member) {
        return WorkspaceDTOs.WorkspaceMemberDTO.builder()
                .id(member.getId())
                .workspaceId(member.getWorkspace().getId())
                .user(authService.mapToDTO(member.getUser()))
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
