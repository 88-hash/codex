import request from '@/utils/request'

export const createOrder = (remark) => {
  return request.post('/order/create', { remark })
}

export const getOrderList = (status) => {
  return request.get('/order/list', { params: { status } })
}

export const getOrderDetail = (id) => {
  return request.get(`/order/detail/${id}`)
}

export const cancelOrder = (id) => {
  return request.put(`/order/cancel/${id}`)
}

export const addComment = (data) => {
  return request.post('/comment/add', data)
}

export const getMyComments = () => {
  return request.get('/comment/my')
}



