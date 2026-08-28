<template>
  <div class="h-screen w-screen overflow-hidden relative bg-slate-100 dark:bg-[#0b0f19]">
    <!-- Ambient Liquid Gradient Backdrops -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none z-0">
      <div class="absolute -top-40 -left-40 w-[500px] h-[500px] bg-gradient-to-tr from-brand-500/20 via-emerald-400/15 to-teal-300/10 rounded-full blur-3xl animate-blob-1 opacity-70"></div>
      <div class="absolute top-1/3 -right-40 w-[600px] h-[600px] bg-gradient-to-bl from-indigo-500/15 via-purple-500/15 to-emerald-400/10 rounded-full blur-3xl animate-blob-2 opacity-60"></div>
      <div class="absolute -bottom-40 left-1/4 w-[550px] h-[550px] bg-gradient-to-u from-emerald-500/20 to-cyan-500/10 rounded-full blur-3xl animate-blob-1 opacity-50"></div>
    </div>

    <!-- Unauthenticated View: Auth Modal / Login -->
    <AuthModal v-if="!authStore.isAuthenticated" class="relative z-10" />

    <!-- Authenticated View: Full Workspace App Layout -->
    <WorkspaceLayout v-else class="relative z-10">
      <router-view />
      <!-- Global Floating AI Copilot Widget -->
      <AiChatWidget />
    </WorkspaceLayout>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useWorkspaceStore } from '@/stores/workspace'
import AuthModal from '@/components/auth/AuthModal.vue'
import WorkspaceLayout from '@/components/layout/WorkspaceLayout.vue'
import AiChatWidget from '@/components/common/AiChatWidget.vue'

const authStore = useAuthStore()
const workspaceStore = useWorkspaceStore()

onMounted(async () => {
  if (authStore.isAuthenticated) {
    await authStore.fetchCurrentUser()
    await workspaceStore.fetchWorkspaces()
  }
})
</script>
