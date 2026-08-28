<template>
  <div class="my-3 p-4 rounded-2xl bg-slate-50/70 dark:bg-[#202020] border border-slate-200/80 dark:border-[#2f2f2f] select-none text-xs">
    <div class="flex items-center gap-2 mb-2.5 font-bold uppercase tracking-wider text-[10px] text-slate-400">
      <ListTree class="w-3.5 h-3.5" />
      <span>Table of Contents</span>
    </div>

    <div v-if="headings.length > 0" class="space-y-1">
      <button
        v-for="h in headings"
        :key="h.id"
        :class="[
          'block w-full text-left py-1 rounded-md text-slate-700 dark:text-slate-300 hover:text-brand-600 dark:hover:text-brand-400 hover:underline transition-colors truncate',
          h.level === 1 ? 'font-semibold' : h.level === 2 ? 'pl-3 text-[11px]' : 'pl-6 text-[11px] text-slate-500'
        ]"
        @click="scrollToHeading(h.id)"
      >
        {{ h.text || 'Untitled Section' }}
      </button>
    </div>

    <div v-else class="text-slate-400 italic text-[11px]">
      Add headings (Heading 1, Heading 2, Heading 3) to see the outline here.
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ListTree } from 'lucide-vue-next'
import { useWorkspaceStore } from '@/stores/workspace'

const workspaceStore = useWorkspaceStore()

const headings = computed(() => {
  return workspaceStore.documentBlocks
    .filter((b) => ['heading_1', 'heading_2', 'heading_3'].includes(b.type))
    .map((b) => ({
      id: b.id,
      text: b.content.text || '',
      level: b.type === 'heading_1' ? 1 : b.type === 'heading_2' ? 2 : 3,
    }))
})

function scrollToHeading(blockId: string) {
  const el = document.getElementById(`block-${blockId}`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    el.focus()
  }
}
</script>
