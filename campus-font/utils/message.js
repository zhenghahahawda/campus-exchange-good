/**
 * 消息通知工具
 */

/**
 * 显示灵动岛消息
 * @param {Object} root - Vue 根实例
 * @param {string} message - 消息内容
 * @param {string} type - 消息类型: success, warning, danger, info
 */
export const showMessage = (root, message, type = 'info') => {
  if (root && root.$emit) {
    root.$emit('show-island-message', message, type)
  }
}

/**
 * 显示成功消息
 */
export const showSuccess = (root, message) => {
  showMessage(root, message, 'success')
}

/**
 * 显示登录成功庆祝消息（带特殊效果）
 */
export const showLoginSuccess = (root, username) => {
  const message = username ? `欢迎回来，${username}！` : '登录成功！'
  showMessage(root, message, 'success')

  // 触发庆祝动画事件
  if (root && root.$emit) {
    root.$emit('celebrate-login')
  }
}

/**
 * 显示警告消息
 */
export const showWarning = (root, message) => {
  showMessage(root, message, 'warning')
}

/**
 * 显示错误消息
 */
export const showError = (root, message) => {
  showMessage(root, message, 'danger')
}

/**
 * 显示信息消息
 */
export const showInfo = (root, message) => {
  showMessage(root, message, 'info')
}
