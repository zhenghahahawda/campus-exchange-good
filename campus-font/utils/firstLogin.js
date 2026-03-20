/**
 * 判断是否为首次登录
 * 根据用户信息中的标志位或本地记录判断
 */

const KEY = 'hasLoggedIn'

/**
 * @param {Object} userInfo - 登录返回的用户信息
 * @returns {boolean}
 */
export function isFirstLogin(userInfo) {
  if (!userInfo) return false

  // 如果后端返回了首次登录标志
  if (userInfo.isFirstLogin === true || userInfo.firstLogin === true) {
    return true
  }

  // 本地记录：如果从未登录过
  const userId = userInfo.userId
  if (!userId) return false

  const key = `${KEY}_${userId}`
  const hasLogged = localStorage.getItem(key)

  if (!hasLogged) {
    localStorage.setItem(key, '1')
    return true
  }

  return false
}
