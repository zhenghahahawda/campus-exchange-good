/**
 * 违规举报系统样式助手
 * @author Kiro
 * @version 1.0
 */

import { REPORT_TYPES, ACTION_TYPES, TYPE_ICONS, ACTION_ICONS } from './illegalConstants'

/**
 * 样式类映射助手
 */
export class StyleHelper {
  /**
   * 获取举报类型样式类
   * @param {string} type - 举报类型
   * @returns {string} 样式类名
   */
  static getTypeBadgeClass(type) {
    const classMap = {
      // 商品举报样式
      [REPORT_TYPES.PRODUCT_FAKE]: 'type-product-fake',
      [REPORT_TYPES.PRODUCT_PROHIBITED]: 'type-product-prohibited',
      [REPORT_TYPES.PRODUCT_COPYRIGHT]: 'type-product-copyright',
      [REPORT_TYPES.PRODUCT_PRICE]: 'type-product-price',
      [REPORT_TYPES.PRODUCT_QUALITY]: 'type-product-quality',
      [REPORT_TYPES.PRODUCT_DESCRIPTION]: 'type-product-description',
      // 用户举报样式
      [REPORT_TYPES.USER_HARASSMENT]: 'type-user-harassment',
      [REPORT_TYPES.USER_FRAUD]: 'type-user-fraud',
      [REPORT_TYPES.USER_SPAM]: 'type-user-spam',
      [REPORT_TYPES.USER_IMPERSONATION]: 'type-user-impersonation',
      [REPORT_TYPES.USER_INAPPROPRIATE]: 'type-user-inappropriate',
      [REPORT_TYPES.USER_TRANSACTION]: 'type-user-transaction'
    }
    return classMap[type] || 'type-other'
  }

  /**
   * 获取举报状态样式类
   * @param {string} status - 举报状态
   * @returns {string} 样式类名
   */
  static getStatusClass(status) {
    const classMap = {
      pending: 'status-pending',
      approved: 'status-approved',
      rejected: 'status-rejected',
      processing: 'status-processing'
    }
    return classMap[status] || 'status-pending'
  }

  /**
   * 获取举报类型图标
   * @param {string} type - 举报类型
   * @returns {string} 图标类名
   */
  static getTypeIcon(type) {
    return TYPE_ICONS[type] || 'el-icon-more'
  }

  /**
   * 获取处理行动图标
   * @param {string} action - 行动类型
   * @returns {string} 图标类名
   */
  static getActionIcon(action) {
    return ACTION_ICONS[action] || 'el-icon-s-tools'
  }

  /**
   * 获取处理行动样式类
   * @param {string} action - 行动类型
   * @returns {string} 样式类名
   */
  static getActionBadgeClass(action) {
    const classMap = {
      [ACTION_TYPES.WARNING]: 'action-warning',
      [ACTION_TYPES.TEMP_BAN]: 'action-temp-ban',
      [ACTION_TYPES.PERM_BAN]: 'action-perm-ban',
      [ACTION_TYPES.REMOVE_PRODUCT]: 'action-remove-product'
    }
    return classMap[action] || 'action-default'
  }
}