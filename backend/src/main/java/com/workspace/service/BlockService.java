package com.workspace.service;

import com.workspace.dto.BlockDTOs;
import com.workspace.entity.Block;
import com.workspace.entity.Page;
import com.workspace.exception.Exceptions;
import com.workspace.repository.BlockRepository;
import com.workspace.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockService {

    private final BlockRepository blockRepository;
    private final PageRepository pageRepository;
    private final WorkspaceService workspaceService;

    @Transactional(readOnly = true)
    public List<BlockDTOs.BlockDTO> getBlocksForPage(UUID pageId, UUID userId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Page not found"));

        workspaceService.validateUserAccess(page.getWorkspace().getId(), userId);

        return blockRepository.findByPageIdOrderByPositionAsc(pageId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BlockDTOs.BlockDTO createBlock(UUID pageId, BlockDTOs.CreateBlockRequest request, UUID userId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Page not found"));

        workspaceService.validateUserAccess(page.getWorkspace().getId(), userId);

        int position = request.getPosition() != null
                ? request.getPosition()
                : blockRepository.getNextPosition(pageId, request.getParentId());

        Block block = Block.builder()
                .id(request.getId() != null ? request.getId() : UUID.randomUUID())
                .page(page)
                .parentId(request.getParentId())
                .type(request.getType() != null ? request.getType() : "paragraph")
                .content(request.getContent() != null ? request.getContent() : new HashMap<>())
                .position(position)
                .build();

        block = blockRepository.save(block);
        return mapToDTO(block);
    }

    @Transactional
    public BlockDTOs.BlockDTO updateBlock(UUID blockId, BlockDTOs.UpdateBlockRequest request, UUID userId) {
        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Block not found"));

        workspaceService.validateUserAccess(block.getPage().getWorkspace().getId(), userId);

        if (request.getType() != null) {
            block.setType(request.getType());
        }
        if (request.getContent() != null) {
            block.setContent(request.getContent());
        }
        if (request.getParentId() != null || (request.getParentId() == null && request.getType() != null && request.getType().equals("kanban_column"))) {
            block.setParentId(request.getParentId());
        }
        if (request.getPosition() != null) {
            block.setPosition(request.getPosition());
        }

        block = blockRepository.save(block);
        return mapToDTO(block);
    }

    @Transactional
    public void deleteBlock(UUID blockId, UUID userId) {
        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Block not found"));

        workspaceService.validateUserAccess(block.getPage().getWorkspace().getId(), userId);

        // Delete children blocks recursively
        blockRepository.deleteByParentId(blockId);
        blockRepository.delete(block);
    }

    @Transactional
    public List<BlockDTOs.BlockDTO> batchMoveBlocks(UUID pageId, BlockDTOs.BatchBlockMoveRequest request, UUID userId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Page not found"));

        workspaceService.validateUserAccess(page.getWorkspace().getId(), userId);

        List<Block> updatedBlocks = new ArrayList<>();
        for (BlockDTOs.BlockReorderItem item : request.getItems()) {
            Optional<Block> blockOpt = blockRepository.findById(item.getId());
            if (blockOpt.isPresent()) {
                Block block = blockOpt.get();
                block.setParentId(item.getParentId());
                block.setPosition(item.getPosition());
                updatedBlocks.add(block);
            }
        }

        blockRepository.saveAll(updatedBlocks);
        return getBlocksForPage(pageId, userId);
    }

    public BlockDTOs.BlockDTO mapToDTO(Block block) {
        return BlockDTOs.BlockDTO.builder()
                .id(block.getId())
                .pageId(block.getPage().getId())
                .parentId(block.getParentId())
                .type(block.getType())
                .content(block.getContent())
                .position(block.getPosition())
                .createdAt(block.getCreatedAt())
                .updatedAt(block.getUpdatedAt())
                .build();
    }
}
