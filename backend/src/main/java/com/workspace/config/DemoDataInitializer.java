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
        // Demo data seeding is best-effort: a failure here must never prevent the
        // application from starting, otherwise auth endpoints (login/register) break.
        try {
            seedDemoData();
        } catch (Exception ex) {
            log.warn("Demo data seeding skipped due to error: {}", ex.getMessage());
        }
    }

    private void seedDemoData() {
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
            log.info("Database already initialized; skipping demo seed.");
            return;
        }

        log.info("Seeding initial demo workspace data...");

        // 1. Users (let the DB generate UUIDs; keep the returned managed references)
        User alex = userRepository.save(User.builder()
                .email("alex@workspace.io")
                .password(passwordEncoder.encode("password123"))
                .name("Alex Morgan")
                .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop&crop=faces")
                .build());
        User sarah = userRepository.save(User.builder()
                .email("sarah@workspace.io")
                .password(passwordEncoder.encode("password123"))
                .name("Sarah Chen")
                .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?w=100&h=100&fit=crop&crop=faces")
                .build());
        User marcus = userRepository.save(User.builder()
                .email("marcus@workspace.io")
                .password(passwordEncoder.encode("password123"))
                .name("Marcus Vance")
                .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=faces")
                .build());

        // 2. Workspace (owner must already be persisted)
        Workspace workspace = workspaceRepository.save(Workspace.builder()
                .name("Acme Engineering & Design")
                .slug("acme-engineering")
                .owner(alex)
                .build());

        // 3. Workspace members
        workspaceMemberRepository.save(WorkspaceMember.builder().workspace(workspace).user(alex).role("OWNER").build());
        workspaceMemberRepository.save(WorkspaceMember.builder().workspace(workspace).user(sarah).role("MEMBER").build());
        workspaceMemberRepository.save(WorkspaceMember.builder().workspace(workspace).user(marcus).role("MEMBER").build());

        // 4. Pages: document + kanban board
        Page docPage = pageRepository.save(Page.builder()
                .workspace(workspace).title("Product Roadmap & Architecture Spec").icon("🚀").isKanban(false).position(0).build());
        Page kanbanPage = pageRepository.save(Page.builder()
                .workspace(workspace).title("Sprint Execution Board").icon("📊").isKanban(true).position(1).build());

        // 5. Document blocks
        blockRepository.save(Block.builder().page(docPage).type("heading_1")
                .content(content("text", "Real-Time Workspace Architecture")).position(0).build());
        blockRepository.save(Block.builder().page(docPage).type("callout")
                .content(content("text", "Welcome to your real-time collaborative workspace. Type / anywhere to trigger the command palette.", "icon", "💡")).position(1).build());
        blockRepository.save(Block.builder().page(docPage).type("heading_2")
                .content(content("text", "Key Highlights & Objectives")).position(2).build());
        blockRepository.save(Block.builder().page(docPage).type("paragraph")
                .content(content("text", "This hybrid platform combines document block editing with interactive Trello Kanban boards, backed by Spring Boot 3 WebSocket STOMP broadcasting.")).position(3).build());
        blockRepository.save(Block.builder().page(docPage).type("todo")
                .content(content("text", "Implement WebSocket STOMP broadcast handler", "checked", true)).position(4).build());
        blockRepository.save(Block.builder().page(docPage).type("todo")
                .content(content("text", "Add VueDraggable Kanban drag-and-drop state sync", "checked", true)).position(5).build());

        // 6. Kanban columns (capture generated ids for card parent references)
        Block backlog = blockRepository.save(Block.builder().page(kanbanPage).type("kanban_column")
                .content(content("title", "Backlog", "color", "slate")).position(0).build());
        Block inProgress = blockRepository.save(Block.builder().page(kanbanPage).type("kanban_column")
                .content(content("title", "In Progress", "color", "amber")).position(1).build());
        Block review = blockRepository.save(Block.builder().page(kanbanPage).type("kanban_column")
                .content(content("title", "Review", "color", "purple")).position(2).build());
        Block completed = blockRepository.save(Block.builder().page(kanbanPage).type("kanban_column")
                .content(content("title", "Completed", "color", "emerald")).position(3).build());

        // 7. Kanban cards (parentId references the persisted column ids)
        blockRepository.save(Block.builder().page(kanbanPage).parentId(backlog.getId()).type("kanban_card")
                .content(content("title", "Integrate AI summary slash command", "description", "Add LLM prompt integration for workspace pages", "priority", "medium", "assignee", "Alex Morgan", "tags", List.of("feature", "ai"))).position(0).build());
        blockRepository.save(Block.builder().page(kanbanPage).parentId(backlog.getId()).type("kanban_card")
                .content(content("title", "Add exported PDF / Markdown downloads", "description", "Generate clean documents from block trees", "priority", "low", "assignee", "Sarah Chen", "tags", List.of("export"))).position(1).build());
        blockRepository.save(Block.builder().page(kanbanPage).parentId(inProgress.getId()).type("kanban_card")
                .content(content("title", "Optimize WebSocket cursor throttle", "description", "Use 50ms requestAnimationFrame throttling for smooth cursors", "priority", "high", "assignee", "Marcus Vance", "tags", List.of("performance"))).position(0).build());
        blockRepository.save(Block.builder().page(kanbanPage).parentId(review.getId()).type("kanban_card")
                .content(content("title", "JWT Auth & Security Filter Chain", "description", "Stateless token validation with Spring Security 6", "priority", "high", "assignee", "Sarah Chen", "tags", List.of("security"))).position(0).build());
        blockRepository.save(Block.builder().page(kanbanPage).parentId(completed.getId()).type("kanban_card")
                .content(content("title", "Vue 3 Composition API & Pinia setup", "description", "Configure reactive store with optimistic updates", "priority", "high", "assignee", "Alex Morgan", "tags", List.of("frontend"))).position(0).build());

        log.info("Demo data seeding completed successfully!");
    }

    private Map<String, Object> content(Object... kv) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }
}
