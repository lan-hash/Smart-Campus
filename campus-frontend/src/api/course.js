import request from '@/utils/request'

// 代课订单列表
export const getOrders = (params) =>
  request.get('/course/orders', { params })

// 订单详情
export const getOrderDetail = (id) =>
  request.get(`/course/orders/${id}`)

// 发布代课需求
export const createOrder = (data) =>
  request.post('/course/orders', data)

// 接单
export const acceptOrder = (id) =>
  request.put(`/course/orders/${id}/accept`)

// 更新订单状态
export const updateOrderStatus = (id, status) =>
  request.put(`/course/orders/${id}/status`, { status })

// 评价
export const evaluateOrder = (id, data) =>
  request.post(`/course/orders/${id}/evaluation`, data)

// 举报
export const reportOrder = (id, data) =>
  request.post(`/course/orders/${id}/report`, data)

// 我的订单
export const getMyOrders = (params) =>
  request.get('/course/my/orders', { params })
