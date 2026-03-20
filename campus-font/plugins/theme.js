// 主题持久化插件
export default ({ app }) => {
  // 只在客户端执行
  if (process.client) {
    // 页面加载时恢复保存的主题
    const savedTheme = localStorage.getItem('selected-theme')
    if (savedTheme && savedTheme !== 'default') {
      document.documentElement.setAttribute('data-theme', savedTheme)
    }
  }
}