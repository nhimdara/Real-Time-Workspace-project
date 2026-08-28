<template>
  <div
    v-if="isOpen"
    ref="menuRef"
    class="fixed z-50 w-56 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-2xl overflow-hidden py-1.5 animate-scale-in text-xs"
    :style="{ top: `${position.y}px`, left: `${position.x}px` }"
    @mousedown.stop
  >
    <!-- Delete Action -->
    <button
      class="w-full px-3 py-2 text-left flex items-center gap-2.5 text-red-500 hover:bg-red-50 dark:hover:bg-red-950/40 transition-colors"
      @click="emit('delete')"
    >
      <Trash2 class="w-4 h-4" />
      <span>Delete</span>
    </button>

    <!-- Duplicate Action -->
    <button
      class="w-full px-3 py-2 text-left flex items-center gap-2.5 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800/60 transition-colors"
      @click="emit('duplicate')"
    >
      <Copy class="w-4 h-4" />
      <span>Duplicate</span>
    </button>

    <div class="my-1 border-t border-slate-200 dark:border-slate-800"></div>

    <!-- Turn Into Header -->
    <div class="px-3 py-1 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
      Turn into
    </div>

    <!-- Turn into list -->
    <div class="max-h-48 overflow-y-auto space-y-0.5 px-1">
      <button
        v-for="item in turnIntoOptions"
        :key="item.type"
        class="w-full px-2.5 py-1.5 rounded-lg text-left flex items-center gap-2 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800/60 transition-colors"
        @click="emit('turnInto', item.type)"
      >
        <component :is="item.icon" class="w-3.5 h-3.5 text-slate-400" />
        <span>{{ item.label }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import {
  Trash2,
  Copy,
  Type,
  Heading1,
  Heading2,
  Heading3,
  CheckSquare,
  List,
  ListOrdered,
  ChevronRight,
  Quote,
  AlertCircle,
  Code,
  ImageIcon,
} from 'lucide-vue-next'
import type { BlockType } from '@/types/workspace'

const props = defineProps<{
  isOpen: boolean
  position: { x: number; y: number }
}>()

const emit = defineEmits<{
  (e: 'delete'): void
  (e: 'duplicate'): void
  (e: 'turnInto', type: BlockType): void
  (e: 'close'): void
}>()

const turnIntoOptions = [
  { type: 'paragraph' as BlockType, label: 'Text', icon: Type },
  { type: 'heading_1' as BlockType, label: 'Heading 1', icon: Heading1 },
  { type: 'heading_2' as BlockType, label: 'Heading 2', icon: Heading2 },
  { type: 'heading_3' as BlockType, label: 'Heading 3', icon: Heading3 },
  { type: 'todo' as BlockType, label: 'To-do List', icon: CheckSquare },
  { type: 'bullet_list' as BlockType, label: 'Bulleted List', icon: List },
  { type: 'numbered_list' as BlockType, label: 'Numbered List', icon: ListOrdered },
  { type: 'toggle' as BlockType, label: 'Toggle List', icon: ChevronRight },
  { type: 'quote' as BlockType, label: 'Quote', icon: Quote },
  { type: 'callout' as BlockType, label: 'Callout', icon: AlertCircle },
  { type: 'code' as BlockType, label: 'Code Block', icon: Code },
  { type: 'image' as BlockType, label: 'Image', icon: ImageIcon },
]

function handleClickOutside(e: MouseEvent) {
  if (props.isOpen) {
    emit('close')
  }
}

onMounted(() => {
  window.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  window.removeEventListener('click', handleClickOutside)
})
</script>
