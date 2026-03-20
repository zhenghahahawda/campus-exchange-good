/**
 * 违规举报管理组合式函数
 * @author Kiro
 * @version 1.0
 */

import { ref, computed, watch, nextTick } from 'vue'
import { 
  ReportFilter, 
  ReportStats, 
  ReportOperations, 
  CreditScoreValidator,
  Paginator 
} from '@/utils/illegalManager'
import { MessageHelper } from '@/utils/messageHelper'
import {
  OperationMessageHelper,
  OPERATION_MESSAGES
} from '@/utils/illegalMessageHelper'
import { 
  REPORT_STATUS, 
  REPORT_TYPES, 
  REPORT_TARGET_TYPES,
  ACTION_TYPES,
  ACTION_LABELS,
  CREDIT_SCORE_CONFIG,
  BAN_DURATION_OPTIONS,
  PAGINATION_CONFIG
} from '@/utils/illegalConstants'
import { generateMockReports } from '@/mock/illegalData'

/**
 * 违规举报管理 Hook
 * @param {Object} vm - Vue实例
 * @returns {Object} 响应式数据和方法
 */
export function useIllegalReports(vm) {
  // ===== 响应式数据 =====
  const reports = ref([])
  const searchQuery = ref('')
  const filterType = ref(REPORT_TYPES.ALL)
  const filterStatus = ref(REPORT_STATUS.ALL)
  const filterTarget = ref(REPORT_TARGET_TYPES.ALL)
  
  // 分页
  const currentPage = ref(1)
  const pageSize = ref(PAGINATION_CONFIG.DEFAULT_PAGE_SIZE)
  
  // 弹窗状态
  const detailDialogVisible = ref(false)
  const progressDialogVisible = ref(false)
  const rejectDialogVisible = ref(false)
  const actionDialogVisible = ref(false)
  const resultDialogVisible = ref(false)
  
  // 当前操作的举报
  const currentReportDetail = ref(null)
  const currentProgressReport = ref(null)
  const currentRejectReport = ref(null)
  const currentActionReport = ref(null)
  const currentResultReport = ref(null)
  
  // 表单数据
  const progressText = ref('')
  const rejectReason = ref('')
  
  // 处理行动相关
  const selectedAction = ref('')
  const actionParams = ref({
    creditDeduction: CREDIT_SCORE_CONFIG.DEFAULT,
    banDuration: BAN_DURATION_OPTIONS[2], // 默认7天
    note: ''
  })
  const actionFeedback = ref({
    [ACTION_TYPES.WARNING]: false,
    [ACTION_TYPES.TEMP_BAN]: false,
    [ACTION_TYPES.PERM_BAN]: false,
    [ACTION_TYPES.REMOVE_PRODUCT]: false
  })
  const isExecuting = ref(false)
  
  // 滚动控制
  const scrollTop = ref(0)

  // ===== 计算属性 =====
  
  // 统计卡片数据
  const statCardsData = computed(() => {
    return ReportStats.generateStatCards(reports.value)
  })
  
  // 筛选后的举报数据
  const filteredReports = computed(() => {
    return ReportFilter.filter(reports.value, {
      status: filterStatus.value,
      type: filterType.value,
      targetType: filterTarget.value,
      search: searchQuery.value
    })
  })
  
  // 分页后的数据
  const paginatedReports = computed(() => {
    return Paginator.paginate(
      filteredReports.value, 
      currentPage.value, 
      pageSize.value
    )
  })

  // ===== 监听器 =====
  
  // 监听信誉分数变化
  watch(() => actionParams.value.creditDeduction, (newValue) => {
    if (newValue !== null && newValue !== undefined) {
      const validation = CreditScoreValidator.validate(newValue)
      if (!validation.isValid) {
        nextTick(() => {
          actionParams.value.creditDeduction = validation.correctedValue
        })
        const type = newValue < CREDIT_SCORE_CONFIG.MIN ? 'low' : 'high'
        OperationMessageHelper.showCreditScoreValidation(vm, type)
      }
    }
  })

  // ===== 方法 =====
  
  /**
   * 加载举报数据
   */
  const loadReports = () => {
    reports.value = generateMockReports()
  }

  /**
   * 筛选方法
   */
  const handleTargetFilter = (target) => {
    filterTarget.value = target
    currentPage.value = 1
  }

  const handleTypeFilter = (type) => {
    filterType.value = type
    currentPage.value = 1
  }

  const handleStatusFilter = (status) => {
    filterStatus.value = status
    currentPage.value = 1
  }

  /**
   * 分页方法
   */
  const handleSizeChange = (val) => {
    pageSize.value = val
    currentPage.value = 1
  }

  const handleCurrentChange = (val) => {
    currentPage.value = val
  }

  /**
   * 弹窗控制
   */
  const handleDialogOpen = () => {
    scrollTop.value = document.documentElement.scrollTop || document.body.scrollTop
    document.body.classList.add('dialog-open')
    document.documentElement.classList.add('dialog-open')
    document.body.style.top = `-${scrollTop.value}px`
  }

  const handleDialogClose = () => {
    document.body.classList.remove('dialog-open')
    document.documentElement.classList.remove('dialog-open')
    document.body.style.top = ''
    
    if (scrollTop.value !== undefined) {
      window.scrollTo(0, scrollTop.value)
      scrollTop.value = 0
    }
    
    currentReportDetail.value = null
  }

  /**
   * 举报详情
   */
  const handleViewDetail = (report) => {
    currentReportDetail.value = report
    detailDialogVisible.value = true
    handleDialogOpen()
  }

  /**
   * 处理进度
   */
  const handleShowProgress = (report) => {
    currentProgressReport.value = report
    progressText.value = ''
    progressDialogVisible.value = true
    handleDialogOpen()
    
    if (!report.progressHistory) {
      vm.$set(report, 'progressHistory', [])
    }
  }

  const handleSaveProgress = () => {
    if (!progressText.value.trim()) {
      OperationMessageHelper.showRequiredFieldWarning(vm, 'progressContent')
      return
    }
    
    ReportOperations.addProgress(currentProgressReport.value, progressText.value)
    
    progressText.value = ''
    progressDialogVisible.value = false
    handleDialogClose()
    
    OperationMessageHelper.showReportOperationSuccess(vm, 'PROGRESS_SAVED')
  }

  const closeProgressDialog = () => {
    progressDialogVisible.value = false
    handleDialogClose()
  }

  /**
   * 驳回处理
   */
  const handleShowRejectDialog = (report) => {
    currentRejectReport.value = report
    rejectReason.value = ''
    rejectDialogVisible.value = true
    handleDialogOpen()
  }

  const handleConfirmReject = () => {
    if (!rejectReason.value.trim()) {
      OperationMessageHelper.showRequiredFieldWarning(vm, 'rejectReason')
      return
    }
    
    ReportOperations.reject(currentRejectReport.value, rejectReason.value)
    
    rejectReason.value = ''
    rejectDialogVisible.value = false
    handleDialogClose()
    
    OperationMessageHelper.showReportOperationSuccess(vm, 'REPORT_REJECTED')
  }

  const closeRejectDialog = () => {
    rejectDialogVisible.value = false
    handleDialogClose()
  }

  /**
   * 处理结果
   */
  const handleShowResult = (report) => {
    currentResultReport.value = report
    resultDialogVisible.value = true
    handleDialogOpen()
  }

  const closeResultDialog = () => {
    resultDialogVisible.value = false
    handleDialogClose()
  }

  /**
   * 处理行动
   */
  const handleShowActionDialog = (report) => {
    currentActionReport.value = report
    selectedAction.value = ''
    actionParams.value = {
      creditDeduction: CREDIT_SCORE_CONFIG.DEFAULT,
      banDuration: BAN_DURATION_OPTIONS[2],
      note: ''
    }
    
    // 重置反馈状态
    Object.keys(actionFeedback.value).forEach(key => {
      actionFeedback.value[key] = false
    })
    
    isExecuting.value = false
    actionDialogVisible.value = true
    handleDialogOpen()
  }

  const selectActionWithFeedback = (action) => {
    // 重置所有反馈状态
    Object.keys(actionFeedback.value).forEach(key => {
      actionFeedback.value[key] = false
    })
    
    selectedAction.value = action
    
    nextTick(() => {
      actionFeedback.value[action] = true
      OperationMessageHelper.showActionSelected(vm, ACTION_LABELS[action])
    })
  }

  const selectDuration = (duration) => {
    actionParams.value.banDuration = duration
    OperationMessageHelper.showBanDurationSet(vm, duration)
  }

  const validateCreditDeduction = (value) => {
    const validation = CreditScoreValidator.validate(value)
    
    if (!validation.isValid) {
      nextTick(() => {
        actionParams.value.creditDeduction = validation.correctedValue
      })
      const type = value < CREDIT_SCORE_CONFIG.MIN ? 'low' : 'high'
      OperationMessageHelper.showCreditScoreValidation(vm, type)
    } else {
      actionParams.value.creditDeduction = validation.correctedValue
    }
  }

  const cancelAction = () => {
    actionDialogVisible.value = false
    selectedAction.value = ''
    isExecuting.value = false
    handleDialogClose()
  }

  const executeActionWithAnimation = async () => {
    if (!selectedAction.value || isExecuting.value) {
      return
    }
    
    isExecuting.value = true
    
    try {
      // 模拟执行过程
      await new Promise(resolve => setTimeout(resolve, 1500))
      
      ReportOperations.executeAction(
        currentActionReport.value,
        selectedAction.value,
        actionParams.value
      )
      
      actionDialogVisible.value = false
      handleDialogClose()
      
      OperationMessageHelper.showActionCompleted(vm, ACTION_LABELS[selectedAction.value])
      
    } catch (error) {
      OperationMessageHelper.showOperationFailed(vm)
    } finally {
      isExecuting.value = false
    }
  }

  /**
   * 其他操作
   */
  const handleSetProcessing = (report) => {
    ReportOperations.setProcessing(report)
    OperationMessageHelper.showReportOperationSuccess(vm, 'REPORT_SET_PROCESSING')
  }

  const handleCompleteProcessing = (report) => {
    ReportOperations.completeProcessing(report)
    OperationMessageHelper.showReportOperationSuccess(vm, 'PROCESSING_COMPLETED')
  }

  const handleExport = () => {
    OperationMessageHelper.showDevelopingFeature(vm, '数据导出功能')
  }

  // 初始化数据
  loadReports()

  return {
    // 响应式数据
    reports,
    searchQuery,
    filterType,
    filterStatus,
    filterTarget,
    currentPage,
    pageSize,
    
    // 弹窗状态
    detailDialogVisible,
    progressDialogVisible,
    rejectDialogVisible,
    actionDialogVisible,
    resultDialogVisible,
    
    // 当前操作对象
    currentReportDetail,
    currentProgressReport,
    currentRejectReport,
    currentActionReport,
    currentResultReport,
    
    // 表单数据
    progressText,
    rejectReason,
    selectedAction,
    actionParams,
    actionFeedback,
    isExecuting,
    
    // 计算属性
    statCardsData,
    filteredReports,
    paginatedReports,
    
    // 方法
    loadReports,
    handleTargetFilter,
    handleTypeFilter,
    handleStatusFilter,
    handleSizeChange,
    handleCurrentChange,
    handleViewDetail,
    handleShowProgress,
    handleSaveProgress,
    closeProgressDialog,
    handleShowRejectDialog,
    handleConfirmReject,
    closeRejectDialog,
    handleShowResult,
    closeResultDialog,
    handleShowActionDialog,
    selectActionWithFeedback,
    selectDuration,
    validateCreditDeduction,
    cancelAction,
    executeActionWithAnimation,
    handleSetProcessing,
    handleCompleteProcessing,
    handleExport,
    handleDialogOpen,
    handleDialogClose
  }
}