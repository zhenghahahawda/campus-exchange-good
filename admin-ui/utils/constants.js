// 商品状态常量
export const GOOD_STATUS = {
  ALL: 'all',
  PENDING: 'pending',
  ACTIVE: 'active',
  SOLD: 'sold',
  OFF_SHELF: 'off_shelf',
  REJECTED: 'rejected'
};

// 商品状态标签映射
export const GOOD_STATUS_LABELS = {
  [GOOD_STATUS.ALL]: '全部',
  [GOOD_STATUS.PENDING]: '待审核',
  [GOOD_STATUS.ACTIVE]: '出售中',
  [GOOD_STATUS.SOLD]: '已售出',
  [GOOD_STATUS.OFF_SHELF]: '已下架',
  [GOOD_STATUS.REJECTED]: '已驳回'
};

// 排序选项
export const SORT_OPTIONS = {
  DEFAULT: 'default',
  TIME_DESC: 'time_desc',
  VIEWS_DESC: 'views_desc',
  FAVORITES_DESC: 'favorites_desc',
  LIKES_DESC: 'likes_desc'
};

// 排序标签映射
export const SORT_LABELS = {
  [SORT_OPTIONS.DEFAULT]: '默认排序',
  [SORT_OPTIONS.TIME_DESC]: '最新发布',
  [SORT_OPTIONS.VIEWS_DESC]: '浏览量最多',
  [SORT_OPTIONS.FAVORITES_DESC]: '收藏量最多',
  [SORT_OPTIONS.LIKES_DESC]: '喜欢量最多'
};

// 配送方式
export const DELIVERY_TYPES = {
  EXPRESS: 'express',
  PICKUP: 'pickup'
};

// 配送方式标签
export const DELIVERY_TYPE_LABELS = {
  [DELIVERY_TYPES.EXPRESS]: '快递配送',
  [DELIVERY_TYPES.PICKUP]: '同城自提'
};

// 商品分类
export const GOOD_CATEGORIES = [
  '数码产品',
  '家居生活',
  '服饰鞋包',
  '图书音像',
  '运动户外',
  '美妆个护',
  '其他'
];

// 分类ID映射 (假设)
export const CATEGORY_MAP = {
  '数码产品': 1,
  '家居生活': 2,
  '服饰鞋包': 3,
  '图书音像': 4,
  '运动户外': 5,
  '美妆个护': 6,
  '其他': 7
};

// 商品成色选项
export const CONDITION_OPTIONS = [
  { value: 'brand_new', label: '全新', description: '未使用过，完好如新' },
  { value: 'like_new', label: '九成新', description: '使用次数少，几乎无磨损' },
  { value: 'good', label: '八成新', description: '正常使用痕迹，功能完好' },
  { value: 'fair', label: '七成及以下', description: '使用痕迹明显，但可正常使用' }
];

// 成色等级映射
export const CONDITION_LEVEL_MAP = {
  'brand_new': 1,
  'like_new': 2,
  'good': 3,
  'fair': 4
};

// 消息类型
export const MESSAGE_TYPES = {
  SUCCESS: 'success',
  WARNING: 'warning',
  ERROR: 'danger',
  INFO: 'info'
};
