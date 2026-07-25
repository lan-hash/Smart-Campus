import request from '@/utils/request'

// 表白墙列表
export const getConfessions = (params) =>
  request.get('/confession/list', { params })

// 发布表白
export const createConfession = (data) => request.post('/confession', data)

// 删除表白
export const deleteConfession = (id) => request.delete(`/confession/${id}`)

// 点赞
export const toggleConfessionLike = (id) =>
  request.post(`/confession/${id}/like`)

// 评论列表
export const getConfessionComments = (id, params) =>
  request.get(`/confession/${id}/comments`, { params })

// 发评论
export const addConfessionComment = (id, data) =>
  request.post(`/confession/${id}/comments`, data)
