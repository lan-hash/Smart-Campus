import request from '@/utils/request'

// 管理员登录
export const adminLogin = (data) => request.post('/admin/login', data)

// 用户管理
export const getUsers = (params) => request.get('/admin/users', { params })

export const updateUserStatus = (id, status) =>
  request.put(`/admin/users/${id}/status`, { status })

// 举报处理
export const getReports = (params) => request.get('/admin/reports', { params })

export const handleReport = (id, data) =>
  request.put(`/admin/reports/${id}`, data)

// 内容审核
export const getReviewList = (params) =>
  request.get('/admin/review', { params })

export const reviewItem = (id, data) =>
  request.put(`/admin/review/${id}`, data)

// 数据统计
export const getDashboardStats = () => request.get('/admin/stats')

// 系统公告
export const createNotice = (data) => request.post('/admin/notice', data)

// 操作日志
export const getLogs = (params) => request.get('/admin/logs', { params })
