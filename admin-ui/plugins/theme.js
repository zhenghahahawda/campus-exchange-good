export default ({ app }) => {
  // 在客户端加载时应用保存的主题
  if (process.client) {
    const savedTheme = localStorage.getItem('selected-theme')
    if (savedTheme && savedTheme !== 'default') {
      document.documentElement.setAttribute('data-theme', savedTheme)
    }
  }
}
