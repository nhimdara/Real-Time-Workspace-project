package com.workspace.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "blocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String type = "paragraph";

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> content = new HashMap<>();

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
