import request from '@/utils/request'

// ========== 管理员登录 ==========
export const adminLogin = (data) => request.post('/admin/login', data)

// ========== 数据统计 ==========
export const getDashboardStats = () => request.get('/admin/dashboard')

// ========== 用户管理 ==========
export const getUsers = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (id, status) =>
  request.put(`/admin/users/${id}/status`, { status })

// ========== 帖子管理 ==========
export const getPosts = (params) => request.get('/admin/posts', { params })
export const updatePostStatus = (id, status) =>
  request.put(`/admin/posts/${id}/status`, { status })
export const updatePostTop = (id, isTop) =>
  request.put(`/admin/posts/${id}/top`, { isTop })
export const updatePostEssence = (id, isEssence) =>
  request.put(`/admin/posts/${id}/essence`, { isEssence })

// ========== 举报处理 ==========
export const getReports = (params) => request.get('/admin/reports', { params })
export const handleReport = (id, data) =>
  request.put(`/admin/reports/${id}/handle`, data)

// ========== 内容审核（复用帖子接口） ==========
export const getReviewList = (params) => request.get('/admin/posts', { params })
export const reviewItem = (id, data) =>
  request.put(`/admin/posts/${id}/status`, data)

// ========== 系统公告 ==========
export const getNotices = (params) => request.get('/admin/notices', { params })
export const createNotice = (data) => request.post('/admin/notices', data)
export const updateNotice = (id, data) => request.put(`/admin/notices/${id}`, data)
export const deleteNotice = (id) => request.delete(`/admin/notices/${id}`)

// ========== 操作日志 ==========
export const getLogs = (params) => request.get('/admin/logs', { params })
