/**
 * URL辅助工具 - 处理用户信息查询参数
 */

import { getUserInfo } from './auth'

/**
 * 获取用户信息查询参数
 * @returns {Object} 包含userId、username、userType的对象
 */
export function getUserQueryParams() {
  const userInfo = getUserInfo()
  
  if (!userInfo || !userInfo.userId) {
    return {}
  }
  
  return {
    userId: userInfo.userId,
    username: userInfo.username,
    userType: userInfo.userType
  }
}

/**
 * 为路由添加用户信息查询参数
 * @param {String} path - 路由路径
 * @param {Object} query - 额外的查询参数
 * @returns {Object} 包含path和query的路由对象
 */
export function addUserQueryToRoute(path, query = {}) {
  const userQuery = getUserQueryParams()
  
  return {
    path,
    query: {
      ...query,
      ...userQuery
    }
  }
}

/**
 * 检查当前URL是否包含用户信息
 * @param {Object} route - Vue Router的route对象
 * @returns {Boolean}
 */
export function hasUserQueryParams(route) {
  return !!(route.query.userId || route.query.uid)
}

/**
 * 从URL查询参数中提取用户信息
 * @param {Object} route - Vue Router的route对象
 * @returns {Object|null} 用户信息对象或null
 */
export function extractUserFromQuery(route) {
  const userId = route.query.userId || route.query.uid
  const username = route.query.username
  const userType = route.query.userType
  
  if (!userId) {
    return null
  }
  
  return {
    userId: parseInt(userId),
    username,
    userType: parseInt(userType)
  }
}
