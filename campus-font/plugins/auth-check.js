/**
 * 认证状态检查插件
 * 在应用启动时验证登录状态
 */

import { getToken, getUserInfo, clearAuth } from '@/utils/auth'

export default async ({ $axios, redirect }) => {
  if (process.client) {
    const token = getToken()
    const userInfo = getUserInfo()

    if (token && userInfo) {
      try {
        const response = await $axios.get('/auth/verify', {
          headers: { Authorization: `Bearer ${token}` }
        })
        if (response.code !== 20000) {
          clearAuth()
          redirect('/login-page/login')
        }
      } catch (error) {
        if (error.response?.status === 404) return
        if (error.response?.status === 401) {
          clearAuth()
          redirect('/login-page/login')
        }
        // 其他错误（网络超时等）不清除登录状态
      }
    }
  }
}
