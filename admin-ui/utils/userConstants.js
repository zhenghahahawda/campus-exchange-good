// 用户状态常量
export const USER_STATUS = {
  ALL: 'all',
  ACTIVE: 'active',
  INACTIVE: 'inactive',
  NORMAL: 'normal',
  ACTIVE_USER: 'active_user'
};

// 用户状态标签映射
export const USER_STATUS_LABELS = {
  [USER_STATUS.ACTIVE]: '正常',
  [USER_STATUS.INACTIVE]: '封禁'
};

// 用户角色常量
export const USER_ROLES = {
  ADMIN: 'admin',
  USER: 'user'
};

// 用户角色标签映射
export const USER_ROLE_LABELS = {
  [USER_ROLES.ADMIN]: '管理员',
  [USER_ROLES.USER]: '普通用户'
};

// 用户类型常量
export const USER_TYPES = {
  NORMAL: 'normal',
  ACTIVE: 'active'
};

// 用户类型标签映射
export const USER_TYPE_LABELS = {
  [USER_TYPES.NORMAL]: '普通用户',
  [USER_TYPES.ACTIVE]: '活跃用户'
};

// 排序选项
export const USER_SORT_OPTIONS = {
  DEFAULT: 'default',
  ONLINE_FIRST: 'online_first',
  REPUTATION_DESC: 'reputation_desc',
  GOODS_DESC: 'goods_desc',
  ORDERS_DESC: 'orders_desc'
};

// 排序标签映射
export const USER_SORT_LABELS = {
  [USER_SORT_OPTIONS.DEFAULT]: '默认排序',
  [USER_SORT_OPTIONS.ONLINE_FIRST]: '在线优先',
  [USER_SORT_OPTIONS.REPUTATION_DESC]: '信誉分: 高到低',
  [USER_SORT_OPTIONS.GOODS_DESC]: '商品数量: 高到低',
  [USER_SORT_OPTIONS.ORDERS_DESC]: '交易订单: 高到低'
};

// 活跃用户登录次数阈值
export const ACTIVE_USER_LOGIN_THRESHOLD = 50;

// 分页配置
export const PAGINATION_CONFIG = {
  PAGE_SIZE: 8,
  DEFAULT_PAGE: 1
};

// 表单验证规则
export const VALIDATION_RULES = {
  EMAIL_REGEX: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PHONE_REGEX: /^1[3-9]\d{9}$/,
  MAX_NAME_LENGTH: 30,
  MAX_USER_ID_LENGTH: 20
};

// 默认头像
export const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png';

// 搜索字段配置
export const SEARCH_FIELDS = [
  'name',
  'email', 
  'userId',
  'accountAddress',
  'school',
  'phone'
];