<template>
  <div
    v-if="isOpen"
    class="fixed inset-y-0 right-0 z-50 w-80 bg-white dark:bg-[#202020] border-l border-slate-200 dark:border-[#2f2f2f] shadow-2xl flex flex-col animate-slide-left select-none text-xs"
  >
    <!-- Comments Header -->
    <div class="p-4 border-b border-slate-200 dark:border-[#2f2f2f] flex items-center justify-between">
      <div class="flex items-center gap-2 font-bold text-slate-800 dark:text-slate-200">
        <MessageSquare class="w-4 h-4 text-brand-500" />
        <span>Page Comments</span>
      </div>
      <button class="p-1 text-slate-400 hover:text-slate-700 dark:hover:text-slate-200" @click="emit('close')">
        <X class="w-4 h-4" />
      </button>
    </div>

    <!-- Comments List -->
    <div class="flex-1 overflow-y-auto p-4 space-y-4">
      <div
        v-for="(c, idx) in comments"
        :key="idx"
        class="p-3 rounded-2xl bg-slate-50 dark:bg-[#282828] border border-slate-100 dark:border-[#333] space-y-1.5"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <img :src="c.avatar" class="w-5 h-5 rounded-full object-cover" />
            <span class="font-semibold text-slate-800 dark:text-slate-200 text-xs">{{ c.author }}</span>
          </div>
          <span class="text-[10px] text-slate-400">{{ c.time }}</span>
        </div>
        <p class="text-xs text-slate-700 dark:text-slate-300 leading-relaxed">{{ c.text }}</p>
      </div>

      <div v-if="comments.length === 0" class="py-12 text-center text-slate-400">
        No comments yet. Start the conversation!
      </div>
    </div>

    <!-- Post Comment Input Box -->
    <div class="p-3 border-t border-slate-200 dark:border-[#2f2f2f] bg-[#fbfbfa] dark:bg-[#1a1a1a]">
      <div class="flex items-center gap-2">
        <input
          v-model="newCommentText"
          type="text"
          placeholder="Write a comment..."
          class="flex-1 px-3 py-2 rounded-xl text-xs bg-white dark:bg-[#282828] border border-slate-200 dark:border-[#3a3a3a] text-slate-900 dark:text-slate-100 outline-none focus:ring-1 focus:ring-brand-500"
          @keydown.enter="postComment"
        />
        <button
          class="p-2 rounded-xl bg-brand-600 hover:bg-brand-500 text-white transition-colors"
          :disabled="!newCommentText.trim()"
          @click="postComment"
        >
          <Send class="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { MessageSquare, X, Send } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const authStore = useAuthStore()
const newCommentText = ref('')

const comments = ref([
  {
    author: 'Sarah Chen',
    avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80',
    text: 'Love the new Notion block layout! Everything feels so smooth and fast.',
    time: '10m ago',
  },
  {
    author: 'Marcus Vance',
    avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
    text: 'WebSockets on port 8088 are working flawlessly. Tested multi-cursor sync.',
    time: '2m ago',
  },
])

function postComment() {
  if (!newCommentText.value.trim()) return

  comments.value.push({
    author: authStore.user?.name || 'Alex Morgan',
    avatar: authStore.user?.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
    text: newCommentText.value.trim(),
    time: 'Just now',
  })

  newCommentText.value = ''
}
</script>
