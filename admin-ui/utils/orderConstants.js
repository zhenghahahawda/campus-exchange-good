// 订单状态常量 V3.0
export const ORDER_STATUS = {
  ALL: 'all',              // 全部（筛选用）
  PENDING: 'pending',      // 待交易：双方确认交换意愿后，等待双方交易
  COMPLETED: 'completed',  // 已完成：双方交易完成，上传了交易图，都确认完成
  TO_PROCESS: 'to_process', // 待处理：有争议，等待管理员处理
  PROCESSING: 'processing', // 处理中：管理员已填写处理方案，正在处理
  CANCELLED: 'cancelled'   // 已取消：取消的订单
};

// 订单状态标签映射
export const ORDER_STATUS_LABELS = {
  [ORDER_STATUS.ALL]: '全部',
  [ORDER_STATUS.PENDING]: '待交易',
  [ORDER_STATUS.COMPLETED]: '已完成',
  [ORDER_STATUS.TO_PROCESS]: '待处理',
  [ORDER_STATUS.PROCESSING]: '处理中',
  [ORDER_STATUS.CANCELLED]: '已取消'
};

// 订单状态颜色映射
export const ORDER_STATUS_COLORS = {
  [ORDER_STATUS.PENDING]: 'warning',    // 橙色
  [ORDER_STATUS.COMPLETED]: 'success',  // 绿色
  [ORDER_STATUS.TO_PROCESS]: 'info',    // 蓝色
  [ORDER_STATUS.PROCESSING]: 'primary', // 主色
  [ORDER_STATUS.CANCELLED]: 'info'      // 灰色
};

// 订单排序选项
export const ORDER_SORT_OPTIONS = {
  DEFAULT: 'default',
  TIME_DESC: 'time_desc',
  TIME_ASC: 'time_asc'
};

// 订单排序标签映射
export const ORDER_SORT_LABELS = {
  [ORDER_SORT_OPTIONS.DEFAULT]: '默认排序',
  [ORDER_SORT_OPTIONS.TIME_DESC]: '最新订单',
  [ORDER_SORT_OPTIONS.TIME_ASC]: '最早订单'
};

// 支付方式
export const PAYMENT_METHODS = {
  WECHAT: 'wechat',
  ALIPAY: 'alipay',
  BALANCE: 'balance',
  CREDIT_CARD: 'credit_card'
};

// 支付方式标签
export const PAYMENT_METHOD_LABELS = {
  [PAYMENT_METHODS.WECHAT]: '微信支付',
  [PAYMENT_METHODS.ALIPAY]: '支付宝',
  [PAYMENT_METHODS.BALANCE]: '余额支付',
  [PAYMENT_METHODS.CREDIT_CARD]: '信用卡'
};

// 配送方式
export const SHIPPING_METHODS = {
  EXPRESS: 'express',
  PICKUP: 'pickup',
  SAME_CITY: 'same_city'
};

// 配送方式标签
export const SHIPPING_METHOD_LABELS = {
  [SHIPPING_METHODS.EXPRESS]: '快递配送',
  [SHIPPING_METHODS.PICKUP]: '到店自提',
  [SHIPPING_METHODS.SAME_CITY]: '同城配送'
};

// 退换货原因（保留用于历史数据兼容）
export const RETURN_EXCHANGE_REASONS = [
  '收到的物品与描述不符',
  '物品存在质量问题',
  '物品成色不符合预期',
  '物品功能异常',
  '收到错误物品',
  '物品有损坏',
  '其他原因'
];

// 订单类型
export const ORDER_TYPES = {
  EXCHANGE: 'exchange'      // 换物订单
};

// 订单类型标签
export const ORDER_TYPE_LABELS = {
  [ORDER_TYPES.EXCHANGE]: '换物订单'
};

// 取消原因选项
export const CANCEL_REASONS = [
  '双方协商不成',
  '找到更合适的交换对象',
  '物品已不需要交换',
  '物品描述不符',
  '其他原因'
];

// 争议原因选项
export const DISPUTE_REASONS = [
  '物品与描述不符',
  '物品存在质量问题',
  '物品成色不符合预期',
  '对方未按约定交易',
  '其他争议'
];

// 实物图上传状态
export const PROOF_STATUS = {
  NOT_UPLOADED: 'not_uploaded',  // 未上传
  UPLOADED: 'uploaded'           // 已上传
};

// 实物图上传状态标签
export const PROOF_STATUS_LABELS = {
  [PROOF_STATUS.NOT_UPLOADED]: '未上传',
  [PROOF_STATUS.UPLOADED]: '已上传'
};
