import { getUserInfo, verifySessionFingerprint, clearAuth, saveSessionFingerprint } from '@/utils/auth'
import { verifySession } from '@/api/auth'

export default ({ app, $axios }) => {
  if (process.client) {
    // 登录成功后保存会话指纹
    app.router.afterEach((to, from) => {
      const userInfo = getUserInfo()
      if (userInfo && userInfo.userId) {
        // 保存会话指纹
        saveSessionFingerprint()
      }
    })

    // 定期验证用户会话（每 2 分钟）
    let verificationInterval = null

    const startSessionVerification = () => {
      if (verificationInterval) {
        clearInterval(verificationInterval)
      }

      verificationInterval = setInterval(async () => {
        const userInfo = getUserInfo()
        if (!userInfo || !userInfo.userId) {
          clearInterval(verificationInterval)
          return
        }

        try {
          // 1. 验证会话指纹
          if (!verifySessionFingerprint()) {
            console.warn('会话指纹验证失败')
            handleSessionInvalid('会话异常，请重新登录')
            return
          }

          // 2. 向后端验证会话
          const response = await verifySession($axios)
          if (response.code === 20000 && response.data) {
            // 验证返回的用户 ID 是否与本地一致
            if (response.data.userId !== userInfo.userId) {
              console.error('用户 ID 不匹配')
              handleSessionInvalid('账号异常，请重新登录')
            }
          }
        } catch (error) {
          console.error('会话验证失败:', error)
          // 如果是 401 错误，会由 axios 拦截器处理
        }
      }, 120000) // 每 2 分钟验证一次
    }

    const handleSessionInvalid = (message) => {
      clearInterval(verificationInterval)
      clearAuth()

      if (app.$root) {
        app.$root.$emit('show-island-message', message, 'warning')
      }

      setTimeout(() => {
        window.location.href = '/login-page/login'
      }, 1500)
    }

    // 监听用户登录事件
    if (app.$root) {
      app.$root.$on('user-logged-in', () => {
        startSessionVerification()
      })

      app.$root.$on('user-logged-out', () => {
        if (verificationInterval) {
          clearInterval(verificationInterval)
        }
      })
    }

    // 如果已经登录，启动验证
    const userInfo = getUserInfo()
    if (userInfo && userInfo.userId) {
      startSessionVerification()
    }

    // 页面卸载时清理
    window.addEventListener('beforeunload', () => {
      if (verificationInterval) {
        clearInterval(verificationInterval)
      }
    })
  }
}
