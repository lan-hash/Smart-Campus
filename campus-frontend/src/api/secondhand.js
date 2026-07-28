import request from '@/utils/request'

// 分类列表
export const getCategories = () => request.get('/secondhand/categories')

// 商品列表
export const getProducts = (params) =>
  request.get('/secondhand/products', { params })

// 商品详情
export const getProductDetail = (id) =>
  request.get(`/secondhand/products/${id}`)

// 发布商品
export const createProduct = (data) =>
  request.post('/secondhand/publish', data)

// 编辑商品
export const updateProduct = (id, data) =>
  request.put(`/secondhand/products/${id}`, data)

// 删除/下架商品
export const deleteProduct = (id) =>
  request.delete(`/secondhand/products/${id}`)

// 收藏
export const toggleFavorite = (id) =>
  request.post(`/secondhand/products/${id}/favorite`)

// 举报
export const reportProduct = (id, data) =>
  request.post(`/secondhand/products/${id}/report`, data)

// 创建交易(购买)
export const createTransaction = (data) =>
  request.post('/secondhand/transaction', data)

// 更新交易状态
export const updateTransactionStatus = (id, status) =>
  request.put(`/secondhand/transaction/${id}/status`, { status })

// 交易评价
export const evaluateTransaction = (id, data) =>
  request.post(`/secondhand/transaction/${id}/evaluation`, data)

// 我的商品/收藏/交易
export const getMyProducts = (params) =>
  request.get('/secondhand/my/products', { params })

export const getMyFavorites = (params) =>
  request.get('/secondhand/my/favorites', { params })

export const getMyTransactions = (params) =>
  request.get('/secondhand/my/transactions', { params })
