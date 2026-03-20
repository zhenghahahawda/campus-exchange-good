import { ref } from 'vue'
import { login } from '@/api/auth'

/**
 * 登录状态管理 Composable
 */
export const useLogin = () => {
  const isLoading = ref(false)
  const hasError = ref(false)

  /**
   * 执行登录
   * @param {Object} axios - axios 实例
   * @param {Object} credentials - 登录凭证
   * @param {string} credentials.account - 账号
   * @param {string} credentials.password - 密码
   * @returns {Promise<Object>} 登录响应
   */
  const performLogin = async (axios, credentials) => {
    isLoading.value = true
    hasError.value = false

    try {
      const response = await login(axios, credentials)

      if (response.code === 20000) {
        return { success: true, data: response }
      } else {
        throw new Error(response.message || '登录失败')
      }
    } catch (error) {
      hasError.value = true
      throw error
    } finally {
      isLoading.value = false
    }
  }

  return {
    isLoading,
    hasError,
    performLogin
  }
}
