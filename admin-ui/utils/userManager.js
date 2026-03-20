import { 
  USER_STATUS, 
  USER_ROLES, 
  USER_SORT_OPTIONS, 
  ACTIVE_USER_LOGIN_THRESHOLD,
  SEARCH_FIELDS,
  VALIDATION_RULES,
  DEFAULT_AVATAR
} from './userConstants';
import { BaseFilter, BaseSorter, validateArray } from './baseFilter';

/**
 * 用户筛选工具类
 */
export class UserFilter extends BaseFilter {
  static filterByStatus(users, status) {
    users = validateArray(users);
    let filtered = users.filter(u => u.userType !== 1 && u.role !== USER_ROLES.ADMIN);

    if (status === USER_STATUS.ALL) return filtered;
    if (status === USER_STATUS.NORMAL) return filtered.filter(u => u.status === 1);
    if (status === USER_STATUS.ACTIVE_USER) return filtered.filter(u => u.status === 2);
    if (status === USER_STATUS.INACTIVE) return filtered.filter(u => u.status === 0);
    return filtered.filter(u => u.status === status);
  }

  static filterBySearch(users, query) {
    return super.filterBySearch(users, query, SEARCH_FIELDS);
  }
}

export class UserSorter extends BaseSorter {
  static sort(users, sortBy) {
    users = validateArray(users);

    if (sortBy === USER_SORT_OPTIONS.ONLINE_FIRST) {
      return [...users].sort((a, b) => {
        if (a.isOnline && !b.isOnline) return -1;
        if (!a.isOnline && b.isOnline) return 1;
        return new Date(b.lastLoginTime) - new Date(a.lastLoginTime);
      });
    }

    return super.sort(users, sortBy, {
      [USER_SORT_OPTIONS.DEFAULT]: { field: 'id', order: 'asc' },
      [USER_SORT_OPTIONS.REPUTATION_DESC]: { field: 'reputation', order: 'desc' },
      [USER_SORT_OPTIONS.GOODS_DESC]: { field: 'goodsCount', order: 'desc' },
      [USER_SORT_OPTIONS.ORDERS_DESC]: { field: 'ordersCount', order: 'desc' }
    });
  }
}

/**
 * 用户统计工具类
 */
export class UserStats {
  /**
   * 计算用户统计数据（优化版：单次遍历）
   * @param {Array} users - 用户列表
   * @returns {Object} 统计结果
   */
  static calculate(users) {
    if (!users || !Array.isArray(users)) {
      return {
        total: 0,
        active: 0,
        inactive: 0,
        normalUsers: 0,
        activeUsers: 0
      };
    }

    // 排除管理员用户进行统计（兼容 userType 和 role 字段）
    const nonAdminUsers = users.filter(u => u.userType !== 1 && u.role !== USER_ROLES.ADMIN);

    return nonAdminUsers.reduce((stats, user) => {
      stats.total++;

      // 后端 status：0=封禁，1=普通用户，2=活跃用户
      if (user.status === 1) {
        stats.normalUsers++;
        stats.active++;
      } else if (user.status === 2) {
        stats.activeUsers++;
        stats.active++;
      } else if (user.status === 0) {
        stats.inactive++;
      }

      return stats;
    }, {
      total: 0,
      active: 0,
      inactive: 0,
      normalUsers: 0,
      activeUsers: 0
    });
  }

  /**
   * 生成统计卡片数据
   * @param {Array} users - 用户列表
   * @returns {Array} 统计卡片数据
   */
  static generateStatCards(users) {
    const stats = this.calculate(users);
    
    return [
      {
        title: '全部用户',
        value: stats.total.toString(),
        icon: 'el-icon-user',
        iconBg: 'bg-primary',
        percentage: '',
        percentageColor: '',
        footerLabel: '平台总用户数',
        status: USER_STATUS.ALL
      },
      {
        title: '普通用户',
        value: stats.normalUsers.toString(),
        icon: 'el-icon-user-solid',
        iconBg: 'bg-info',
        percentage: `${stats.total > 0 ? ((stats.normalUsers/stats.total)*100).toFixed(1) : 0}%`,
        percentageColor: 'text-success',
        footerLabel: '占总用户比例',
        status: USER_STATUS.NORMAL
      },
      {
        title: '活跃用户',
        value: stats.activeUsers.toString(),
        icon: 'el-icon-star-on',
        iconBg: 'bg-success',
        percentage: `${stats.total > 0 ? ((stats.activeUsers/stats.total)*100).toFixed(1) : 0}%`,
        percentageColor: 'text-success',
        footerLabel: '占总用户比例',
        status: USER_STATUS.ACTIVE_USER
      },
      {
        title: '已封禁',
        value: stats.inactive.toString(),
        icon: 'el-icon-close',
        iconBg: 'bg-danger',
        percentage: `${stats.total > 0 ? ((stats.inactive/stats.total)*100).toFixed(1) : 0}%`,
        percentageColor: 'text-danger',
        footerLabel: '占总用户比例',
        status: USER_STATUS.INACTIVE
      }
    ];
  }
}

/**
 * 用户操作工具类
 */
export class UserOperations {
  /**
   * 生成新的用户ID
   * @param {Array} users - 用户列表
   * @returns {string} 新的用户ID
   */
  static generateUserId(users) {
    if (!users || !Array.isArray(users) || users.length === 0) {
      return 'U001';
    }

    const maxId = Math.max(...users.map(u => {
      // 将 userId 转为字符串，兼容数字和字符串类型
      const userIdStr = String(u.userId || '');
      const idNum = parseInt(userIdStr.replace('U', ''));
      return isNaN(idNum) ? 0 : idNum;
    }));

    return `U${String(maxId + 1).padStart(3, '0')}`;
  }

  /**
   * 创建新用户
   * @param {Object} formData - 表单数据
   * @param {Array} existingUsers - 现有用户列表
   * @returns {Object} 新用户对象
   */
  static createUser(formData, existingUsers) {
    return {
      ...formData,
      id: Date.now(), // 使用时间戳作为ID
      status: 1, // 默认为普通用户
      isActive: true,
      goodsCount: 0,
      ordersCount: 0,
      loginCount: formData.userType === USER_TYPES.ACTIVE ? 51 : 30,
      createdAt: new Date().toISOString().split('T')[0],
      isOnline: false,
      lastLoginTime: new Date().toISOString().replace('T', ' ').split('.')[0],
      avatar: formData.avatar || DEFAULT_AVATAR
    };
  }

  /**
   * 更新用户
   * @param {Array} users - 用户列表
   * @param {Object} updatedUser - 更新后的用户对象
   * @returns {Array} 更新后的用户列表
   */
  static updateUser(users, updatedUser) {
    const index = users.findIndex(u => u.id === updatedUser.id);
    if (index !== -1) {
      const newUsers = [...users];
      // 根据用户类型设置登录次数
      const loginCount = updatedUser.userType === USER_TYPES.ACTIVE ? 
        Math.max(users[index].loginCount, ACTIVE_USER_LOGIN_THRESHOLD + 1) : 
        Math.min(users[index].loginCount, ACTIVE_USER_LOGIN_THRESHOLD);
      
      newUsers[index] = { 
        ...newUsers[index], 
        ...updatedUser, 
        loginCount 
      };
      return newUsers;
    }
    return users;
  }

  /**
   * 删除用户
   * @param {Array} users - 用户列表
   * @param {number} userId - 用户ID
   * @returns {Array} 删除后的用户列表
   */
  static deleteUser(users, userId) {
    return users.filter(user => user.id !== userId);
  }

  /**
   * 切换用户状态
   * @param {Object} user - 用户对象
   * @returns {string} 状态消息
   */
  static toggleUserStatus(user) {
    // 根据 isActive 切换 status：true 时设为 1（普通用户），false 时设为 0（封禁）
    user.status = user.isActive ? 1 : 0;
    return user.isActive ? '用户已解除封禁' : '用户已封禁';
  }
}

/**
 * 用户显示工具类
 */
export class UserDisplay {
  /**
   * 获取用户类型标签
   * @param {Object} user - 用户对象
   * @returns {string} 用户类型标签
   */
  static getUserTypeLabel(user) {
    // 后端 status：0=封禁，1=普通用户，2=活跃用户
    const typeMap = {
      0: '已封禁',
      1: '普通用户',
      2: '活跃用户'
    };
    return typeMap[user.status] || '未知状态';
  }

  /**
   * 获取用户类型图标
   * @param {Object} user - 用户对象
   * @returns {string} 图标类名
   */
  static getUserTypeIcon(user) {
    // 后端 status：0=封禁，1=普通用户，2=活跃用户
    const iconMap = {
      0: 'el-icon-close',
      1: 'el-icon-user-solid',
      2: 'el-icon-star-on'
    };
    return iconMap[user.status] || 'el-icon-user-solid';
  }

  /**
   * 获取用户类型徽章样式类
   * @param {Object} user - 用户对象
   * @returns {string} 样式类名
   */
  static getUserTypeBadgeClass(user) {
    // 后端 status：0=封禁，1=普通用户，2=活跃用户
    const classMap = {
      0: 'badge-banned',
      1: 'badge-normal',
      2: 'badge-active'
    };
    return classMap[user.status] || 'badge-normal';
  }

  /**
   * 格式化在线状态文本
   * @param {Object} user - 用户对象
   * @returns {string} 在线状态文本
   */
  static getOnlineStatusText(user) {
    if (user.isOnline) {
      return '在线中';
    } else {
      return `最近登录: ${this.formatDateTime(user.lastLoginTime)}`;
    }
  }

  /**
   * 格式化日期时间
   * @param {string} dateTimeStr - 日期时间字符串
   * @returns {string} 格式化后的时间
   */
  static formatDateTime(dateTimeStr) {
    const date = new Date(dateTimeStr);
    const now = new Date();
    const diff = now - date;
    
    // 小于1分钟
    if (diff < 1000 * 60) {
      return '刚刚';
    }
    // 小于1小时
    if (diff < 1000 * 60 * 60) {
      return Math.floor(diff / (1000 * 60)) + '分钟前';
    }
    // 小于24小时
    if (diff < 1000 * 60 * 60 * 24) {
      return Math.floor(diff / (1000 * 60 * 60)) + '小时前';
    }
    // 小于7天
    if (diff < 1000 * 60 * 60 * 24 * 7) {
      return Math.floor(diff / (1000 * 60 * 60 * 24)) + '天前';
    }
    // 超过7天显示具体日期
    return date.toLocaleDateString('zh-CN') + ' ' + 
           date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }
}

/**
 * 用户表单验证工具类
 */
export class UserValidator {
  /**
   * 验证用户表单
   * @param {Object} formData - 表单数据
   * @param {boolean} isEditMode - 是否为编辑模式
   * @returns {Object} 验证结果 { isValid: boolean, errors: Array }
   */
  static validateUserForm(formData, isEditMode = false) {
    const errors = [];

    // 验证用户名
    if (!formData.name || formData.name.trim() === '') {
      errors.push('请输入用户名');
    } else if (formData.name.length > VALIDATION_RULES.MAX_NAME_LENGTH) {
      errors.push(`用户名不能超过${VALIDATION_RULES.MAX_NAME_LENGTH}个字符`);
    }

    // 验证密码（仅新增模式）
    if (!isEditMode) {
      if (!formData.password || formData.password.trim() === '') {
        errors.push('请输入密码');
      } else if (formData.password.length < 6) {
        errors.push('密码至少需要6位');
      }
    }

    // 验证邮箱
    if (!formData.email || formData.email.trim() === '') {
      errors.push('请输入邮箱');
    } else if (!VALIDATION_RULES.EMAIL_REGEX.test(formData.email)) {
      errors.push('请输入正确的邮箱格式');
    }

    // 验证手机号
    if (!formData.phone || formData.phone.trim() === '') {
      errors.push('请输入手机号');
    } else if (!VALIDATION_RULES.PHONE_REGEX.test(formData.phone)) {
      errors.push('请输入正确的手机号格式');
    }

    // 验证账号地址（编辑模式下可选）
    if (!isEditMode && (!formData.accountAddress || formData.accountAddress.trim() === '')) {
      errors.push('请输入账号地址');
    }

    // 验证学校（编辑模式下可选）
    if (!isEditMode && (!formData.school || formData.school.trim() === '')) {
      errors.push('请输入学校名称');
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }
}