import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

const normalizeUserInfo = (value) => {
  const source = value && typeof value === 'object' ? { ...value } : {}
  const avatar = source.avatar || source.avatarUrl || ''
  return {
    ...source,
    avatar
  }
}

const readStoredUserInfo = () => {
  try {
    return normalizeUserInfo(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  } catch {
    return {}
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(readStoredUserInfo())

  const persistUserInfo = (value) => {
    const normalized = normalizeUserInfo(value)
    userInfo.value = normalized
    localStorage.setItem('userInfo', JSON.stringify(normalized))
    return normalized
  }

  const sendCode = async (phone) => {
    await request.post('/auth/sendCode', { phone })
  }

  const login = async (phone, code) => {
    const res = await request.post('/auth/login', { phone, code })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    persistUserInfo(res.data.user)
    return {
      ...res.data,
      user: userInfo.value
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const updateUserInfo = async (data) => {
    const res = await request.put('/user/update', data)
    return persistUserInfo(res.data)
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
