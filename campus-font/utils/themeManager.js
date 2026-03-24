// 主题管理工具类 - 校园换物平台
export class ThemeManager {
  /**
   * 应用主题
   * @param {Object} theme - 主题对象
   */
  static applyTheme(theme) {
    const root = document.documentElement;
    const themeId = typeof theme === 'string' ? theme : theme.id;

    if (themeId === 'default') {
      root.removeAttribute('data-theme');
    } else {
      root.setAttribute('data-theme', themeId);
    }

    // 应用 CSS 变量
    if (theme.cssVariables) {
      Object.entries(theme.cssVariables).forEach(([key, value]) => {
        root.style.setProperty(key, value);
      });
    }

    // 应用壁纸并持久化
    if (theme.wallpaper) {
      this.applyWallpaper(theme.wallpaper, theme.wallpaperType);
      localStorage.setItem('selected-theme-wallpaper', theme.wallpaper);
      localStorage.setItem('selected-theme-wallpaper-type', theme.wallpaperType || '');
    } else {
      this.applyWallpaper(null, null);
      localStorage.removeItem('selected-theme-wallpaper');
      localStorage.removeItem('selected-theme-wallpaper-type');
    }

    // 保存到 localStorage
    localStorage.setItem('selected-theme', themeId);
  }

  /**
   * 应用壁纸
   * @param {string} url - 壁纸URL
   * @param {string} type - 壁纸类型 (image/video)
   */
  static applyWallpaper(url, type) {
    const event = new CustomEvent('theme-wallpaper-change', {
      detail: { url, type }
    });
    window.dispatchEvent(event);
  }

  /**
   * 获取当前主题
   * @returns {string} 当前主题ID
   */
  static getCurrentTheme() {
    const root = document.documentElement;
    return root.getAttribute('data-theme') || 'default';
  }

  /**
   * 从 localStorage 加载主题
   * @returns {string} 保存的主题ID
   */
  static loadSavedTheme() {
    return localStorage.getItem('selected-theme') || 'default';
  }

  /**
   * 主题切换动画（使用 View Transitions API）
   * @param {Object|string} theme - 目标主题对象或ID
   * @param {Object} clickEvent - 点击事件对象
   * @param {Function} callback - 切换完成回调
   * @returns {Promise} 切换完成的Promise
   */
  static transitionTheme(theme, clickEvent, callback) {
    const root = document.documentElement;

    // 不支持 View Transitions API 时直接应用
    if (!document.startViewTransition) {
      this.applyTheme(theme);
      if (callback) callback();
      return Promise.resolve();
    }

    // 获取点击位置
    const rect = clickEvent.currentTarget.getBoundingClientRect();
    const x = rect.left + rect.width / 2;
    const y = rect.top + rect.height / 2;

    // 计算扩散半径
    const endRadius = Math.hypot(
      Math.max(x, window.innerWidth - x),
      Math.max(y, window.innerHeight - y)
    );

    // 开始过渡
    const transition = document.startViewTransition(() => {
      this.applyTheme(theme);
    });

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
      );
    });

    return transition.finished.then(() => {
      if (callback) callback();
    });
  }
}

/**
 * 主题筛选工具
 */
export class ThemeFilter {
  /**
   * 按分类筛选主题
   * @param {Array} themes - 主题列表
   * @param {string} categoryId - 分类ID
   * @returns {Array} 筛选后的主题列表
   */
  static filterByCategory(themes, categoryId) {
    if (categoryId === 'all') {
      return themes;
    }
    return themes.filter(theme => 
      theme.category && theme.category.includes(categoryId)
    );
  }

  /**
   * 按搜索关键词筛选主题
   * @param {Array} themes - 主题列表
   * @param {string} query - 搜索关键词
   * @returns {Array} 筛选后的主题列表
   */
  static filterBySearch(themes, query) {
    if (!query.trim()) {
      return themes;
    }
    
    const lowerQuery = query.toLowerCase();
    return themes.filter(theme => 
      theme.name.toLowerCase().includes(lowerQuery) ||
      theme.description.toLowerCase().includes(lowerQuery) ||
      theme.tags.some(tag => tag.toLowerCase().includes(lowerQuery))
    );
  }

  /**
   * 综合筛选（分类 + 搜索）
   * @param {Array} themes - 主题列表
   * @param {string} categoryId - 分类ID
   * @param {string} searchQuery - 搜索关键词
   * @returns {Array} 筛选后的主题列表
   */
  static filter(themes, categoryId, searchQuery) {
    let filtered = this.filterByCategory(themes, categoryId);
    filtered = this.filterBySearch(filtered, searchQuery);
    return filtered;
  }

  /**
   * 更新分类计数
   * @param {Array} categories - 分类列表
   * @param {Array} themes - 主题列表
   * @returns {Array} 更新后的分类列表
   */
  static updateCategoryCounts(categories, themes) {
    return categories.map(category => {
      if (category.id === 'all') {
        return { ...category, count: themes.length };
      }
      
      const count = themes.filter(theme => 
        theme.category && theme.category.includes(category.id)
      ).length;
      
      return { ...category, count };
    });
  }
}

/**
 * 分页工具
 */
export class Paginator {
  /**
   * 获取分页数据
   * @param {Array} items - 数据列表
   * @param {number} currentPage - 当前页码
   * @param {number} pageSize - 每页数量
   * @returns {Object} 分页信息
   */
  static paginate(items, currentPage, pageSize) {
    const totalPages = Math.ceil(items.length / pageSize);
    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = Math.min(startIndex + pageSize, items.length);
    const paginatedItems = items.slice(startIndex, endIndex);
    
    return {
      items: paginatedItems,
      totalPages,
      startIndex,
      endIndex,
      hasNext: currentPage < totalPages,
      hasPrev: currentPage > 1
    };
  }
}