/**
 * 违规举报系统常量定义
 * @author Kiro
 * @version 1.0
 */

// 举报对象类型
export const REPORT_TARGET_TYPES = {
  ALL: 'all',
  PRODUCT: 'product',
  USER: 'user'
}

// 举报对象类型标签
export const REPORT_TARGET_LABELS = {
  [REPORT_TARGET_TYPES.ALL]: '全部对象',
  [REPORT_TARGET_TYPES.PRODUCT]: '商品举报',
  [REPORT_TARGET_TYPES.USER]: '用户举报'
}

// 举报类型
export const REPORT_TYPES = {
  ALL: 'all',
  // 商品举报类别
  PRODUCT_FAKE: 'product-fake',
  PRODUCT_PROHIBITED: 'product-prohibited',
  PRODUCT_COPYRIGHT: 'product-copyright',
  PRODUCT_PRICE: 'product-price',
  PRODUCT_QUALITY: 'product-quality',
  PRODUCT_DESCRIPTION: 'product-description',
  // 用户举报类别
  USER_HARASSMENT: 'user-harassment',
  USER_FRAUD: 'user-fraud',
  USER_SPAM: 'user-spam',
  USER_IMPERSONATION: 'user-impersonation',
  USER_INAPPROPRIATE: 'user-inappropriate',
  USER_TRANSACTION: 'user-transaction'
}

// 举报类型标签
export const REPORT_TYPE_LABELS = {
  [REPORT_TYPES.ALL]: '全部类型',
  // 商品举报类别
  [REPORT_TYPES.PRODUCT_FAKE]: '商品-虚假信息',
  [REPORT_TYPES.PRODUCT_PROHIBITED]: '商品-违禁物品',
  [REPORT_TYPES.PRODUCT_COPYRIGHT]: '商品-侵权盗版',
  [REPORT_TYPES.PRODUCT_PRICE]: '商品-价格欺诈',
  [REPORT_TYPES.PRODUCT_QUALITY]: '商品-质量问题',
  [REPORT_TYPES.PRODUCT_DESCRIPTION]: '商品-描述不符',
  // 用户举报类别
  [REPORT_TYPES.USER_HARASSMENT]: '用户-骚扰辱骂',
  [REPORT_TYPES.USER_FRAUD]: '用户-诈骗行为',
  [REPORT_TYPES.USER_SPAM]: '用户-垃圾信息',
  [REPORT_TYPES.USER_IMPERSONATION]: '用户-冒充他人',
  [REPORT_TYPES.USER_INAPPROPRIATE]: '用户-不当行为',
  [REPORT_TYPES.USER_TRANSACTION]: '用户-交易纠纷'
}

// 举报状态
export const REPORT_STATUS = {
  ALL: 'all',
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected',
  PROCESSING: 'processing'
}

// 举报状态标签
export const REPORT_STATUS_LABELS = {
  [REPORT_STATUS.ALL]: '全部状态',
  [REPORT_STATUS.PENDING]: '待处理',
  [REPORT_STATUS.APPROVED]: '已处理',
  [REPORT_STATUS.REJECTED]: '已驳回',
  [REPORT_STATUS.PROCESSING]: '处理中'
}

// 处理行动类型
export const ACTION_TYPES = {
  WARNING: 'warning',
  TEMP_BAN: 'tempBan',
  PERM_BAN: 'permBan',
  REMOVE_PRODUCT: 'removeProduct'
}

// 处理行动标签
export const ACTION_LABELS = {
  [ACTION_TYPES.WARNING]: '警告处理',
  [ACTION_TYPES.TEMP_BAN]: '临时封禁',
  [ACTION_TYPES.PERM_BAN]: '永久封禁',
  [ACTION_TYPES.REMOVE_PRODUCT]: '下架商品'
}

// 处理行动执行标签
export const ACTION_EXECUTION_LABELS = {
  [ACTION_TYPES.WARNING]: '警告并扣除信誉值',
  [ACTION_TYPES.TEMP_BAN]: '短时间封禁',
  [ACTION_TYPES.PERM_BAN]: '永久封禁',
  [ACTION_TYPES.REMOVE_PRODUCT]: '下架商品'
}

// 信誉分数配置
export const CREDIT_SCORE_CONFIG = {
  MIN: 1,
  MAX: 100,
  DEFAULT: 10,
  LIGHT_VIOLATION_THRESHOLD: 5,
  SEVERE_VIOLATION_THRESHOLD: 50
}

// 封禁时长选项
export const BAN_DURATION_OPTIONS = ['1', '3', '7', '15', '30']

// 分页配置
export const PAGINATION_CONFIG = {
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZE_OPTIONS: [10, 20, 50, 100]
}

// 图标映射
export const TYPE_ICONS = {
  // 商品举报图标
  [REPORT_TYPES.PRODUCT_FAKE]: 'el-icon-warning-outline',
  [REPORT_TYPES.PRODUCT_PROHIBITED]: 'el-icon-remove-outline',
  [REPORT_TYPES.PRODUCT_COPYRIGHT]: 'el-icon-document-copy',
  [REPORT_TYPES.PRODUCT_PRICE]: 'el-icon-price-tag',
  [REPORT_TYPES.PRODUCT_QUALITY]: 'el-icon-star-off',
  [REPORT_TYPES.PRODUCT_DESCRIPTION]: 'el-icon-edit-outline',
  // 用户举报图标
  [REPORT_TYPES.USER_HARASSMENT]: 'el-icon-user-solid',
  [REPORT_TYPES.USER_FRAUD]: 'el-icon-warning',
  [REPORT_TYPES.USER_SPAM]: 'el-icon-message',
  [REPORT_TYPES.USER_IMPERSONATION]: 'el-icon-user',
  [REPORT_TYPES.USER_INAPPROPRIATE]: 'el-icon-close',
  [REPORT_TYPES.USER_TRANSACTION]: 'el-icon-goods'
}

// 处理行动图标
export const ACTION_ICONS = {
  [ACTION_TYPES.WARNING]: 'el-icon-warning-outline',
  [ACTION_TYPES.TEMP_BAN]: 'el-icon-time',
  [ACTION_TYPES.PERM_BAN]: 'el-icon-circle-close',
  [ACTION_TYPES.REMOVE_PRODUCT]: 'el-icon-delete'
}