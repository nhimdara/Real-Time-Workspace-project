<template>
  <div class="flex h-screen overflow-hidden bg-white dark:bg-[#191919] text-[#37352f] dark:text-[#d4d4d4] font-sans">
    <!-- Notion-Style Sidebar -->
    <aside
      class="h-full flex flex-col transition-all duration-200 ease-in-out border-r border-slate-200 dark:border-[#2f2f2f] bg-[#f7f6f3] dark:bg-[#202020] z-20 select-none text-[13px]"
      :class="isSidebarCollapsed ? 'w-14' : 'w-64'"
    >
      <!-- Workspace Switcher Header -->
      <div class="p-3 flex items-center justify-between border-b border-slate-200/60 dark:border-[#2a2a2a]">
        <div v-if="!isSidebarCollapsed" class="flex items-center gap-2.5 min-w-0">
          <div class="w-6 h-6 rounded-lg bg-brand-600 text-white flex items-center justify-center font-bold text-xs shadow-sm shrink-0">
            {{ currentWorkspace?.name.charAt(0) || 'W' }}
          </div>
          <div class="min-w-0">
            <h2 class="text-xs font-semibold truncate text-slate-800 dark:text-slate-200">
              {{ currentWorkspace?.name || 'Workspace' }}
            </h2>
          </div>
        </div>

        <button
          class="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-200/70 dark:hover:bg-[#2f2f2f]"
          :title="isSidebarCollapsed ? 'Expand Sidebar' : 'Collapse Sidebar'"
          @click="isSidebarCollapsed = !isSidebarCollapsed"
        >
          <PanelLeftClose v-if="!isSidebarCollapsed" class="w-4 h-4" />
          <PanelLeftOpen v-else class="w-4 h-4" />
        </button>
      </div>

      <!-- Quick Find, AI & System Links -->
      <div v-if="!isSidebarCollapsed" class="px-2 pt-2 space-y-0.5">
        <!-- Quick Search -->
        <button
          class="w-full px-2.5 py-1.5 rounded-lg flex items-center justify-between text-slate-600 dark:text-slate-400 hover:bg-slate-200/60 dark:hover:bg-[#2a2a2a] transition-colors group text-xs"
          @click="showCommandPalette = true"
        >
          <div class="flex items-center gap-2">
            <Search class="w-3.5 h-3.5" />
            <span>Search</span>
          </div>
          <kbd class="text-[10px] font-mono text-slate-400 dark:text-slate-500 bg-slate-200 dark:bg-[#333] px-1.5 py-0.5 rounded">Ctrl+K</kbd>
        </button>

        <!-- Notion AI Search -->
        <button
          class="w-full px-2.5 py-1.5 rounded-lg flex items-center gap-2 text-purple-600 dark:text-purple-400 hover:bg-purple-50 dark:hover:bg-purple-950/30 transition-colors text-xs font-medium"
          @click="showCommandPalette = true"
        >
          <Sparkles class="w-3.5 h-3.5" />
          <span>Ask AI</span>
        </button>

        <!-- Settings & Members -->
        <button
          class="w-full px-2.5 py-1.5 rounded-lg flex items-center gap-2 text-slate-600 dark:text-slate-400 hover:bg-slate-200/60 dark:hover:bg-[#2a2a2a] transition-colors text-xs"
          @click="showInviteModal = true"
        >
          <Settings class="w-3.5 h-3.5" />
          <span>Settings & Members</span>
        </button>
      </div>

      <!-- Navigation & Pages Tree -->
      <div class="flex-1 overflow-y-auto px-2 py-3 space-y-4">
        <!-- Favorites Section -->
        <div v-if="!isSidebarCollapsed && favoritePages.length > 0">
          <div class="px-2 py-1 text-[11px] font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1">
            <Star class="w-3 h-3 fill-amber-400 text-amber-400" />
            <span>Favorites</span>
          </div>
          <div class="space-y-0.5 mt-0.5">
            <button
              v-for="page in favoritePages"
              :key="'fav-' + page.id"
              class="w-full flex items-center justify-between px-2.5 py-1.5 rounded-lg text-xs transition-colors"
              :class="currentPage?.id === page.id ? 'bg-slate-200/80 dark:bg-[#2f2f2f] text-slate-900 dark:text-white font-medium' : 'text-slate-600 dark:text-slate-400 hover:bg-slate-200/60 dark:hover:bg-[#2a2a2a]'"
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
          <div class="flex items-center justify-between px-2 py-1 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
            <span>Pages</span>
            <button
              class="p-1 rounded hover:bg-slate-200 dark:hover:bg-[#2f2f2f] text-slate-500 hover:text-brand-500 transition-colors"
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
              class="group/page flex items-center justify-between px-2 py-1.5 rounded-lg text-xs transition-colors"
              :class="currentPage?.id === page.id ? 'bg-slate-200/90 dark:bg-[#2f2f2f] text-slate-900 dark:text-white font-medium' : 'text-slate-600 dark:text-slate-400 hover:bg-slate-200/60 dark:hover:bg-[#2a2a2a]'"
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
                  class="p-0.5 rounded text-slate-400 hover:text-amber-400"
                  :title="isFavorite(page.id) ? 'Remove Favorite' : 'Add to Favorites'"
                  @click.stop="toggleFavorite(page.id)"
                >
                  <Star class="w-3 h-3" :class="isFavorite(page.id) ? 'fill-amber-400 text-amber-400' : ''" />
                </button>
                <button
                  class="p-0.5 rounded text-slate-400 hover:text-red-500"
                  title="Delete page"
                  @click.stop="deletePage(page.id)"
                >
                  <Trash2 class="w-3 h-3" />
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
            class="w-9 h-9 rounded-xl flex items-center justify-center text-base transition-colors"
            :class="currentPage?.id === page.id ? 'bg-slate-200 dark:bg-[#2f2f2f]' : 'hover:bg-slate-200/60 dark:hover:bg-[#2a2a2a]'"
            :title="page.title"
            @click="selectPage(page.id)"
          >
            {{ page.icon || '📄' }}
          </button>
        </div>
      </div>

      <!-- Bottom New Page Button & User Bar -->
      <div class="p-2 border-t border-slate-200/60 dark:border-[#2a2a2a] bg-[#f7f6f3] dark:bg-[#202020]">
        <!-- + New Page Button -->
        <button
          v-if="!isSidebarCollapsed"
          class="w-full py-1.5 px-3 mb-2 rounded-lg border border-dashed border-slate-300 dark:border-[#353535] text-xs font-medium text-slate-600 dark:text-slate-400 hover:border-brand-500 hover:text-brand-500 dark:hover:border-brand-500 dark:hover:text-brand-400 flex items-center justify-center gap-1.5 transition-all"
          @click="showCreatePageModal = true"
        >
          <Plus class="w-3.5 h-3.5" />
          <span>New page</span>
        </button>

        <div v-if="!isSidebarCollapsed" class="flex items-center justify-between px-1">
          <div class="flex items-center gap-2 min-w-0">
            <img
              :src="authStore.user?.avatarUrl"
              class="w-6 h-6 rounded-full object-cover border border-slate-300 dark:border-slate-700 shrink-0"
              alt="Avatar"
            />
            <span class="text-xs font-medium truncate text-slate-800 dark:text-slate-200">
              {{ authStore.user?.name }}
            </span>
          </div>

          <div class="flex items-center gap-1">
            <button
              class="p-1 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-200 dark:hover:bg-[#2f2f2f]"
              title="Toggle Dark Mode"
              @click="toggleDarkMode"
            >
              <Sun v-if="isDark" class="w-3.5 h-3.5" />
              <Moon v-else class="w-3.5 h-3.5" />
            </button>
            <button
              class="p-1 rounded-lg text-slate-400 hover:text-red-500 hover:bg-slate-200 dark:hover:bg-[#2f2f2f]"
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
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden bg-white dark:bg-[#191919]">
      <!-- Notion Top Navigation Bar -->
      <header class="h-12 border-b border-slate-200/60 dark:border-[#2a2a2a] px-6 flex items-center justify-between z-10 select-none text-xs">
        <!-- Breadcrumb & Star -->
        <div class="flex items-center gap-2 text-slate-500 dark:text-slate-400 min-w-0">
          <span class="truncate hover:text-slate-900 dark:hover:text-slate-200 cursor-pointer">{{ currentWorkspace?.name }}</span>
          <span>/</span>
          <div class="flex items-center gap-1.5 font-medium text-slate-900 dark:text-slate-100 truncate">
            <span>{{ currentPage?.icon || '📄' }}</span>
            <span class="truncate">{{ currentPage?.title || 'Untitled' }}</span>
          </div>

          <button
            v-if="currentPage"
            class="p-1 rounded text-slate-400 hover:text-amber-400 transition-colors ml-1"
            title="Favorite Page"
            @click="toggleFavorite(currentPage.id)"
          >
            <Star class="w-3.5 h-3.5" :class="isFavorite(currentPage.id) ? 'fill-amber-400 text-amber-400' : ''" />
          </button>
        </div>

        <!-- Right Action Items: View Toggle, Online Peers, Share, Options -->
        <div class="flex items-center gap-3">
          <!-- Active Online Collaboration Avatars -->
          <div class="flex items-center -space-x-1.5">
            <div
              v-for="peer in activePeers"
              :key="peer.userId"
              class="relative group"
            >
              <img
                :src="peer.userAvatar"
                class="w-6 h-6 rounded-full object-cover border-2 border-white dark:border-[#191919] shadow-sm"
                :title="peer.userName"
              />
              <span
                class="absolute bottom-0 right-0 w-1.5 h-1.5 rounded-full ring-1 ring-white dark:ring-[#191919]"
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
          <div class="flex items-center bg-slate-100 dark:bg-[#202020] p-0.5 rounded-lg border border-slate-200/80 dark:border-[#2f2f2f]">
            <button
              class="px-2 py-1 rounded-md transition-all flex items-center gap-1"
              :class="!currentPage?.isKanban ? 'bg-white dark:bg-[#2f2f2f] text-slate-900 dark:text-white font-semibold shadow-sm' : 'text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'"
              @click="togglePageType(false)"
            >
              <FileText class="w-3.5 h-3.5" />
              <span>Doc</span>
            </button>
            <button
              class="px-2 py-1 rounded-md transition-all flex items-center gap-1"
              :class="currentPage?.isKanban ? 'bg-white dark:bg-[#2f2f2f] text-slate-900 dark:text-white font-semibold shadow-sm' : 'text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'"
              @click="togglePageType(true)"
            >
              <Kanban class="w-3.5 h-3.5" />
              <span>Board</span>
            </button>
          </div>

          <!-- Share Button -->
          <button
            class="px-3 py-1 rounded-lg font-semibold text-slate-700 dark:text-slate-200 bg-slate-100 dark:bg-[#202020] hover:bg-slate-200 dark:hover:bg-[#2a2a2a] transition-colors flex items-center gap-1.5"
            @click="showInviteModal = true"
          >
            <Share2 class="w-3.5 h-3.5" />
            <span>Share</span>
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

    <!-- Create Page Modal -->
    <div
      v-if="showCreatePageModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm animate-fade-in"
      @click.self="showCreatePageModal = false"
    >
      <div class="w-full max-w-sm p-6 rounded-3xl bg-white dark:bg-[#202020] border border-slate-200 dark:border-[#2f2f2f] shadow-2xl animate-scale-in">
        <h3 class="text-base font-bold text-slate-900 dark:text-slate-100 mb-4">Create New Page</h3>
        <label class="block text-xs font-medium text-slate-600 dark:text-slate-400 mb-1.5">Page Title</label>
        <input
          v-model="newPageTitle"
          type="text"
          placeholder="e.g. Project Roadmap"
          class="w-full px-3.5 py-2 rounded-xl text-xs bg-slate-100 dark:bg-[#2a2a2a] border border-slate-200 dark:border-[#353535] text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-brand-500 outline-none mb-4"
          @keydown.enter="submitCreatePage"
        />

        <label class="block text-xs font-medium text-slate-600 dark:text-slate-400 mb-1.5">Format</label>
        <div class="grid grid-cols-2 gap-2 mb-6">
          <button
            type="button"
            class="p-3 rounded-2xl border text-xs font-semibold flex flex-col items-center gap-1.5 transition-all"
            :class="!newPageIsKanban ? 'border-brand-500 bg-brand-500/10 text-brand-600 dark:text-brand-400' : 'border-slate-200 dark:border-[#2f2f2f] text-slate-500'"
            @click="newPageIsKanban = false"
          >
            <FileText class="w-5 h-5" /> Document (Notion)
          </button>
          <button
            type="button"
            class="p-3 rounded-2xl border text-xs font-semibold flex flex-col items-center gap-1.5 transition-all"
            :class="newPageIsKanban ? 'border-brand-500 bg-brand-500/10 text-brand-600 dark:text-brand-400' : 'border-slate-200 dark:border-[#2f2f2f] text-slate-500'"
            @click="newPageIsKanban = true"
          >
            <Kanban class="w-5 h-5" /> Board (Trello)
          </button>
        </div>

        <div class="flex items-center justify-end gap-2">
          <button
            class="px-3.5 py-1.5 text-xs text-slate-500 hover:text-slate-700 dark:hover:text-slate-300"
            @click="showCreatePageModal = false"
          >
            Cancel
          </button>
          <button
            class="px-4 py-1.5 rounded-xl text-xs font-semibold text-white bg-brand-600 hover:bg-brand-500 shadow-md"
            @click="submitCreatePage"
          >
            Create Page
          </button>
        </div>
      </div>
    </div>

    <!-- Share / Invite Modal -->
    <div
      v-if="showInviteModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm animate-fade-in"
      @click.self="showInviteModal = false"
    >
      <div class="w-full max-w-sm p-6 rounded-3xl bg-white dark:bg-[#202020] border border-slate-200 dark:border-[#2f2f2f] shadow-2xl animate-scale-in">
        <h3 class="text-base font-bold text-slate-900 dark:text-slate-100 mb-1">Share & Invite</h3>
        <p class="text-xs text-slate-400 mb-4">Collaborate on this workspace in real time.</p>
        
        <label class="block text-xs font-medium text-slate-600 dark:text-slate-400 mb-1.5">Teammate Email</label>
        <input
          v-model="inviteEmail"
          type="email"
          placeholder="colleague@workspace.io"
          class="w-full px-3.5 py-2 rounded-xl text-xs bg-slate-100 dark:bg-[#2a2a2a] border border-slate-200 dark:border-[#353535] text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-brand-500 outline-none mb-4"
          @keydown.enter="submitInvite"
        />

        <div class="flex items-center justify-end gap-2">
          <button
            class="px-3.5 py-1.5 text-xs text-slate-500 hover:text-slate-700 dark:hover:text-slate-300"
            @click="showInviteModal = false"
          >
            Cancel
          </button>
          <button
            class="px-4 py-1.5 rounded-xl text-xs font-semibold text-white bg-brand-600 hover:bg-brand-500 shadow-md"
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
