import { ref } from 'vue'
import { register } from '@/api/auth'

/**
 * 注册状态管理 Composable
 */
export const useRegister = () => {
  const isLoading = ref(false)
  const hasError = ref(false)

  /**
   * 执行注册
   * @param {Object} axios - axios 实例
   * @param {Object} userData - 用户数据
   * @param {string} userData.username - 用户名
   * @param {string} userData.password - 密码
   * @param {string} userData.email - 邮箱地址
   * @param {string} userData.phone - 手机号码
   * @param {string} userData.school - 学校名称
   * @param {number} userData.gender - 性别
   * @param {string} userData.address - 真实地址（选填）
   * @returns {Promise<Object>} 注册响应
   */
  const performRegister = async (axios, userData) => {
    isLoading.value = true
    hasError.value = false

    try {
      const response = await register(axios, userData)

      if (response.code === 20000) {
        return { success: true, data: response }
      } else {
        throw new Error(response.message || '注册失败')
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
    performRegister
  }
}
