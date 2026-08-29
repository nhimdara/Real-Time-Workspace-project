-- V2__seed_demo_data.sql
-- Seed Demo Users (passwords are BCrypt hash of "password123": $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG)

INSERT INTO users (id, email, password, name, avatar_url)
VALUES 
    ('a0000000-0000-0000-0000-000000000001', 'alex@workspace.io', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Alex Morgan', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop&crop=faces'),
    ('a0000000-0000-0000-0000-000000000002', 'sarah@workspace.io', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Sarah Chen', 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=100&h=100&fit=crop&crop=faces'),
    ('a0000000-0000-0000-0000-000000000003', 'marcus@workspace.io', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Marcus Vance', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=faces')
ON CONFLICT (id) DO NOTHING;

-- Seed Workspace
INSERT INTO workspaces (id, name, slug, owner_id)
VALUES 
    ('b0000000-0000-0000-0000-000000000001', 'Acme Engineering & Design', 'acme-engineering', 'a0000000-0000-0000-0000-000000000001')
ON CONFLICT (id) DO NOTHING;

-- Seed Workspace Members
INSERT INTO workspace_members (id, workspace_id, user_id, role)
VALUES 
    ('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'OWNER'),
    ('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002', 'MEMBER'),
    ('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003', 'MEMBER')
ON CONFLICT (id) DO NOTHING;

-- Seed Pages: 1 Notion Document & 1 Kanban Board
INSERT INTO pages (id, workspace_id, parent_page_id, title, icon, is_kanban, position)
VALUES 
    ('d0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', NULL, 'Product Roadmap & Architecture Spec', '🚀', FALSE, 0),
    ('d0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', NULL, 'Sprint Execution Board', '📊', TRUE, 1)
ON CONFLICT (id) DO NOTHING;

-- Seed Blocks for Document Page (d0000000-0000-0000-0000-000000000001)
INSERT INTO blocks (id, page_id, parent_id, type, content, position)
VALUES 
    ('e0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', NULL, 'heading_1', '{"text": "Real-Time Workspace Architecture"}', 0),
    ('e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', NULL, 'callout', '{"text": "Welcome to your real-time collaborative workspace. Type / anywhere to trigger the quick command palette.", "icon": "💡"}', 1),
    ('e0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001', NULL, 'heading_2', '{"text": "Key Highlights & Objectives"}', 2),
    ('e0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001', NULL, 'paragraph', '{"text": "This hybrid platform combines document block editing with interactive Trello Kanban boards, backed by Spring Boot 3 WebSocket STOMP broadcasting and Vue 3 reactive state."}', 3),
    ('e0000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000001', NULL, 'todo', '{"text": "Implement WebSocket STOMP broadcast handler", "checked": true}', 4),
    ('e0000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000001', NULL, 'todo', '{"text": "Add VueDraggable Kanban drag-and-drop state sync", "checked": true}', 5),
    ('e0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000001', NULL, 'todo', '{"text": "Multi-user live cursor rendering overlay", "checked": false}', 6),
    ('e0000000-0000-0000-0000-000000000008', 'd0000000-0000-0000-0000-000000000001', NULL, 'code', '{"text": "// Spring Boot STOMP page update broadcast\n@MessageMapping(\"/page/{pageId}/update\")\npublic void handlePageEvent(@DestinationVariable UUID pageId, RealtimeEventDTO event) {\n    messagingTemplate.convertAndSend(\"/topic/page/\" + pageId, event);\n}", "language": "java"}', 7)
ON CONFLICT (id) DO NOTHING;

-- Seed Blocks for Kanban Board Page (d0000000-0000-0000-0000-000000000002)
-- Columns (parentId = NULL, type = 'kanban_column')
INSERT INTO blocks (id, page_id, parent_id, type, content, position)
VALUES 
    ('f0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000002', NULL, 'kanban_column', '{"title": "Backlog", "color": "slate"}', 0),
    ('f0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000002', NULL, 'kanban_column', '{"title": "In Progress", "color": "amber"}', 1),
    ('f0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000002', NULL, 'kanban_column', '{"title": "Review", "color": "purple"}', 2),
    ('f0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000002', NULL, 'kanban_column', '{"title": "Completed", "color": "emerald"}', 3)
ON CONFLICT (id) DO NOTHING;

-- Cards (parentId = Column Block ID, type = 'kanban_card')
INSERT INTO blocks (id, page_id, parent_id, type, content, position)
VALUES 
    ('f0000000-0000-0000-0000-000000000011', 'd0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000001', 'kanban_card', '{"title": "Integrate AI summary slash command", "description": "Add LLM prompt integration for workspace pages", "priority": "medium", "tags": ["feature", "ai"], "assignee": "Alex Morgan"}', 0),
    ('f0000000-0000-0000-0000-000000000012', 'd0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000001', 'kanban_card', '{"title": "Add exported PDF / Markdown downloads", "description": "Generate clean documents from block trees", "priority": "low", "tags": ["export"], "assignee": "Sarah Chen"}', 1),
    ('f0000000-0000-0000-0000-000000000013', 'd0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000002', 'kanban_card', '{"title": "Optimize WebSocket cursor throttle", "description": "Use 50ms requestAnimationFrame throttling for buttery smooth cursors", "priority": "high", "tags": ["performance"], "assignee": "Marcus Vance"}', 0),
    ('f0000000-0000-0000-0000-000000000014', 'd0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000002', 'kanban_card', '{"title": "PostgreSQL JSONB GIN index benchmarks", "description": "Test sub-millisecond query performance on nested JSON properties", "priority": "medium", "tags": ["database"], "assignee": "Alex Morgan"}', 1),
    ('f0000000-0000-0000-0000-000000000015', 'd0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000003', 'kanban_card', '{"title": "JWT Auth & Security Filter Chain", "description": "Stateless token validation with Spring Security 6 & JJWT", "priority": "high", "tags": ["security"], "assignee": "Sarah Chen"}', 0),
    ('f0000000-0000-0000-0000-000000000016', 'd0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000004', 'kanban_card', '{"title": "Vue 3 Composition API & Pinia setup", "description": "Configure reactive store with optimistic mutation updates", "priority": "high", "tags": ["frontend"], "assignee": "Alex Morgan"}', 0)
ON CONFLICT (id) DO NOTHING;
