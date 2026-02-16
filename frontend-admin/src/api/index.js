import request from '@/utils/request'

// 认证
export const adminLogin = (data) => request.post('/auth/admin/login', data)
export const getAdminInfo = () => request.get('/auth/info')

// 统计
export const getDashboard = () => request.get('/statistics/dashboard')
export const getTopSales = (params) => request.get('/statistics/topSales', { params })
export const getSalesTrend = (params) => request.get('/statistics/salesTrend', { params })
export const getLowStock = () => request.get('/statistics/lowStock')
export const getExpiring = (params) => request.get('/statistics/expiring', { params })

// 商品
export const getGoodsList = (params) => request.get('/goods/list', { params })
export const getGoodsDetail = (id) => request.get(`/goods/detail/${id}`)
export const addGoods = (data) => request.post('/goods/add', data)
export const updateGoods = (data) => request.put('/goods/update', data)
export const updateGoodsSale = (id, isOnSale) => request.put(`/goods/sale/${id}`, null, { params: { isOnSale } })
export const deleteGoods = (id) => request.delete(`/goods/delete/${id}`)

// 分类
export const getCategoryList = () => request.get('/category/list')
export const getCategoryAll = () => request.get('/category/all')
export const addCategory = (data) => request.post('/category/add', data)
export const updateCategory = (data) => request.put('/category/update', data)
export const deleteCategory = (id) => request.delete(`/category/delete/${id}`)

// 订单
export const getOrders = (params) => request.get('/verify/orders', { params })
export const getOrderList = (params) => request.get('/order/list', { params })
export const searchOrder = (verifyCode) => request.get('/verify/search', { params: { verifyCode } })
export const verifyOrder = (orderId) => request.post(`/verify/confirm/${orderId}`)
export const cancelOrder = (orderId, reason) => request.post(`/verify/cancel/${orderId}`, { reason })

// 店员
export const getStaffList = () => request.get('/admin/list')
export const addStaff = (data) => request.post('/admin/add', data)
export const updateStaff = (data) => request.put('/admin/update', data)
export const resetPassword = (id, password) => request.put(`/admin/resetPassword/${id}`, { password })
export const deleteStaff = (id) => request.delete(`/admin/delete/${id}`)

// 文件上传
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}



