// 商品管理工具类 - 校园换物平台

/**
 * 商品筛选工具类
 */
export class GoodsFilter {
  /**
   * 按搜索关键词筛选
   * @param {Array} goods - 商品列表
   * @param {string} query - 搜索关键词
   * @returns {Array} 筛选后的列表
   */
  static filterBySearch(goods, query) {
    if (!query || !query.trim()) return goods;
    
    const lowerQuery = query.toLowerCase();
    return goods.filter(item => 
      item.good_name.toLowerCase().includes(lowerQuery) ||
      item.description.toLowerCase().includes(lowerQuery)
    );
  }

  /**
   * 按分类筛选
   * @param {Array} goods - 商品列表
   * @param {number} category_id - 分类ID
   * @returns {Array} 筛选后的列表
   */
  static filterByCategory(goods, category_id) {
    if (!category_id) return goods;
    return goods.filter(item => item.category_id === category_id);
  }

  /**
   * 按上架状态筛选（默认只显示已上架商品）
   * @param {Array} goods - 商品列表
   * @param {number} shelf_status - 上架状态（0=已下架，1=已上架）
   * @returns {Array} 筛选后的列表
   */
  static filterByShelfStatus(goods, shelf_status = 1) {
    return goods.filter(item => item.shelf_status === shelf_status);
  }

  /**
   * 综合筛选
   * @param {Array} goods - 商品列表
   * @param {Object} filters - 筛选条件对象
   * @returns {Array} 筛选后的列表
   */
  static filter(goods, { query, category, showOffShelf = false }) {
    let result = this.filterBySearch(goods, query);
    result = this.filterByCategory(result, category);
    // 默认只显示已上架商品
    if (!showOffShelf) {
      result = this.filterByShelfStatus(result, 1);
    }
    return result;
  }
}

/**
 * 商品排序工具类
 */
export class GoodsSorter {
  /**
   * 排序商品列表
   * @param {Array} goods - 商品列表
   * @param {string} sortBy - 排序方式
   * @returns {Array} 排序后的列表
   */
  static sort(goods, sortBy) {
    const result = [...goods];
    
    switch (sortBy) {
      case 'newest':
        // 按创建时间降序
        return result.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));
      case 'popular':
        // 按浏览量降序
        return result.sort((a, b) => b.view_count - a.view_count);
      case 'distance':
      default:
        // 默认按创建时间降序
        return result.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));
    }
  }
}
