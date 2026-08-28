<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 flex items-start justify-center pt-20 px-4 bg-black/50 backdrop-blur-sm animate-fade-in"
    @click.self="close"
  >
    <div
      class="w-full max-w-xl rounded-3xl bg-white dark:bg-[#202020] border border-purple-200 dark:border-purple-900/50 shadow-2xl overflow-hidden p-5 animate-scale-in"
    >
      <!-- Header -->
      <div class="flex items-center justify-between mb-3">
        <div class="flex items-center gap-2 text-purple-600 dark:text-purple-400 font-bold text-sm">
          <Sparkles class="w-4 h-4 animate-pulse" />
          <span>Notion AI Assistant</span>
        </div>
        <button class="p-1 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200" @click="close">
          <X class="w-4 h-4" />
        </button>
      </div>

      <!-- Prompt Input -->
      <div class="relative mb-4">
        <input
          v-model="customPrompt"
          type="text"
          placeholder="Ask AI to write, summarize, brainstorm, or edit..."
          class="w-full px-4 py-3 rounded-2xl text-xs bg-slate-50 dark:bg-[#2a2a2a] border border-purple-300 dark:border-purple-800/60 text-slate-900 dark:text-slate-100 outline-none focus:ring-2 focus:ring-purple-500/50"
          :disabled="isGenerating"
          @keydown.enter="runAIPrompt(customPrompt)"
        />
        <button
          class="absolute right-2 top-2 px-3 py-1.5 rounded-xl text-xs font-semibold text-white bg-purple-600 hover:bg-purple-500 transition-all flex items-center gap-1"
          :disabled="isGenerating || !customPrompt.trim()"
          @click="runAIPrompt(customPrompt)"
        >
          <Sparkles class="w-3.5 h-3.5" />
          <span>{{ isGenerating ? 'Thinking...' : 'Generate' }}</span>
        </button>
      </div>

      <!-- Quick Preset Actions -->
      <div class="grid grid-cols-2 gap-2 mb-4">
        <button
          v-for="action in aiActions"
          :key="action.title"
          class="p-2.5 rounded-2xl text-left border border-slate-100 dark:border-[#2f2f2f] hover:border-purple-400 dark:hover:border-purple-600 hover:bg-purple-50/50 dark:hover:bg-purple-950/20 transition-all group"
          :disabled="isGenerating"
          @click="runAction(action)"
        >
          <div class="flex items-center gap-2 text-xs font-semibold text-slate-800 dark:text-slate-200 group-hover:text-purple-600 dark:group-hover:text-purple-400">
            <component :is="action.icon" class="w-3.5 h-3.5 text-purple-500" />
            <span>{{ action.title }}</span>
          </div>
          <p class="text-[10px] text-slate-400 mt-0.5">{{ action.description }}</p>
        </button>
      </div>

      <!-- AI Live Result Box -->
      <div v-if="aiResult" class="p-3.5 rounded-2xl bg-purple-50/50 dark:bg-purple-950/20 border border-purple-200 dark:border-purple-900/60 mb-4 animate-fade-in">
        <div class="text-[10px] font-bold text-purple-600 dark:text-purple-400 uppercase tracking-wider mb-1 flex items-center justify-between">
          <span>AI Generated Output</span>
          <span class="text-[10px] font-normal text-slate-400">Ready to insert</span>
        </div>
        <p class="text-xs text-slate-800 dark:text-slate-200 whitespace-pre-wrap leading-relaxed">
          {{ aiResult }}
        </p>
        <div class="flex items-center justify-end gap-2 mt-3">
          <button
            class="px-3 py-1 rounded-xl text-xs text-slate-500 hover:text-slate-700 dark:hover:text-slate-300"
            @click="aiResult = ''"
          >
            Discard
          </button>
          <button
            class="px-3.5 py-1 rounded-xl text-xs font-semibold text-white bg-purple-600 hover:bg-purple-500 shadow-md flex items-center gap-1"
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

  setTimeout(() => {
    switch (action.type) {
      case 'summary':
        aiResult.value = `### 📌 Executive Summary\n- **Project Scope**: Full-stack real-time collaboration workspace with STOMP WebSockets and reactive optimistic state.\n- **Status**: Backend Spring Boot 3 running seamlessly on port 8088 with flyway migrations.\n- **Next Milestones**: Multi-user live editing, database persistence, and customizable document canvas.`
        break
      case 'action_items':
        aiResult.value = `- [ ] Configure PostgreSQL high-availability replication\n- [ ] Add real-time user mention notifications (@user)\n- [ ] Finalize end-to-end integration tests\n- [ ] Deploy production Vite bundle`
        break
      case 'improve':
        aiResult.value = `This document outlines our engineering roadmap, architected for high concurrency, seamless real-time WebSocket synchronization, and modern web aesthetics.`
        break
      case 'translate_khmer':
        aiResult.value = `### ឯកសារការងារ\nនេះជាប្រព័ន្ធសហការការងារក្នុងពេលជាក់ស្ដែងដែលបង្កើតឡើងដោយប្រើប្រាស់ Java Spring Boot និង Vue 3។`
        break
      case 'brainstorm':
        aiResult.value = `1. **AI Voice-to-Note**: Automatically transcribe voice memos into formatted blocks.\n2. **Smart Database Rollups**: Cross-table relations and formula columns.\n3. **Public Share Links**: Read-only guest sharing with custom domain support.\n4. **Webhooks & Automation**: Trigger external webhooks on block updates.\n5. **Version History**: Time-travel revisions with diff inspection.`
        break
      case 'code':
        aiResult.value = `@RestController\n@RequestMapping("/api/v1/workspace")\npublic class WorkspaceApiController {\n    @GetMapping("/{id}/summary")\n    public ResponseEntity<WorkspaceSummary> getSummary(@PathVariable UUID id) {\n        return ResponseEntity.ok(workspaceService.getSummary(id));\n    }\n}`
        break
    }
    isGenerating.value = false
  }, 700)
}

async function runAIPrompt(promptText: string) {
  if (!promptText.trim()) return
  isGenerating.value = true
  aiResult.value = ''

  setTimeout(() => {
    aiResult.value = `### ✨ AI Response to "${promptText}"\n\nHere is the generated output based on your workspace context:\n- Architected for high scalability and modularity.\n- Integrated with reactive Vue 3 store and Spring Security authentication.\n- Ready for instant deployment and team collaboration.`
    isGenerating.value = false
  }, 800)
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
