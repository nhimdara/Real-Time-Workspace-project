package com.workspace.controller;

import com.workspace.dto.PageDTOs;
import com.workspace.security.SecurityUtils;
import com.workspace.service.PageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @GetMapping("/workspaces/{workspaceId}/pages")
    public ResponseEntity<List<PageDTOs.PageDTO>> getWorkspacePages(@PathVariable UUID workspaceId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(pageService.getWorkspacePages(workspaceId, userId));
    }

    @GetMapping("/pages/{pageId}")
    public ResponseEntity<PageDTOs.PageDTO> getPage(@PathVariable UUID pageId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(pageService.getPageById(pageId, userId));
    }

    @PostMapping("/workspaces/{workspaceId}/pages")
    public ResponseEntity<PageDTOs.PageDTO> createPage(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody PageDTOs.CreatePageRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(pageService.createPage(workspaceId, request, userId));
    }

    @PutMapping("/pages/{pageId}")
    public ResponseEntity<PageDTOs.PageDTO> updatePage(
            @PathVariable UUID pageId,
            @RequestBody PageDTOs.UpdatePageRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(pageService.updatePage(pageId, request, userId));
    }

    @DeleteMapping("/pages/{pageId}")
    public ResponseEntity<Void> deletePage(@PathVariable UUID pageId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        pageService.deletePage(pageId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/workspaces/{workspaceId}/pages/reorder")
    public ResponseEntity<List<PageDTOs.PageDTO>> reorderPage(
            @PathVariable UUID workspaceId,
            @RequestBody PageDTOs.ReorderPageRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(pageService.reorderPage(workspaceId, request, userId));
    }
}
