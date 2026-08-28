<template>
  <div
    v-if="isOpen"
    ref="menuRef"
    class="fixed z-50 w-80 rounded-2xl bg-[#202020] text-[#d4d4d4] border border-[#2f2f2f] shadow-2xl overflow-hidden flex flex-col animate-scale-in text-[13px] select-none"
    :style="{ top: `${position.y}px`, left: `${position.x}px` }"
    @mousedown.prevent
  >
    <!-- Scrollable Items Area -->
    <div ref="listRef" class="max-h-[380px] overflow-y-auto p-1.5 space-y-3 custom-scrollbar">
      <!-- Section Loop -->
      <div v-for="section in groupedSections" :key="section.title" class="space-y-0.5">
        <div class="px-2.5 py-1 text-[11px] font-semibold text-slate-400">
          {{ section.title }}
        </div>

        <button
          v-for="item in section.items"
          :key="item.id"
          :class="[
            'w-full px-2.5 py-1.5 rounded-lg text-left flex items-center justify-between transition-colors group',
            flatItems[selectedIndex]?.id === item.id
              ? 'bg-[#2a2a2a] text-white font-medium'
              : 'hover:bg-[#282828] text-[#d4d4d4]'
          ]"
          @click="selectItem(item.type)"
          @mouseenter="onHover(item.id)"
        >
          <div class="flex items-center gap-2.5 min-w-0">
            <!-- Icon -->
            <div class="w-5 h-5 flex items-center justify-center shrink-0 text-slate-300">
              <component :is="item.icon" v-if="typeof item.icon !== 'string'" class="w-4 h-4" />
              <span v-else class="text-xs font-bold font-mono">{{ item.icon }}</span>
            </div>

            <!-- Label -->
            <span class="truncate text-xs">{{ item.label }}</span>

            <!-- Badge (New / Beta) -->
            <span
              v-if="item.badge"
              class="px-1.5 py-0.2 text-[9px] font-semibold rounded shrink-0"
              :class="item.badge === 'New' ? 'bg-blue-500/20 text-blue-400 border border-blue-500/30' : 'bg-slate-700 text-slate-300'"
            >
              {{ item.badge }}
            </span>
          </div>

          <!-- Shortcut Hint (#, ##, ---) -->
          <span
            v-if="item.shortcut"
            class="text-[11px] font-mono text-slate-500 shrink-0 ml-2"
          >
            {{ item.shortcut }}
          </span>
        </button>
      </div>
    </div>

    <!-- Sticky Notion Footer -->
    <div class="px-3 py-2 border-t border-[#2a2a2a] bg-[#1d1d1d] flex items-center justify-between text-xs text-slate-400">
      <span>Close menu</span>
      <kbd class="text-[10px] font-mono bg-[#2a2a2a] px-1.5 py-0.5 rounded text-slate-400">esc</kbd>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import {
  Type,
  CheckSquare,
  List,
  ListOrdered,
  ChevronRight,
  Quote,
  Minus,
  Link2,
  AlertCircle,
  Table,
  Kanban,
  LayoutGrid,
  Calendar,
  Layers,
  MapPin,
  ImageIcon,
  Video,
  Volume2,
  Code,
  ListTree,
  Sigma,
  Columns2,
  Columns3,
  Columns4,
  Sparkles,
  FileCode,
} from 'lucide-vue-next'
import type { BlockType } from '@/types/workspace'

const props = defineProps<{
  isOpen: boolean
  position: { x: number; y: number }
  query: string
}>()

const emit = defineEmits<{
  (e: 'select', type: BlockType): void
  (e: 'close'): void
}>()

const selectedIndex = ref(0)
const menuRef = ref<HTMLElement | null>(null)
const listRef = ref<HTMLElement | null>(null)

interface MenuItem {
  id: string
  type: BlockType
  label: string
  icon: any
  badge?: string
  shortcut?: string
  section: string
}

const allMenuItems: MenuItem[] = [
  // Suggested
  { id: 'ai-notes', type: 'callout', label: 'AI Meeting Notes', icon: Sparkles, badge: 'Beta', section: 'Suggested' },
  { id: 'html-embed', type: 'code', label: 'HTML · Embeds', icon: FileCode, badge: 'New', section: 'Suggested' },

  // Basic blocks
  { id: 'text', type: 'paragraph', label: 'Text', icon: 'T', section: 'Basic blocks' },
  { id: 'h1', type: 'heading_1', label: 'Heading 1', icon: 'H₁', shortcut: '#', section: 'Basic blocks' },
  { id: 'h2', type: 'heading_2', label: 'Heading 2', icon: 'H₂', shortcut: '##', section: 'Basic blocks' },
  { id: 'h3', type: 'heading_3', label: 'Heading 3', icon: 'H₃', shortcut: '###', section: 'Basic blocks' },
  { id: 'todo', type: 'todo', label: 'To-do list', icon: CheckSquare, shortcut: '[]', section: 'Basic blocks' },
  { id: 'bullet', type: 'bullet_list', label: 'Bulleted list', icon: List, shortcut: '-', section: 'Basic blocks' },
  { id: 'numbered', type: 'numbered_list', label: 'Numbered list', icon: ListOrdered, shortcut: '1.', section: 'Basic blocks' },
  { id: 'toggle', type: 'toggle', label: 'Toggle list', icon: ChevronRight, shortcut: '>', section: 'Basic blocks' },
  { id: 'quote', type: 'quote', label: 'Quote', icon: Quote, shortcut: '"', section: 'Basic blocks' },
  { id: 'divider', type: 'divider', label: 'Divider', icon: Minus, shortcut: '---', section: 'Basic blocks' },
  { id: 'link', type: 'paragraph', label: 'Link to page', icon: Link2, section: 'Basic blocks' },
  { id: 'callout', type: 'callout', label: 'Callout', icon: AlertCircle, section: 'Basic blocks' },

  // Database
  { id: 'table-view', type: 'paragraph', label: 'Table view', icon: Table, section: 'Database' },
  { id: 'board-view', type: 'kanban_card', label: 'Board view', icon: Kanban, section: 'Database' },
  { id: 'gallery-view', type: 'paragraph', label: 'Gallery view', icon: LayoutGrid, section: 'Database' },
  { id: 'list-view', type: 'bullet_list', label: 'List view', icon: List, section: 'Database' },
  { id: 'calendar-view', type: 'paragraph', label: 'Calendar view', icon: Calendar, section: 'Database' },
  { id: 'timeline-view', type: 'paragraph', label: 'Timeline view', icon: Layers, section: 'Database' },
  { id: 'map-view', type: 'paragraph', label: 'Map view', icon: MapPin, badge: 'New', section: 'Database' },

  // Media
  { id: 'image', type: 'image', label: 'Image', icon: ImageIcon, section: 'Media' },
  { id: 'video', type: 'image', label: 'Video', icon: Video, section: 'Media' },
  { id: 'audio', type: 'paragraph', label: 'Audio', icon: Volume2, section: 'Media' },
  { id: 'code', type: 'code', label: 'Code', icon: Code, shortcut: '```', section: 'Media' },

  // Advanced blocks
  { id: 'toc', type: 'paragraph', label: 'Table of contents', icon: ListTree, section: 'Advanced blocks' },
  { id: 'equation', type: 'code', label: 'Block equation', icon: Sigma, section: 'Advanced blocks' },
  { id: 'toggle-h1', type: 'toggle', label: 'Toggle heading 1', icon: '▶H₁', shortcut: '# >', section: 'Advanced blocks' },
  { id: 'toggle-h2', type: 'toggle', label: 'Toggle heading 2', icon: '▶H₂', shortcut: '## >', section: 'Advanced blocks' },
  { id: 'toggle-h3', type: 'toggle', label: 'Toggle heading 3', icon: '▶H₃', shortcut: '### >', section: 'Advanced blocks' },
  { id: '2-cols', type: 'paragraph', label: '2 columns', icon: Columns2, section: 'Advanced blocks' },
  { id: '3-cols', type: 'paragraph', label: '3 columns', icon: Columns3, section: 'Advanced blocks' },
  { id: '4-cols', type: 'paragraph', label: '4 columns', icon: Columns4, section: 'Advanced blocks' },
]

const filteredItems = computed(() => {
  if (!props.query) return allMenuItems
  const q = props.query.toLowerCase()
  return allMenuItems.filter(
    (item) =>
      item.label.toLowerCase().includes(q) ||
      item.id.toLowerCase().includes(q) ||
      item.section.toLowerCase().includes(q)
  )
})

const flatItems = computed(() => filteredItems.value)

const groupedSections = computed(() => {
  const sections: { title: string; items: MenuItem[] }[] = []
  const map = new Map<string, MenuItem[]>()

  filteredItems.value.forEach((item) => {
    if (!map.has(item.section)) {
      map.set(item.section, [])
    }
    map.get(item.section)!.push(item)
  })

  map.forEach((items, title) => {
    sections.push({ title, items })
  })

  return sections
})

watch(
  () => props.query,
  () => {
    selectedIndex.value = 0
  }
)

function onHover(id: string) {
  const idx = flatItems.value.findIndex((i) => i.id === id)
  if (idx !== -1) selectedIndex.value = idx
}

function selectItem(type: BlockType) {
  emit('select', type)
}

function handleKeyDown(e: KeyboardEvent) {
  if (!props.isOpen) return

  if (e.key === 'ArrowDown') {
    e.preventDefault()
    selectedIndex.value = (selectedIndex.value + 1) % flatItems.value.length
    scrollToSelected()
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    selectedIndex.value =
      (selectedIndex.value - 1 + flatItems.value.length) % flatItems.value.length
    scrollToSelected()
  } else if (e.key === 'Enter') {
    e.preventDefault()
    if (flatItems.value[selectedIndex.value]) {
      selectItem(flatItems.value[selectedIndex.value].type)
    }
  } else if (e.key === 'Escape') {
    emit('close')
  }
}

function scrollToSelected() {
  nextTick(() => {
    const el = listRef.value?.querySelector('.bg-\\[\\#2a2a2a\\]')
    if (el) {
      el.scrollIntoView({ block: 'nearest' })
    }
  })
}

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #383838;
  border-radius: 9999px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #4a4a4a;
}
</style>
