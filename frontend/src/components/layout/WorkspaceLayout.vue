<template>
  <div class="flex h-screen overflow-hidden text-[#37352f] dark:text-[#e2e8f0] font-sans relative">
    <!-- Mobile Sidebar Backdrop Overlay -->
    <div
      v-if="isMobileSidebarOpen"
      class="fixed inset-0 bg-slate-950/60 backdrop-blur-xs z-30 md:hidden animate-fade-in"
      @click="isMobileSidebarOpen = false"
    ></div>

    <!-- Liquid Glass Sidebar -->
    <aside
      class="h-full flex flex-col transition-all duration-300 ease-in-out border-r border-white/40 dark:border-white/10 bg-white/70 dark:bg-[#0f172a]/90 backdrop-blur-2xl z-40 select-none text-[13px] shadow-2xl shadow-black/5 fixed md:static inset-y-0 left-0"
      :class="[
        isSidebarCollapsed ? 'w-14' : 'w-64',
        isMobileSidebarOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
      ]"
    >
      <!-- Workspace Switcher Header -->
      <div class="p-3 flex items-center justify-between border-b border-white/30 dark:border-white/10">
        <div v-if="!isSidebarCollapsed" class="flex items-center gap-2.5 min-w-0">
          <div class="w-7 h-7 rounded-xl bg-gradient-to-tr from-brand-600 to-emerald-400 text-white flex items-center justify-center font-bold text-xs shadow-md shadow-brand-500/30 border border-white/40 shrink-0">
            {{ currentWorkspace?.name.charAt(0) || 'W' }}
          </div>
          <div class="min-w-0">
            <h2 class="text-xs font-bold truncate text-slate-800 dark:text-slate-100 tracking-tight">
              {{ currentWorkspace?.name || 'Workspace' }}
            </h2>
            <span class="text-[10px] text-brand-600 dark:text-brand-400 font-medium block">Pro Workspace</span>
          </div>
        </div>

        <button
          class="p-1.5 rounded-xl text-slate-400 hover:text-slate-700 dark:hover:text-slate-100 hover:bg-white/50 dark:hover:bg-white/10 transition-colors backdrop-blur-sm"
          :title="isSidebarCollapsed ? 'Expand Sidebar' : 'Collapse Sidebar'"
          @click="isSidebarCollapsed = !isSidebarCollapsed"
        >
          <PanelLeftClose v-if="!isSidebarCollapsed" class="w-4 h-4" />
          <PanelLeftOpen v-else class="w-4 h-4" />
        </button>
      </div>

      <!-- Quick Find, AI & System Links -->
      <div v-if="!isSidebarCollapsed" class="px-2.5 pt-2.5 space-y-1">
        <!-- Quick Search -->
        <button
          class="w-full px-3 py-1.5 rounded-xl flex items-center justify-between text-slate-600 dark:text-slate-300 bg-white/40 dark:bg-white/5 hover:bg-white/80 dark:hover:bg-white/10 border border-white/40 dark:border-white/10 backdrop-blur-md transition-all shadow-sm group text-xs"
          @click="showCommandPalette = true"
        >
          <div class="flex items-center gap-2">
            <Search class="w-3.5 h-3.5 text-brand-500" />
            <span>Search</span>
          </div>
          <kbd class="text-[10px] font-mono text-slate-500 dark:text-slate-400 bg-white/60 dark:bg-slate-800/80 px-1.5 py-0.5 rounded-md border border-white/40 dark:border-slate-700 shadow-2xs">Ctrl+K</kbd>
        </button>

        <!-- Notion AI Search -->
        <button
          class="w-full px-3 py-1.5 rounded-xl flex items-center gap-2 text-purple-600 dark:text-purple-300 bg-purple-500/10 hover:bg-purple-500/20 border border-purple-500/30 backdrop-blur-md transition-all text-xs font-semibold shadow-sm"
          @click="showCommandPalette = true"
        >
          <Sparkles class="w-3.5 h-3.5 text-purple-500 animate-pulse" />
          <span>Ask AI</span>
        </button>

        <!-- Settings & Members -->
        <button
          class="w-full px-3 py-1.5 rounded-xl flex items-center gap-2 text-slate-600 dark:text-slate-300 hover:bg-white/60 dark:hover:bg-white/10 transition-colors text-xs"
          @click="showInviteModal = true"
        >
          <Settings class="w-3.5 h-3.5" />
          <span>Settings & Members</span>
        </button>
      </div>

      <!-- Navigation & Pages Tree -->
      <div class="flex-1 overflow-y-auto px-2.5 py-3 space-y-4">
        <!-- Favorites Section -->
        <div v-if="!isSidebarCollapsed && favoritePages.length > 0">
          <div class="px-2 py-1 text-[10px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider flex items-center gap-1">
            <Star class="w-3 h-3 fill-amber-400 text-amber-400" />
            <span>Favorites</span>
          </div>
          <div class="space-y-0.5 mt-0.5">
            <button
              v-for="page in favoritePages"
              :key="'fav-' + page.id"
              class="w-full flex items-center justify-between px-2.5 py-1.5 rounded-xl text-xs transition-all"
              :class="currentPage?.id === page.id ? 'bg-white/80 dark:bg-white/15 text-slate-900 dark:text-white font-bold border border-white/60 dark:border-white/20 shadow-sm' : 'text-slate-600 dark:text-slate-300 hover:bg-white/50 dark:hover:bg-white/10'"
              @click="selectPage(page.id)"
            >
              <div class="flex items-center gap-2 min-w-0">
                <span class="text-sm shrink-0">{{ page.icon || '📄' }}</span>
                <span class="truncate">{{ page.title || 'Untitled' }}</span>
              </div>
            </button>
          </div>
        </div>

        <!-- Teamspaces / Pages Section -->
        <div v-if="!isSidebarCollapsed">
          <div class="flex items-center justify-between px-2 py-1 text-[10px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider">
            <span>Pages</span>
            <button
              class="p-1 rounded-lg hover:bg-white/60 dark:hover:bg-white/15 text-slate-400 hover:text-brand-500 transition-colors"
              title="Add Page"
              @click="showCreatePageModal = true"
            >
              <Plus class="w-3.5 h-3.5" />
            </button>
          </div>

          <!-- Page Tree -->
          <div class="space-y-0.5 mt-0.5">
            <div
              v-for="page in pages"
              :key="page.id"
              class="group/page flex items-center justify-between px-2.5 py-1.5 rounded-xl text-xs transition-all"
              :class="currentPage?.id === page.id ? 'bg-white/80 dark:bg-white/15 text-slate-900 dark:text-white font-bold border border-white/60 dark:border-white/20 shadow-sm' : 'text-slate-600 dark:text-slate-300 hover:bg-white/50 dark:hover:bg-white/10'"
            >
              <button
                class="flex-1 flex items-center gap-2 min-w-0 text-left"
                @click="selectPage(page.id)"
              >
                <span class="text-sm shrink-0">{{ page.icon || (page.isKanban ? '📊' : '📄') }}</span>
                <span class="truncate">{{ page.title || 'Untitled' }}</span>
              </button>

              <!-- Page Quick Actions -->
              <div class="opacity-0 group-hover/page:opacity-100 flex items-center gap-1 shrink-0">
                <button
                  class="p-0.5 rounded text-slate-400 hover:text-amber-400 transition-colors"
                  :title="isFavorite(page.id) ? 'Remove Favorite' : 'Add to Favorites'"
                  @click.stop="toggleFavorite(page.id)"
                >
                  <Star class="w-3 h-3" :class="isFavorite(page.id) ? 'fill-amber-400 text-amber-400' : ''" />
                </button>
                <button
                  class="p-0.5 rounded text-slate-400 hover:text-red-500 transition-colors"
                  title="Delete page"
                  @click.stop="deletePage(page.id)"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Collapsed Sidebar Icons -->
        <div v-else class="space-y-2 flex flex-col items-center">
          <button
            v-for="page in pages"
            :key="page.id"
            class="w-9 h-9 rounded-2xl flex items-center justify-center text-base transition-all"
            :class="currentPage?.id === page.id ? 'bg-white/80 dark:bg-white/20 shadow-md border border-white/50 dark:border-white/20' : 'hover:bg-white/50 dark:hover:bg-white/10'"
            :title="page.title"
            @click="selectPage(page.id)"
          >
            {{ page.icon || '📄' }}
          </button>
        </div>
      </div>

      <!-- Bottom New Page Button & User Bar -->
      <div class="p-2.5 border-t border-white/30 dark:border-white/10">
        <!-- + New Page Button -->
        <button
          v-if="!isSidebarCollapsed"
          class="w-full py-2 px-3 mb-2.5 rounded-xl border border-dashed border-slate-300/80 dark:border-slate-700/80 text-xs font-semibold text-slate-600 dark:text-slate-300 hover:border-brand-500 hover:text-brand-500 dark:hover:border-brand-400 dark:hover:text-brand-400 bg-white/30 dark:bg-white/5 hover:bg-white/60 dark:hover:bg-white/10 flex items-center justify-center gap-1.5 transition-all shadow-sm"
          @click="showCreatePageModal = true"
        >
          <Plus class="w-3.5 h-3.5" />
          <span>New page</span>
        </button>

        <div v-if="!isSidebarCollapsed" class="flex items-center justify-between px-1">
          <div class="flex items-center gap-2 min-w-0">
            <img
              :src="authStore.user?.avatarUrl"
              class="w-7 h-7 rounded-full object-cover border-2 border-white/60 dark:border-white/20 shadow-sm shrink-0"
              alt="Avatar"
            />
            <span class="text-xs font-semibold truncate text-slate-800 dark:text-slate-200">
              {{ authStore.user?.name }}
            </span>
          </div>

          <div class="flex items-center gap-1">
            <button
              class="p-1.5 rounded-xl text-slate-400 hover:text-slate-700 dark:hover:text-slate-100 hover:bg-white/50 dark:hover:bg-white/10 transition-colors"
              title="Toggle Dark Mode"
              @click="toggleDarkMode"
            >
              <Sun v-if="isDark" class="w-3.5 h-3.5 text-amber-400" />
              <Moon v-else class="w-3.5 h-3.5" />
            </button>
            <button
              class="p-1.5 rounded-xl text-slate-400 hover:text-red-500 hover:bg-white/50 dark:hover:bg-white/10 transition-colors"
              title="Sign Out"
              @click="authStore.logout()"
            >
              <LogOut class="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Notion Canvas -->
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- Notion Top Navigation Bar (Liquid Glass) -->
      <header class="h-12 border-b border-white/30 dark:border-white/10 bg-white/40 dark:bg-[#0f172a]/50 backdrop-blur-xl px-3 md:px-6 flex items-center justify-between z-10 select-none text-xs shadow-sm">
        <!-- Breadcrumb & Mobile Menu Toggle -->
        <div class="flex items-center gap-2 text-slate-500 dark:text-slate-400 min-w-0">
          <button
            class="md:hidden p-1.5 rounded-xl text-slate-600 dark:text-slate-200 hover:bg-white/50 dark:hover:bg-white/10 transition-colors shrink-0"
            title="Toggle Menu"
            @click="isMobileSidebarOpen = !isMobileSidebarOpen"
          >
            <Menu class="w-4 h-4" />
          </button>

          <span class="truncate hover:text-slate-900 dark:hover:text-slate-200 cursor-pointer font-medium hidden sm:inline">{{ currentWorkspace?.name }}</span>
          <span class="opacity-40 hidden sm:inline">/</span>
          <div class="flex items-center gap-1.5 font-bold text-slate-900 dark:text-slate-100 truncate">
            <span>{{ currentPage?.icon || '📄' }}</span>
            <span class="truncate text-xs md:text-xs">{{ currentPage?.title || 'Untitled' }}</span>
          </div>

          <button
            v-if="currentPage"
            class="p-1 rounded-lg text-slate-400 hover:text-amber-400 transition-colors ml-1"
            title="Favorite Page"
            @click="toggleFavorite(currentPage.id)"
          >
            <Star class="w-3.5 h-3.5" :class="isFavorite(currentPage.id) ? 'fill-amber-400 text-amber-400' : ''" />
          </button>
        </div>

        <!-- Right Action Items: View Toggle, Online Peers, Share, Options -->
        <div class="flex items-center gap-1.5 sm:gap-3 shrink-0">
          <!-- Active Online Collaboration Avatars -->
          <div class="hidden sm:flex items-center -space-x-1.5">
            <div
              v-for="peer in activePeers"
              :key="peer.userId"
              class="relative group"
            >
              <img
                :src="peer.userAvatar"
                class="w-6 h-6 rounded-full object-cover border-2 border-white dark:border-[#0f172a] shadow-sm"
                :title="peer.userName"
              />
              <span
                class="absolute bottom-0 right-0 w-1.5 h-1.5 rounded-full ring-1 ring-white dark:ring-[#0f172a]"
                :style="{ backgroundColor: peer.color || '#22c55e' }"
              ></span>
            </div>

            <!-- Current User Avatar -->
            <div class="relative group pl-1">
              <img
                :src="authStore.user?.avatarUrl"
                class="w-6 h-6 rounded-full object-cover border-2 border-brand-500 shadow-sm"
                :title="authStore.user?.name + ' (You)'"
              />
            </div>
          </div>

          <!-- Document vs Kanban Mode Switcher -->
          <div class="flex items-center bg-white/40 dark:bg-white/10 p-0.5 sm:p-1 rounded-xl border border-white/40 dark:border-white/10 backdrop-blur-md shadow-2xs shrink-0">
            <button
              class="px-2 sm:px-2.5 py-1 rounded-lg transition-all flex items-center gap-1 sm:gap-1.5 text-xs font-semibold whitespace-nowrap"
              :class="!currentPage?.isKanban ? 'bg-white dark:bg-slate-800 text-slate-900 dark:text-white shadow-md border border-white/60 dark:border-white/10' : 'text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'"
              @click="togglePageType(false)"
            >
              <FileText class="w-3.5 h-3.5 shrink-0" />
              <span class="hidden xs:inline">Doc</span>
            </button>
            <button
              class="px-2 sm:px-2.5 py-1 rounded-lg transition-all flex items-center gap-1 sm:gap-1.5 text-xs font-semibold whitespace-nowrap"
              :class="currentPage?.isKanban ? 'bg-white dark:bg-slate-800 text-slate-900 dark:text-white shadow-md border border-white/60 dark:border-white/10' : 'text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'"
              @click="togglePageType(true)"
            >
              <Kanban class="w-3.5 h-3.5 shrink-0" />
              <span class="hidden xs:inline">Board</span>
            </button>
          </div>

          <!-- Share Button -->
          <button
            class="px-2.5 sm:px-3 py-1.5 rounded-xl font-bold text-slate-700 dark:text-slate-200 bg-white/50 dark:bg-white/10 hover:bg-white/80 dark:hover:bg-white/20 border border-white/40 dark:border-white/10 backdrop-blur-md transition-all flex items-center gap-1 sm:gap-1.5 shadow-2xs whitespace-nowrap shrink-0 text-xs"
            @click="showInviteModal = true"
          >
            <Share2 class="w-3.5 h-3.5 text-brand-500 shrink-0" />
            <span class="hidden xs:inline">Share</span>
          </button>
        </div>
      </header>

      <!-- Main Document Scrollable Canvas -->
      <main class="flex-1 overflow-y-auto">
        <slot />
      </main>
    </div>

    <!-- Quick Find / Command Palette Modal -->
    <CommandPalette
      :is-open="showCommandPalette"
      @close="showCommandPalette = false"
    />

    <!-- Create Page Modal (Liquid Glass) -->
    <div
      v-if="showCreatePageModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 backdrop-blur-md animate-fade-in"
      @click.self="showCreatePageModal = false"
    >
      <div class="w-full max-w-sm p-6 rounded-3xl liquid-glass-modal animate-scale-in">
        <h3 class="text-base font-bold text-slate-900 dark:text-slate-100 mb-4">Create New Page</h3>
        <label class="block text-xs font-medium text-slate-600 dark:text-slate-400 mb-1.5">Page Title</label>
        <input
          v-model="newPageTitle"
          type="text"
          placeholder="e.g. Project Roadmap"
          class="w-full px-3.5 py-2.5 rounded-xl text-xs liquid-glass-input text-slate-900 dark:text-slate-100 outline-none mb-4"
          @keydown.enter="submitCreatePage"
        />

        <label class="block text-xs font-medium text-slate-600 dark:text-slate-400 mb-1.5">Format</label>
        <div class="grid grid-cols-2 gap-2 mb-6">
          <button
            type="button"
            class="p-3 rounded-2xl border text-xs font-bold flex flex-col items-center gap-1.5 transition-all"
            :class="!newPageIsKanban ? 'border-brand-500 bg-brand-500/15 text-brand-600 dark:text-brand-400 shadow-md shadow-brand-500/10' : 'border-white/30 dark:border-white/10 bg-white/20 dark:bg-white/5 text-slate-500'"
            @click="newPageIsKanban = false"
          >
            <FileText class="w-5 h-5" /> Document (Notion)
          </button>
          <button
            type="button"
            class="p-3 rounded-2xl border text-xs font-bold flex flex-col items-center gap-1.5 transition-all"
            :class="newPageIsKanban ? 'border-brand-500 bg-brand-500/15 text-brand-600 dark:text-brand-400 shadow-md shadow-brand-500/10' : 'border-white/30 dark:border-white/10 bg-white/20 dark:bg-white/5 text-slate-500'"
            @click="newPageIsKanban = true"
          >
            <Kanban class="w-5 h-5" /> Board (Trello)
          </button>
        </div>

        <div class="flex items-center justify-end gap-2">
          <button
            class="px-3.5 py-2 text-xs font-medium text-slate-500 hover:text-slate-700 dark:hover:text-slate-300"
            @click="showCreatePageModal = false"
          >
            Cancel
          </button>
          <button
            class="px-4 py-2 rounded-xl text-xs font-bold text-white liquid-glass-btn"
            @click="submitCreatePage"
          >
            Create Page
          </button>
        </div>
      </div>
    </div>

    <!-- Share / Invite Modal (Liquid Glass) -->
    <div
      v-if="showInviteModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 backdrop-blur-md animate-fade-in"
      @click.self="showInviteModal = false"
    >
      <div class="w-full max-w-sm p-6 rounded-3xl liquid-glass-modal animate-scale-in">
        <h3 class="text-base font-bold text-slate-900 dark:text-slate-100 mb-1">Share & Invite</h3>
        <p class="text-xs text-slate-400 mb-4">Collaborate on this workspace in real time.</p>
        
        <label class="block text-xs font-medium text-slate-600 dark:text-slate-400 mb-1.5">Teammate Email</label>
        <input
          v-model="inviteEmail"
          type="email"
          placeholder="colleague@workspace.io"
          class="w-full px-3.5 py-2.5 rounded-xl text-xs liquid-glass-input text-slate-900 dark:text-slate-100 outline-none mb-4"
          @keydown.enter="submitInvite"
        />

        <div class="flex items-center justify-end gap-2">
          <button
            class="px-3.5 py-2 text-xs font-medium text-slate-500 hover:text-slate-700 dark:hover:text-slate-300"
            @click="showInviteModal = false"
          >
            Cancel
          </button>
          <button
            class="px-4 py-2 rounded-xl text-xs font-bold text-white liquid-glass-btn"
            @click="submitInvite"
          >
            Send Invite
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  PanelLeftClose,
  PanelLeftOpen,
  Menu,
  Search,
  Sparkles,
  Settings,
  Star,
  Plus,
  Trash2,
  FileText,
  Kanban,
  Share2,
  Sun,
  Moon,
  LogOut,
} from 'lucide-vue-next'
import { useWorkspaceStore } from '@/stores/workspace'
import { useAuthStore } from '@/stores/auth'
import CommandPalette from '@/components/common/CommandPalette.vue'

const workspaceStore = useWorkspaceStore()
const authStore = useAuthStore()

const isSidebarCollapsed = ref(false)
const isMobileSidebarOpen = ref(false)
const isDark = ref(document.documentElement.classList.contains('dark'))
const showCommandPalette = ref(false)

// Modals
const showCreatePageModal = ref(false)
const newPageTitle = ref('')
const newPageIsKanban = ref(false)

const showInviteModal = ref(false)
const inviteEmail = ref('')

// Favorite pages stored in localStorage
const favorites = ref<string[]>(
  JSON.parse(localStorage.getItem('workspace_favorites') || '[]')
)

const currentWorkspace = computed(() => workspaceStore.currentWorkspace)
const pages = computed(() => workspaceStore.pages)
const currentPage = computed(() => workspaceStore.currentPage)
const activePeers = computed(() => workspaceStore.activePeers)

const favoritePages = computed(() => {
  return pages.value.filter((p) => favorites.value.includes(p.id))
})

function isFavorite(pageId: string) {
  return favorites.value.includes(pageId)
}

function toggleFavorite(pageId: string) {
  if (favorites.value.includes(pageId)) {
    favorites.value = favorites.value.filter((id) => id !== pageId)
  } else {
    favorites.value.push(pageId)
  }
  localStorage.setItem('workspace_favorites', JSON.stringify(favorites.value))
}

function selectPage(pageId: string) {
  workspaceStore.selectPage(pageId)
}

function deletePage(pageId: string) {
  if (confirm('Delete this page?')) {
    workspaceStore.deletePage(pageId)
  }
}

function toggleDarkMode() {
  isDark.value = !isDark.value
  if (isDark.value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

async function togglePageType(isKanban: boolean) {
  if (currentPage.value) {
    await workspaceStore.updatePage(currentPage.value.id, {
      isKanban,
      icon: isKanban ? '📊' : '📄',
    })

    if (isKanban && workspaceStore.kanbanColumns.length === 0) {
      await workspaceStore.initializeDefaultKanbanBoard(currentPage.value.id)
    }
  }
}

async function submitCreatePage() {
  if (!newPageTitle.value.trim()) return

  await workspaceStore.createPage({
    title: newPageTitle.value.trim(),
    isKanban: newPageIsKanban.value,
    icon: newPageIsKanban.value ? '📊' : '📄',
  })

  newPageTitle.value = ''
  showCreatePageModal.value = false
}

async function submitInvite() {
  if (!inviteEmail.value.trim() || !currentWorkspace.value) return

  try {
    await workspaceStore.inviteMember(currentWorkspace.value.id, inviteEmail.value.trim())
    alert(`Invitation sent to ${inviteEmail.value}!`)
    inviteEmail.value = ''
    showInviteModal.value = false
  } catch (err: any) {
    alert(err.response?.data?.message || 'Failed to send invite')
  }
}
</script>
