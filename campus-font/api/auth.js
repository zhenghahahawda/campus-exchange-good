import { setToken, setRefreshToken, setUserInfo } from '@/utils/auth'

/**
 * 认证相关 API
 */

/**
 * 用户登录
 * @param {Object} data - 登录数据
 * @param {string} data.account - 账号（用户名、邮箱或手机号）
 * @param {string} data.password - 密码
 * @returns {Promise}
 */
export function login(axios, data) {
  return axios({
    url: '/user/login',
    method: 'post',
    data: {
      account: data.account,
      password: data.password
    }
  })
}

/**
 * 用户登出
 * @returns {Promise}
 */
export function logout(axios) {
  return axios({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 获取用户信息
 * @returns {Promise}
 */
export function getUserInfo(axios) {
  return axios({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息
 * @returns {Promise}
 */
export function updateUserInfo(axios, data) {
  return axios({
    url: '/user/info',
    method: 'put',
    data
  })
}

/**
 * 上传头像
 * @param {FormData} formData - 包含头像文件的 FormData
 * @returns {Promise}
 */
export function uploadAvatar(axios, formData) {
  return axios({
    url: '/user/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 刷新 Token
 * @param {string} refreshToken - 刷新令牌
 * @returns {Promise}
 */
export function refreshToken(axios, refreshToken) {
  return axios({
    url: '/user/refresh-token',
    method: 'post',
    data: {
      refreshToken
    }
  })
}

/**
 * 修改密码
 * @param {Object} data - 修改密码数据
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise}
 */
export function changePassword(axios, data) {
  return axios({
    url: '/auth/change-password',
    method: 'post',
    data
  })
}

/**
 * 忘记密码 - 发送验证码
 * @param {Object} data - 数据
 * @param {string} data.account - 账号（邮箱或手机号）
 * @returns {Promise}
 */
export function sendResetCode(axios, data) {
  return axios({
    url: '/auth/send-reset-code',
    method: 'post',
    data
  })
}

/**
 * 忘记密码 - 重置密码
 * @param {Object} data - 重置密码数据
 * @param {string} data.account - 账号
 * @param {string} data.code - 验证码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise}
 */
export function resetPassword(axios, data) {
  return axios({
    url: '/auth/reset-password',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 * @param {Object} data - 注册数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.email - 邮箱地址
 * @param {string} data.phone - 手机号码
 * @param {string} data.school - 学校名称
 * @param {number} data.gender - 性别：1=男，2=女
 * @param {string} data.address - 真实地址（选填）
 * @returns {Promise}
 */
export function register(axios, data) {
  return axios({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 验证当前用户会话
 * @returns {Promise}
 */
export function verifySession(axios) {
  return axios({
    url: '/auth/verify-session',
    method: 'get'
  })
}
