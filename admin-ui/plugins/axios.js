import { getToken, setToken, setRefreshToken, setUserInfo, clearAuth, getRefreshToken, getUserInfo } from '@/utils/auth'

// 获取灵动岛实例
let islandInstance = null
// 是否正在刷新令牌
let isRefreshing = false
// 待重试的请求队列
let refreshSubscribers = []

export default ({ $axios, app, req }, inject) => {
  // 在客户端获取灵动岛实例
  if (process.client) {
    islandInstance = app
  }

  // 显示灵动岛消息
  const showIslandMessage = (message, type = 'info') => {
    if (islandInstance && islandInstance.$root) {
      islandInstance.$root.$emit('show-island-message', message, type)
    }
  }

  // 跳转到登录页的通用逻辑
  const redirectToLogin = (message, type = 'warning', delay = 1500) => {
    clearAuth()
    if (message) {
      showIslandMessage(message, type)
    }
    setTimeout(() => {
      if (process.client) {
        window.location.href = '/login-page/login'
      }
    }, delay)
  }

  // 判断是否为强制下线/会话失效，不应再尝试 refresh
  const isForcedLogoutMessage = (message = '') => {
    return ['会话已失效', '会话不存在', '会话已过期', '令牌已失效'].some(keyword => message.includes(keyword))
  }

  // 执行刷新队列中的所有请求
  const onRefreshed = (token) => {
    refreshSubscribers.forEach(callback => callback(token))
    refreshSubscribers = []
  }

  // 刷新令牌
  const handleRefreshToken = async () => {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      return null
    }

    try {
      // 使用特殊标记跳过请求拦截器的某些检查
      const response = await $axios.post('/api/user/refresh-token',
        { refreshToken },
        {
          headers: { 'X-Skip-Auth-Check': 'true' }
        }
      )
      
      const data = response.data || response
      if (data && (data.token || (data.data && data.data.token))) {
        const tokenInfo = data.token ? data : data.data
        const { token, refreshToken: newRefreshToken, expiresIn, status, userId, username, userType } = tokenInfo

        // 保存新的 token
        if (token) {
          setToken(token, expiresIn)
        }
        if (newRefreshToken) {
          setRefreshToken(newRefreshToken)
        }

        // 更新用户信息
        const currentUserInfo = getUserInfo()
        if (currentUserInfo) {
          const updatedUserInfo = {
            ...currentUserInfo,
            ...(status !== undefined && { status }),
            ...(userId && { userId }),
            ...(username && { username }),
            ...(userType !== undefined && { userType: Number(userType) })
          }
          setUserInfo(updatedUserInfo)

          // 同步到 Vuex store
          if (process.client && app.$store) {
            app.$store.commit('SET_USER_INFO', updatedUserInfo)
          }
        }

        return token
      }
      return null
    } catch (error) {
      console.error('刷新令牌失败:', error)
      return null
    }
  }

  // 1. 请求拦截器：自动带上 Authorization: Bearer {token}
  $axios.onRequest(config => {
    // 获取 cookie (如果是服务端，从 req.headers.cookie 获取)
    const cookieStr = process.server && req ? req.headers.cookie : null
    const token = getToken(cookieStr)
    const userInfo = getUserInfo(cookieStr)


    if (token) {
      // 核心要求：自动带上 Authorization 头
      // 强制使用 set 方法，确保覆盖可能存在的旧值
      config.headers.common['Authorization'] = `Bearer ${token}`
      config.headers['Authorization'] = `Bearer ${token}`
      
      // 其他辅助头
      if (userInfo && userInfo.userId) {
        config.headers.common['X-User-ID'] = String(userInfo.userId)
        config.headers['X-User-ID'] = String(userInfo.userId)
      }
      if (userInfo && userInfo.userType !== undefined) {
        config.headers.common['X-User-Type'] = String(userInfo.userType)
        config.headers['X-User-Type'] = String(userInfo.userType)
      }

      // 状态校验
      if (userInfo && userInfo.status === 0 && !config.headers['X-Skip-Auth-Check']) {
        if (process.client) {
          redirectToLogin('您的账户已被封禁，请联系管理员', 'danger', 2000)
        }
        return Promise.reject(new Error('账户已被封禁'))
      }
    } else {
        // 如果没有 Token，打印警告（仅调试用）
        console.warn('Axios Request: No token found for', config.url)
    }

    return config
  })

  // response interceptor
  $axios.interceptors.response.use(
    async response => {
      const res = response.data

      // 如果 HTTP 状态码是 2xx，直接认为成功
      if (response.status >= 200 && response.status < 300) {
        // 增加对后端自定义业务码的拦截 (如 403 无权限)
        if (res.code === 403 || res.code === 40300) {
          redirectToLogin('您没有该操作权限，请重新登录', 'danger')
          return Promise.reject(new Error('无权限访问'))
        }
        
        // 处理后端业务码：50014 未提供认证令牌 或 Token 无效
        if (res.code === 50014 || res.code === 401) {
          const authMessage = res.message || res.msg || ''
          if (isForcedLogoutMessage(authMessage)) {
            redirectToLogin(authMessage, 'warning')
            return Promise.reject(new Error(authMessage || '会话已失效'))
          }

          if (!response.config.url.includes('/refresh-token')) {
            const originalRequest = response.config
            
            if (isRefreshing) {
              return new Promise(resolve => {
                addRefreshSubscriber(token => {
                  originalRequest.headers['Authorization'] = `Bearer ${token}`
                  resolve($axios(originalRequest))
                })
              })
            }

            isRefreshing = true
            const newToken = await handleRefreshToken()
            
            if (newToken) {
              isRefreshing = false
              onRefreshed(newToken)
              originalRequest.headers['Authorization'] = `Bearer ${newToken}`
              return $axios(originalRequest)
            } else {
              isRefreshing = false
              redirectToLogin('登录已过期，请重新登录', 'warning')
              return Promise.reject(new Error('Token失效'))
            }
          } else {
             redirectToLogin('登录已过期，请重新登录', 'warning')
             return Promise.reject(new Error('Token失效'))
          }
        }

        // 保存登录返回的 token
        if (res.data && res.data.token) {
          const d = res.data
          setToken(d.token, d.expiresIn)
          if (d.refreshToken) {
            setRefreshToken(d.refreshToken)
          }
          if (d.userId && d.username) {
            const userInfo = {
              userId: d.userId,
              username: d.username,
              userType: d.userType !== undefined ? Number(d.userType) : undefined,
              status: d.status ?? 1,
              role: Number(d.userType) === 1 ? 'admin' : 'user',
              email: d.email,
              phone: d.phone,
              avatar: d.avatar
            }
            setUserInfo(userInfo)
            if (process.client && app.$store) {
              app.$store.commit('SET_USER_INFO', userInfo)
            }
            
            // 校验
            const localUser = getUserInfo()
            if (localUser && localUser.userId && d.userId && String(localUser.userId) !== String(d.userId)) {
              redirectToLogin('检测到账号异常，请重新登录', 'danger')
              return Promise.reject(new Error('用户身份不匹配'))
            }
          }
        }

        return res
      }
      return response
    },
    async error => {
      console.log('err' + error) // for debug

      if (error.response && error.response.data && error.response.data.code === 50015) {
        const banMessage = error.response.data.message || error.response.data.msg || '您的账户已被封禁，请联系管理员'
        redirectToLogin(banMessage, 'danger', 2000)
        return Promise.reject(error)
      }

      if (error.response && error.response.data) {
        const authMessage = error.response.data.message || error.response.data.msg || ''
        if (isForcedLogoutMessage(authMessage)) {
          redirectToLogin(authMessage, 'warning')
          return Promise.reject(error)
        }
      }

      // 401 未授权 或 403 无权限
      const isUnauthorized = error.response && (error.response.status === 401)
      const isForbidden = error.response && error.response.status === 403

      if (isUnauthorized || isForbidden) {
        const originalRequest = error.config

        if (isForbidden) {
          redirectToLogin('您没有该操作权限，请重新登录或联系管理员', 'danger')
          return Promise.reject(error)
        }

        if (originalRequest.url.includes('/refresh-token')) {
          redirectToLogin('登录已过期，请重新登录', 'warning')
          return Promise.reject(error)
        }

        if (isRefreshing) {
          return new Promise((resolve) => {
            addRefreshSubscriber((token) => {
              originalRequest.headers['Authorization'] = `Bearer ${token}`
              resolve($axios(originalRequest))
            })
          })
        }

        isRefreshing = true
        const newToken = await handleRefreshToken()

        if (newToken) {
          isRefreshing = false
          onRefreshed(newToken)
          originalRequest.headers['Authorization'] = `Bearer ${newToken}`
          return $axios(originalRequest)
        } else {
          isRefreshing = false
          redirectToLogin('登录已过期，请重新登录', 'warning')
          return Promise.reject(error)
        }
      } else if (error.response && error.response.data) {
        const errorMessage = error.response.data.message || error.response.data.msg || '请求失败'
        showIslandMessage(errorMessage, 'danger')
        console.error('后端错误:', error.response.data)
      } else {
        showIslandMessage(error.message || '网络错误', 'danger')
      }

      return Promise.reject(error)
    }
  )
}
