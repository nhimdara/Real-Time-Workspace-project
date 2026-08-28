package com.workspace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "parent_page_id")
    private UUID parentPageId;

    @Column(nullable = false)
    @Builder.Default
    private String title = "Untitled";

    @Column(length = 50)
    @Builder.Default
    private String icon = "📄";

    @Column(name = "is_kanban", nullable = false)
    @Builder.Default
    private boolean isKanban = false;

    @Column(nullable = false)
    @Builder.Default
    private int position = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
