import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'
import wsService from '@/services/websocket'
import type { User } from '@/types/workspace'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('workspace_token'))
  const user = ref<User | null>(
    localStorage.getItem('workspace_user')
      ? JSON.parse(localStorage.getItem('workspace_user')!)
      : null
  )
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value && !!user.value)

  // Pre-assigned distinct cursor color per user
  const cursorColor = computed(() => {
    if (!user.value) return '#3b82f6'
    const colors = [
      '#ef4444', '#f97316', '#eab308', '#22c55e', 
      '#06b6d4', '#3b82f6', '#8b5cf6', '#ec4899'
    ]
    let hash = 0
    for (let i = 0; i < user.value.id.length; i++) {
      hash = user.value.id.charCodeAt(i) + ((hash << 5) - hash)
    }
    return colors[Math.abs(hash) % colors.length]
  })

  async function login(credentials: { email: string; password: string }) {
    loading.value = true
    error.value = null
    try {
      const res = await api.post('/auth/login', credentials)
      token.value = res.data.token
      user.value = res.data.user
      localStorage.setItem('workspace_token', res.data.token)
      localStorage.setItem('workspace_user', JSON.stringify(res.data.user))
      
      // Connect WebSocket with new token
      await wsService.connect(res.data.token)
      return res.data
    } catch (err: any) {
      error.value = err.response?.data?.details?.[0] || err.response?.data?.message || 'Invalid email or password'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function register(data: { email: string; password: string; name: string; avatarUrl?: string }) {
    loading.value = true
    error.value = null
    try {
      const res = await api.post('/auth/register', data)
      token.value = res.data.token
      user.value = res.data.user
      localStorage.setItem('workspace_token', res.data.token)
      localStorage.setItem('workspace_user', JSON.stringify(res.data.user))

      // Connect WebSocket with new token
      await wsService.connect(res.data.token)
      return res.data
    } catch (err: any) {
      error.value = err.response?.data?.details?.[0] || err.response?.data?.message || 'Registration failed'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchCurrentUser() {
    if (!token.value) return null
    try {
      const res = await api.get('/auth/me')
      user.value = res.data
      localStorage.setItem('workspace_user', JSON.stringify(res.data))
      return res.data
    } catch (err) {
      logout()
      return null
    }
  }

  async function logout() {
    try {
      await api.post('/auth/logout')
    } catch {
      // ignore
    }
    token.value = null
    user.value = null
    localStorage.removeItem('workspace_token')
    localStorage.removeItem('workspace_user')
    wsService.disconnect()
  }

  return {
    token,
    user,
    loading,
    error,
    isAuthenticated,
    cursorColor,
    login,
    register,
    fetchCurrentUser,
    logout,
  }
})
