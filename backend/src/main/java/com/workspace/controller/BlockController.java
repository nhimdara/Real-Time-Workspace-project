package com.workspace.controller;

import com.workspace.dto.BlockDTOs;
import com.workspace.security.SecurityUtils;
import com.workspace.service.BlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @GetMapping("/pages/{pageId}/blocks")
    public ResponseEntity<List<BlockDTOs.BlockDTO>> getBlocks(@PathVariable UUID pageId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(blockService.getBlocksForPage(pageId, userId));
    }

    @PostMapping("/pages/{pageId}/blocks")
    public ResponseEntity<BlockDTOs.BlockDTO> createBlock(
            @PathVariable UUID pageId,
            @Valid @RequestBody BlockDTOs.CreateBlockRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(blockService.createBlock(pageId, request, userId));
    }

    @PutMapping("/blocks/{blockId}")
    public ResponseEntity<BlockDTOs.BlockDTO> updateBlock(
            @PathVariable UUID blockId,
            @RequestBody BlockDTOs.UpdateBlockRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(blockService.updateBlock(blockId, request, userId));
    }

    @DeleteMapping("/blocks/{blockId}")
    public ResponseEntity<Void> deleteBlock(@PathVariable UUID blockId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        blockService.deleteBlock(blockId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/pages/{pageId}/blocks/batch-move")
    public ResponseEntity<List<BlockDTOs.BlockDTO>> batchMoveBlocks(
            @PathVariable UUID pageId,
            @RequestBody BlockDTOs.BatchBlockMoveRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(blockService.batchMoveBlocks(pageId, request, userId));
    }
}
