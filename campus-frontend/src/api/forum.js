import request from '@/utils/request'

// 上传图片（通用，供编辑器使用）
export const uploadImage = (formData) =>
  request.post('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

// 版块
export const getCategories = (type) =>
  request.get('/forum/categories', { params: { type } })

// 帖子列表(学习/游戏共用，type 区分)
export const getPosts = (params) => request.get('/forum/posts', { params })

// 帖子详情
export const getPostDetail = (id) => request.get(`/forum/posts/${id}`)

// 发帖
export const createPost = (data) => request.post('/forum/publish', data)

// 编辑帖子
export const updatePost = (id, data) => request.put(`/forum/posts/${id}`, data)

// 删除帖子
export const deletePost = (id) => request.delete(`/forum/posts/${id}`)

// 点赞/取消点赞
export const toggleLike = (id) => request.post(`/forum/posts/${id}/like`)

// 收藏/取消收藏
export const toggleCollect = (id) => request.post(`/forum/posts/${id}/collect`)

// ========== 评论 ==========
export const getComments = (postId, params) =>
  request.get(`/forum/posts/${postId}/comments`, { params })

export const addComment = (postId, data) =>
  request.post(`/forum/posts/${postId}/comments`, data)

export const replyComment = (commentId, data) =>
  request.post(`/forum/comments/${commentId}/reply`, data)

export const likeComment = (commentId) =>
  request.post(`/forum/comments/${commentId}/like`)

// ========== 我的帖子/收藏 ==========
export const getMyPosts = (params) => request.get('/forum/my/posts', { params })

export const getMyCollects = (params) => request.get('/forum/my/collects', { params })

// AI 智能分类(发帖时预览分类)
export const aiClassify = (data) => request.post('/forum/ai/classify', data)
