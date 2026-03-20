import { MessageBox, Message } from 'element-ui'
import { getToken, setToken, setRefreshToken, clearAuth } from '@/utils/auth'

let isRefreshing = false
let failedQueue = []
let statusCheckTimer = null
let isLoggingOut = false

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

// 强制下线处理
const forceLogout = (redirect, reason = '') => {
  if (isLoggingOut) return
  isLoggingOut = true
  isRefreshing = false
  failedQueue = []
  clearAuth()
  localStorage.removeItem('userInfo')
  if (statusCheckTimer) {
    clearInterval(statusCheckTimer)
    statusCheckTimer = null
  }
  redirect('/login-page/login')
}

export default ({ $axios, redirect }) => {
  // 登录成功后重置登出状态
  $axios.resetAuthState = () => {
    isLoggingOut = false
    isRefreshing = false
    failedQueue = []
  }

  // 启动定时检测用户状态（每30秒检查一次）
  if (process.client) {
    const startStatusCheck = () => {
      if (statusCheckTimer) clearInterval(statusCheckTimer)
      statusCheckTimer = setInterval(async () => {
        const token = getToken()
        if (!token) return  // 未登录不检查
        try {
          await $axios.get('/user/info')
        } catch (e) {
          // 401会被response拦截器处理，自动跳转登录
        }
      }, 30000)  // 30秒检查一次
    }
    
    // 页面加载时启动
    startStatusCheck()
  }

  // request interceptor
  $axios.interceptors.request.use(
    config => {
      const token = getToken()
      if (token) {
        config.headers = config.headers || {}
        config.headers['Authorization'] = `Bearer ${token}`
      }
      return config
    },
    error => {
      console.error('❌ 请求错误:', error)
      return Promise.reject(error)
    }
  )

  // response interceptor
  $axios.interceptors.response.use(
    response => {
      const res = response.data

      // 检查业务状态码
      if (res.code !== 20000) {
        // Token相关错误处理
        if (res.code === 50008 || res.code === 50012 || res.code === 50014) {
          MessageBox.confirm('登录已过期，请重新登录', '登录过期', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            clearAuth()
            redirect('/login-page/login')
          })
        }
        
        return Promise.reject(new Error(res.message || 'Error'))
      } else {
        // 自动保存新token（如果后端返回了新token）
        if (res.data && res.data.token) {
          setToken(res.data.token)
        }
        if (res.data && res.data.refreshToken) {
          setRefreshToken(res.data.refreshToken)
        }
        
        return res
      }
    },
    async error => {
      const originalRequest = error.config

      // 处理401错误
      if (error.response && error.response.status === 401) {
        // 如果是 refresh-token 接口本身返回401，直接踢出，避免死循环
        if (originalRequest.url?.includes('/auth/refresh-token')) {
          forceLogout(redirect)
          return Promise.reject(error)
        }

        if (originalRequest._retry) {
          forceLogout(redirect)
          return Promise.reject(error)
        }

        // 如果已经在刷新中，加入队列等待
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject })
          }).then(token => {
            originalRequest._retry = true
            originalRequest.headers['Authorization'] = 'Bearer ' + token
            return $axios(originalRequest)
          }).catch(err => Promise.reject(err))
        }

        const refreshToken = localStorage.getItem('refreshToken')

        // 没有 refreshToken，说明根本没登录，直接踢出
        if (!refreshToken) {
          const accessToken = getToken()
          if (!accessToken) {
            forceLogout(redirect)
          }
          return Promise.reject(error)
        }

        originalRequest._retry = true
        isRefreshing = true
        try {
          const res = await $axios.post('/auth/refresh-token', { refreshToken })
          const newToken = res.data.token
          setToken(newToken)
          setRefreshToken(res.data.refreshToken)
          originalRequest.headers['Authorization'] = 'Bearer ' + newToken
          processQueue(null, newToken)
          return $axios(originalRequest)
        } catch (e) {
          processQueue(e, null)
          forceLogout(redirect)
          return Promise.reject(e)
        } finally {
          isRefreshing = false
        }
      }

      // 根据HTTP状态码处理其他错误
      if (error.response) {
        const { status } = error.response
        
        switch (status) {
          case 403:
            Message({
              message: '访问被拒绝，权限不足',
              type: 'error',
              duration: 5 * 1000
            })
            break
          case 404:
            break
          case 500:
            break
          default:
            break
        }
      } else if (error.code === 'ECONNREFUSED') {
        Message({
          message: '无法连接到服务器，请检查后端服务是否启动',
          type: 'error',
          duration: 8 * 1000
        })
      } else {
        console.error('网络错误:', error.message)
      }
      
      return Promise.reject(error)
    }
  )
}
