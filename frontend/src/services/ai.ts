import api from '@/services/api'

export interface AiGenerateRequest {
  prompt?: string
  actionType: string
  contextText?: string
  imageUrl?: string
}

export interface AiGenerateResponse {
  result: string
  actionType: string
  items?: string[]
}

export interface AiChatMessage {
  role: 'user' | 'assistant'
  content: string
  imageUrl?: string
}

export interface AiChatRequest {
  message: string
  pageContext?: string
  imageUrl?: string
  history?: AiChatMessage[]
}

export interface AiChatResponse {
  reply: string
}

export const aiService = {
  async generate(req: AiGenerateRequest): Promise<AiGenerateResponse> {
    const res = await api.post('/ai/generate', req)
    return res.data
  },

  async chat(req: AiChatRequest): Promise<AiChatResponse> {
    const res = await api.post('/ai/chat', req)
    return res.data
  },
}

export default aiService
