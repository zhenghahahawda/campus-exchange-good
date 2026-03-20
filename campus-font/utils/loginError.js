/**
 * 登录错误解析工具
 */

export const ERROR_MESSAGES = {
  VERIFICATION_REQUIRED: '请先完成人机验证',
  INVALID_CREDENTIALS: '账号或密码错误',
  ACCOUNT_BANNED: '账号已被封禁，请联系管理员',
  NETWORK_ERROR: '网络连接失败，请检查网络',
  SERVER_ERROR: '服务器错误，请稍后重试',
  UNKNOWN_ERROR: '登录失败，请稍后重试'
}

/**
 * 解析登录错误，返回用户友好的错误信息
 * @param {Error} error
 * @returns {{ message: string, field: string|null }}
 */
export function parseLoginError(error) {
  const msg = error?.message || ''

  if (msg.includes('账号') || msg.includes('密码') || msg.includes('credentials')) {
    return { message: ERROR_MESSAGES.INVALID_CREDENTIALS, field: 'account' }
  }

  if (msg.includes('封禁') || msg.includes('banned') || error?.response?.status === 403) {
    return { message: ERROR_MESSAGES.ACCOUNT_BANNED, field: null }
  }

  if (msg.includes('Network') || msg.includes('ECONNREFUSED') || msg.includes('network')) {
    return { message: ERROR_MESSAGES.NETWORK_ERROR, field: null }
  }

  if (error?.response?.status >= 500) {
    return { message: ERROR_MESSAGES.SERVER_ERROR, field: null }
  }

  return { message: msg || ERROR_MESSAGES.UNKNOWN_ERROR, field: null }
}
