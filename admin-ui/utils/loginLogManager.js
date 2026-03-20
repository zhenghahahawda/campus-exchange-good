import { 
  LOGIN_STATUS, 
  LOGIN_LOG_SORT_OPTIONS,
  PAGINATION_CONFIG
} from './loginLogConstants';
import { BaseFilter, BaseSorter, validateArray } from './baseFilter';

/**
 * 登录日志筛选工具类
 */
export class LoginLogFilter extends BaseFilter {
  static filterByStatus(logs, status) {
    logs = validateArray(logs);
    if (status === LOGIN_STATUS.ALL) return logs;
    return logs.filter(log => log.loginStatus === status);
  }

  static filterBySearch(logs, query) {
    return super.filterBySearch(logs, query, ['userId', 'loginIp', 'browser', 'os', 'failReason']);
  }
}

export class LoginLogSorter extends BaseSorter {
  static sort(logs, sortBy) {
    return super.sort(validateArray(logs), sortBy, {
      [LOGIN_LOG_SORT_OPTIONS.LATEST]: { field: 'loginTime', order: 'desc' },
      [LOGIN_LOG_SORT_OPTIONS.OLDEST]: { field: 'loginTime', order: 'asc' }
    });
  }
}

/**
 * 登录日志统计工具类
 */
export class LoginLogStats {
  static calculate(logs) {
    logs = validateArray(logs);
    return logs.reduce((stats, log) => {
      stats.total++;
      if (log.loginStatus === 1) { // 1 代表成功
        stats.success++;
      } else {
        stats.failed++;
      }
      return stats;
    }, { total: 0, success: 0, failed: 0 });
  }

  static generateStatCards(logs) {
    const stats = this.calculate(logs);
    return [
      {
        title: '总登录次数',
        value: stats.total.toString(),
        icon: 'el-icon-monitor',
        iconBg: 'bg-primary',
        footerLabel: '平台累计登录次数',
        status: LOGIN_STATUS.ALL
      },
      {
        title: '登录成功',
        value: stats.success.toString(),
        icon: 'el-icon-circle-check',
        iconBg: 'bg-success',
        footerLabel: '登录成功记录',
        status: LOGIN_STATUS.SUCCESS
      },
      {
        title: '登录失败',
        value: stats.failed.toString(),
        icon: 'el-icon-circle-close',
        iconBg: 'bg-danger',
        footerLabel: '异常登录尝试',
        status: LOGIN_STATUS.FAILED
      }
    ];
  }
}

/**
 * 登录日志操作工具类
 */
export class LoginLogOperations {
  static createLog(formData, existingLogs) {
    const maxId = existingLogs.length > 0 ? Math.max(...existingLogs.map(l => l.logId)) : 0;
    return {
      ...formData,
      logId: maxId + 1,
      loginTime: new Date().toISOString().replace('T', ' ').split('.')[0]
    };
  }

  static updateLog(logs, updatedLog) {
    const index = logs.findIndex(l => l.logId === updatedLog.logId);
    if (index !== -1) {
      const newLogs = [...logs];
      newLogs[index] = { ...newLogs[index], ...updatedLog };
      return newLogs;
    }
    return logs;
  }

  static deleteLog(logs, logId) {
    return logs.filter(log => log.logId !== logId);
  }
}
