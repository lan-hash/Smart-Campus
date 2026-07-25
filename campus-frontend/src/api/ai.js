import request from '@/utils/request'

// AI 智能答疑对话(SSE 流式)
export const chatWithAI = (data) =>
  request.post('/ai/chat', data)

// 获取历史会话
export const getChatHistory = (params) =>
  request.get('/ai/chat/history', { params })

// AI 个性化推荐
export const getRecommendations = (params) =>
  request.get('/ai/recommend', { params })

// AI 内容审核(前端预览，实际由后端触发)
export const reviewContent = (data) =>
  request.post('/ai/review', data)
