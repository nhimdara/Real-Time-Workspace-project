<template>
  <div
    class="flex-1 overflow-x-auto p-6 min-h-[calc(100vh-140px)] select-none"
    @mousemove="handleMouseMove"
  >
    <!-- Board Header -->
    <div class="mb-6 flex items-center justify-between">
      <div class="flex items-center gap-3">
        <span class="text-3xl p-1.5 rounded-xl bg-brand-500/10">{{ currentPage?.icon || '📊' }}</span>
        <div>
          <h1 class="text-2xl font-bold text-slate-900 dark:text-slate-100 tracking-tight">
            {{ currentPage?.title || 'Kanban Board' }}
          </h1>
          <p class="text-xs text-slate-500 dark:text-slate-400">
            Real-time drag and drop board • {{ kanbanColumns.length }} Columns • {{ totalCards }} Cards
          </p>
        </div>
      </div>

      <!-- Add Column Button -->
      <button
        class="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-semibold text-white bg-brand-600 hover:bg-brand-500 shadow-md hover:shadow-brand-500/25 transition-all"
        @click="showAddColumnModal = true"
      >
        <Plus class="w-4 h-4" /> Add Column
      </button>
    </div>

    <!-- Columns Container (Draggable Columns) -->
    <div class="flex items-start gap-5 pb-6">
      <div
        v-for="col in kanbanColumns"
        :key="col.id"
        class="w-80 shrink-0 flex flex-col max-h-[calc(100vh-200px)] rounded-2xl bg-slate-100/90 dark:bg-slate-900/80 border border-slate-200 dark:border-slate-800 shadow-sm backdrop-blur-sm"
      >
        <!-- Column Header -->
        <div class="p-3.5 flex items-center justify-between border-b border-slate-200/80 dark:border-slate-800/80">
          <div class="flex items-center gap-2">
            <span
              class="w-2.5 h-2.5 rounded-full"
              :class="getColumnColorClass(col.content.color)"
            ></span>
            <h3 class="text-sm font-bold text-slate-800 dark:text-slate-200">
              {{ col.content.title || 'Untitled Column' }}
            </h3>
            <span
              class="px-2 py-0.5 rounded-full text-[11px] font-semibold bg-slate-200 dark:bg-slate-800 text-slate-600 dark:text-slate-400"
            >
              {{ getCards(col.id).length }}
            </span>
          </div>

          <!-- Column Actions -->
          <div class="flex items-center gap-1">
            <button
              class="p-1 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-200 dark:hover:bg-slate-800"
              title="Add Card"
              @click="openAddCard(col.id)"
            >
              <Plus class="w-4 h-4" />
            </button>
            <button
              class="p-1 rounded-lg text-slate-400 hover:text-red-500 hover:bg-slate-200 dark:hover:bg-slate-800"
              title="Delete Column"
              @click="deleteColumn(col.id)"
            >
              <Trash2 class="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        <!-- Cards List (VueDraggable for cross-column card dragging) -->
        <div class="flex-1 overflow-y-auto p-3 space-y-2.5 min-h-[120px]">
          <draggable
            :list="getCards(col.id)"
            group="kanban-cards"
            item-key="id"
            ghost-class="ghost-card"
            drag-class="drag-card"
            animation="200"
            class="min-h-[100px] space-y-2.5"
            @change="(evt: any) => onCardDragChange(evt, col.id)"
          >
            <template #item="{ element: card }">
              <div
                :key="card.id"
                class="group/card relative p-3.5 rounded-xl bg-white dark:bg-slate-800/90 border border-slate-200/80 dark:border-slate-700/80 shadow-sm hover:shadow-md hover:border-brand-500/40 dark:hover:border-brand-500/40 transition-all cursor-grab active:cursor-grabbing"
                @click="openCardDetail(card)"
              >
                <!-- Tags / Priority Header -->
                <div class="flex items-center justify-between gap-2 mb-2">
                  <span
                    v-if="card.content.priority"
                    class="px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wider"
                    :class="getPriorityBadgeClass(card.content.priority)"
                  >
                    {{ card.content.priority }}
                  </span>

                  <!-- Delete Card Action -->
                  <button
                    class="opacity-0 group-hover/card:opacity-100 p-1 text-slate-400 hover:text-red-500 rounded transition-opacity"
                    title="Delete card"
                    @click.stop="deleteCard(card.id)"
                  >
                    <Trash2 class="w-3.5 h-3.5" />
                  </button>
                </div>

                <!-- Card Title -->
                <h4 class="text-sm font-semibold text-slate-900 dark:text-slate-100 leading-snug mb-1.5">
                  {{ card.content.title || 'Untitled Card' }}
                </h4>

                <!-- Card Description Snippet -->
                <p
                  v-if="card.content.description"
                  class="text-xs text-slate-500 dark:text-slate-400 line-clamp-2 mb-2.5 font-normal"
                >
                  {{ card.content.description }}
                </p>

                <!-- Tags list -->
                <div v-if="card.content.tags && card.content.tags.length > 0" class="flex flex-wrap gap-1 mb-2.5">
                  <span
                    v-for="tag in card.content.tags"
                    :key="tag"
                    class="px-1.5 py-0.5 rounded bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 text-[10px] font-medium"
                  >
                    #{{ tag }}
                  </span>
                </div>

                <!-- Card Footer (Assignee & Date) -->
                <div class="flex items-center justify-between text-xs text-slate-400 pt-1 border-t border-slate-100 dark:border-slate-700/60">
                  <div class="flex items-center gap-1.5">
                    <div
                      v-if="card.content.assignee"
                      class="w-5 h-5 rounded-full bg-brand-500/20 text-brand-500 font-bold flex items-center justify-center text-[10px]"
                    >
                      {{ card.content.assignee.charAt(0) }}
                    </div>
                    <span class="text-[11px] truncate max-w-[120px]">{{ card.content.assignee || 'Unassigned' }}</span>
                  </div>
                  <span class="text-[10px]">{{ formatDate(card.updatedAt || card.createdAt) }}</span>
                </div>
              </div>
            </template>
          </draggable>

          <!-- Inline Add Card Input -->
          <div v-if="addingCardColumnId === col.id" class="p-2.5 rounded-xl bg-white dark:bg-slate-800 border border-brand-500 shadow-sm animate-scale-in">
            <textarea
              v-model="newCardTitle"
              rows="2"
              placeholder="What needs to be done?"
              class="w-full text-xs bg-transparent border-none outline-none resize-none text-slate-800 dark:text-slate-200 placeholder-slate-400"
              @keydown.enter.prevent="submitAddCard(col.id)"
              @keydown.esc="addingCardColumnId = null"
            ></textarea>
            <div class="flex items-center justify-end gap-2 mt-2">
              <button
                class="px-2.5 py-1 text-xs text-slate-500 hover:text-slate-700 dark:hover:text-slate-300"
                @click="addingCardColumnId = null"
              >
                Cancel
              </button>
              <button
                class="px-3 py-1 rounded-lg text-xs font-semibold text-white bg-brand-600 hover:bg-brand-500"
                @click="submitAddCard(col.id)"
              >
                Add
              </button>
            </div>
          </div>

          <!-- Bottom Add Card Button -->
          <button
            v-else
            class="w-full py-2 px-3 rounded-xl border border-dashed border-slate-300 dark:border-slate-800 text-xs font-medium text-slate-500 dark:text-slate-400 hover:border-brand-500 hover:text-brand-500 dark:hover:border-brand-500 dark:hover:text-brand-400 hover:bg-white/50 dark:hover:bg-slate-800/50 flex items-center justify-center gap-1.5 transition-all"
            @click="openAddCard(col.id)"
          >
            <Plus class="w-3.5 h-3.5" /> Add a card
          </button>
        </div>
      </div>

      <!-- Add New Column Quick Card -->
      <div
        class="w-80 shrink-0 p-4 rounded-2xl border-2 border-dashed border-slate-300 dark:border-slate-800 hover:border-brand-500 dark:hover:border-brand-500 hover:bg-slate-100/50 dark:hover:bg-slate-900/50 transition-all flex flex-col items-center justify-center gap-2 cursor-pointer text-slate-500 dark:text-slate-400 hover:text-brand-500 min-h-[150px]"
        @click="showAddColumnModal = true"
      >
        <Plus class="w-6 h-6" />
        <span class="text-xs font-semibold">Add another column</span>
      </div>
    </div>

    <!-- Add Column Modal -->
    <div
      v-if="showAddColumnModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 backdrop-blur-sm animate-fade-in"
      @click.self="showAddColumnModal = false"
    >
      <div class="w-full max-w-sm p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-2xl animate-scale-in">
        <h3 class="text-lg font-bold text-slate-900 dark:text-slate-100 mb-4">Create New Column</h3>
        
        <label class="block text-xs font-medium text-slate-600 dark:text-slate-400 mb-1.5">Column Title</label>
        <input
          v-model="newColumnTitle"
          type="text"
          placeholder="e.g. Quality Assurance"
          class="w-full px-3.5 py-2 rounded-xl text-sm bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-brand-500 outline-none mb-4"
          @keydown.enter="submitAddColumn"
        />

        <label class="block text-xs font-medium text-slate-600 dark:text-slate-400 mb-2">Accent Color</label>
        <div class="flex items-center gap-3 mb-6">
          <button
            v-for="c in colorOptions"
            :key="c.name"
            class="w-7 h-7 rounded-full transition-transform"
            :class="[c.bg, selectedColumnColor === c.name ? 'ring-4 ring-brand-500/30 scale-110' : 'hover:scale-105']"
            @click="selectedColumnColor = c.name"
          ></button>
        </div>

        <div class="flex items-center justify-end gap-2.5">
          <button
            class="px-4 py-2 rounded-xl text-xs font-medium text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
            @click="showAddColumnModal = false"
          >
            Cancel
          </button>
          <button
            class="px-4 py-2 rounded-xl text-xs font-semibold text-white bg-brand-600 hover:bg-brand-500 shadow-md shadow-brand-500/25"
            @click="submitAddColumn"
          >
            Create Column
          </button>
        </div>
      </div>
    </div>

    <!-- Card Detail Modal -->
    <div
      v-if="selectedCard"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 backdrop-blur-sm animate-fade-in"
      @click.self="selectedCard = null"
    >
      <div class="w-full max-w-lg p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-2xl animate-scale-in">
        <div class="flex items-start justify-between mb-4">
          <span class="text-xs font-semibold uppercase tracking-wider text-brand-600 dark:text-brand-400">
            Card Details
          </span>
          <button
            class="p-1 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200"
            @click="selectedCard = null"
          >
            <X class="w-4 h-4" />
          </button>
        </div>

        <!-- Title -->
        <input
          v-model="editCardForm.title"
          type="text"
          class="w-full text-xl font-bold bg-transparent border-b border-transparent hover:border-slate-300 dark:hover:border-slate-700 focus:border-brand-500 outline-none pb-1 mb-4 text-slate-900 dark:text-slate-100"
          placeholder="Card title..."
        />

        <!-- Description -->
        <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">Description</label>
        <textarea
          v-model="editCardForm.description"
          rows="3"
          placeholder="Add detailed task notes or checklist..."
          class="w-full px-3 py-2 text-xs rounded-xl bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 outline-none focus:ring-2 focus:ring-brand-500 text-slate-800 dark:text-slate-200 mb-4"
        ></textarea>

        <!-- Properties Grid -->
        <div class="grid grid-cols-2 gap-4 mb-6">
          <div>
            <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">Priority</label>
            <select
              v-model="editCardForm.priority"
              class="w-full px-3 py-1.5 text-xs rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-800 dark:text-slate-200 outline-none"
            >
              <option value="low">Low</option>
              <option value="medium">Medium</option>
              <option value="high">High</option>
              <option value="urgent">Urgent</option>
            </select>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">Assignee</label>
            <input
              v-model="editCardForm.assignee"
              type="text"
              placeholder="Assignee name..."
              class="w-full px-3 py-1.5 text-xs rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-800 dark:text-slate-200 outline-none"
            />
          </div>
        </div>

        <div class="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-800">
          <button
            class="text-xs text-red-500 hover:text-red-600 font-medium flex items-center gap-1"
            @click="deleteCard(selectedCard.id); selectedCard = null"
          >
            <Trash2 class="w-3.5 h-3.5" /> Delete card
          </button>
          <div class="flex items-center gap-2">
            <button
              class="px-3 py-1.5 text-xs text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg"
              @click="selectedCard = null"
            >
              Cancel
            </button>
            <button
              class="px-4 py-1.5 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-500 rounded-lg shadow-md"
              @click="saveCardDetail"
            >
              Save Changes
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import draggable from 'vuedraggable'
import { Plus, Trash2, X } from 'lucide-vue-next'
import { useWorkspaceStore } from '@/stores/workspace'
import { useAuthStore } from '@/stores/auth'
import wsService from '@/services/websocket'
import type { Block } from '@/types/workspace'

const workspaceStore = useWorkspaceStore()
const authStore = useAuthStore()

const currentPage = computed(() => workspaceStore.currentPage)
const kanbanColumns = computed(() => workspaceStore.kanbanColumns)

const totalCards = computed(() => {
  return workspaceStore.blocks.filter((b) => b.type === 'kanban_card').length
})

// Column creation state
const showAddColumnModal = ref(false)
const newColumnTitle = ref('')
const selectedColumnColor = ref('slate')

const colorOptions = [
  { name: 'slate', bg: 'bg-slate-400' },
  { name: 'amber', bg: 'bg-amber-400' },
  { name: 'purple', bg: 'bg-purple-500' },
  { name: 'emerald', bg: 'bg-emerald-500' },
  { name: 'blue', bg: 'bg-blue-500' },
  { name: 'rose', bg: 'bg-rose-500' },
]

// Add card state
const addingCardColumnId = ref<string | null>(null)
const newCardTitle = ref('')

// Card detail modal state
const selectedCard = ref<Block | null>(null)
const editCardForm = ref({
  title: '',
  description: '',
  priority: 'medium' as 'low' | 'medium' | 'high' | 'urgent',
  assignee: '',
})

function getCards(columnId: string) {
  return workspaceStore.getColumnCards(columnId)
}

function getColumnColorClass(color?: string) {
  switch (color) {
    case 'amber': return 'bg-amber-400'
    case 'purple': return 'bg-purple-500'
    case 'emerald': return 'bg-emerald-500'
    case 'blue': return 'bg-blue-500'
    case 'rose': return 'bg-rose-500'
    default: return 'bg-slate-400'
  }
}

function getPriorityBadgeClass(priority?: string) {
  switch (priority) {
    case 'urgent': return 'bg-red-500/15 text-red-500 border border-red-500/30'
    case 'high': return 'bg-amber-500/15 text-amber-500 border border-amber-500/30'
    case 'medium': return 'bg-blue-500/15 text-blue-500 border border-blue-500/30'
    case 'low': return 'bg-emerald-500/15 text-emerald-500 border border-emerald-500/30'
    default: return 'bg-slate-500/15 text-slate-400'
  }
}

function formatDate(dateStr?: string) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
}

/**
 * Handle VueDraggable change on cards (cross-column drag & intra-column reordering)
 */
function onCardDragChange(evt: any, targetColumnId: string) {
  // If card added to new column or reordered inside column
  if (evt.added || evt.moved) {
    const card: Block = evt.added ? evt.added.element : evt.moved.element
    const newIndex: number = evt.added ? evt.added.newIndex : evt.moved.newIndex

    // Immediately calculate updated positions for all cards in this target column
    const columnCards = getCards(targetColumnId).filter((c) => c.id !== card.id)
    columnCards.splice(newIndex, 0, card)

    const reorderBatch = columnCards.map((c, idx) => ({
      id: c.id,
      parentId: targetColumnId,
      position: idx,
    }))

    // Batch update both optimistic state, STOMP broadcast and REST backend
    workspaceStore.batchMoveBlocks(reorderBatch)
  }
}

async function submitAddColumn() {
  if (!newColumnTitle.value.trim()) return

  await workspaceStore.createBlock({
    type: 'kanban_column',
    parentId: null,
    content: {
      title: newColumnTitle.value.trim(),
      color: selectedColumnColor.value,
    },
    position: kanbanColumns.value.length,
  })

  newColumnTitle.value = ''
  showAddColumnModal.value = false
}

function openAddCard(columnId: string) {
  addingCardColumnId.value = columnId
  newCardTitle.value = ''
}

async function submitAddCard(columnId: string) {
  if (!newCardTitle.value.trim()) return

  await workspaceStore.createBlock({
    type: 'kanban_card',
    parentId: columnId,
    content: {
      title: newCardTitle.value.trim(),
      priority: 'medium',
      assignee: authStore.user?.name || 'Unassigned',
      tags: ['task'],
    },
    position: getCards(columnId).length,
  })

  newCardTitle.value = ''
  addingCardColumnId.value = null
}

function deleteColumn(columnId: string) {
  if (confirm('Delete this column and all of its cards?')) {
    workspaceStore.deleteBlock(columnId)
  }
}

function deleteCard(cardId: string) {
  workspaceStore.deleteBlock(cardId)
}

function openCardDetail(card: Block) {
  selectedCard.value = card
  editCardForm.value = {
    title: card.content.title || '',
    description: card.content.description || '',
    priority: card.content.priority || 'medium',
    assignee: card.content.assignee || '',
  }
}

function saveCardDetail() {
  if (!selectedCard.value) return

  workspaceStore.updateBlock(selectedCard.value.id, {
    content: {
      ...selectedCard.value.content,
      title: editCardForm.value.title,
      description: editCardForm.value.description,
      priority: editCardForm.value.priority,
      assignee: editCardForm.value.assignee,
    },
  })

  selectedCard.value = null
}

function handleMouseMove(e: MouseEvent) {
  if (!currentPage.value || !authStore.user) return

  wsService.sendCursorPosition(currentPage.value.id, {
    userId: authStore.user.id,
    userName: authStore.user.name,
    userAvatar: authStore.user.avatarUrl,
    x: e.clientX,
    y: e.clientY,
    color: authStore.cursorColor,
  })
}
</script>
