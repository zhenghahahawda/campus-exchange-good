import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'
const RefreshTokenKey = 'Admin-Refresh-Token'
const UserInfoKey = 'Admin-User-Info'

/**
 * 获取 Token
 * 支持从 Cookie (客户端) 或直接传入的 cookie 字符串 (服务端) 获取
 */
export function getToken(cookieStr = null) {
  if (process.server && cookieStr) {
    const match = cookieStr.match(new RegExp('(^| )' + TokenKey + '=([^;]*)(;|$)'))
    return match ? decodeURIComponent(match[2]) : null
  }
  return Cookies.get(TokenKey)
}

export function setToken(token, expiresIn) {
  // expiresIn 单位是秒，转换为天数
  const expires = expiresIn ? expiresIn / (24 * 60 * 60) : 7
  return Cookies.set(TokenKey, token, { expires })
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}

export function getRefreshToken(cookieStr = null) {
  if (process.server && cookieStr) {
    const match = cookieStr.match(new RegExp('(^| )' + RefreshTokenKey + '=([^;]*)(;|$)'))
    return match ? decodeURIComponent(match[2]) : null
  }
  return Cookies.get(RefreshTokenKey)
}

export function setRefreshToken(refreshToken) {
  // refreshToken 有效期7天
  return Cookies.set(RefreshTokenKey, refreshToken, { expires: 7 })
}

export function removeRefreshToken() {
  return Cookies.remove(RefreshTokenKey)
}

export function getUserInfo(cookieStr = null) {
  let userInfoStr = null
  if (process.server && cookieStr) {
    const match = cookieStr.match(new RegExp('(^| )' + UserInfoKey + '=([^;]*)(;|$)'))
    userInfoStr = match ? decodeURIComponent(match[2]) : null
  } else {
    userInfoStr = Cookies.get(UserInfoKey)
  }
  
  try {
    return userInfoStr ? JSON.parse(userInfoStr) : null
  } catch (e) {
    return null
  }
}

export function setUserInfo(userInfo) {
  return Cookies.set(UserInfoKey, JSON.stringify(userInfo), { expires: 7 })
}

/**
 * 获取用户状态
 * @returns {number|null} 0=封禁，1=正常，2=活跃
 */
export function getUserStatus() {
  const userInfo = getUserInfo()
  return userInfo?.status ?? null
}

/**
 * 检查用户是否被封禁
 * @returns {boolean}
 */
export function isUserBanned() {
  const status = getUserStatus()
  return status === 0
}

export function removeUserInfo() {
  return Cookies.remove(UserInfoKey)
}

export function clearAuth() {
  removeToken()
  removeRefreshToken()
  removeUserInfo()
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
  return String(userInfo.userId) === String(expectedUserId)
}

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
  const fingerprint = generateSessionFingerprint()
  if (fingerprint) {
    Cookies.set('Session-Fingerprint', fingerprint, { expires: 7 })
  }
}

/**
 * 验证会话指纹
 * @returns {boolean} 是否匹配
 */
export function verifySessionFingerprint() {
  const savedFingerprint = Cookies.get('Session-Fingerprint')
  const currentFingerprint = generateSessionFingerprint()

  if (!savedFingerprint || !currentFingerprint) {
    return false
  }

  return savedFingerprint === currentFingerprint
}
