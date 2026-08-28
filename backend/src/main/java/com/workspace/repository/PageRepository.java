package com.workspace.repository;

import com.workspace.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PageRepository extends JpaRepository<Page, UUID> {
    List<Page> findByWorkspaceIdOrderByPositionAsc(UUID workspaceId);
    List<Page> findByWorkspaceIdAndParentPageIdOrderByPositionAsc(UUID workspaceId, UUID parentPageId);

    @Query("SELECT COALESCE(MAX(p.position), -1) + 1 FROM Page p WHERE p.workspace.id = :workspaceId")
    int getNextPositionForWorkspace(@Param("workspaceId") UUID workspaceId);
}
