import request from '@/utils/request'

// ========== 私聊 ==========
export const getChatList = () => request.get('/message/chat/list')

export const getChatMessages = (userId, params) =>
  request.get(`/message/chat/${userId}`, { params })

export const sendChatMessage = (userId, data) =>
  request.post(`/message/chat/${userId}`, data)

// ========== 通知 ==========
export const getNotifications = (params) =>
  request.get('/message/notification', { params })

export const markRead = (id) =>
  request.put(`/message/notification/${id}/read`)

export const markAllRead = () =>
  request.put('/message/notification/read-all')

export const getUnreadCount = () =>
  request.get('/message/notification/unread-count')

// 系统公告
export const getNotices = () => request.get('/message/notices')
