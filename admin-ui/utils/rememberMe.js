import Cookies from 'js-cookie'

const REMEMBER_ACCOUNT_KEY = 'remember-account'
const REMEMBER_FLAG_KEY = 'remember-me'

/**
 * 保存记住的账号（仅保存账号，不保存密码以确保安全）
 * @param {string} account - 账号
 */
export function saveRememberMe(account) {
  Cookies.set(REMEMBER_ACCOUNT_KEY, account, { expires: 30 }) // 30天
  Cookies.set(REMEMBER_FLAG_KEY, 'true', { expires: 30 })
}

/**
 * 获取记住的账号（仅返回账号，不返回密码）
 * @returns {Object|null} { account, remember } 或 null
 */
export function getRememberMe() {
  const remember = Cookies.get(REMEMBER_FLAG_KEY)

  if (remember !== 'true') {
    return null
  }

  const account = Cookies.get(REMEMBER_ACCOUNT_KEY)

  if (!account) {
    return null
  }

  return {
    account,
    remember: true
  }
}

/**
 * 清除记住的账号
 */
export function clearRememberMe() {
  Cookies.remove(REMEMBER_ACCOUNT_KEY)
  Cookies.remove(REMEMBER_FLAG_KEY)
}

/**
 * 检查是否有记住的登录信息
 * @returns {boolean}
 */
export function hasRememberMe() {
  return Cookies.get(REMEMBER_FLAG_KEY) === 'true'
}
