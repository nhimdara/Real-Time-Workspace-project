# Real-Time Workspace Platform (Notion & Trello Hybrid)

A production-grade, collaborative workspace platform combining Notion-style block document editing with Trello-style interactive Kanban boards and multi-user live cursor synchronization. Built with **Spring Boot 3** (Java 17+), **Spring WebSocket (STOMP)**, **Spring Security JWT**, **PostgreSQL (JSONB)**, **Flyway**, and **Vue 3 (Composition API + TypeScript + Pinia + Tailwind CSS)**.

---

## 🏛️ Architecture Overview

```
                      +---------------------------------------+
                      |       Vue 3 + Vite + TypeScript       |
                      |  - Pinia Stores (auth, workspace)    |
                      |  - Notion BlockEditor & Slash Menu    |
                      |  - Trello KanbanBoard (VueDraggable)  |
                      |  - Multiplayer Live Cursors Overlay   |
                      +-------------------+-------------------+
                                          |
                   REST (JSON/JWT)        |       WebSocket (STOMP)
                   /api/v1/*              |       /ws -> /topic/page/*
                                          |
                      +-------------------v-------------------+
                      |         Spring Boot 3 Backend         |
                      |  - Stateless Spring Security (JWT)    |
                      |  - STOMP Broker & SimpMessagingTemplate|
                      |  - Hibernate 6 JSONB Block Mapping    |
                      |  - Flyway Database Migrations         |
                      +-------------------+-------------------+
                                          |
                                          v
                      +---------------------------------------+
                      |         PostgreSQL Database           |
                      |  - Users, Workspaces, Members, Pages  |
                      |  - Blocks Table (JSONB content + GIN) |
                      +---------------------------------------+
```

---

## 🚀 Key Features

### 1. Notion-Style Block Editor (`BlockEditor.vue`)
- **Block Types**: Headings (`h1`, `h2`, `h3`), Paragraphs, To-do Checklists, Bulleted Lists, Code Blocks, Callout Banners, and Dividers.
- **Slash Commands Menu (`/`)**: Type `/` to open the quick-insert palette.
- **Keyboard Shortcuts**: `Enter` creates the next block below, `Backspace` on empty deletes the block and moves focus to the previous one.
- **Drag Reordering**: Reorder blocks with drag handles via `vuedraggable`.
- **Optimistic Sync**: Instant local state mutation + debounced WebSocket broadcast & REST persistence.

### 2. Trello-Style Kanban Board (`KanbanBoard.vue`)
- **Multi-Column Drag-and-Drop**: Drag cards freely across columns or reorder cards within a column.
- **Real-Time Column & Position Sync**: Automatically updates card block `parentId` to destination column UUID and recalculates `position`.
- **Interactive Card Management**: Add columns with custom accent colors, add cards inline, edit tags, priority (`urgent`, `high`, `medium`, `low`), and assignees.
- **Card Detail Flyout**: Rich modal for detailed card management.

### 3. Multiplayer Real-Time Collaboration (`LiveCursors.vue` & STOMP)
- **Multi-User Live Cursors**: Floating cursor avatars and nametags render peer movements in real time with 50ms throttling.
- **Bi-directional STOMP Messaging**: `@MessageMapping("/page/{pageId}/update")` broadcasts `BLOCK_CREATE`, `BLOCK_UPDATE`, `BLOCK_DELETE`, `BLOCK_BATCH_MOVE`, and `CURSOR_MOVE` events to `/topic/page/{pageId}`.
- **Active Peer Presence**: Header pills display all online collaborators actively viewing the page.

### 4. Enterprise-Grade Security & Data Persistence
- **Stateless JWT Authentication**: Spring Security 6 filter chain with BCrypt password hashing.
- **Hibernate 6 JSONB Mapping**: `Block.content` is mapped to `Map<String, Object>` backed by PostgreSQL `jsonb` column with GIN indexing.
- **Flyway Migrations**: Automated schema generation (`V1__initial_schema.sql`) and seed data (`V2__seed_demo_data.sql`).

---

## 📁 Repository Structure

```
Real-Time Workspace project/
├── docker-compose.yml              # PostgreSQL container
├── README.md                       # Complete documentation
├── backend/                        # Java Spring Boot 3 backend
│   ├── pom.xml                     # Maven build configuration
│   ├── mvnw.cmd & mvnw             # Maven wrapper scripts
│   ├── .mvn/wrapper/               # Maven wrapper properties
│   └── src/main/
│       ├── java/com/workspace/
│       │   ├── WorkspaceApplication.java
│       │   ├── config/             # SecurityConfig, WebSocketConfig, Interceptors
│       │   ├── controller/         # Auth, Workspace, Page, Block, Realtime Controllers
│       │   ├── dto/                # Request/Response & Realtime Event DTOs
│       │   ├── entity/             # User, Workspace, WorkspaceMember, Page, Block (JSONB)
│       │   ├── exception/          # GlobalExceptionHandler & Error Responses
│       │   ├── repository/         # Spring Data JPA Repositories
│       │   ├── security/           # JwtService, JwtFilter, UserDetails
│       │   └── service/            # AuthService, WorkspaceService, PageService, BlockService
│       └── resources/
│           ├── application.yml     # Application configuration
│           └── db/migration/       # Flyway V1 & V2 SQL migrations
└── frontend/                       # Vue 3 + TypeScript frontend
    ├── package.json
    ├── vite.config.ts
    ├── tailwind.config.js
    ├── index.html
    └── src/
        ├── App.vue
        ├── main.ts
        ├── assets/main.css         # Glassmorphism & custom styling
        ├── types/workspace.ts      # TypeScript interfaces
        ├── services/               # Axios API client & STOMP WebSocket service
        ├── stores/                 # Pinia Auth & Workspace stores
        ├── views/WorkspaceView.vue
        └── components/
            ├── layout/WorkspaceLayout.vue
            ├── editor/BlockEditor.vue
            ├── editor/SlashMenu.vue
            ├── kanban/KanbanBoard.vue
            ├── realtime/LiveCursors.vue
            └── auth/AuthModal.vue
```

---

## 🛠️ Quick Start Guide

### Step 1: Start PostgreSQL
```bash
docker-compose up -d
```
This spins up PostgreSQL 16 on port `5432` with database `workspace_db`.

### Step 2: Run Spring Boot 3 Backend
```bash
cd backend
./mvnw spring-boot:run
```
*(On Windows cmd/powershell, run `mvnw.cmd spring-boot:run`)*

Flyway will automatically run the schema migrations and seed demo data. The backend will start on `http://localhost:8080`.

### Step 3: Run Vue 3 Frontend
```bash
cd frontend
npm install
npm run dev
```
The frontend will start on `http://localhost:5173`.

---

## 🔑 Demo Login Accounts

All demo accounts have password: **`password123`** (or click the quick 1-click login buttons on the login screen):

| Name | Email | Role |
| :--- | :--- | :--- |
| **Alex Morgan** | `alex@workspace.io` | Lead / Workspace Owner |
| **Sarah Chen** | `sarah@workspace.io` | Developer / Member |
| **Marcus Vance** | `marcus@workspace.io` | UX Designer / Member |

---

## 🔌 API Endpoints Summary

### Authentication (`/api/v1/auth`)
- `POST /register`: Create new user account.
- `POST /login`: Authenticate and obtain JWT token.
- `GET /me`: Get current authenticated user profile.

### Workspaces (`/api/v1/workspaces`)
- `GET /`: List all workspaces for authenticated user.
- `GET /{workspaceId}`: Get workspace details.
- `POST /`: Create workspace.
- `PUT /{workspaceId}`: Update workspace name/slug.
- `DELETE /{workspaceId}`: Delete workspace (Owner only).
- `GET /{workspaceId}/members`: List workspace collaborators.
- `POST /{workspaceId}/invite`: Invite new member by email.

### Pages (`/api/v1`)
- `GET /workspaces/{workspaceId}/pages`: List pages in workspace.
- `GET /pages/{pageId}`: Get single page.
- `POST /workspaces/{workspaceId}/pages`: Create new page (`isKanban`: boolean).
- `PUT /pages/{pageId}`: Update page title, icon, or view mode.
- `DELETE /pages/{pageId}`: Delete page.

### Blocks (`/api/v1`)
- `GET /pages/{pageId}/blocks`: Get all blocks for page (ordered by position).
- `POST /pages/{pageId}/blocks`: Create a new block (content in JSONB).
- `PUT /blocks/{blockId}`: Update block content, type, parentId, or position.
- `DELETE /blocks/{blockId}`: Delete block and cascade to children.
- `POST /pages/{pageId}/blocks/batch-move`: Batch reorder blocks (Kanban column drop).

### WebSocket STOMP Channels (`/ws`)
- **Subscribe Destination**: `/topic/page/{pageId}`
- **Send Destination**: `/app/page/{pageId}/update`
- **Event Types**: `BLOCK_CREATE`, `BLOCK_UPDATE`, `BLOCK_DELETE`, `BLOCK_BATCH_MOVE`, `CURSOR_MOVE`, `USER_JOIN`, `USER_LEAVE`, `PAGE_UPDATE`.
