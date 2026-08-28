<template>
  <div
    v-if="isVisible"
    ref="toolbarRef"
    class="fixed z-50 flex items-center gap-0.5 p-1 rounded-xl bg-slate-900 text-slate-100 border border-slate-700 shadow-2xl backdrop-blur-xl animate-scale-in text-xs"
    :style="{ top: `${position.top}px`, left: `${position.left}px` }"
    @mousedown.prevent
  >
    <!-- Bold -->
    <button
      class="p-1.5 rounded-lg hover:bg-slate-800 text-slate-300 hover:text-white transition-colors"
      title="Bold (Ctrl+B)"
      @click="format('bold')"
    >
      <Bold class="w-3.5 h-3.5" />
    </button>

    <!-- Italic -->
    <button
      class="p-1.5 rounded-lg hover:bg-slate-800 text-slate-300 hover:text-white transition-colors"
      title="Italic (Ctrl+I)"
      @click="format('italic')"
    >
      <Italic class="w-3.5 h-3.5" />
    </button>

    <!-- Underline -->
    <button
      class="p-1.5 rounded-lg hover:bg-slate-800 text-slate-300 hover:text-white transition-colors"
      title="Underline (Ctrl+U)"
      @click="format('underline')"
    >
      <Underline class="w-3.5 h-3.5" />
    </button>

    <!-- Strikethrough -->
    <button
      class="p-1.5 rounded-lg hover:bg-slate-800 text-slate-300 hover:text-white transition-colors"
      title="Strikethrough"
      @click="format('strikeThrough')"
    >
      <Strikethrough class="w-3.5 h-3.5" />
    </button>

    <!-- Inline Code -->
    <button
      class="p-1.5 rounded-lg hover:bg-slate-800 text-slate-300 hover:text-white transition-colors font-mono font-bold"
      title="Code"
      @click="formatCode"
    >
      <Code class="w-3.5 h-3.5" />
    </button>

    <!-- Divider -->
    <div class="w-px h-4 bg-slate-700 mx-1"></div>

    <!-- Link Button -->
    <button
      class="p-1.5 rounded-lg hover:bg-slate-800 text-slate-300 hover:text-white transition-colors flex items-center gap-1"
      title="Add Link"
      @click="createLink"
    >
      <Link2 class="w-3.5 h-3.5" />
    </button>

    <!-- Color Highlight Picker -->
    <div class="relative">
      <button
        class="p-1.5 rounded-lg hover:bg-slate-800 text-slate-300 hover:text-white transition-colors flex items-center gap-0.5"
        title="Highlight Color"
        @click="showColorPicker = !showColorPicker"
      >
        <Highlighter class="w-3.5 h-3.5 text-amber-400" />
      </button>

      <!-- Color Dropdown -->
      <div
        v-if="showColorPicker"
        class="absolute top-full mt-2 left-0 p-2 rounded-xl bg-slate-900 border border-slate-700 shadow-xl grid grid-cols-4 gap-1.5 z-50 min-w-[140px]"
      >
        <button
          v-for="color in highlightColors"
          :key="color.name"
          class="flex items-center gap-1 px-2 py-1 rounded hover:bg-slate-800 text-[11px]"
          @click="applyHighlight(color.bg)"
        >
          <span class="w-3 h-3 rounded-full" :style="{ backgroundColor: color.hex }"></span>
          <span class="text-slate-300">{{ color.name }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import {
  Bold,
  Italic,
  Underline,
  Strikethrough,
  Code,
  Link2,
  Highlighter,
} from 'lucide-vue-next'

const isVisible = ref(false)
const position = ref({ top: 0, left: 0 })
const showColorPicker = ref(false)

const highlightColors = [
  { name: 'Yellow', hex: '#fef08a', bg: '#fef08a' },
  { name: 'Green', hex: '#bbf7d0', bg: '#bbf7d0' },
  { name: 'Blue', hex: '#bfdbfe', bg: '#bfdbfe' },
  { name: 'Pink', hex: '#fbcfe8', bg: '#fbcfe8' },
  { name: 'Purple', hex: '#e9d5ff', bg: '#e9d5ff' },
  { name: 'Default', hex: 'transparent', bg: 'transparent' },
]

function format(command: string, value: string | null = null) {
  document.execCommand(command, false, value as any)
}

function formatCode() {
  const selection = window.getSelection()
  if (!selection || selection.rangeCount === 0) return

  const text = selection.toString()
  if (text) {
    document.execCommand('insertHTML', false, `<code class="px-1.5 py-0.5 rounded bg-slate-200 dark:bg-slate-800 font-mono text-xs text-amber-500">${text}</code>`)
  }
}

function createLink() {
  const url = prompt('Enter link URL:')
  if (url) {
    document.execCommand('createLink', false, url)
  }
}

function applyHighlight(bg: string) {
  if (bg === 'transparent') {
    document.execCommand('removeFormat', false)
  } else {
    document.execCommand('hiliteColor', false, bg)
  }
  showColorPicker.value = false
}

function handleSelectionChange() {
  const selection = window.getSelection()
  if (!selection || selection.isCollapsed || !selection.toString().trim()) {
    isVisible.value = false
    showColorPicker.value = false
    return
  }

  // Check if selection is within an editable text element
  const anchorNode = selection.anchorNode
  const parentEl = anchorNode?.nodeType === Node.ELEMENT_NODE
    ? (anchorNode as HTMLElement)
    : anchorNode?.parentElement

  if (!parentEl?.closest('.editable-text')) {
    isVisible.value = false
    return
  }

  const range = selection.getRangeAt(0)
  const rect = range.getBoundingClientRect()

  position.value = {
    top: Math.max(10, rect.top - 46),
    left: Math.min(window.innerWidth - 260, Math.max(10, rect.left + rect.width / 2 - 120)),
  }
  isVisible.value = true
}

onMounted(() => {
  document.addEventListener('selectionchange', handleSelectionChange)
})

onUnmounted(() => {
  document.removeEventListener('selectionchange', handleSelectionChange)
})
</script>
