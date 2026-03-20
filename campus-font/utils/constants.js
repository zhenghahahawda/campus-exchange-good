// 常量定义 - 校园换物平台

/**
 * 商品审核状态 (status)
 */
export const GOOD_AUDIT_STATUS = {
  PENDING: 0,    // 待审核
  APPROVED: 1,   // 已通过
  REJECTED: 2    // 已拒绝
};

/**
 * 商品上架状态 (shelf_status)
 */
export const GOOD_SHELF_STATUS = {
  OFF_SHELF: 0,  // 已下架
  ON_SHELF: 1    // 已上架
};

/**
 * 商品审核状态文本映射
 */
export const GOOD_AUDIT_STATUS_TEXT = {
  [GOOD_AUDIT_STATUS.PENDING]: '待审核',
  [GOOD_AUDIT_STATUS.APPROVED]: '已通过',
  [GOOD_AUDIT_STATUS.REJECTED]: '已拒绝'
};

/**
 * 商品上架状态文本映射
 */
export const GOOD_SHELF_STATUS_TEXT = {
  [GOOD_SHELF_STATUS.OFF_SHELF]: '已下架',
  [GOOD_SHELF_STATUS.ON_SHELF]: '已上架'
};

/**
 * 商品显示状态文本映射 (用于前端展示)
 */
export const GOOD_STATUS_TEXT = {
  'available': '可交换',
  'exchanging': '交换中',
  'exchanged': '已交换',
  'off_shelf': '已下架'
};

/**
 * 商品新旧程度 (condition_level)
 */
export const CONDITION_LEVEL = {
  BRAND_NEW: 1,        // 全新
  ALMOST_NEW: 2,       // 九成新
  GOOD: 3,             // 八成新
  FAIR: 4              // 七成新
};

/**
 * 商品新旧程度文本映射
 */
export const CONDITION_LEVEL_TEXT = {
  [CONDITION_LEVEL.BRAND_NEW]: '全新',
  [CONDITION_LEVEL.ALMOST_NEW]: '九成新',
  [CONDITION_LEVEL.GOOD]: '八成新',
  [CONDITION_LEVEL.FAIR]: '七成新及以下'
};

/**
 * 商品分类 (对应 goods_categories 表)
 */
export const GOOD_CATEGORIES = [
  { label: '全部分类', value: '', code: '', icon: 'el-icon-menu', color: '#303133' },
  // 数码产品类
  { label: '数码产品', value: 1, code: 'digital', icon: 'el-icon-mobile-phone', color: '#409EFF' },
  { label: '手机通讯', value: 2, code: 'phone', icon: 'el-icon-phone', color: '#409EFF' },
  { label: '电脑办公', value: 3, code: 'computer', icon: 'el-icon-monitor', color: '#409EFF' },
  { label: '摄影摄像', value: 4, code: 'camera', icon: 'el-icon-camera', color: '#409EFF' },
  { label: '智能设备', value: 5, code: 'smart', icon: 'el-icon-watch', color: '#409EFF' },
  // 图书教育类
  { label: '图书文具', value: 6, code: 'books', icon: 'el-icon-reading', color: '#67C23A' },
  { label: '教材教辅', value: 7, code: 'textbook', icon: 'el-icon-notebook-2', color: '#67C23A' },
  { label: '考研资料', value: 8, code: 'exam', icon: 'el-icon-document', color: '#67C23A' },
  { label: '文具用品', value: 9, code: 'stationery', icon: 'el-icon-edit', color: '#67C23A' },
  { label: '乐器音像', value: 10, code: 'music', icon: 'el-icon-headset', color: '#67C23A' },
  // 生活用品类
  { label: '家居生活', value: 11, code: 'home', icon: 'el-icon-house', color: '#F56C6C' },
  { label: '家具家电', value: 12, code: 'furniture', icon: 'el-icon-refrigerator', color: '#F56C6C' },
  { label: '厨房用品', value: 13, code: 'kitchen', icon: 'el-icon-fork-spoon', color: '#F56C6C' },
  { label: '床上用品', value: 14, code: 'bedding', icon: 'el-icon-moon', color: '#F56C6C' },
  { label: '日用百货', value: 15, code: 'daily', icon: 'el-icon-shopping-bag-1', color: '#F56C6C' },
  // 服饰配饰类
  { label: '服饰鞋包', value: 16, code: 'fashion', icon: 'el-icon-umbrella', color: '#909399' },
  { label: '男装', value: 17, code: 'menswear', icon: 'el-icon-user', color: '#909399' },
  { label: '女装', value: 18, code: 'womenswear', icon: 'el-icon-female', color: '#909399' },
  { label: '鞋靴', value: 19, code: 'shoes', icon: 'el-icon-guide', color: '#909399' },
  { label: '箱包配饰', value: 20, code: 'bags', icon: 'el-icon-suitcase', color: '#909399' },
  // 运动户外类
  { label: '运动户外', value: 21, code: 'sports', icon: 'el-icon-bicycle', color: '#E6A23C' },
  { label: '运动器材', value: 22, code: 'equipment', icon: 'el-icon-basketball', color: '#E6A23C' },
  { label: '自行车', value: 23, code: 'bike', icon: 'el-icon-bicycle', color: '#E6A23C' },
  { label: '户外装备', value: 24, code: 'outdoor', icon: 'el-icon-place', color: '#E6A23C' },
  { label: '运动服饰', value: 25, code: 'sportswear', icon: 'el-icon-trophy', color: '#E6A23C' },
  // 美妆个护类
  { label: '美妆个护', value: 26, code: 'beauty', icon: 'el-icon-magic-stick', color: '#FF69B4' },
  { label: '护肤品', value: 27, code: 'skincare', icon: 'el-icon-star-on', color: '#FF69B4' },
  { label: '彩妆', value: 28, code: 'makeup', icon: 'el-icon-brush', color: '#FF69B4' },
  { label: '个人护理', value: 29, code: 'personal', icon: 'el-icon-present', color: '#FF69B4' },
  // 其他类别
  { label: '玩具手办', value: 30, code: 'toys', icon: 'el-icon-present', color: '#606266' },
  { label: '宠物用品', value: 31, code: 'pet', icon: 'el-icon-cherry', color: '#606266' },
  { label: '其他闲置', value: 32, code: 'other', icon: 'el-icon-more', color: '#606266' }
];

/**
 * 排序选项
 */
export const SORT_OPTIONS = [
  { label: '最新发布', value: 'newest' },
  { label: '浏览热度', value: 'popular' },
  { label: '距离最近', value: 'distance' }
];

/**
 * 消息类型
 */
export const MESSAGE_TYPES = {
  SUCCESS: 'success',
  WARNING: 'warning',
  ERROR: 'error',
  INFO: 'info'
};
