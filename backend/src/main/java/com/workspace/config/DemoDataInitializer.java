package com.workspace.config;

import com.workspace.entity.Block;
import com.workspace.entity.Page;
import com.workspace.entity.User;
import com.workspace.entity.Workspace;
import com.workspace.entity.WorkspaceMember;
import com.workspace.repository.BlockRepository;
import com.workspace.repository.PageRepository;
import com.workspace.repository.UserRepository;
import com.workspace.repository.WorkspaceMemberRepository;
import com.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final PageRepository pageRepository;
    private final BlockRepository blockRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Ensure demo users exist or have their passwords synced to password123
        List<String> demoEmails = List.of("alex@workspace.io", "sarah@workspace.io", "marcus@workspace.io");
        for (String email : demoEmails) {
            userRepository.findByEmail(email).ifPresent(u -> {
                u.setPassword(passwordEncoder.encode("password123"));
                userRepository.save(u);
                log.info("Verified and updated demo user password for: {}", email);
            });
        }

        if (userRepository.count() > 0 && workspaceRepository.count() > 0) {
            log.info("Database initialized.");
            return;
        }

        log.info("Seeding initial demo workspace data...");

        // 1. Create Users
        User alex = User.builder()
                .id(UUID.fromString("a0000000-0000-0000-0000-000000000001"))
                .email("alex@workspace.io")
                .password(passwordEncoder.encode("password123"))
                .name("Alex Morgan")
                .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop&crop=faces")
                .build();

        User sarah = User.builder()
                .id(UUID.fromString("a0000000-0000-0000-0000-000000000002"))
                .email("sarah@workspace.io")
                .password(passwordEncoder.encode("password123"))
                .name("Sarah Chen")
                .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?w=100&h=100&fit=crop&crop=faces")
                .build();

        User marcus = User.builder()
                .id(UUID.fromString("a0000000-0000-0000-0000-000000000003"))
                .email("marcus@workspace.io")
                .password(passwordEncoder.encode("password123"))
                .name("Marcus Vance")
                .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=faces")
                .build();

        userRepository.saveAll(List.of(alex, sarah, marcus));

        // 2. Create Workspace
        Workspace workspace = Workspace.builder()
                .id(UUID.fromString("b0000000-0000-0000-0000-000000000001"))
                .name("Acme Engineering & Design")
                .slug("acme-engineering")
                .owner(alex)
                .build();
        workspaceRepository.save(workspace);

        // 3. Workspace Members
        workspaceMemberRepository.save(WorkspaceMember.builder().workspace(workspace).user(alex).role("OWNER").build());
        workspaceMemberRepository.save(WorkspaceMember.builder().workspace(workspace).user(sarah).role("MEMBER").build());
        workspaceMemberRepository.save(WorkspaceMember.builder().workspace(workspace).user(marcus).role("MEMBER").build());

        // 4. Pages: Document & Kanban Board
        Page docPage = Page.builder()
                .id(UUID.fromString("d0000000-0000-0000-0000-000000000001"))
                .workspace(workspace)
                .title("Product Roadmap & Architecture Spec")
                .icon("🚀")
                .isKanban(false)
                .position(0)
                .build();

        Page kanbanPage = Page.builder()
                .id(UUID.fromString("d0000000-0000-0000-0000-000000000002"))
                .workspace(workspace)
                .title("Sprint Execution Board")
                .icon("📊")
                .isKanban(true)
                .position(1)
                .build();

        pageRepository.saveAll(List.of(docPage, kanbanPage));

        // 5. Document Blocks
        blockRepository.save(Block.builder()
                .id(UUID.fromString("e0000000-0000-0000-0000-000000000001"))
                .page(docPage)
                .type("heading_1")
                .content(new HashMap<>(Map.of("text", "Real-Time Workspace Architecture")))
                .position(0)
                .build());

        blockRepository.save(Block.builder()
                .id(UUID.fromString("e0000000-0000-0000-0000-000000000002"))
                .page(docPage)
                .type("callout")
                .content(new HashMap<>(Map.of("text", "Welcome to your real-time collaborative workspace. Type / anywhere to trigger the command palette.", "icon", "💡")))
                .position(1)
                .build());

        blockRepository.save(Block.builder()
                .id(UUID.fromString("e0000000-0000-0000-0000-000000000003"))
                .page(docPage)
                .type("heading_2")
                .content(new HashMap<>(Map.of("text", "Key Highlights & Objectives")))
                .position(2)
                .build());

        blockRepository.save(Block.builder()
                .id(UUID.fromString("e0000000-0000-0000-0000-000000000004"))
                .page(docPage)
                .type("paragraph")
                .content(new HashMap<>(Map.of("text", "This hybrid platform combines document block editing with interactive Trello Kanban boards, backed by Spring Boot 3 WebSocket STOMP broadcasting.")))
                .position(3)
                .build());

        blockRepository.save(Block.builder()
                .id(UUID.fromString("e0000000-0000-0000-0000-000000000005"))
                .page(docPage)
                .type("todo")
                .content(new HashMap<>(Map.of("text", "Implement WebSocket STOMP broadcast handler", "checked", true)))
                .position(4)
                .build());

        blockRepository.save(Block.builder()
                .id(UUID.fromString("e0000000-0000-0000-0000-000000000006"))
                .page(docPage)
                .type("todo")
                .content(new HashMap<>(Map.of("text", "Add VueDraggable Kanban drag-and-drop state sync", "checked", true)))
                .position(5)
                .build());

        blockRepository.save(Block.builder()
                .id(UUID.fromString("e0000000-0000-0000-0000-000000000007"))
                .page(docPage)
                .type("code")
                .content(new HashMap<>(Map.of("text", "// Spring Boot STOMP broadcast handler\n@MessageMapping(\"/page/{pageId}/update\")\npublic void handlePageEvent(@DestinationVariable UUID pageId, RealtimeEventDTO event) {\n    messagingTemplate.convertAndSend(\"/topic/page/\" + pageId, event);\n}", "language", "java")))
                .position(6)
                .build());

        // 6. Kanban Columns & Cards
        UUID col1 = UUID.fromString("f0000000-0000-0000-0000-000000000001");
        UUID col2 = UUID.fromString("f0000000-0000-0000-0000-000000000002");
        UUID col3 = UUID.fromString("f0000000-0000-0000-0000-000000000003");
        UUID col4 = UUID.fromString("f0000000-0000-0000-0000-000000000004");

        blockRepository.save(Block.builder().id(col1).page(kanbanPage).type("kanban_column").content(new HashMap<>(Map.of("title", "Backlog", "color", "slate"))).position(0).build());
        blockRepository.save(Block.builder().id(col2).page(kanbanPage).type("kanban_column").content(new HashMap<>(Map.of("title", "In Progress", "color", "amber"))).position(1).build());
        blockRepository.save(Block.builder().id(col3).page(kanbanPage).type("kanban_column").content(new HashMap<>(Map.of("title", "Review", "color", "purple"))).position(2).build());
        blockRepository.save(Block.builder().id(col4).page(kanbanPage).type("kanban_column").content(new HashMap<>(Map.of("title", "Completed", "color", "emerald"))).position(3).build());

        // Kanban Cards
        blockRepository.save(Block.builder().id(UUID.fromString("f0000000-0000-0000-0000-000000000011")).page(kanbanPage).parentId(col1).type("kanban_card").content(new HashMap<>(Map.of("title", "Integrate AI summary slash command", "description", "Add LLM prompt integration for workspace pages", "priority", "medium", "assignee", "Alex Morgan", "tags", List.of("feature", "ai")))).position(0).build());
        blockRepository.save(Block.builder().id(UUID.fromString("f0000000-0000-0000-0000-000000000012")).page(kanbanPage).parentId(col1).type("kanban_card").content(new HashMap<>(Map.of("title", "Add exported PDF / Markdown downloads", "description", "Generate clean documents from block trees", "priority", "low", "assignee", "Sarah Chen", "tags", List.of("export")))).position(1).build());
        blockRepository.save(Block.builder().id(UUID.fromString("f0000000-0000-0000-0000-000000000013")).page(kanbanPage).parentId(col2).type("kanban_card").content(new HashMap<>(Map.of("title", "Optimize WebSocket cursor throttle", "description", "Use 50ms requestAnimationFrame throttling for smooth cursors", "priority", "high", "assignee", "Marcus Vance", "tags", List.of("performance")))).position(0).build());
        blockRepository.save(Block.builder().id(UUID.fromString("f0000000-0000-0000-0000-000000000014")).page(kanbanPage).parentId(col2).type("kanban_card").content(new HashMap<>(Map.of("title", "PostgreSQL JSONB GIN index benchmarks", "description", "Test sub-millisecond query performance on nested JSON", "priority", "medium", "assignee", "Alex Morgan", "tags", List.of("database")))).position(1).build());
        blockRepository.save(Block.builder().id(UUID.fromString("f0000000-0000-0000-0000-000000000015")).page(kanbanPage).parentId(col3).type("kanban_card").content(new HashMap<>(Map.of("title", "JWT Auth & Security Filter Chain", "description", "Stateless token validation with Spring Security 6", "priority", "high", "assignee", "Sarah Chen", "tags", List.of("security")))).position(0).build());
        blockRepository.save(Block.builder().id(UUID.fromString("f0000000-0000-0000-0000-000000000016")).page(kanbanPage).parentId(col4).type("kanban_card").content(new HashMap<>(Map.of("title", "Vue 3 Composition API & Pinia setup", "description", "Configure reactive store with optimistic updates", "priority", "high", "assignee", "Alex Morgan", "tags", List.of("frontend")))).position(0).build());

        log.info("Demo data seeding completed successfully!");
    }
}
