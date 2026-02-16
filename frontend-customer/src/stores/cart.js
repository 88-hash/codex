import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useCartStore = defineStore('cart', () => {
  const cartList = ref([])

  const totalCount = computed(() => {
    return cartList.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const checkedItems = computed(() => {
    return cartList.value.filter(item => item.isChecked && item.goodsIsOnSale)
  })

  const checkedTotal = computed(() => {
    return checkedItems.value.reduce((sum, item) => {
      return sum + item.goodsPrice * item.quantity
    }, 0)
  })

  const fetchCart = async () => {
    const res = await request.get('/cart/list')
    cartList.value = res.data || []
  }

  const addToCart = async (goodsId, quantity = 1) => {
    await request.post('/cart/add', { goodsId, quantity })
    await fetchCart()
  }

  const updateQuantity = async (id, quantity) => {
    await request.put(`/cart/quantity/${id}`, null, { params: { quantity } })
    const item = cartList.value.find(i => i.id === id)
    if (item) item.quantity = quantity
  }

  const updateChecked = async (id, isChecked) => {
    await request.put(`/cart/checked/${id}`, null, { params: { isChecked } })
    const item = cartList.value.find(i => i.id === id)
    if (item) item.isChecked = isChecked
  }

  const updateAllChecked = async (isChecked) => {
    await request.put('/cart/checkedAll', null, { params: { isChecked } })
    cartList.value.forEach(item => item.isChecked = isChecked)
  }

  const removeItem = async (id) => {
    await request.delete(`/cart/delete/${id}`)
    cartList.value = cartList.value.filter(item => item.id !== id)
  }

  const clearCart = () => {
    cartList.value = []
  }

  return {
    cartList,
    totalCount,
    checkedItems,
    checkedTotal,
    fetchCart,
    addToCart,
    updateQuantity,
    updateChecked,
    updateAllChecked,
    removeItem,
    clearCart
  }
})



