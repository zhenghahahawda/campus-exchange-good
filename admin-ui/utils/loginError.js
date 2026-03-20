/**
 * 登录错误处理工具
 */

/**
 * 解析登录错误
 * @param {Error} error - 错误对象
 * @returns {Object} { message: string, field: string|null }
 */
export const parseLoginError = (error) => {
  let message = '登录失败，请检查您的账号密码'
  let field = null

  if (error.response && error.response.data) {
    const { data } = error.response

    if (data.message) {
      message = data.message

      // 判断是否为账号密码相关错误
      if (data.message.includes('账号') || data.message.includes('密码')) {
        field = 'account'
      }
    }
  } else if (error.message) {
    message = error.message
  }

  return { message, field }
}

/**
 * 常见错误消息映射
 */
export const ERROR_MESSAGES = {
  VERIFICATION_REQUIRED: '请先完成人机验证',
  LOGIN_FAILED: '登录失败，请检查您的账号密码',
  ACCOUNT_DISABLED: '账户已被禁用',
  ACCOUNT_LOCKED: '账户已被锁定，请稍后再试',
  INVALID_CREDENTIALS: '账号或密码错误'
}
