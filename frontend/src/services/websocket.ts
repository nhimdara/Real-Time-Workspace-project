import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { RealtimeEvent } from '@/types/workspace'

type EventCallback = (event: RealtimeEvent) => void

class WebSocketService {
  private client: Client | null = null
  private activeSubscriptions: Map<string, any> = new Map()
  private isConnected: boolean = false
  private listeners: Map<string, Set<EventCallback>> = new Map()
  private lastCursorSend: number = 0

  public connect(token?: string): Promise<void> {
    return new Promise((resolve) => {
      if (this.client && this.isConnected) {
        resolve()
        return
      }

      const authToken = token || localStorage.getItem('workspace_token') || ''

      this.client = new Client({
        webSocketFactory: () => new SockJS('/ws'),
        connectHeaders: {
          Authorization: `Bearer ${authToken}`,
          token: authToken,
        },
        debug: (str) => {
          if (import.meta.env.DEV) {
            // console.debug('[STOMP]', str)
          }
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
      })

      this.client.onConnect = (frame) => {
        this.isConnected = true
        // Resubscribe to active topics
        this.activeSubscriptions.forEach((_, pageId) => {
          this.subscribeToPage(pageId)
        })
        resolve()
      }

      this.client.onDisconnect = () => {
        this.isConnected = false
      }

      this.client.onStompError = (frame) => {
        console.warn('STOMP protocol error', frame.headers['message'])
      }

      this.client.activate()
    })
  }

  public disconnect() {
    if (this.client) {
      this.activeSubscriptions.clear()
      this.client.deactivate()
      this.isConnected = false
    }
  }

  public subscribeToPage(pageId: string, callback?: EventCallback): () => void {
    const topic = `/topic/page/${pageId}`

    if (callback) {
      if (!this.listeners.has(pageId)) {
        this.listeners.set(pageId, new Set())
      }
      this.listeners.get(pageId)!.add(callback)
    }

    if (this.client && this.isConnected && !this.activeSubscriptions.has(pageId)) {
      const sub = this.client.subscribe(topic, (message) => {
        try {
          const event: RealtimeEvent = JSON.parse(message.body)
          const pageListeners = this.listeners.get(pageId)
          if (pageListeners) {
            pageListeners.forEach((cb) => cb(event))
          }
        } catch (e) {
          console.error('Error parsing STOMP message', e)
        }
      })
      this.activeSubscriptions.set(pageId, sub)
    }

    return () => {
      if (callback && this.listeners.has(pageId)) {
        this.listeners.get(pageId)!.delete(callback)
      }
    }
  }

  public unsubscribeFromPage(pageId: string) {
    const sub = this.activeSubscriptions.get(pageId)
    if (sub) {
      sub.unsubscribe()
      this.activeSubscriptions.delete(pageId)
    }
    this.listeners.delete(pageId)
  }

  public sendEvent(pageId: string, event: Partial<RealtimeEvent>) {
    if (!this.client || !this.isConnected) return

    this.client.publish({
      destination: `/app/page/${pageId}/update`,
      body: JSON.stringify({
        ...event,
        pageId,
      }),
    })
  }

  public sendCursorPosition(
    pageId: string,
    cursor: {
      userId: string
      userName: string
      userAvatar?: string
      x: number
      y: number
      activeBlockId?: string
      color: string
    }
  ) {
    const now = Date.now()
    // Throttle to 50ms (20fps cursor updates for smooth live feel)
    if (now - this.lastCursorSend < 50) return
    this.lastCursorSend = now

    this.sendEvent(pageId, {
      type: 'CURSOR_MOVE',
      senderId: cursor.userId,
      senderName: cursor.userName,
      senderAvatar: cursor.userAvatar,
      payload: cursor,
    })
  }
}

export const wsService = new WebSocketService()
export default wsService
