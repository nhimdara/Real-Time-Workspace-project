<template>
  <div class="pointer-events-none fixed inset-0 z-50 overflow-hidden">
    <div
      v-for="cursor in cursors"
      :key="cursor.userId"
      class="absolute transition-all duration-75 ease-out flex items-start gap-1"
      :style="{
        left: `${cursor.x}px`,
        top: `${cursor.y}px`,
      }"
    >
      <!-- Cursor Pointer SVG -->
      <svg
        class="w-5 h-5 drop-shadow-md transform -translate-x-1 -translate-y-1"
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M5.65376 12.3673H5.46026L5.31717 12.4976L0.500002 16.8829L0.500002 1.19841L11.7841 12.3673H5.65376Z"
          :fill="cursor.color || '#3b82f6'"
          stroke="white"
          stroke-width="1.5"
        />
      </svg>

      <!-- User Badge Tag -->
      <div
        class="px-2 py-0.5 rounded-full text-xs font-semibold text-white shadow-lg flex items-center gap-1.5 whitespace-nowrap animate-scale-in"
        :style="{ backgroundColor: cursor.color || '#3b82f6' }"
      >
        <img
          v-if="cursor.userAvatar"
          :src="cursor.userAvatar"
          class="w-3.5 h-3.5 rounded-full object-cover border border-white/40"
          alt="Avatar"
        />
        <span>{{ cursor.userName }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useWorkspaceStore } from '@/stores/workspace'

const workspaceStore = useWorkspaceStore()
const cursors = computed(() => workspaceStore.activePeers)
</script>
