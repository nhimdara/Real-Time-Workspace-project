<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden text-slate-100">
    <!-- Ambient liquid background glow blobs -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -left-40 w-[500px] h-[500px] bg-gradient-to-tr from-brand-500/30 to-emerald-400/20 rounded-full blur-3xl animate-blob-1"></div>
      <div class="absolute -bottom-40 -right-40 w-[500px] h-[500px] bg-gradient-to-bl from-teal-400/25 to-indigo-500/20 rounded-full blur-3xl animate-blob-2"></div>
    </div>

    <div class="w-full max-w-md p-8 rounded-3xl liquid-glass-modal relative z-10 animate-scale-in">
      <!-- App Brand Logo -->
      <div class="flex items-center justify-center gap-3 mb-6">
        <div class="p-3 rounded-2xl bg-gradient-to-tr from-brand-500/30 to-emerald-400/20 text-brand-400 border border-white/40 dark:border-white/10 shadow-lg backdrop-blur-md">
          <Layers class="w-8 h-8" />
        </div>
        <div>
          <h1 class="text-2xl font-extrabold tracking-tight text-slate-900 dark:text-white">Real-Time Workspace</h1>
          <p class="text-xs font-medium text-slate-600 dark:text-slate-400">Notion Docs & Trello Kanban Hybrid</p>
        </div>
      </div>

      <!-- Tab Switcher -->
      <div class="flex p-1 mb-6 rounded-2xl liquid-glass border border-white/40 dark:border-white/10">
        <button
          class="flex-1 py-2 text-xs font-bold rounded-xl transition-all duration-200"
          :class="isLogin ? 'liquid-glass-btn text-white shadow-md' : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'"
          @click="isLogin = true"
        >
          Sign In
        </button>
        <button
          class="flex-1 py-2 text-xs font-bold rounded-xl transition-all duration-200"
          :class="!isLogin ? 'liquid-glass-btn text-white shadow-md' : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'"
          @click="isLogin = false"
        >
          Register
        </button>
      </div>

      <!-- Error Alert -->
      <div
        v-if="errorMessage"
        class="mb-4 p-3 rounded-xl bg-red-500/15 border border-red-500/30 text-red-500 dark:text-red-400 text-xs flex items-center gap-2 backdrop-blur-md"
      >
        <AlertTriangle class="w-4 h-4 shrink-0" />
        <span>{{ errorMessage }}</span>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div v-if="!isLogin">
          <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">Full Name</label>
          <input
            v-model="form.name"
            type="text"
            required
            placeholder="Alex Morgan"
            class="w-full px-4 py-2.5 text-xs rounded-xl liquid-glass-input text-slate-900 dark:text-slate-100 placeholder-slate-400 outline-none"
          />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">Email Address</label>
          <input
            v-model="form.email"
            type="email"
            required
            placeholder="alex@workspace.io"
            class="w-full px-4 py-2.5 text-xs rounded-xl liquid-glass-input text-slate-900 dark:text-slate-100 placeholder-slate-400 outline-none"
          />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">Password</label>
          <input
            v-model="form.password"
            type="password"
            required
            minlength="6"
            placeholder="••••••••"
            class="w-full px-4 py-2.5 text-xs rounded-xl liquid-glass-input text-slate-900 dark:text-slate-100 placeholder-slate-400 outline-none"
          />
        </div>

        <button
          type="submit"
          :disabled="authStore.loading"
          class="w-full py-3 rounded-xl text-xs font-bold text-white liquid-glass-btn disabled:opacity-50 transition-all mt-2"
        >
          {{ authStore.loading ? 'Authenticating...' : (isLogin ? 'Sign In' : 'Create Account') }}
        </button>
      </form>

      <!-- Demo One-Click Logins -->
      <div class="mt-6 pt-6 border-t border-white/40 dark:border-white/10">
        <p class="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider text-center mb-3">
          Quick Demo Accounts (1-Click Login)
        </p>
        <div class="grid grid-cols-3 gap-2.5">
          <button
            type="button"
            class="p-2.5 rounded-2xl liquid-glass-card text-center transition-all group border border-white/40 dark:border-white/10"
            @click="quickLogin('alex@workspace.io', 'password123')"
          >
            <img
              src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop&crop=faces"
              class="w-8 h-8 rounded-full mx-auto mb-1 border-2 border-brand-500/60 shadow-sm group-hover:scale-110 transition-transform"
            />
            <span class="text-[10px] font-bold block truncate text-slate-800 dark:text-slate-200">Alex (Lead)</span>
          </button>

          <button
            type="button"
            class="p-2.5 rounded-2xl liquid-glass-card text-center transition-all group border border-white/40 dark:border-white/10"
            @click="quickLogin('sarah@workspace.io', 'password123')"
          >
            <img
              src="https://images.unsplash.com/photo-1517841905240-472988babdf9?w=100&h=100&fit=crop&crop=faces"
              class="w-8 h-8 rounded-full mx-auto mb-1 border-2 border-brand-500/60 shadow-sm group-hover:scale-110 transition-transform"
            />
            <span class="text-[10px] font-bold block truncate text-slate-800 dark:text-slate-200">Sarah (Dev)</span>
          </button>

          <button
            type="button"
            class="p-2.5 rounded-2xl liquid-glass-card text-center transition-all group border border-white/40 dark:border-white/10"
            @click="quickLogin('marcus@workspace.io', 'password123')"
          >
            <img
              src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=faces"
              class="w-8 h-8 rounded-full mx-auto mb-1 border-2 border-brand-500/60 shadow-sm group-hover:scale-110 transition-transform"
            />
            <span class="text-[10px] font-bold block truncate text-slate-800 dark:text-slate-200">Marcus (UX)</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Layers, AlertTriangle } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useWorkspaceStore } from '@/stores/workspace'

const authStore = useAuthStore()
const workspaceStore = useWorkspaceStore()

const isLogin = ref(true)
const errorMessage = ref<string | null>(null)

const form = reactive({
  name: '',
  email: '',
  password: '',
})

async function handleSubmit() {
  errorMessage.value = null
  try {
    if (isLogin.value) {
      await authStore.login({
        email: form.email,
        password: form.password,
      })
    } else {
      await authStore.register({
        name: form.name,
        email: form.email,
        password: form.password,
      })
    }
    await workspaceStore.fetchWorkspaces()
  } catch (err: any) {
    errorMessage.value = authStore.error || 'Authentication error'
  }
}

async function quickLogin(email: string, pass: string) {
  form.email = email
  form.password = pass
  isLogin.value = true
  await handleSubmit()
}
</script>
