import request from '@/utils/request'

// ========== 认证相关 ==========
export const login = (data) => request.post('/user/login', data)

export const register = (data) => request.post('/user/register', data)

export const logout = () => request.post('/user/logout')

export const getUserInfo = () => request.get('/user/info')

// ========== 个人资料 ==========
export const updateProfile = (data) => request.put('/user/profile', data)

export const uploadAvatar = (formData) =>
  request.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

// 校园认证
export const campusVerify = (data) => request.post('/user/campus-verify', data)

// ========== 个人主页/统计 ==========
export const getUserDetail = (userId) => request.get(`/user/${userId}`)

export const getUserStats = (userId) => request.get(`/user/${userId}/stats`)

// ========== 关注 / 拉黑 ==========
export const followUser = (userId) => request.post(`/user/follow/${userId}`)

export const unfollowUser = (userId) => request.delete(`/user/follow/${userId}`)

export const blockUser = (userId) => request.post(`/user/block/${userId}`)

export const unblockUser = (userId) => request.delete(`/user/block/${userId}`)

export const getFollowingList = (params) => request.get('/user/following', { params })

export const getFansList = (params) => request.get('/user/fans', { params })
