<template>
  <div class="fixed bottom-6 right-6 z-50 select-none">
    <!-- Floating AI Trigger Pill Button -->
    <button
      v-if="!isOpen"
      class="group flex items-center gap-2.5 px-4 py-3 rounded-full bg-gradient-to-r from-purple-600 via-indigo-600 to-brand-600 text-white font-bold text-xs shadow-[0_8px_25px_rgba(168,85,247,0.4)] hover:shadow-[0_12px_35px_rgba(168,85,247,0.6)] hover:scale-105 transition-all duration-300 border border-white/40 backdrop-blur-md"
      @click="isOpen = true"
    >
      <Sparkles class="w-4 h-4 animate-pulse text-amber-300" />
      <span>AI Copilot</span>
      <span class="px-1.5 py-0.5 rounded-full text-[10px] bg-white/20 border border-white/30 text-amber-200 uppercase font-extrabold tracking-wider">PRO</span>
    </button>

    <!-- Floating AI Chat Window (Liquid Glass) -->
    <div
      v-else
      class="w-80 md:w-96 h-[480px] rounded-3xl liquid-glass-modal shadow-[0_20px_60px_rgba(0,0,0,0.5)] border border-purple-500/40 flex flex-col overflow-hidden animate-scale-in"
    >
      <!-- Chat Header -->
      <div class="px-4 py-3.5 bg-gradient-to-r from-purple-600/90 to-indigo-600/90 backdrop-blur-md text-white flex items-center justify-between shadow-sm">
        <div class="flex items-center gap-2 font-extrabold text-xs">
          <div class="p-1 rounded-lg bg-white/20 border border-white/30">
            <Bot class="w-4 h-4 text-amber-300" />
          </div>
          <div>
            <h3 class="leading-tight">Workspace AI Copilot</h3>
            <span class="text-[9px] text-purple-200 font-medium block">Powered by Workspace AI</span>
          </div>
        </div>

        <div class="flex items-center gap-1">
          <button
            class="p-1 rounded-lg hover:bg-white/20 text-white/80 hover:text-white transition-colors"
            title="Clear Chat"
            @click="clearChat"
          >
            <RotateCcw class="w-3.5 h-3.5" />
          </button>
          <button
            class="p-1 rounded-lg hover:bg-white/20 text-white/80 hover:text-white transition-colors"
            title="Minimize"
            @click="isOpen = false"
          >
            <Minus class="w-4 h-4" />
          </button>
        </div>
      </div>

      <!-- Quick Action Chips -->
      <div class="px-3 py-2 bg-purple-500/5 dark:bg-purple-950/20 border-b border-purple-500/20 flex items-center gap-1.5 overflow-x-auto scrollbar-none">
        <button
          class="px-2.5 py-1 rounded-xl text-[10px] font-bold whitespace-nowrap shrink-0 bg-white/50 dark:bg-white/10 hover:bg-purple-500/20 text-purple-700 dark:text-purple-300 border border-purple-500/30 transition-all flex items-center gap-1"
          @click="sendQuickAction('summary')"
        >
          <FileText class="w-3 h-3" /> Summarize
        </button>
        <button
          class="px-2.5 py-1 rounded-xl text-[10px] font-bold whitespace-nowrap shrink-0 bg-white/50 dark:bg-white/10 hover:bg-purple-500/20 text-purple-700 dark:text-purple-300 border border-purple-500/30 transition-all flex items-center gap-1"
          @click="sendQuickAction('action_items')"
        >
          <CheckSquare class="w-3 h-3" /> To-Dos
        </button>
        <button
          class="px-2.5 py-1 rounded-xl text-[10px] font-bold whitespace-nowrap shrink-0 bg-white/50 dark:bg-white/10 hover:bg-purple-500/20 text-purple-700 dark:text-purple-300 border border-purple-500/30 transition-all flex items-center gap-1"
          @click="sendQuickAction('brainstorm')"
        >
          <Lightbulb class="w-3 h-3" /> Brainstorm
        </button>
      </div>

      <!-- Messages Stream Container -->
      <div ref="chatContainerRef" class="flex-1 overflow-y-auto p-4 space-y-3.5 text-xs">
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="flex flex-col"
          :class="msg.role === 'user' ? 'items-end' : 'items-start'"
        >
          <!-- User Bubble -->
          <div
            v-if="msg.role === 'user'"
            class="max-w-[85%] px-3.5 py-2.5 rounded-2xl rounded-tr-xs bg-gradient-to-r from-purple-600 to-indigo-600 text-white font-medium shadow-md space-y-1.5"
          >
            <img
              v-if="msg.imageUrl"
              :src="msg.imageUrl"
              class="w-full max-h-36 rounded-xl object-cover border border-white/30 shadow-xs"
              alt="Attached Image"
            />
            <div>{{ msg.content }}</div>
          </div>

          <!-- Assistant Bubble -->
          <div
            v-else
            class="max-w-[90%] p-3.5 rounded-2xl rounded-tl-xs liquid-glass border border-purple-500/30 text-slate-900 dark:text-slate-100 shadow-sm space-y-1.5"
          >
            <div class="flex items-center gap-1.5 font-bold text-purple-600 dark:text-purple-400 mb-1 text-[11px]">
              <Sparkles class="w-3.5 h-3.5" />
              <span>AI Copilot</span>
            </div>
            <img
              v-if="msg.imageUrl"
              :src="msg.imageUrl"
              class="w-full max-h-40 rounded-xl object-cover border border-purple-500/30 shadow-xs"
              alt="Generated / Analyzed Image"
            />
            <div class="whitespace-pre-wrap leading-relaxed font-normal">
              {{ msg.content }}
            </div>

            <!-- Insert to page button -->
            <div class="mt-2.5 pt-2 border-t border-white/30 dark:border-white/10 flex items-center justify-between">
              <button
                v-if="msg.imageUrl"
                class="px-2.5 py-1 rounded-lg text-[10px] font-bold text-emerald-600 dark:text-emerald-400 hover:bg-emerald-500/20 border border-emerald-500/30 transition-all flex items-center gap-1"
                @click="insertImageToDoc(msg.imageUrl)"
              >
                <ImageIcon class="w-3 h-3" /> Insert Image
              </button>
              <button
                class="px-2.5 py-1 rounded-lg text-[10px] font-bold text-purple-600 dark:text-purple-300 hover:bg-purple-500/20 border border-purple-500/30 transition-all flex items-center gap-1 ml-auto"
                @click="insertMsgToDoc(msg.content)"
              >
                <Plus class="w-3 h-3" /> Insert Text
              </button>
            </div>
          </div>
        </div>

        <!-- Typing Indicator -->
        <div v-if="loading" class="flex items-center gap-2 text-purple-500 font-semibold text-[11px] p-2">
          <Sparkles class="w-3.5 h-3.5 animate-spin" />
          <span>AI is analyzing your request...</span>
        </div>
      </div>

      <!-- Attachment Preview Bar -->
      <div v-if="attachedImageUrl" class="px-3 py-1.5 bg-purple-500/10 border-t border-purple-500/20 flex items-center justify-between backdrop-blur-md">
        <div class="flex items-center gap-2 min-w-0">
          <img :src="attachedImageUrl" class="w-8 h-8 rounded-lg object-cover border border-purple-500/40 shrink-0" />
          <span class="text-[10px] text-purple-300 font-semibold truncate">{{ attachedFileName || 'Attached Image' }}</span>
        </div>
        <button type="button" class="p-1 text-slate-400 hover:text-red-400 transition-colors" @click="removeAttachment">
          <X class="w-3.5 h-3.5" />
        </button>
      </div>

      <!-- Input Form Bar -->
      <form @submit.prevent="sendMessage" class="p-3 border-t border-white/30 dark:border-white/10 bg-white/30 dark:bg-white/5 backdrop-blur-md flex items-center gap-2">
        <button
          type="button"
          class="p-2 rounded-xl text-slate-400 hover:text-purple-500 hover:bg-white/20 dark:hover:bg-white/10 transition-colors shrink-0"
          title="Upload or Attach Image"
          @click="triggerFileInput"
        >
          <Paperclip class="w-4 h-4" />
        </button>
        <input
          ref="fileInputRef"
          type="file"
          accept="image/*"
          class="hidden"
          @change="handleFileSelected"
        />

        <input
          v-model="inputQuery"
          type="text"
          placeholder="Ask AI or describe image..."
          class="flex-1 px-3 py-2 rounded-xl text-xs liquid-glass-input text-slate-900 dark:text-slate-100 outline-none font-medium"
          :disabled="loading"
        />
        <button
          type="submit"
          class="p-2.5 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white shadow-md transition-all disabled:opacity-50 shrink-0"
          :disabled="loading || (!inputQuery.trim() && !attachedImageUrl)"
        >
          <Send class="w-3.5 h-3.5" />
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted } from 'vue'
import {
  Sparkles,
  Bot,
  Minus,
  RotateCcw,
  Send,
  FileText,
  CheckSquare,
  Lightbulb,
  Plus,
  Paperclip,
  ImageIcon,
  X,
} from 'lucide-vue-next'
import { useWorkspaceStore } from '@/stores/workspace'
import aiService, { type AiChatMessage } from '@/services/ai'
import api from '@/services/api'

const STORAGE_KEY = 'workspace_ai_chat_history'

const workspaceStore = useWorkspaceStore()

const isOpen = ref(false)
const inputQuery = ref('')
const loading = ref(false)
const chatContainerRef = ref<HTMLElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)

const attachedImageUrl = ref<string | null>(null)
const attachedFileName = ref<string | null>(null)

function loadChatHistory(): AiChatMessage[] {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      const parsed = JSON.parse(saved)
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed
      }
    }
  } catch (err) {
    console.warn('Failed to parse AI chat history from localStorage', err)
  }
  return [
    {
      role: 'assistant',
      content: '👋 Hi! I am your AI Copilot. Ask me questions, upload images, summarize your document, or generate task breakdowns!',
    },
  ]
}

const messages = ref<AiChatMessage[]>(loadChatHistory())

// Automatically persist conversation history to localStorage
watch(
  messages,
  (newMsgs) => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(newMsgs))
    } catch (err) {
      console.warn('Failed to save AI chat to localStorage', err)
    }
  },
  { deep: true }
)

function scrollToBottom() {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

onMounted(() => {
  scrollToBottom()
})

function clearChat() {
  if (confirm('Reset AI chat history?')) {
    messages.value = [
      {
        role: 'assistant',
        content: 'Conversation reset. How can I help you next?',
      },
    ]
    attachedImageUrl.value = null
    attachedFileName.value = null
    try {
      localStorage.removeItem(STORAGE_KEY)
    } catch {
      // ignore
    }
  }
}

function triggerFileInput() {
  fileInputRef.value?.click()
}

async function handleFileSelected(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (!files || !files[0]) return

  const file = files[0]
  attachedFileName.value = file.name

  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    if (res.data?.url) {
      attachedImageUrl.value = res.data.url
    }
  } catch {
    // Client-side fallback via FileReader Base64 data URL
    const reader = new FileReader()
    reader.onload = () => {
      attachedImageUrl.value = reader.result as string
    }
    reader.readAsDataURL(file)
  }
}

function removeAttachment() {
  attachedImageUrl.value = null
  attachedFileName.value = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

async function sendMessage() {
  if ((!inputQuery.value.trim() && !attachedImageUrl.value) || loading.value) return

  const userText = inputQuery.value.trim() || (attachedImageUrl.value ? 'Uploaded attached image' : '')
  const currentImageUrl = attachedImageUrl.value

  inputQuery.value = ''
  removeAttachment()

  messages.value.push({
    role: 'user',
    content: userText,
    imageUrl: currentImageUrl || undefined,
  })
  scrollToBottom()

  loading.value = true

  const docText = workspaceStore.documentBlocks
    .map((b) => b.content.text)
    .filter(Boolean)
    .join('\n')

  try {
    const promptMessage = currentImageUrl
      ? `${userText} (Image URL attached: ${currentImageUrl})`
      : userText

    const res = await aiService.chat({
      message: promptMessage,
      pageContext: docText,
      imageUrl: currentImageUrl || undefined,
      history: [],
    })

    messages.value.push({
      role: 'assistant',
      content: res.reply,
    })
  } catch {
    messages.value.push({
      role: 'assistant',
      content: 'Failed to connect to AI assistant. Please try again.',
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

async function sendQuickAction(actionType: string) {
  loading.value = true

  const docText = workspaceStore.documentBlocks
    .map((b) => b.content.text)
    .filter(Boolean)
    .join('\n')

  try {
    const res = await aiService.generate({
      actionType,
      contextText: docText,
    })

    messages.value.push({ role: 'assistant', content: res.result })
  } catch {
    messages.value.push({
      role: 'assistant',
      content: 'Failed to generate AI request.',
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

async function insertImageToDoc(url: string) {
  await workspaceStore.createBlock({
    type: 'image',
    content: {
      url,
      caption: 'AI Copilot Attachment',
      width: '100%',
      alignment: 'center',
    },
    position: workspaceStore.documentBlocks.length,
  })
}

async function insertMsgToDoc(text: string) {
  const lines = text.split('\n').filter(Boolean)
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
}
</script>
