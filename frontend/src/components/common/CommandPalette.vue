<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 flex items-start justify-center pt-24 px-4 bg-black/50 backdrop-blur-sm animate-fade-in"
    @click.self="close"
  >
    <div
      class="w-full max-w-xl rounded-2xl bg-white dark:bg-[#202020] border border-slate-200 dark:border-[#2f2f2f] shadow-2xl overflow-hidden animate-scale-in flex flex-col"
    >
      <!-- Search Input Header -->
      <div class="flex items-center gap-3 px-4 py-3 border-b border-slate-200 dark:border-[#2f2f2f]">
        <Search class="w-4 h-4 text-slate-400 shrink-0" />
        <input
          ref="inputRef"
          v-model="searchQuery"
          type="text"
          placeholder="Search pages or type a command..."
          class="w-full bg-transparent border-none outline-none text-sm text-slate-900 dark:text-slate-100 placeholder-slate-400"
          @keydown.down.prevent="navigateDown"
          @keydown.up.prevent="navigateUp"
          @keydown.enter.prevent="selectCurrent"
          @keydown.esc="close"
        />
        <kbd class="px-2 py-0.5 rounded bg-slate-100 dark:bg-[#2f2f2f] text-[10px] font-mono text-slate-500">ESC</kbd>
      </div>

      <!-- Results List -->
      <div class="max-h-80 overflow-y-auto p-2 space-y-1">
        <div v-if="filteredPages.length > 0">
          <div class="px-2 py-1 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
            Pages
          </div>
          <button
            v-for="(page, idx) in filteredPages"
            :key="page.id"
            :class="[
              'w-full px-3 py-2 rounded-xl text-left flex items-center justify-between text-xs transition-colors',
              selectedIndex === idx
                ? 'bg-brand-500/10 text-brand-600 dark:text-brand-400 font-semibold'
                : 'hover:bg-slate-100 dark:hover:bg-[#2a2a2a] text-slate-700 dark:text-slate-300'
            ]"
            @click="goToPage(page.id)"
            @mouseenter="selectedIndex = idx"
          >
            <div class="flex items-center gap-2.5 min-w-0">
              <span class="text-base">{{ page.icon || (page.isKanban ? '📊' : '📄') }}</span>
              <span class="truncate">{{ page.title || 'Untitled' }}</span>
            </div>
            <span class="text-[10px] text-slate-400 uppercase">{{ page.isKanban ? 'Board' : 'Page' }}</span>
          </button>
        </div>

        <!-- Quick Actions -->
        <div class="pt-2 border-t border-slate-100 dark:border-[#2f2f2f] mt-1">
          <div class="px-2 py-1 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
            Quick Actions
          </div>
          <button
            class="w-full px-3 py-2 rounded-xl text-left flex items-center gap-2.5 text-xs hover:bg-slate-100 dark:hover:bg-[#2a2a2a] text-slate-700 dark:text-slate-300 transition-colors"
            @click="createNewDoc"
          >
            <Plus class="w-4 h-4 text-brand-500" />
            <span>Create new document</span>
          </button>
          <button
            class="w-full px-3 py-2 rounded-xl text-left flex items-center gap-2.5 text-xs hover:bg-slate-100 dark:hover:bg-[#2a2a2a] text-slate-700 dark:text-slate-300 transition-colors"
            @click="createNewBoard"
          >
            <Kanban class="w-4 h-4 text-brand-500" />
            <span>Create new Kanban board</span>
          </button>
        </div>

        <div v-if="filteredPages.length === 0 && searchQuery" class="py-6 text-center text-xs text-slate-400">
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
