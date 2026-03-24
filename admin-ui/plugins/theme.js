import { ThemeManager } from '@/utils/themeManager'

export default ({ app }) => {
  // 在客户端加载时应用保存的主题
  if (process.client) {
    const savedTheme = ThemeManager.getCurrentTheme()
    document.documentElement.setAttribute('data-theme', savedTheme)
  }
}
