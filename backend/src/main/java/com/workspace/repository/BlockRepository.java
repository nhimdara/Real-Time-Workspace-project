package com.workspace.repository;

import com.workspace.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BlockRepository extends JpaRepository<Block, UUID> {
    List<Block> findByPageIdOrderByPositionAsc(UUID pageId);
    List<Block> findByPageIdAndParentIdOrderByPositionAsc(UUID pageId, UUID parentId);

    @Query("SELECT COALESCE(MAX(b.position), -1) + 1 FROM Block b WHERE b.page.id = :pageId AND (:parentId IS NULL AND b.parentId IS NULL OR b.parentId = :parentId)")
    int getNextPosition(@Param("pageId") UUID pageId, @Param("parentId") UUID parentId);

    @Modifying
    @Query("DELETE FROM Block b WHERE b.page.id = :pageId")
    void deleteByPageId(@Param("pageId") UUID pageId);

    @Modifying
    @Query("DELETE FROM Block b WHERE b.parentId = :parentId")
    void deleteByParentId(@Param("parentId") UUID parentId);
}
