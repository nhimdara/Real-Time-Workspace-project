<template>
  <div class="h-full relative">
    <!-- Multi-user live cursor overlay -->
    <LiveCursors />

    <!-- Conditional View: Notion Document Editor vs Trello Kanban Board -->
    <div v-if="workspaceStore.currentPage" class="h-full">
      <KanbanBoard v-if="workspaceStore.currentPage.isKanban" />
      <BlockEditor v-else />
    </div>

    <!-- Empty State if no page selected -->
    <div
      v-else
      class="h-full flex flex-col items-center justify-center p-8 text-center"
    >
      <div class="p-4 rounded-3xl bg-brand-500/10 text-brand-500 mb-4 animate-pulse-subtle">
        <Sparkles class="w-8 h-8" />
      </div>
      <h3 class="text-lg font-bold text-slate-800 dark:text-slate-200 mb-1">
        Select or create a page to get started
      </h3>
      <p class="text-xs text-slate-400 max-w-sm">
        Collaborate in real time using Notion-style rich text blocks or interactive Kanban boards.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Sparkles } from 'lucide-vue-next'
import { useWorkspaceStore } from '@/stores/workspace'
import LiveCursors from '@/components/realtime/LiveCursors.vue'
import BlockEditor from '@/components/editor/BlockEditor.vue'
import KanbanBoard from '@/components/kanban/KanbanBoard.vue'

const workspaceStore = useWorkspaceStore()
</script>
