/**
 * 违规举报管理工具类
 * @author Kiro
 * @version 1.0
 */

import {
  REPORT_STATUS,
  REPORT_TYPES,
  REPORT_TARGET_TYPES,
  ACTION_TYPES,
  ACTION_EXECUTION_LABELS,
  CREDIT_SCORE_CONFIG
} from './illegalConstants'

/**
 * 举报筛选器
 */
export class ReportFilter {
  /**
   * 按状态筛选举报
   * @param {Array} reports - 举报列表
   * @param {string} status - 状态
   * @returns {Array} 筛选后的举报列表
   */
  static filterByStatus(reports, status) {
    if (status === REPORT_STATUS.ALL) return reports
    return reports.filter(report => report.status === status)
  }

  /**
   * 按类型筛选举报
   * @param {Array} reports - 举报列表
   * @param {string} type - 类型
   * @returns {Array} 筛选后的举报列表
   */
  static filterByType(reports, type) {
    if (type === REPORT_TYPES.ALL) return reports
    return reports.filter(report => report.type === type)
  }

  /**
   * 按目标类型筛选举报
   * @param {Array} reports - 举报列表
   * @param {string} targetType - 目标类型
   * @returns {Array} 筛选后的举报列表
   */
  static filterByTargetType(reports, targetType) {
    if (targetType === REPORT_TARGET_TYPES.ALL) return reports
    return reports.filter(report => report.targetType === targetType)
  }

  /**
   * 按搜索关键词筛选举报
   * @param {Array} reports - 举报列表
   * @param {string} query - 搜索关键词
   * @returns {Array} 筛选后的举报列表
   */
  static filterBySearch(reports, query) {
    if (!query) return reports
    
    const lowerQuery = query.toLowerCase()
    return reports.filter(report => 
      report.title.toLowerCase().includes(lowerQuery) ||
      report.description.toLowerCase().includes(lowerQuery) ||
      report.reporterName.toLowerCase().includes(lowerQuery) ||
      report.targetName.toLowerCase().includes(lowerQuery) ||
      (report.productName && report.productName.toLowerCase().includes(lowerQuery))
    )
  }

  /**
   * 综合筛选
   * @param {Array} reports - 举报列表
   * @param {Object} filters - 筛选条件
   * @returns {Array} 筛选后的举报列表
   */
  static filter(reports, filters) {
    let filtered = reports

    if (filters.status) {
      filtered = this.filterByStatus(filtered, filters.status)
    }

    if (filters.type) {
      filtered = this.filterByType(filtered, filters.type)
    }

    if (filters.targetType) {
      filtered = this.filterByTargetType(filtered, filters.targetType)
    }

    if (filters.search) {
      filtered = this.filterBySearch(filtered, filters.search)
    }

    return filtered
  }
}

/**
 * 举报统计器
 */
export class ReportStats {
  /**
   * 计算举报统计数据
   * @param {Array} reports - 举报列表
   * @returns {Object} 统计数据
   */
  static calculate(reports) {
    const total = reports.length
    const pending = reports.filter(r => r.status === REPORT_STATUS.PENDING).length
    const approved = reports.filter(r => r.status === REPORT_STATUS.APPROVED).length
    const rejected = reports.filter(r => r.status === REPORT_STATUS.REJECTED).length
    const productReports = reports.filter(r => r.targetType === REPORT_TARGET_TYPES.PRODUCT).length
    const userReports = reports.filter(r => r.targetType === REPORT_TARGET_TYPES.USER).length

    return {
      total,
      pending,
      approved,
      rejected,
      productReports,
      userReports,
      pendingPercentage: total > 0 ? ((pending / total) * 100).toFixed(1) : '0',
      approvedPercentage: total > 0 ? ((approved / total) * 100).toFixed(1) : '0',
      rejectedPercentage: total > 0 ? ((rejected / total) * 100).toFixed(1) : '0'
    }
  }

  /**
   * 生成统计卡片数据
   * @param {Array} reports - 举报列表
   * @returns {Array} 统计卡片数据
   */
  static generateStatCards(reports) {
    const stats = this.calculate(reports)

    return [
      {
        title: '总举报数',
        value: stats.total.toString(),
        icon: 'el-icon-warning-outline',
        iconBg: 'bg-primary',
        percentage: `商品:${stats.productReports} 用户:${stats.userReports}`,
        percentageColor: 'text-info',
        footerLabel: '举报分类统计',
        status: REPORT_STATUS.ALL
      },
      {
        title: '待处理',
        value: stats.pending.toString(),
        icon: 'el-icon-time',
        iconBg: 'bg-warning',
        percentage: `${stats.pendingPercentage}%`,
        percentageColor: 'text-warning',
        footerLabel: '占总举报比例',
        status: REPORT_STATUS.PENDING
      },
      {
        title: '已处理',
        value: stats.approved.toString(),
        icon: 'el-icon-check',
        iconBg: 'bg-success',
        percentage: `${stats.approvedPercentage}%`,
        percentageColor: 'text-success',
        footerLabel: '占总举报比例',
        status: REPORT_STATUS.APPROVED
      },
      {
        title: '已驳回',
        value: stats.rejected.toString(),
        icon: 'el-icon-close',
        iconBg: 'bg-danger',
        percentage: `${stats.rejectedPercentage}%`,
        percentageColor: 'text-danger',
        footerLabel: '占总举报比例',
        status: REPORT_STATUS.REJECTED
      }
    ]
  }
}

/**
 * 举报操作管理器
 */
export class ReportOperations {
  /**
   * 审核通过举报
   * @param {Object} report - 举报对象
   * @returns {Object} 更新后的举报对象
   */
  static approve(report) {
    report.status = REPORT_STATUS.APPROVED
    report.processedAt = new Date()
    return report
  }

  /**
   * 驳回举报
   * @param {Object} report - 举报对象
   * @param {string} reason - 驳回理由
   * @returns {Object} 更新后的举报对象
   */
  static reject(report, reason) {
    report.status = REPORT_STATUS.REJECTED
    report.rejectReason = reason.trim()
    report.processedAt = new Date()
    return report
  }

  /**
   * 设置为处理中
   * @param {Object} report - 举报对象
   * @returns {Object} 更新后的举报对象
   */
  static setProcessing(report) {
    report.status = REPORT_STATUS.PROCESSING
    return report
  }

  /**
   * 添加处理进度
   * @param {Object} report - 举报对象
   * @param {string} content - 进度内容
   * @returns {Object} 更新后的举报对象
   */
  static addProgress(report, content) {
    if (!report.progressHistory) {
      report.progressHistory = []
    }

    const progressRecord = {
      time: new Date(),
      content: content.trim()
    }

    report.progressHistory.unshift(progressRecord)
    return report
  }

  /**
   * 完成处理
   * @param {Object} report - 举报对象
   * @returns {Object} 更新后的举报对象
   */
  static completeProcessing(report) {
    if (!report.progressHistory) {
      report.progressHistory = []
    }

    const completeRecord = {
      time: new Date(),
      content: '处理已完成，举报问题已解决。'
    }

    report.progressHistory.unshift(completeRecord)
    report.status = REPORT_STATUS.APPROVED
    report.processedAt = new Date()
    
    return report
  }

  /**
   * 执行处理行动
   * @param {Object} report - 举报对象
   * @param {string} action - 行动类型
   * @param {Object} params - 行动参数
   * @returns {Object} 更新后的举报对象
   */
  static executeAction(report, action, params) {
    const actionResult = {
      action,
      actionLabel: ACTION_EXECUTION_LABELS[action],
      params: { ...params },
      executedAt: new Date()
    }

    // 构建处理描述
    let actionDescription = ACTION_EXECUTION_LABELS[action]
    if (action === ACTION_TYPES.WARNING) {
      actionDescription += `，扣除信誉分数${params.creditDeduction}分`
    } else if (action === ACTION_TYPES.TEMP_BAN) {
      actionDescription += `，封禁${params.banDuration}天`
    }

    if (params.note) {
      actionDescription += `。处理说明：${params.note}`
    }

    // 保存处理结果
    report.actionResult = actionResult
    report.actionDescription = actionDescription
    report.status = REPORT_STATUS.APPROVED
    report.processedAt = new Date()

    // 添加处理记录
    this.addProgress(report, `执行处理行动：${actionDescription}`)

    return report
  }
}

/**
 * 信誉分数验证器
 */
export class CreditScoreValidator {
  /**
   * 验证信誉分数
   * @param {number} value - 分数值
   * @returns {Object} 验证结果
   */
  static validate(value) {
    const numValue = typeof value === 'number' ? value : parseInt(value) || CREDIT_SCORE_CONFIG.DEFAULT

    if (numValue < CREDIT_SCORE_CONFIG.MIN) {
      return {
        isValid: false,
        correctedValue: CREDIT_SCORE_CONFIG.MIN,
        message: `信誉分数不能少于${CREDIT_SCORE_CONFIG.MIN}分`
      }
    }

    if (numValue > CREDIT_SCORE_CONFIG.MAX) {
      return {
        isValid: false,
        correctedValue: CREDIT_SCORE_CONFIG.MAX,
        message: `信誉分数不能超过${CREDIT_SCORE_CONFIG.MAX}分`
      }
    }

    return {
      isValid: true,
      correctedValue: numValue,
      message: null
    }
  }

  /**
   * 获取违规程度
   * @param {number} score - 分数
   * @returns {Object} 违规程度信息
   */
  static getViolationLevel(score) {
    if (score <= CREDIT_SCORE_CONFIG.LIGHT_VIOLATION_THRESHOLD) {
      return {
        level: 'light',
        label: '轻微违规',
        icon: 'el-icon-info',
        class: 'param-hint'
      }
    }

    if (score >= CREDIT_SCORE_CONFIG.SEVERE_VIOLATION_THRESHOLD) {
      return {
        level: 'severe',
        label: '严重违规',
        icon: 'el-icon-warning',
        class: 'param-hint warning'
      }
    }

    return {
      level: 'normal',
      label: '',
      icon: '',
      class: ''
    }
  }
}

/**
 * 内容格式化工具
 */
export class ContentFormatter {
  /**
   * 格式化内容，去除多余空白和换行
   * @param {string} content - 原始内容
   * @returns {string} 格式化后的内容
   */
  static formatContent(content) {
    if (!content) return ''

    // 去除开头和结尾的空白字符
    let formatted = content.trim()

    // 将多个连续的换行符替换为最多两个换行符
    formatted = formatted.replace(/\n{3,}/g, '\n\n')

    // 去除开头和结尾的换行符
    formatted = formatted.replace(/^\n+/, '').replace(/\n+$/, '')

    return formatted
  }

  /**
   * 将内容转换为HTML格式
   * @param {string} content - 原始内容
   * @returns {string} HTML格式的内容
   */
  static formatContentToHtml(content) {
    if (!content) return ''

    // 先进行基本格式化
    let formatted = this.formatContent(content)

    // 转义HTML特殊字符
    formatted = formatted
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')

    // 将换行符转换为<br>标签
    formatted = formatted.replace(/\n/g, '<br>')

    return formatted
  }

  /**
   * 格式化日期
   * @param {Date} date - 日期对象
   * @returns {string} 格式化后的日期字符串
   */
  static formatDate(date) {
    return new Intl.DateTimeFormat('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date)
  }
}

/**
 * 分页工具
 */
export class Paginator {
  /**
   * 分页处理
   * @param {Array} items - 数据列表
   * @param {number} currentPage - 当前页码
   * @param {number} pageSize - 每页大小
   * @returns {Array} 当前页数据
   */
  static paginate(items, currentPage, pageSize) {
    const start = (currentPage - 1) * pageSize
    const end = start + pageSize
    return items.slice(start, end)
  }
}