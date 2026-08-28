import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'
import wsService from '@/services/websocket'
import { useAuthStore } from '@/stores/auth'
import type { Workspace, WorkspaceMember, Page, Block, RemoteCursor, RealtimeEvent, BlockType, BlockContent } from '@/types/workspace'

export const useWorkspaceStore = defineStore('workspace', () => {
  const authStore = useAuthStore()

  // State
  const workspaces = ref<Workspace[]>([])
  const currentWorkspace = ref<Workspace | null>(null)
  const members = ref<WorkspaceMember[]>([])
  const pages = ref<Page[]>([])
  const currentPage = ref<Page | null>(null)
  const blocks = ref<Block[]>([])
  const remoteCursors = ref<Map<string, RemoteCursor>>(new Map())
  const loading = ref(false)
  const activeUnsubscribe = ref<(() => void) | null>(null)

  // Getters
  const isOwner = computed(() => {
    return currentWorkspace.value?.ownerId === authStore.user?.id
  })

  // Group blocks for document view (parentId == null)
  const documentBlocks = computed(() => {
    return blocks.value
      .filter((b) => !b.parentId && b.type !== 'kanban_column')
      .sort((a, b) => a.position - b.position)
  })

  // Get Kanban columns (type == 'kanban_column')
  const kanbanColumns = computed(() => {
    return blocks.value
      .filter((b) => b.type === 'kanban_column')
      .sort((a, b) => a.position - b.position)
  })

  // Helper to get cards for a specific column
  function getColumnCards(columnId: string) {
    return blocks.value
      .filter((b) => b.parentId === columnId)
      .sort((a, b) => a.position - b.position)
  }

  // Active online peers on this page (within last 15s)
  const activePeers = computed(() => {
    const list: RemoteCursor[] = []
    const threshold = Date.now() - 15000
    remoteCursors.value.forEach((cursor) => {
      if ((cursor.lastSeen || 0) >= threshold && cursor.userId !== authStore.user?.id) {
        list.push(cursor)
      }
    })
    return list
  })

  // Actions
  async function fetchWorkspaces() {
    loading.value = true
    try {
      const res = await api.get('/workspaces')
      workspaces.value = res.data
      if (workspaces.value.length > 0 && !currentWorkspace.value) {
        await selectWorkspace(workspaces.value[0].id)
      }
    } catch (err) {
      console.error('Failed to fetch workspaces', err)
    } finally {
      loading.value = false
    }
  }

  async function selectWorkspace(workspaceId: string) {
    const ws = workspaces.value.find((w) => w.id === workspaceId)
    if (ws) {
      currentWorkspace.value = ws
      await Promise.all([fetchPages(workspaceId), fetchMembers(workspaceId)])
    }
  }

  async function createWorkspace(data: { name: string; slug: string }) {
    const res = await api.post('/workspaces', data)
    workspaces.value.push(res.data)
    await selectWorkspace(res.data.id)
    return res.data
  }

  async function fetchMembers(workspaceId: string) {
    try {
      const res = await api.get(`/workspaces/${workspaceId}/members`)
      members.value = res.data
    } catch (err) {
      console.error('Failed to fetch members', err)
    }
  }

  async function inviteMember(workspaceId: string, email: string, role = 'MEMBER') {
    const res = await api.post(`/workspaces/${workspaceId}/invite`, { email, role })
    members.value.push(res.data)
    return res.data
  }

  async function fetchPages(workspaceId: string) {
    try {
      const res = await api.get(`/workspaces/${workspaceId}/pages`)
      pages.value = res.data
      if (pages.value.length > 0 && (!currentPage.value || currentPage.value.workspaceId !== workspaceId)) {
        await selectPage(pages.value[0].id)
      }
    } catch (err) {
      console.error('Failed to fetch pages', err)
    }
  }

  async function selectPage(pageId: string) {
    const page = pages.value.find((p) => p.id === pageId)
    if (!page) return

    // Clean up previous page subscription
    if (activeUnsubscribe.value) {
      activeUnsubscribe.value()
      activeUnsubscribe.value = null
    }

    currentPage.value = page
    remoteCursors.value.clear()

    // Fetch blocks
    await fetchBlocks(pageId)

    // Ensure WebSocket connected
    await wsService.connect()

    // Subscribe to real-time events for this page
    activeUnsubscribe.value = wsService.subscribeToPage(pageId, handleRealtimeEvent)

    // Broadcast user joined
    if (authStore.user) {
      wsService.sendEvent(pageId, {
        type: 'USER_JOIN',
        senderId: authStore.user.id,
        senderName: authStore.user.name,
        senderAvatar: authStore.user.avatarUrl,
        payload: { userId: authStore.user.id },
      })
    }
  }

  async function createPage(data: { title: string; icon?: string; isKanban?: boolean; parentPageId?: string | null }) {
    if (!currentWorkspace.value) return
    const res = await api.post(`/workspaces/${currentWorkspace.value.id}/pages`, data)
    pages.value.push(res.data)
    await selectPage(res.data.id)

    // If new Kanban page, initialize default columns
    if (res.data.isKanban) {
      await initializeDefaultKanbanBoard(res.data.id)
    } else {
      // Initialize default first block
      await createBlock({
        type: 'paragraph',
        content: { text: '' },
        position: 0,
      })
    }

    return res.data
  }

  async function updatePage(pageId: string, data: Partial<Page>) {
    // Optimistic
    const idx = pages.value.findIndex((p) => p.id === pageId)
    if (idx !== -1) {
      pages.value[idx] = { ...pages.value[idx], ...data }
    }
    if (currentPage.value?.id === pageId) {
      currentPage.value = { ...currentPage.value, ...data }
    }

    try {
      const res = await api.put(`/pages/${pageId}`, data)
      // Broadcast page update
      if (currentPage.value?.id === pageId) {
        wsService.sendEvent(pageId, {
          type: 'PAGE_UPDATE',
          senderId: authStore.user?.id || '',
          payload: res.data,
        })
      }
      return res.data
    } catch (err) {
      console.error('Failed to update page', err)
    }
  }

  async function deletePage(pageId: string) {
    await api.delete(`/pages/${pageId}`)
    pages.value = pages.value.filter((p) => p.id !== pageId)
    if (currentPage.value?.id === pageId) {
      if (pages.value.length > 0) {
        await selectPage(pages.value[0].id)
      } else {
        currentPage.value = null
        blocks.value = []
      }
    }
  }

  async function fetchBlocks(pageId: string) {
    try {
      const res = await api.get(`/pages/${pageId}/blocks`)
      blocks.value = res.data
    } catch (err) {
      console.error('Failed to fetch blocks', err)
      blocks.value = []
    }
  }

  // Real-time Event Handler from STOMP
  function handleRealtimeEvent(event: RealtimeEvent) {
    if (event.senderId === authStore.user?.id && event.type !== 'BLOCK_BATCH_MOVE') {
      // Ignore self-emitted events except for batch reorders
      return
    }

    switch (event.type) {
      case 'CURSOR_MOVE': {
        const cursor = event.payload as RemoteCursor
        cursor.lastSeen = Date.now()
        remoteCursors.value.set(cursor.userId, cursor)
        break
      }
      case 'BLOCK_CREATE': {
        const newBlock = event.payload as Block
        const existingIdx = blocks.value.findIndex((b) => b.id === newBlock.id)
        if (existingIdx === -1) {
          blocks.value.push(newBlock)
        }
        break
      }
      case 'BLOCK_UPDATE': {
        const updated = event.payload as Block
        const idx = blocks.value.findIndex((b) => b.id === updated.id)
        if (idx !== -1) {
          blocks.value[idx] = { ...blocks.value[idx], ...updated }
        } else {
          blocks.value.push(updated)
        }
        break
      }
      case 'BLOCK_DELETE': {
        const blockId = event.payload?.blockId || event.payload
        blocks.value = blocks.value.filter((b) => b.id !== blockId && b.parentId !== blockId)
        break
      }
      case 'BLOCK_BATCH_MOVE': {
        const movedItems = event.payload as { id: string; parentId: string | null; position: number }[]
        if (Array.isArray(movedItems)) {
          movedItems.forEach((item) => {
            const b = blocks.value.find((block) => block.id === item.id)
            if (b) {
              b.parentId = item.parentId
              b.position = item.position
            }
          })
        }
        break
      }
      case 'PAGE_UPDATE': {
        const updatedPage = event.payload as Page
        const pIdx = pages.value.findIndex((p) => p.id === updatedPage.id)
        if (pIdx !== -1) {
          pages.value[pIdx] = { ...pages.value[pIdx], ...updatedPage }
        }
        if (currentPage.value?.id === updatedPage.id) {
          currentPage.value = { ...currentPage.value, ...updatedPage }
        }
        break
      }
      case 'USER_LEAVE': {
        if (event.payload?.userId) {
          remoteCursors.value.delete(event.payload.userId)
        }
        break
      }
    }
  }

  // Optimistic Block Creation
  async function createBlock(payload: {
    id?: string
    type: BlockType
    parentId?: string | null
    content?: BlockContent
    position?: number
  }) {
    if (!currentPage.value) return

    const tempId = payload.id || crypto.randomUUID()
    const newBlock: Block = {
      id: tempId,
      pageId: currentPage.value.id,
      parentId: payload.parentId || null,
      type: payload.type,
      content: payload.content || {},
      position: payload.position ?? blocks.value.length,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }

    // 1. Optimistic local update
    blocks.value.push(newBlock)

    // 2. Broadcast via WebSocket immediately
    wsService.sendEvent(currentPage.value.id, {
      type: 'BLOCK_CREATE',
      senderId: authStore.user?.id || '',
      payload: newBlock,
    })

    // 3. Persist to Backend REST API
    try {
      const res = await api.post(`/pages/${currentPage.value.id}/blocks`, {
        id: tempId,
        type: payload.type,
        parentId: payload.parentId,
        content: payload.content,
        position: payload.position,
      })
      // Replace with confirmed backend payload
      const idx = blocks.value.findIndex((b) => b.id === tempId)
      if (idx !== -1) {
        blocks.value[idx] = res.data
      }
      return res.data
    } catch (err) {
      console.error('Failed to persist created block', err)
      // Rollback on error
      blocks.value = blocks.value.filter((b) => b.id !== tempId)
    }
  }

  // Optimistic Block Update
  async function updateBlock(blockId: string, payload: Partial<Block>) {
    if (!currentPage.value) return

    const block = blocks.value.find((b) => b.id === blockId)
    if (!block) return

    // 1. Optimistic local update
    Object.assign(block, payload)

    // 2. Broadcast to STOMP peers
    wsService.sendEvent(currentPage.value.id, {
      type: 'BLOCK_UPDATE',
      senderId: authStore.user?.id || '',
      payload: block,
    })

    // 3. Persist via REST API
    try {
      await api.put(`/blocks/${blockId}`, {
        type: payload.type || block.type,
        content: payload.content || block.content,
        parentId: payload.parentId !== undefined ? payload.parentId : block.parentId,
        position: payload.position !== undefined ? payload.position : block.position,
      })
    } catch (err) {
      console.error('Failed to persist block update', err)
    }
  }

  // Optimistic Block Deletion
  async function deleteBlock(blockId: string) {
    if (!currentPage.value) return

    // 1. Optimistic removal
    blocks.value = blocks.value.filter((b) => b.id !== blockId && b.parentId !== blockId)

    // 2. Broadcast
    wsService.sendEvent(currentPage.value.id, {
      type: 'BLOCK_DELETE',
      senderId: authStore.user?.id || '',
      payload: { blockId },
    })

    // 3. Persist
    try {
      await api.delete(`/blocks/${blockId}`)
    } catch (err) {
      console.error('Failed to delete block', err)
    }
  }

  // Batch move blocks (Kanban Drag & Drop or Page Reordering)
  async function batchMoveBlocks(items: { id: string; parentId: string | null; position: number }[]) {
    if (!currentPage.value || items.length === 0) return

    // 1. Optimistic local update
    items.forEach((item) => {
      const b = blocks.value.find((block) => block.id === item.id)
      if (b) {
        b.parentId = item.parentId
        b.position = item.position
      }
    })

    // 2. Broadcast over STOMP
    wsService.sendEvent(currentPage.value.id, {
      type: 'BLOCK_BATCH_MOVE',
      senderId: authStore.user?.id || '',
      payload: items,
    })

    // 3. Persist to API
    try {
      await api.post(`/pages/${currentPage.value.id}/blocks/batch-move`, { items })
    } catch (err) {
      console.error('Failed to batch move blocks', err)
    }
  }

  // Helper to initialize standard Kanban columns for new Kanban boards
  async function initializeDefaultKanbanBoard(pageId: string) {
    const defaultCols = [
      { title: 'To Do', color: 'slate', position: 0 },
      { title: 'In Progress', color: 'amber', position: 1 },
      { title: 'Completed', color: 'emerald', position: 2 },
    ]

    for (const col of defaultCols) {
      await createBlock({
        type: 'kanban_column',
        parentId: null,
        content: { title: col.title, color: col.color },
        position: col.position,
      })
    }
  }

  return {
    workspaces,
    currentWorkspace,
    members,
    pages,
    currentPage,
    blocks,
    remoteCursors,
    loading,
    isOwner,
    documentBlocks,
    kanbanColumns,
    activePeers,
    getColumnCards,
    fetchWorkspaces,
    selectWorkspace,
    createWorkspace,
    fetchMembers,
    inviteMember,
    fetchPages,
    selectPage,
    createPage,
    updatePage,
    deletePage,
    fetchBlocks,
    createBlock,
    updateBlock,
    deleteBlock,
    batchMoveBlocks,
    initializeDefaultKanbanBoard,
  }
})
