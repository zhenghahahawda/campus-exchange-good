import { ORDER_STATUS, ORDER_SORT_OPTIONS, PROOF_STATUS } from './orderConstants';
import { BaseFilter, BaseSorter, validateArray } from './baseFilter';

/**
 * 订单筛选工具类 V2.0
 */
export class OrderFilter extends BaseFilter {
  static filterByStatus(orders, status) {
    return super.filterByStatus(orders, status, ORDER_STATUS.ALL);
  }

  static filterBySearch(orders, query) {
    return super.filterBySearch(orders, query, [
      'orderNo', 'buyer.name', 'buyer.phone',
      'seller.name', 'seller.phone',
      'buyerItem.name', 'sellerItem.name'
    ]);
  }
}

export class OrderSorter extends BaseSorter {
  static sort(orders, sortBy) {
    return super.sort(validateArray(orders), sortBy, {
      [ORDER_SORT_OPTIONS.DEFAULT]: { field: 'createTime', order: 'desc' },
      [ORDER_SORT_OPTIONS.TIME_DESC]: { field: 'createTime', order: 'desc' },
      [ORDER_SORT_OPTIONS.TIME_ASC]: { field: 'createTime', order: 'asc' }
    });
  }
}

/**
 * 订单统计工具类 V3.0
 */
export class OrderStats {
  /**
   * 计算订单统计数据（优化版：单次遍历）
   * @param {Array} orders - 订单列表
   * @returns {Object} 统计数据
   */
  static calculate(orders) {
    if (!orders || !Array.isArray(orders)) {
      return {
        pending: 0,
        completed: 0,
        toProcess: 0,
        cancelled: 0
      };
    }

    return orders.reduce((stats, order) => {
      switch (order.status) {
        case ORDER_STATUS.PENDING:
          stats.pending++;
          break;
        case ORDER_STATUS.COMPLETED:
          stats.completed++;
          break;
        case ORDER_STATUS.TO_PROCESS:
          stats.toProcess++;
          break;
        case ORDER_STATUS.PROCESSING:
          stats.processing++;
          break;
        case ORDER_STATUS.CANCELLED:
          stats.cancelled++;
          break;
      }
      return stats;
    }, {
      pending: 0,
      completed: 0,
      toProcess: 0,
      processing: 0,
      cancelled: 0
    });
  }
}

/**
 * 订单操作工具类 V3.0
 */
export class OrderOperations {
  /**
   * 上传实物交换图
   * @param {Object} order - 订单对象
   * @param {string} side - 哪一方 ('buyer' 或 'seller')
   * @param {Array} images - 图片数组
   */
  static uploadProof(order, side, images) {
    if (!order.exchangeProofs) return;
    
    if (side === 'buyer') {
      order.exchangeProofs.buyer.uploaded = true;
      order.exchangeProofs.buyer.images = images;
      order.exchangeProofs.buyer.uploadTime = new Date().getTime();
    } else if (side === 'seller') {
      order.exchangeProofs.seller.uploaded = true;
      order.exchangeProofs.seller.images = images;
      order.exchangeProofs.seller.uploadTime = new Date().getTime();
    }
    
    // 如果双方都已上传，自动完成订单
    if (order.exchangeProofs.buyer.uploaded && order.exchangeProofs.seller.uploaded) {
      order.status = ORDER_STATUS.COMPLETED;
      order.completeTime = new Date().getTime();
    }
  }

  /**
   * 取消订单（从待交易状态）
   * @param {Object} order - 订单对象
   * @param {string} reason - 取消原因
   * @returns {boolean} 是否成功
   */
  static cancelFromPending(order, reason) {
    if (order.status !== ORDER_STATUS.PENDING) {
      return false;
    }
    
    order.status = ORDER_STATUS.CANCELLED;
    order.cancelReason = reason;
    order.cancelTime = new Date().getTime();
    return true;
  }

  /**
   * 开始处理待处理订单 - 填写处理方案，转为处理中
   * @param {Object} order - 订单对象
   * @param {string} adminNote - 处理方案
   * @returns {boolean} 是否成功
   */
  static startProcessing(order, adminNote) {
    if (order.status !== ORDER_STATUS.TO_PROCESS) {
      return false;
    }

    order.status = ORDER_STATUS.PROCESSING;
    order.adminNote = adminNote;
    order.processTime = new Date().getTime();
    return true;
  }

  /**
   * 完成处理 - 从处理中转为已完成
   * @param {Object} order - 订单对象
   * @returns {boolean} 是否成功
   */
  static completeProcessing(order) {
    if (order.status !== ORDER_STATUS.PROCESSING) {
      return false;
    }

    order.status = ORDER_STATUS.COMPLETED;
    order.completeTime = new Date().getTime();
    return true;
  }

  /**
   * 处理待处理订单 - 标记为已取消
   * @param {Object} order - 订单对象
   * @param {string} adminNote - 管理员备注
   * @returns {boolean} 是否成功
   */
  static markAsCancelled(order, adminNote) {
    if (order.status !== ORDER_STATUS.TO_PROCESS) {
      return false;
    }
    
    order.status = ORDER_STATUS.CANCELLED;
    order.cancelTime = new Date().getTime();
    order.adminNote = adminNote;
    return true;
  }

  /**
   * 将已完成订单改为待处理
   * @param {Object} order - 订单对象
   * @returns {boolean} 是否成功
   */
  static moveToProcess(order) {
    if (order.status !== ORDER_STATUS.COMPLETED) {
      return false;
    }
    
    order.status = ORDER_STATUS.TO_PROCESS;
    order.processTime = new Date().getTime();
    return true;
  }

  /**
   * 删除订单
   * @param {Array} orders - 订单列表
   * @param {string} orderId - 订单ID
   * @returns {Array} 更新后的订单列表
   */
  static delete(orders, orderId) {
    if (!orders || !Array.isArray(orders)) {
      return [];
    }
    return orders.filter(order => order.id !== orderId);
  }
}
