// 主题管理工具类 - 数据库化版本
// CSS变量通过JS动态注入，不再依赖SCSS中的[data-theme]选择器

const DEFAULT_THEME_KEY = 'ios-style'
const LEGACY_THEME_KEYS = ['default']

// 所有可注入的CSS变量名列表，用于清除时遍历
const CSS_VAR_KEYS = [
  '--color-primary', '--color-primary-rgb',
  '--color-secondary-success', '--color-secondary-success-rgb',
  '--color-secondary-warning', '--color-secondary-warning-rgb',
  '--color-secondary-danger', '--color-secondary-danger-rgb',
  '--color-secondary-info', '--color-secondary-info-rgb',
  '--color-bg-page', '--color-bg-surface', '--color-border',
  '--color-text-primary', '--color-text-secondary', '--color-text-disabled',
  '--color-sidebar-user-card-bg',
  '--color-nav-active-bg', '--color-nav-active-text',
  '--color-pill-bg', '--color-pill-active-bg', '--color-pill-active-text',
  '--glass-bg', '--glass-border', '--glass-shadow', '--glass-blur',
  '--card-bg', '--card-border', '--card-shadow',
  '--theme-wallpaper'
]

export class ThemeManager {
  static normalizeThemeKey(themeKey) {
    if (!themeKey || LEGACY_THEME_KEYS.includes(themeKey)) {
      return DEFAULT_THEME_KEY
    }
    return themeKey
  }

  static normalizeThemeData(themeData) {
    if (!themeData) return null
    return {
      ...themeData,
      themeKey: this.normalizeThemeKey(themeData.themeKey)
    }
  }

  /**
   * 应用主题（接收完整主题数据对象）
   * @param {Object} themeData - 主题数据 { themeKey, cssVariables, ... }
   */
  static applyTheme(themeData) {
    const normalizedThemeData = this.normalizeThemeData(themeData)
    if (!normalizedThemeData) return

    const root = document.documentElement

    // 解析 cssVariables（可能是JSON字符串或对象）
    let vars = normalizedThemeData.cssVariables
    if (typeof vars === 'string') {
      try { vars = JSON.parse(vars) } catch (e) { vars = {} }
    }
    // 从 css_variables 中移除 --theme-wallpaper，壁纸由独立字段管理
    if (vars) delete vars['--theme-wallpaper']
    this.applyThemeVariables(vars || {})

    // 根据 wallpaperUrl + wallpaperType 动态生成 --theme-wallpaper
    if (normalizedThemeData.wallpaperType === 'image' && normalizedThemeData.wallpaperUrl) {
      root.style.setProperty('--theme-wallpaper', `url(${normalizedThemeData.wallpaperUrl}) center/cover no-repeat fixed`)
    }

    root.setAttribute('data-theme', normalizedThemeData.themeKey)

    // 本地持久化
    localStorage.setItem('selected-theme', normalizedThemeData.themeKey)
    localStorage.setItem('selected-theme-data', JSON.stringify(normalizedThemeData))
  }

  /**
   * 通过JS动态注入CSS变量到:root
   */
  static applyThemeVariables(cssVariables) {
    const root = document.documentElement
    // 先清除旧的自定义变量
    this.clearCustomVariables()
    // 注入新变量
    Object.entries(cssVariables).forEach(([key, value]) => {
      root.style.setProperty(key, value)
    })
  }

  /**
   * 清除所有自定义注入的CSS变量，回到:root默认值
   */
  static clearCustomVariables() {
    const root = document.documentElement
    CSS_VAR_KEYS.forEach(key => {
      root.style.removeProperty(key)
    })
  }

  /**
   * 获取当前主题key
   */
  static getCurrentTheme() {
    return this.normalizeThemeKey(localStorage.getItem('selected-theme'))
  }

  /**
   * 获取当前主题完整数据（从localStorage）
   */
  static getCurrentThemeData() {
    try {
      const data = localStorage.getItem('selected-theme-data')
      return data ? this.normalizeThemeData(JSON.parse(data)) : null
    } catch (e) {
      return null
    }
  }

  /**
   * 从localStorage加载并应用已保存的主题
   */
  static loadSavedTheme() {
    const themeData = this.getCurrentThemeData()
    if (themeData) {
      this.applyTheme(themeData)
      return themeData.themeKey
    }

    const themeKey = this.getCurrentTheme()
    if (process.client) {
      document.documentElement.setAttribute('data-theme', themeKey)
      localStorage.setItem('selected-theme', themeKey)
      localStorage.removeItem('selected-theme-data')
    }
    return themeKey
  }

  /**
   * 主题切换动画（使用 View Transitions API）
   * @param {Object} themeData - 目标主题数据对象
   * @param {Object} clickEvent - 点击事件对象
   * @param {Function} callback - 切换完成回调
   */
  static transitionTheme(themeData, clickEvent, callback) {
    const normalizedThemeData = this.normalizeThemeData(themeData)
    if (!normalizedThemeData) {
      return Promise.resolve()
    }

    // 不支持 View Transitions API 时直接应用
    if (!document.startViewTransition) {
      this.applyTheme(normalizedThemeData)
      if (callback) callback()
      return Promise.resolve()
    }

    // 获取点击位置
    const rect = clickEvent.currentTarget.getBoundingClientRect()
    const x = rect.left + rect.width / 2
    const y = rect.top + rect.height / 2

    // 计算扩散半径
    const endRadius = Math.hypot(
      Math.max(x, window.innerWidth - x),
      Math.max(y, window.innerHeight - y)
    )

    // 开始过渡
    const transition = document.startViewTransition(() => {
      this.applyTheme(normalizedThemeData)
    })

    transition.ready.then(() => {
      document.documentElement.animate(
        {
          clipPath: [
            `circle(0px at ${x}px ${y}px)`,
            `circle(${endRadius}px at ${x}px ${y}px)`,
          ],
        },
        {
          duration: 600,
          easing: 'ease-in-out',
          pseudoElement: '::view-transition-new(root)',
        }
      )
    })

    return transition.finished.then(() => {
      if (callback) callback()
    })
  }
}

/**
 * 主题筛选工具
 */
export class ThemeFilter {
  /**
   * 按分类筛选主题
   */
  static filterByCategory(themes, categoryId) {
    if (categoryId === 'all') return themes
    return themes.filter(theme => {
      let cats = theme.category
      if (typeof cats === 'string') {
        try { cats = JSON.parse(cats) } catch (e) { cats = [] }
      }
      return Array.isArray(cats) && cats.includes(categoryId)
    })
  }

  /**
   * 按搜索关键词筛选主题
   */
  static filterBySearch(themes, query) {
    if (!query.trim()) return themes
    const lowerQuery = query.toLowerCase()
    return themes.filter(theme => {
      let tags = theme.tags
      if (typeof tags === 'string') {
        try { tags = JSON.parse(tags) } catch (e) { tags = [] }
      }
      return (
        theme.name.toLowerCase().includes(lowerQuery) ||
        (theme.description && theme.description.toLowerCase().includes(lowerQuery)) ||
        (Array.isArray(tags) && tags.some(tag => tag.toLowerCase().includes(lowerQuery)))
      )
    })
  }

  /**
   * 综合筛选（分类 + 搜索）
   */
  static filter(themes, categoryId, searchQuery) {
    let filtered = this.filterByCategory(themes, categoryId)
    filtered = this.filterBySearch(filtered, searchQuery)
    return filtered
  }

  /**
   * 更新分类计数
   */
  static updateCategoryCounts(categories, themes) {
    return categories.map(category => {
      if (category.id === 'all') {
        return { ...category, count: themes.length }
      }
      const count = themes.filter(theme => {
        let cats = theme.category
        if (typeof cats === 'string') {
          try { cats = JSON.parse(cats) } catch (e) { cats = [] }
        }
        return Array.isArray(cats) && cats.includes(category.id)
      }).length
      return { ...category, count }
    })
  }
}

/**
 * 分页工具
 */
export class Paginator {
  static paginate(items, currentPage, pageSize) {
    const totalPages = Math.ceil(items.length / pageSize)
    const startIndex = (currentPage - 1) * pageSize
    const endIndex = Math.min(startIndex + pageSize, items.length)
    const paginatedItems = items.slice(startIndex, endIndex)
    return {
      items: paginatedItems,
      totalPages,
      startIndex,
      endIndex,
      hasNext: currentPage < totalPages,
      hasPrev: currentPage > 1
    }
  }
}
