<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 flex items-start justify-center pt-24 px-4 bg-slate-950/40 backdrop-blur-md animate-fade-in"
    @click.self="close"
  >
    <div
      class="w-full max-w-xl rounded-3xl liquid-glass-modal overflow-hidden animate-scale-in flex flex-col shadow-2xl"
    >
      <!-- Search Input Header -->
      <div class="flex items-center gap-3 px-4 py-3.5 border-b border-white/30 dark:border-white/10">
        <Search class="w-4 h-4 text-brand-500 shrink-0" />
        <input
          ref="inputRef"
          v-model="searchQuery"
          type="text"
          placeholder="Search pages or type a command..."
          class="w-full bg-transparent border-none outline-none text-sm text-slate-900 dark:text-slate-100 placeholder-slate-400 font-medium"
          @keydown.down.prevent="navigateDown"
          @keydown.up.prevent="navigateUp"
          @keydown.enter.prevent="selectCurrent"
          @keydown.esc="close"
        />
        <kbd class="px-2 py-0.5 rounded-md bg-white/60 dark:bg-white/10 text-[10px] font-mono text-slate-500 dark:text-slate-400 border border-white/40 dark:border-white/10 shadow-2xs">ESC</kbd>
      </div>

      <!-- Results List -->
      <div class="max-h-80 overflow-y-auto p-2.5 space-y-1">
        <div v-if="filteredPages.length > 0">
          <div class="px-2.5 py-1 text-[10px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider">
            Pages
          </div>
          <button
            v-for="(page, idx) in filteredPages"
            :key="page.id"
            :class="[
              'w-full px-3.5 py-2.5 rounded-xl text-left flex items-center justify-between text-xs transition-all',
              selectedIndex === idx
                ? 'bg-white/80 dark:bg-white/15 text-slate-900 dark:text-white font-bold border border-white/60 dark:border-white/20 shadow-sm'
                : 'hover:bg-white/50 dark:hover:bg-white/10 text-slate-700 dark:text-slate-300'
            ]"
            @click="goToPage(page.id)"
            @mouseenter="selectedIndex = idx"
          >
            <div class="flex items-center gap-2.5 min-w-0">
              <span class="text-base">{{ page.icon || (page.isKanban ? '📊' : '📄') }}</span>
              <span class="truncate">{{ page.title || 'Untitled' }}</span>
            </div>
            <span class="text-[10px] font-bold px-2 py-0.5 rounded-md bg-white/50 dark:bg-white/10 text-slate-500 dark:text-slate-400 uppercase border border-white/30 dark:border-white/10">{{ page.isKanban ? 'Board' : 'Page' }}</span>
          </button>
        </div>

        <!-- Quick Actions -->
        <div class="pt-2 border-t border-white/30 dark:border-white/10 mt-1">
          <div class="px-2.5 py-1 text-[10px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider">
            Quick Actions
          </div>
          <button
            class="w-full px-3.5 py-2.5 rounded-xl text-left flex items-center gap-2.5 text-xs hover:bg-white/50 dark:hover:bg-white/10 text-slate-700 dark:text-slate-300 transition-all font-semibold"
            @click="createNewDoc"
          >
            <Plus class="w-4 h-4 text-brand-500" />
            <span>Create new document</span>
          </button>
          <button
            class="w-full px-3.5 py-2.5 rounded-xl text-left flex items-center gap-2.5 text-xs hover:bg-white/50 dark:hover:bg-white/10 text-slate-700 dark:text-slate-300 transition-all font-semibold"
            @click="createNewBoard"
          >
            <Kanban class="w-4 h-4 text-brand-500" />
            <span>Create new Kanban board</span>
          </button>
        </div>

        <div v-if="filteredPages.length === 0 && searchQuery" class="py-6 text-center text-xs text-slate-400 font-medium">
          No pages found for "{{ searchQuery }}"
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { Search, Plus, Kanban } from 'lucide-vue-next'
import { useWorkspaceStore } from '@/stores/workspace'

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const workspaceStore = useWorkspaceStore()
const searchQuery = ref('')
const selectedIndex = ref(0)
const inputRef = ref<HTMLInputElement | null>(null)

watch(
  () => props.isOpen,
  (open) => {
    if (open) {
      searchQuery.value = ''
      selectedIndex.value = 0
      nextTick(() => inputRef.value?.focus())
    }
  }
)

const filteredPages = computed(() => {
  if (!searchQuery.value.trim()) return workspaceStore.pages
  const q = searchQuery.value.toLowerCase()
  return workspaceStore.pages.filter((p) => p.title.toLowerCase().includes(q))
})

function navigateDown() {
  if (filteredPages.value.length > 0) {
    selectedIndex.value = (selectedIndex.value + 1) % filteredPages.value.length
  }
}

function navigateUp() {
  if (filteredPages.value.length > 0) {
    selectedIndex.value = (selectedIndex.value - 1 + filteredPages.value.length) % filteredPages.value.length
  }
}

function selectCurrent() {
  if (filteredPages.value[selectedIndex.value]) {
    goToPage(filteredPages.value[selectedIndex.value].id)
  }
}

function goToPage(pageId: string) {
  workspaceStore.selectPage(pageId)
  close()
}

async function createNewDoc() {
  await workspaceStore.createPage({ title: 'Untitled', isKanban: false })
  close()
}

async function createNewBoard() {
  await workspaceStore.createPage({ title: 'New Board', isKanban: true })
  close()
}

function close() {
  emit('close')
}

function handleGlobalKey(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    if (props.isOpen) close()
    else {
      // Open via parent
    }
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleGlobalKey)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleGlobalKey)
})
</script>
