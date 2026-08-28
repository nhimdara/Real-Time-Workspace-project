package com.workspace.service;

import com.workspace.dto.PageDTOs;
import com.workspace.entity.Page;
import com.workspace.entity.Workspace;
import com.workspace.exception.Exceptions;
import com.workspace.repository.BlockRepository;
import com.workspace.repository.PageRepository;
import com.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final WorkspaceRepository workspaceRepository;
    private final BlockRepository blockRepository;
    private final WorkspaceService workspaceService;

    @Transactional(readOnly = true)
    public List<PageDTOs.PageDTO> getWorkspacePages(UUID workspaceId, UUID userId) {
        workspaceService.validateUserAccess(workspaceId, userId);
        return pageRepository.findByWorkspaceIdOrderByPositionAsc(workspaceId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageDTOs.PageDTO getPageById(UUID pageId, UUID userId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Page not found"));

        workspaceService.validateUserAccess(page.getWorkspace().getId(), userId);
        return mapToDTO(page);
    }

    @Transactional
    public PageDTOs.PageDTO createPage(UUID workspaceId, PageDTOs.CreatePageRequest request, UUID userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Workspace not found"));

        workspaceService.validateUserAccess(workspaceId, userId);

        int nextPosition = pageRepository.getNextPositionForWorkspace(workspaceId);

        Page page = Page.builder()
                .workspace(workspace)
                .parentPageId(request.getParentPageId())
                .title(request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle() : "Untitled")
                .icon(request.getIcon() != null ? request.getIcon() : (request.isKanban() ? "📊" : "📄"))
                .isKanban(request.isKanban())
                .position(nextPosition)
                .build();

        page = pageRepository.save(page);
        return mapToDTO(page);
    }

    @Transactional
    public PageDTOs.PageDTO updatePage(UUID pageId, PageDTOs.UpdatePageRequest request, UUID userId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Page not found"));

        workspaceService.validateUserAccess(page.getWorkspace().getId(), userId);

        if (request.getTitle() != null) {
            page.setTitle(request.getTitle());
        }
        if (request.getIcon() != null) {
            page.setIcon(request.getIcon());
        }
        if (request.getIsKanban() != null) {
            page.setKanban(request.getIsKanban());
        }
        if (request.getParentPageId() != null) {
            page.setParentPageId(request.getParentPageId());
        }
        if (request.getPosition() != null) {
            page.setPosition(request.getPosition());
        }

        page = pageRepository.save(page);
        return mapToDTO(page);
    }

    @Transactional
    public void deletePage(UUID pageId, UUID userId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Page not found"));

        workspaceService.validateUserAccess(page.getWorkspace().getId(), userId);
        pageRepository.delete(page);
    }

    @Transactional
    public List<PageDTOs.PageDTO> reorderPage(UUID workspaceId, PageDTOs.ReorderPageRequest request, UUID userId) {
        workspaceService.validateUserAccess(workspaceId, userId);

        Page page = pageRepository.findById(request.getPageId())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Page not found"));

        page.setPosition(request.getNewPosition());
        if (request.getNewParentPageId() != null) {
            page.setParentPageId(request.getNewParentPageId());
        }
        pageRepository.save(page);

        return getWorkspacePages(workspaceId, userId);
    }

    public PageDTOs.PageDTO mapToDTO(Page page) {
        return PageDTOs.PageDTO.builder()
                .id(page.getId())
                .workspaceId(page.getWorkspace().getId())
                .parentPageId(page.getParentPageId())
                .title(page.getTitle())
                .icon(page.getIcon())
                .isKanban(page.isKanban())
                .position(page.getPosition())
                .createdAt(page.getCreatedAt())
                .updatedAt(page.getUpdatedAt())
                .build();
    }
}
