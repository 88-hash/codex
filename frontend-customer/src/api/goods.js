import request from '@/utils/request'

export const getGoodsList = (params) => {
  return request.get('/goods/list', { params })
}

export const getGoodsDetail = (id) => {
  return request.get(`/goods/detail/${id}`)
}

export const getCategoryList = () => {
  return request.get('/category/list')
}



