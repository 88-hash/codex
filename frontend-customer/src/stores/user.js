import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const sendCode = async (phone) => {
    await request.post('/auth/sendCode', { phone })
  }

  const login = async (phone, code) => {
    const res = await request.post('/auth/login', { phone, code })
    token.value = res.data.token
    userInfo.value = res.data.user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data.user))
    return res.data
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const updateUserInfo = async (data) => {
    await request.put('/user/update', data)
    userInfo.value = { ...userInfo.value, ...data }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  const isLoggedIn = () => !!token.value

  return {
    token,
    userInfo,
    sendCode,
    login,
    logout,
    updateUserInfo,
    isLoggedIn
  }
})



