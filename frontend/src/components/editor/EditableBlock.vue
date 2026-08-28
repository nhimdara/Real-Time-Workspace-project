<template>
  <!-- HEADING 1 -->
  <h1
    v-if="type === 'heading_1'"
    :id="id"
    ref="elRef"
    contenteditable="true"
    class="editable-text text-2xl font-bold text-slate-900 dark:text-slate-100 outline-none mt-4 mb-2 tracking-tight"
    :data-placeholder="placeholder || 'Heading 1'"
    @input="handleInput"
    @keydown="handleKeydown"
  ></h1>

  <!-- HEADING 2 -->
  <h2
    v-else-if="type === 'heading_2'"
    :id="id"
    ref="elRef"
    contenteditable="true"
    class="editable-text text-xl font-semibold text-slate-800 dark:text-slate-200 outline-none mt-3 mb-1.5 tracking-tight"
    :data-placeholder="placeholder || 'Heading 2'"
    @input="handleInput"
    @keydown="handleKeydown"
  ></h2>

  <!-- HEADING 3 -->
  <h3
    v-else-if="type === 'heading_3'"
    :id="id"
    ref="elRef"
    contenteditable="true"
    class="editable-text text-lg font-medium text-slate-800 dark:text-slate-200 outline-none mt-2 mb-1 tracking-tight"
    :data-placeholder="placeholder || 'Heading 3'"
    @input="handleInput"
    @keydown="handleKeydown"
  ></h3>

  <!-- NUMBERED LIST -->
  <div
    v-else-if="type === 'numbered_list'"
    :id="id"
    ref="elRef"
    contenteditable="true"
    class="editable-text outline-none text-sm leading-relaxed text-slate-800 dark:text-slate-200 py-0.5"
    :data-placeholder="placeholder || 'Numbered item'"
    @input="handleInput"
    @keydown="handleKeydown"
  ></div>

  <!-- QUOTE BLOCK -->
  <blockquote
    v-else-if="type === 'quote'"
    :id="id"
    ref="elRef"
    contenteditable="true"
    class="editable-text outline-none text-base italic text-slate-700 dark:text-slate-300 pl-4 border-l-4 border-slate-300 dark:border-slate-700 my-2 py-0.5"
    :data-placeholder="placeholder || 'Empty quote...'"
    @input="handleInput"
    @keydown="handleKeydown"
  ></blockquote>

  <!-- TOGGLE HEADER -->
  <div
    v-else-if="type === 'toggle'"
    :id="id"
    ref="elRef"
    contenteditable="true"
    class="editable-text outline-none text-sm font-medium text-slate-800 dark:text-slate-200 py-0.5"
    :data-placeholder="placeholder || 'Toggle heading...'"
    @input="handleInput"
    @keydown="handleKeydown"
  ></div>

  <!-- CODE BLOCK -->
  <pre
    v-else-if="type === 'code'"
    :id="id"
    ref="elRef"
    contenteditable="true"
    class="editable-text outline-none whitespace-pre-wrap font-mono text-emerald-400 text-xs"
    :data-placeholder="placeholder || '// Write code here...'"
    @input="handleInput"
    @keydown="handleKeydown"
  ></pre>

  <!-- DEFAULT TEXT / PARAGRAPH / TODO / LIST / CALLOUT -->
  <div
    v-else
    :id="id"
    ref="elRef"
    contenteditable="true"
    :class="[
      'editable-text outline-none text-sm leading-relaxed py-0.5',
      customClass || 'text-slate-800 dark:text-slate-200'
    ]"
    :data-placeholder="placeholder || 'Type \'/\' for commands or start writing...'"
    @input="handleInput"
    @keydown="handleKeydown"
  ></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import type { BlockType } from '@/types/workspace'

const props = defineProps<{
  id?: string
  type: BlockType
  modelValue: string
  placeholder?: string
  customClass?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'input', event: Event): void
  (e: 'keydown', event: KeyboardEvent): void
}>()

const elRef = ref<HTMLElement | null>(null)

// Initialize innerText on mount
onMounted(() => {
  if (elRef.value) {
    elRef.value.innerText = props.modelValue || ''
  }
})

// Watch modelValue from remote STOMP updates - ONLY sync to DOM if not currently focused
watch(
  () => props.modelValue,
  (newVal) => {
    if (elRef.value && document.activeElement !== elRef.value) {
      if (elRef.value.innerText !== (newVal || '')) {
        elRef.value.innerText = newVal || ''
      }
    }
  }
)

function handleInput(e: Event) {
  const target = e.target as HTMLElement
  const text = target.innerText || ''
  emit('update:modelValue', text)
  emit('input', e)
}

function handleKeydown(e: KeyboardEvent) {
  emit('keydown', e)
}

function focus() {
  if (elRef.value) {
    elRef.value.focus()
    const selection = window.getSelection()
    const range = document.createRange()
    range.selectNodeContents(elRef.value)
    range.collapse(false)
    selection?.removeAllRanges()
    selection?.addRange(range)
  }
}

defineExpose({
  focus,
  getElement: () => elRef.value,
})
</script>

<style scoped>
.editable-text {
  user-select: text !important;
  -webkit-user-select: text !important;
  -webkit-touch-callout: default !important;
}

.editable-text:empty:before {
  content: attr(data-placeholder);
  color: rgb(148 163 184 / 0.7);
  pointer-events: none;
  display: block;
}
.dark .editable-text:empty:before {
  color: rgb(100 116 139 / 0.7);
}
</style>
