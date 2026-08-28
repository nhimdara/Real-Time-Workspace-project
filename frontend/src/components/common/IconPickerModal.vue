<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm animate-fade-in"
    @click.self="close"
  >
    <div
      class="w-full max-w-sm rounded-3xl bg-white dark:bg-[#202020] border border-slate-200 dark:border-[#2f2f2f] shadow-2xl overflow-hidden p-5 animate-scale-in"
    >
      <div class="flex items-center justify-between mb-3">
        <h3 class="text-sm font-bold text-slate-900 dark:text-slate-100">Select Icon</h3>
        <button
          class="p-1 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200"
          @click="close"
        >
          <X class="w-4 h-4" />
        </button>
      </div>

      <!-- Search Filter -->
      <div class="flex items-center gap-2 px-3 py-2 rounded-xl bg-slate-100 dark:bg-[#2a2a2a] border border-slate-200 dark:border-[#353535] mb-4">
        <Search class="w-3.5 h-3.5 text-slate-400 shrink-0" />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Filter emojis..."
          class="w-full bg-transparent text-xs outline-none text-slate-900 dark:text-slate-100 placeholder-slate-400"
        />
      </div>

      <!-- Emoji Grid -->
      <div class="max-h-64 overflow-y-auto grid grid-cols-6 gap-2 p-1">
        <button
          v-for="emoji in filteredEmojis"
          :key="emoji"
          class="w-10 h-10 rounded-xl flex items-center justify-center text-xl hover:bg-slate-100 dark:hover:bg-[#2f2f2f] transition-transform hover:scale-110"
          @click="selectEmoji(emoji)"
        >
          {{ emoji }}
        </button>
      </div>

      <div class="mt-4 pt-3 border-t border-slate-200 dark:border-[#2f2f2f] flex justify-between items-center">
        <button
          class="text-xs text-red-500 hover:text-red-600 font-medium"
          @click="selectEmoji('')"
        >
          Remove icon
        </button>
        <button
          class="px-3 py-1.5 rounded-xl text-xs font-semibold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-[#2a2a2a]"
          @click="close"
        >
          Cancel
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { X, Search } from 'lucide-vue-next'

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits<{
  (e: 'select', emoji: string): void
  (e: 'close'): void
}>()

const searchQuery = ref('')

const allEmojis = [
  '🚀', '📄', '💡', '📊', '🔥', '🎯', '✨', '⚡', '💻', '📝', '🧠', '🛠️',
  '📚', '📅', '📌', '📎', '🔒', '🔑', '🎨', '🎬', '🎧', '🎸', '🎮', '🕹️',
  '🌟', '⭐', '🌈', '☀️', '🌙', '☁️', '🌊', '🌿', '🍀', '🍎', '☕', '🍕',
  '🏆', '🥇', '🎁', '🎈', '🎉', '🎊', '📈', '📉', '💼', '📦', '🏷️', '🔖',
  '😀', '😎', '🤖', '👾', '👻', '🦄', '🦁', '🐱', '🐶', '🦊', '🐼', '🐨'
]

const filteredEmojis = computed(() => {
  if (!searchQuery.value) return allEmojis
  return allEmojis // filter if keyword mapping added
})

function selectEmoji(emoji: string) {
  emit('select', emoji)
  close()
}

function close() {
  emit('close')
}
</script>
