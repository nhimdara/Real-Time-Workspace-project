<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 flex items-start justify-center pt-20 px-4 bg-slate-950/40 backdrop-blur-md animate-fade-in"
    @click.self="close"
  >
    <div
      class="w-full max-w-xl rounded-3xl liquid-glass-modal border border-purple-500/30 shadow-[0_20px_50px_rgba(168,85,247,0.15)] overflow-hidden p-6 animate-scale-in"
    >
      <!-- Header -->
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-2 text-purple-600 dark:text-purple-400 font-extrabold text-sm">
          <Sparkles class="w-4 h-4 animate-pulse" />
          <span>Notion AI Assistant</span>
        </div>
        <button class="p-1.5 rounded-xl text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-white/40 dark:hover:bg-white/10 transition-colors" @click="close">
          <X class="w-4 h-4" />
        </button>
      </div>

      <!-- Prompt Input -->
      <div class="relative mb-4">
        <input
          v-model="customPrompt"
          type="text"
          placeholder="Ask AI to write, summarize, brainstorm, or edit..."
          class="w-full pl-4 pr-28 py-3 rounded-2xl text-xs liquid-glass-input border-purple-400/50 dark:border-purple-600/50 text-slate-900 dark:text-slate-100 outline-none font-medium"
          :disabled="isGenerating"
          @keydown.enter="runAIPrompt(customPrompt)"
        />
        <button
          class="absolute right-1.5 top-1.5 px-3.5 py-2 rounded-xl text-xs font-bold text-white bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 shadow-md shadow-purple-500/25 transition-all flex items-center gap-1.5 disabled:opacity-50"
          :disabled="isGenerating || !customPrompt.trim()"
          @click="runAIPrompt(customPrompt)"
        >
          <Sparkles class="w-3.5 h-3.5" />
          <span>{{ isGenerating ? 'Thinking...' : 'Generate' }}</span>
        </button>
      </div>

      <!-- Quick Preset Actions -->
      <div class="grid grid-cols-2 gap-2.5 mb-4">
        <button
          v-for="action in aiActions"
          :key="action.title"
          class="p-3 rounded-2xl text-left liquid-glass-card hover:border-purple-400 dark:hover:border-purple-500 transition-all group border border-white/40 dark:border-white/10"
          :disabled="isGenerating"
          @click="runAction(action)"
        >
          <div class="flex items-center gap-2 text-xs font-bold text-slate-900 dark:text-slate-100 group-hover:text-purple-600 dark:group-hover:text-purple-400">
            <component :is="action.icon" class="w-3.5 h-3.5 text-purple-500" />
            <span>{{ action.title }}</span>
          </div>
          <p class="text-[10px] text-slate-500 dark:text-slate-400 mt-0.5 font-normal">{{ action.description }}</p>
        </button>
      </div>

      <!-- AI Live Result Box -->
      <div v-if="aiResult" class="p-4 rounded-2xl bg-purple-500/10 backdrop-blur-md border border-purple-500/30 mb-4 animate-fade-in">
        <div class="text-[10px] font-bold text-purple-600 dark:text-purple-400 uppercase tracking-wider mb-1 flex items-center justify-between">
          <span>AI Generated Output</span>
          <span class="text-[10px] font-normal text-slate-400">Ready to insert</span>
        </div>
        <p class="text-xs text-slate-900 dark:text-slate-100 whitespace-pre-wrap leading-relaxed font-medium">
          {{ aiResult }}
        </p>
        <div class="flex items-center justify-end gap-2 mt-3">
          <button
            class="px-3 py-1.5 text-xs font-semibold text-slate-500 hover:text-slate-700 dark:hover:text-slate-300"
            @click="aiResult = ''"
          >
            Discard
          </button>
          <button
            class="px-4 py-1.5 rounded-xl text-xs font-bold text-white bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 shadow-md flex items-center gap-1.5"
            @click="insertIntoDocument"
          >
            <Check class="w-3.5 h-3.5" />
            <span>Insert Below</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  Sparkles,
  X,
  FileText,
  CheckSquare,
  Wand2,
  Languages,
  Code,
  Lightbulb,
  Check,
} from 'lucide-vue-next'
import { useWorkspaceStore } from '@/stores/workspace'
import aiService from '@/services/ai'

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const workspaceStore = useWorkspaceStore()
const customPrompt = ref('')
const isGenerating = ref(false)
const aiResult = ref('')

const aiActions = [
  {
    title: 'Summarize Document',
    description: 'Create concise executive key takeaways',
    icon: FileText,
    type: 'summary',
  },
  {
    title: 'Generate Action Items',
    description: 'Extract to-dos with checkboxes from page',
    icon: CheckSquare,
    type: 'action_items',
  },
  {
    title: 'Improve Writing',
    description: 'Polish tone, grammar and vocabulary',
    icon: Wand2,
    type: 'improve',
  },
  {
    title: 'Translate to Khmer',
    description: 'Translate page content into Khmer',
    icon: Languages,
    type: 'translate_khmer',
  },
  {
    title: 'Brainstorm Ideas',
    description: 'Generate 5 creative initiatives',
    icon: Lightbulb,
    type: 'brainstorm',
  },
  {
    title: 'Generate Code Snippet',
    description: 'Generate backend API handler in Java/TS',
    icon: Code,
    type: 'code',
  },
]

async function runAction(action: typeof aiActions[0]) {
  isGenerating.value = true
  aiResult.value = ''

  const docText = workspaceStore.documentBlocks
    .map((b) => b.content.text)
    .filter(Boolean)
    .join('\n')

  try {
    const res = await aiService.generate({
      actionType: action.type,
      contextText: docText,
    })
    aiResult.value = res.result
  } catch {
    aiResult.value = 'Failed to generate AI completion. Please check network connection.'
  } finally {
    isGenerating.value = false
  }
}

async function runAIPrompt(promptText: string) {
  if (!promptText.trim()) return
  isGenerating.value = true
  aiResult.value = ''

  const docText = workspaceStore.documentBlocks
    .map((b) => b.content.text)
    .filter(Boolean)
    .join('\n')

  try {
    const res = await aiService.generate({
      prompt: promptText.trim(),
      actionType: 'custom',
      contextText: docText,
    })
    aiResult.value = res.result
  } catch {
    aiResult.value = 'Failed to generate AI completion. Please try again.'
  } finally {
    isGenerating.value = false
  }
}

async function insertIntoDocument() {
  if (!aiResult.value) return

  const lines = aiResult.value.split('\n').filter(Boolean)
  for (const line of lines) {
    if (line.startsWith('### ')) {
      await workspaceStore.createBlock({
        type: 'heading_3',
        content: { text: line.replace('### ', '') },
      })
    } else if (line.startsWith('- [ ] ')) {
      await workspaceStore.createBlock({
        type: 'todo',
        content: { text: line.replace('- [ ] ', ''), checked: false },
      })
    } else if (line.startsWith('- ')) {
      await workspaceStore.createBlock({
        type: 'bullet_list',
        content: { text: line.replace('- ', '') },
      })
    } else {
      await workspaceStore.createBlock({
        type: 'paragraph',
        content: { text: line },
      })
    }
  }

  aiResult.value = ''
  customPrompt.value = ''
  close()
}

function close() {
  emit('close')
}
</script>
