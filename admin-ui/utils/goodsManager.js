import { GOOD_STATUS, SORT_OPTIONS } from './constants';
import { BaseFilter, BaseSorter, validateArray } from './baseFilter';

/**
 * 商品筛选工具类
 */
export class GoodsFilter extends BaseFilter {
  static filterByStatus(goods, status) {
    goods = validateArray(goods);
    if (status === GOOD_STATUS.ALL) return goods;

    if (status === GOOD_STATUS.ACTIVE) {
      return goods.filter(item =>
        item.status === GOOD_STATUS.ACTIVE ||
        (item.status === GOOD_STATUS.OFF_SHELF && item.isActive)
      );
    }

    if (status === GOOD_STATUS.OFF_SHELF) {
      return goods.filter(item =>
        item.status === GOOD_STATUS.OFF_SHELF ||
        (item.status === GOOD_STATUS.ACTIVE && !item.isActive)
      );
    }

    return goods.filter(item => item.status === status);
  }

  static filterBySearch(goods, query) {
    return super.filterBySearch(goods, query, ['id', 'name', 'description', 'seller.name']);
  }
}

/**
 * 商品排序工具类
 */
export class GoodsSorter extends BaseSorter {
  static sort(goods, sortBy) {
    return super.sort(validateArray(goods), sortBy, {
      [SORT_OPTIONS.DEFAULT]: { field: 'createTime', order: 'desc' },
      [SORT_OPTIONS.TIME_DESC]: { field: 'createTime', order: 'desc' },
      [SORT_OPTIONS.VIEWS_DESC]: { field: 'views', order: 'desc' },
      [SORT_OPTIONS.FAVORITES_DESC]: { field: 'favorites', order: 'desc' },
      [SORT_OPTIONS.LIKES_DESC]: { field: 'likes', order: 'desc' }
    });
  }
}

/**
 * 商品统计工具类
 */
export class GoodsStats {
  /**
   * 计算各状态商品数量（优化版：单次遍历）
   * @param {Array} goods - 商品列表
   * @returns {Object} 统计结果
   */
  static calculate(goods) {
    if (!goods || !Array.isArray(goods)) {
      return {
        total: 0,
        pending: 0,
        active: 0,
        sold: 0,
        offShelf: 0,
        rejected: 0
      };
    }

    return goods.reduce((stats, item) => {
      stats.total++;
      switch (item.status) {
        case GOOD_STATUS.PENDING:
          stats.pending++;
          break;
        case GOOD_STATUS.ACTIVE:
          stats.active++;
          break;
        case GOOD_STATUS.SOLD:
          stats.sold++;
          break;
        case GOOD_STATUS.OFF_SHELF:
          stats.offShelf++;
          break;
        case GOOD_STATUS.REJECTED:
          stats.rejected++;
          break;
      }
      return stats;
    }, {
      total: 0,
      pending: 0,
      active: 0,
      sold: 0,
      offShelf: 0,
      rejected: 0
    });
  }
}

/**
 * 商品操作工具类
 */
export class GoodsOperations {
  /**
   * 生成交换凭证码
   * @param {Object} good - 商品对象
   * @returns {string} 凭证码
   */
  static generateExchangeCode(good) {
    // 格式: EX-学校简称-日期-随机码
    const schoolCode = good.school ? good.school.substring(0, 2) : 'XX';
    const date = new Date();
    const dateStr = `${date.getFullYear()}${String(date.getMonth() + 1).padStart(2, '0')}${String(date.getDate()).padStart(2, '0')}`;
    const randomCode = Math.random().toString(36).substring(2, 6).toUpperCase();
    return `EX-${schoolCode}-${dateStr}-${randomCode}`;
  }

  /**
   * 审核通过
   * @param {Object} good - 商品对象
   */
  static approve(good) {
    good.status = GOOD_STATUS.ACTIVE;
    good.isActive = true;
    // 生成交换凭证码
    good.exchangeCode = this.generateExchangeCode(good);
  }

  /**
   * 驳回商品
   * @param {Object} good - 商品对象
   * @param {string} reason - 驳回理由
   */
  static reject(good, reason) {
    good.status = GOOD_STATUS.REJECTED;
    good.rejectReason = reason;
  }

  /**
   * 切换上下架状态
   * @param {Object} good - 商品对象
   * @returns {string} 新状态
   */
  static toggleShelf(good) {
    if (good.isActive) {
      good.status = GOOD_STATUS.ACTIVE;
      return GOOD_STATUS.ACTIVE;
    } else {
      good.status = GOOD_STATUS.OFF_SHELF;
      return GOOD_STATUS.OFF_SHELF;
    }
  }

  /**
   * 删除商品
   * @param {Array} goods - 商品列表
   * @param {number} goodId - 商品ID
   * @returns {Array} 删除后的商品列表
   */
  static delete(goods, goodId) {
    return goods.filter(item => item.id !== goodId);
  }

  /**
   * 更新商品
   * @param {Array} goods - 商品列表
   * @param {Object} updatedGood - 更新后的商品对象
   * @returns {Array} 更新后的商品列表
   */
  static update(goods, updatedGood) {
    const index = goods.findIndex(g => g.id === updatedGood.id);
    if (index !== -1) {
      const newGoods = [...goods];
      newGoods[index] = { ...newGoods[index], ...updatedGood };
      return newGoods;
    }
    return goods;
  }
}
