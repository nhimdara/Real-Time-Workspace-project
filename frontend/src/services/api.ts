import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosError } from 'axios'

const api: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
  // Free-tier hosting (Render) can cold-start slowly; give the first request room.
  timeout: 60000,
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

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms))

// Response interceptor: handle 401, and transparently retry transient
// cold-start failures (network error / 502 / 503 / 504) a few times.
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    if (error.response && error.response.status === 401) {
      if (!window.location.pathname.includes('/login')) {
        localStorage.removeItem('workspace_token')
        localStorage.removeItem('workspace_user')
      }
      return Promise.reject(error)
    }

    const config = error.config as (InternalAxiosRequestConfig & { _retryCount?: number }) | undefined
    const status = error.response?.status
    const isTransient = !error.response || status === 502 || status === 503 || status === 504

    if (config && isTransient) {
      config._retryCount = config._retryCount ?? 0
      if (config._retryCount < 4) {
        config._retryCount += 1
        const delays = [1500, 3000, 5000, 8000]
        await sleep(delays[config._retryCount - 1] || 3000)
        return api(config)
      }
    }

    return Promise.reject(error)
  }
)

export default api
