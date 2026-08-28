import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'

const api: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('workspace_token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor to handle 401 Unauthorized
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Clear token and notify if not already on auth page
      if (!window.location.pathname.includes('/login')) {
        localStorage.removeItem('workspace_token')
        localStorage.removeItem('workspace_user')
      }
    }
    return Promise.reject(error)
  }
)

export default api
