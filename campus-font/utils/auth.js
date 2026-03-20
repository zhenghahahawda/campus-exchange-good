/**
 * 认证工具函数 - 使用 localStorage 存储
 */

const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_INFO_KEY = 'userInfo'

// ==================== Token 管理 ====================

export function getToken() {
  if (typeof window === 'undefined') return null
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function setToken(token) {
  if (typeof window === 'undefined') return
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
}

export function removeToken() {
  if (typeof window === 'undefined') return
  localStorage.removeItem(ACCESS_TOKEN_KEY)
}

export function getRefreshToken() {
  if (typeof window === 'undefined') return null
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setRefreshToken(refreshToken) {
  if (typeof window === 'undefined') return
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

export function removeRefreshToken() {
  if (typeof window === 'undefined') return
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

// ==================== 用户信息管理 ====================

export function getUserInfo() {
  if (typeof window === 'undefined') return null
  const userInfo = localStorage.getItem(USER_INFO_KEY)
  try {
    return userInfo ? JSON.parse(userInfo) : null
  } catch (e) {
    console.error('解析用户信息失败:', e)
    return null
  }
}

export function setUserInfo(userInfo) {
  if (typeof window === 'undefined') return
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

export function removeUserInfo() {
  if (typeof window === 'undefined') return
  localStorage.removeItem(USER_INFO_KEY)
}

// ==================== 清除所有认证信息 ====================

export function clearAuth() {
  if (typeof window === 'undefined') return
  removeToken()
  removeRefreshToken()
  removeUserInfo()
  // 也可以使用 localStorage.clear() 清除所有数据
}

// ==================== 登录状态检查 ====================

/**
 * 检查用户是否已登录
 * @returns {boolean}
 */
export function isLoggedIn() {
  const token = getToken()
  const userInfo = getUserInfo()
  return !!(token && userInfo)
}

/**
 * 验证当前用户身份是否一致
 * @param {string} expectedUserId - 期望的用户 ID
 * @returns {boolean} 是否匹配
 */
export function verifyCurrentUser(expectedUserId) {
  const userInfo = getUserInfo()
  if (!userInfo || !userInfo.userId) {
    return false
  }
  return userInfo.userId === expectedUserId
}

// ==================== 会话指纹（可选安全功能）====================

/**
 * 生成会话指纹（用于检测会话异常）
 * @returns {string} 会话指纹
 */
export function generateSessionFingerprint() {
  if (typeof window === 'undefined') return ''

  const userInfo = getUserInfo()
  const userId = userInfo?.userId || ''
  const userAgent = navigator.userAgent
  const screenResolution = `${window.screen.width}x${window.screen.height}`
  const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone

  // 简单的哈希函数
  const hash = (str) => {
    let hash = 0
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i)
      hash = ((hash << 5) - hash) + char
      hash = hash & hash
    }
    return hash.toString(36)
  }

  return hash(`${userId}-${userAgent}-${screenResolution}-${timezone}`)
}

/**
 * 保存会话指纹
 */
export function saveSessionFingerprint() {
  if (typeof window === 'undefined') return
  const fingerprint = generateSessionFingerprint()
  if (fingerprint) {
    localStorage.setItem('sessionFingerprint', fingerprint)
  }
}

/**
 * 验证会话指纹
 * @returns {boolean} 是否匹配
 */
export function verifySessionFingerprint() {
  if (typeof window === 'undefined') return false
  const savedFingerprint = localStorage.getItem('sessionFingerprint')
  const currentFingerprint = generateSessionFingerprint()

  if (!savedFingerprint || !currentFingerprint) {
    return false
  }

  return savedFingerprint === currentFingerprint
}
