<template>
  <div class="my-2.5 rounded-2xl bg-[#1e1e1e] dark:bg-[#141414] text-slate-100 border border-slate-700/60 dark:border-[#2f2f2f] shadow-lg font-mono text-xs relative group/code">
    <!-- Header: Language dropdown & Copy button -->
    <div class="flex items-center justify-between px-3.5 py-2 bg-[#252526] dark:bg-[#1a1a1a] border-b border-slate-700/50 dark:border-[#2a2a2a] text-slate-400 select-none rounded-t-2xl">
      <!-- Language Picker Dropdown -->
      <div ref="dropdownRef" class="relative">
        <button
          class="flex items-center gap-1.5 px-2 py-1 rounded-lg hover:bg-slate-700/60 dark:hover:bg-[#2c2c2c] text-slate-300 hover:text-white transition-colors text-[11px] font-sans font-medium"
          @click.stop="toggleDropdown"
        >
          <Code class="w-3 h-3 text-amber-400" />
          <span>{{ currentLanguageLabel }}</span>
          <ChevronDown class="w-3 h-3 text-slate-400" />
        </button>

        <!-- Dropdown Menu (Fixed Overflow Clipping + Search Filter) -->
        <div
          v-if="isDropdownOpen"
          class="absolute top-full left-0 mt-1 w-52 max-h-72 overflow-hidden rounded-xl bg-[#252526] dark:bg-[#1e1e1e] border border-slate-700 dark:border-[#3a3a3a] shadow-2xl p-1.5 z-50 flex flex-col font-sans animate-scale-in"
          @click.stop
        >
          <!-- Search input -->
          <div class="p-1 pb-1.5 border-b border-slate-700/50 dark:border-[#333] mb-1">
            <input
              ref="searchInputRef"
              v-model="searchQuery"
              type="text"
              placeholder="Search language..."
              class="w-full px-2.5 py-1 text-xs rounded-lg bg-[#1e1e1e] dark:bg-[#151515] border border-slate-700 dark:border-[#333] outline-none text-slate-200 placeholder-slate-500"
            />
          </div>

          <!-- Language list -->
          <div class="overflow-y-auto max-h-48 space-y-0.5 custom-scrollbar">
            <button
              v-for="lang in filteredLanguages"
              :key="lang.id"
              :class="[
                'w-full px-2.5 py-1.5 rounded-lg text-left text-xs flex items-center justify-between transition-colors',
                (modelValueLanguage || 'typescript') === lang.id
                  ? 'bg-brand-600 text-white font-medium'
                  : 'text-slate-300 hover:bg-slate-700/60 dark:hover:bg-[#2a2a2a]'
              ]"
              @click="selectLanguage(lang.id)"
            >
              <span>{{ lang.name }}</span>
              <Check v-if="(modelValueLanguage || 'typescript') === lang.id" class="w-3.5 h-3.5" />
            </button>
            <div v-if="filteredLanguages.length === 0" class="py-2 text-center text-xs text-slate-500">
              No language found
            </div>
          </div>
        </div>
      </div>

      <!-- Copy Code Button -->
      <button
        class="flex items-center gap-1 px-2.5 py-1 rounded-lg hover:bg-slate-700/60 dark:hover:bg-[#2c2c2c] text-slate-400 hover:text-white transition-colors text-[11px] font-sans"
        :title="copied ? 'Copied to clipboard' : 'Copy code'"
        @click="copyCode"
      >
        <Check v-if="copied" class="w-3.5 h-3.5 text-emerald-400" />
        <Copy v-else class="w-3.5 h-3.5" />
        <span>{{ copied ? 'Copied' : 'Copy' }}</span>
      </button>
    </div>

    <!-- Code Editor Body with Tab & Caret support -->
    <div class="p-4 rounded-b-2xl">
      <pre
        ref="codeRef"
        contenteditable="true"
        class="editable-text outline-none whitespace-pre-wrap font-mono text-emerald-400 dark:text-emerald-300 text-xs leading-relaxed empty:before:content-[attr(data-placeholder)] empty:before:text-slate-600"
        data-placeholder="// Write or paste your code snippet here..."
        @input="onInput"
        @keydown="onKeydown"
      ></pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Code, ChevronDown, Copy, Check } from 'lucide-vue-next'

const props = defineProps<{
  modelValueText: string
  modelValueLanguage?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValueText', text: string): void
  (e: 'update:modelValueLanguage', lang: string): void
  (e: 'exitBelow'): void
  (e: 'input', event: Event): void
  (e: 'keydown', event: KeyboardEvent): void
}>()

const codeRef = ref<HTMLElement | null>(null)
const dropdownRef = ref<HTMLElement | null>(null)
const searchInputRef = ref<HTMLInputElement | null>(null)
const isDropdownOpen = ref(false)
const searchQuery = ref('')
const copied = ref(false)

const supportedLanguages = [
  { id: 'typescript', name: 'TypeScript' },
  { id: 'javascript', name: 'JavaScript' },
  { id: 'java', name: 'Java' },
  { id: 'python', name: 'Python' },
  { id: 'sql', name: 'SQL' },
  { id: 'html', name: 'HTML' },
  { id: 'css', name: 'CSS' },
  { id: 'cpp', name: 'C++' },
  { id: 'csharp', name: 'C#' },
  { id: 'go', name: 'Go' },
  { id: 'rust', name: 'Rust' },
  { id: 'json', name: 'JSON' },
  { id: 'bash', name: 'Bash / Shell' },
  { id: 'kotlin', name: 'Kotlin' },
  { id: 'swift', name: 'Swift' },
  { id: 'php', name: 'PHP' },
  { id: 'yaml', name: 'YAML' },
  { id: 'markdown', name: 'Markdown' },
]

const currentLanguageLabel = computed(() => {
  const lang = supportedLanguages.find(
    (l) => l.id === (props.modelValueLanguage || 'typescript')
  )
  return lang ? lang.name : 'TypeScript'
})

const filteredLanguages = computed(() => {
  if (!searchQuery.value.trim()) return supportedLanguages
  const q = searchQuery.value.toLowerCase()
  return supportedLanguages.filter(
    (l) => l.name.toLowerCase().includes(q) || l.id.toLowerCase().includes(q)
  )
})

function toggleDropdown() {
  isDropdownOpen.value = !isDropdownOpen.value
  if (isDropdownOpen.value) {
    searchQuery.value = ''
    nextTick(() => searchInputRef.value?.focus())
  }
}

function selectLanguage(langId: string) {
  emit('update:modelValueLanguage', langId)
  isDropdownOpen.value = false
}

function handleClickOutside(e: MouseEvent) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target as Node)) {
    isDropdownOpen.value = false
  }
}

onMounted(() => {
  if (codeRef.value) {
    codeRef.value.innerText = props.modelValueText || ''
  }
  window.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  window.removeEventListener('click', handleClickOutside)
})

watch(
  () => props.modelValueText,
  (newVal) => {
    if (codeRef.value && document.activeElement !== codeRef.value) {
      if (codeRef.value.innerText !== (newVal || '')) {
        codeRef.value.innerText = newVal || ''
      }
    }
  }
)

function onInput(e: Event) {
  const text = (e.target as HTMLElement).innerText || ''
  emit('update:modelValueText', text)
  emit('input', e)
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Tab') {
    e.preventDefault()
    document.execCommand('insertText', false, '  ')
    return
  }

  // Shift+Enter or Ctrl+Enter / Cmd+Enter exits the code block and starts a new line below!
  if ((e.shiftKey && e.key === 'Enter') || ((e.ctrlKey || e.metaKey) && e.key === 'Enter')) {
    e.preventDefault()
    emit('exitBelow')
    return
  }

  emit('keydown', e)
}

async function copyCode() {
  const text = codeRef.value?.innerText || props.modelValueText || ''
  await navigator.clipboard.writeText(text)
  copied.value = true
  setTimeout(() => {
    copied.value = false
  }, 2000)
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 9999px;
}
</style>
