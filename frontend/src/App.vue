<template>
  <div class="h-screen w-screen overflow-hidden">
    <!-- Unauthenticated View: Auth Modal / Login -->
    <AuthModal v-if="!authStore.isAuthenticated" />

    <!-- Authenticated View: Full Workspace App Layout -->
    <WorkspaceLayout v-else>
      <router-view />
    </WorkspaceLayout>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useWorkspaceStore } from '@/stores/workspace'
import AuthModal from '@/components/auth/AuthModal.vue'
import WorkspaceLayout from '@/components/layout/WorkspaceLayout.vue'

const authStore = useAuthStore()
const workspaceStore = useWorkspaceStore()

onMounted(async () => {
  if (authStore.isAuthenticated) {
    await authStore.fetchCurrentUser()
    await workspaceStore.fetchWorkspaces()
  }
})
</script>
