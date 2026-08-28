export interface User {
  id: string
  email: string
  name: string
  avatarUrl?: string
  createdAt?: string
}

export interface Workspace {
  id: string
  name: string
  slug: string
  ownerId: string
  role?: string
  createdAt?: string
  updatedAt?: string
}

export interface WorkspaceMember {
  id: string
  workspaceId: string
  user: User
  role: 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER'
  createdAt?: string
}

export interface Page {
  id: string
  workspaceId: string
  parentPageId?: string | null
  title: string
  icon?: string
  isKanban: boolean
  position: number
  createdAt?: string
  updatedAt?: string
}

export type BlockType = 
  | 'paragraph'
  | 'heading_1'
  | 'heading_2'
  | 'heading_3'
  | 'todo'
  | 'bullet_list'
  | 'numbered_list'
  | 'toggle'
  | 'quote'
  | 'code'
  | 'callout'
  | 'divider'
  | 'image'
  | 'table'
  | 'toc'
  | 'column_list'
  | 'kanban_column'
  | 'kanban_card'

export interface BlockContent {
  text?: string
  checked?: boolean
  isOpen?: boolean
  language?: string
  icon?: string
  url?: string
  caption?: string
  color?: string
  bgColor?: string
  // Table properties
  tableData?: string[][]
  hasHeaderRow?: boolean
  hasHeaderCol?: boolean
  // Kanban column properties
  title?: string
  // Kanban card properties
  description?: string
  priority?: 'low' | 'medium' | 'high' | 'urgent'
  tags?: string[]
  assignee?: string
  dueDate?: string
  [key: string]: any
}

export interface Block {
  id: string
  pageId: string
  parentId?: string | null
  type: BlockType
  content: BlockContent
  position: number
  createdAt?: string
  updatedAt?: string
}

export interface RemoteCursor {
  userId: string
  userName: string
  userAvatar?: string
  x: number
  y: number
  activeBlockId?: string
  color: string
  lastSeen?: number
}

export type RealtimeEventType = 
  | 'BLOCK_CREATE'
  | 'BLOCK_UPDATE'
  | 'BLOCK_DELETE'
  | 'BLOCK_BATCH_MOVE'
  | 'CURSOR_MOVE'
  | 'USER_JOIN'
  | 'USER_LEAVE'
  | 'PAGE_UPDATE'

export interface RealtimeEvent<T = any> {
  type: RealtimeEventType
  pageId: string
  senderId: string
  senderName?: string
  senderAvatar?: string
  payload: T
  timestamp?: string
}
