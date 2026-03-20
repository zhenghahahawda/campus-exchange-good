<template>
  <div class="illegal-page">
    <!-- 页面头部 -->
    <div class="page-header-wrapper">
      <PageHeader title="违规举报" description="管理用户举报内容，维护平台秩序" />
      <div class="header-actions">
        <button class="glass-btn btn-export" @click="handleExport">
          <i class="el-icon-download"></i>
          <span>导出数据</span>
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row section-container">
      <div class="stat-item" v-for="(card, index) in statCardsData" :key="index">
        <StatCard v-bind="card" @click.native="handleStatusFilter(card.status)" 
          :class="{ 'active-stat-card': filterStatus === card.status }" />
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <IllegalFilterBar
      :search="searchQuery"
      :target="filterTarget"
      :type="filterType"
      :status="filterStatus"
      @search-change="searchQuery = $event"
      @target-change="handleTargetFilter"
      @type-change="handleTypeFilter"
      @status-change="handleStatusFilter"
    />

    <!-- 举报列表 -->
    <IllegalReportsList
      :key="updateTrigger"
      :reports="paginatedReports"
      :loading="false"
      @view-detail="handleViewDetail"
      @show-action-dialog="handleShowActionDialog"
      @show-progress="handleShowProgress"
      @complete-processing="handleCompleteProcessing"
      @show-result="handleShowResult"
      @show-reject-dialog="handleShowRejectDialog"
      @set-processing="handleSetProcessing"
      @refresh="loadReports"
    />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="filteredReports.length > 0">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[8, 16, 32, 64]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredReports.length"
        class="glass-pagination"
      />
    </div>

    <!-- 弹窗组件 -->
    <IllegalDialogs
      :detail-visible="detailDialogVisible"
      :progress-visible="progressDialogVisible"
      :reject-visible="rejectDialogVisible"
      :action-visible="actionDialogVisible"
      :result-visible="resultDialogVisible"
      :current-detail="currentReportDetail"
      :current-progress="currentProgressReport"
      :current-reject="currentRejectReport"
      :current-action="currentActionReport"
      :current-result="currentResultReport"
      :progress-text="progressText"
      :reject-reason="rejectReason"
      :selected-action="selectedAction"
      :action-params="actionParams"
      :action-feedback="actionFeedback"
      :is-executing="isExecuting"
      @update:detail-visible="detailDialogVisible = $event"
      @update:progress-visible="progressDialogVisible = $event"
      @update:reject-visible="rejectDialogVisible = $event"
      @update:action-visible="actionDialogVisible = $event"
      @update:result-visible="resultDialogVisible = $event"
      @update:progress-text="progressText = $event"
      @update:reject-reason="rejectReason = $event"
      @update:selected-action="selectedAction = $event"
      @save-progress="handleSaveProgress"
      @confirm-reject="handleConfirmReject"
      @select-action="selectActionWithFeedback"
      @select-duration="selectDuration"
      @validate-credit="validateCreditDeduction"
      @cancel-action="cancelAction"
      @execute-action="executeActionWithAnimation"
      @close-progress="closeProgressDialog"
      @close-reject="closeRejectDialog"
      @close-result="closeResultDialog"
      @show-reject-dialog="handleShowRejectDialog"
      @set-processing="handleSetProcessing"
      @show-action-dialog="handleShowActionDialog"
      @dialog-open="handleDialogOpen"
      @dialog-close="handleDialogClose"
    />
  </div>
</template>

<script>
import PageHeader from '@/components/common/PageHeader.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import IllegalFilterBar from '@/components/illegal/IllegalFilterBar.vue'
import IllegalReportsList from '@/components/illegal/IllegalReportsList.vue'
import IllegalDialogs from '@/components/illegal/IllegalDialogs.vue'

import { ReportFilter, ReportStats, CreditScoreValidator } from '@/utils/illegalManager'
import { OperationMessageHelper } from '@/utils/illegalMessageHelper'
import { isSuccess } from '@/composables/useApi'
import {
  REPORT_STATUS,
  REPORT_TYPES,
  REPORT_TARGET_TYPES,
  ACTION_TYPES,
  CREDIT_SCORE_CONFIG,
  BAN_DURATION_OPTIONS
} from '@/utils/illegalConstants'

export default {
  name: 'IllegalPage',
  
  components: {
    PageHeader,
    StatCard,
    IllegalFilterBar,
    IllegalReportsList,
    IllegalDialogs
  },
  
  data() {
    return {
      // 搜索和筛选
      searchQuery: '',
      filterType: REPORT_TYPES.ALL,
      filterStatus: REPORT_STATUS.ALL,
      filterTarget: REPORT_TARGET_TYPES.ALL,
      
      // 分页
      currentPage: 1,
      pageSize: 8,
      
      // 强制更新触发器
      updateTrigger: 0,
      
      // 弹窗状态
      detailDialogVisible: false,
      progressDialogVisible: false,
      rejectDialogVisible: false,
      actionDialogVisible: false,
      resultDialogVisible: false,
      
      // 当前操作的举报
      currentReportDetail: null,
      currentProgressReport: null,
      currentRejectReport: null,
      currentActionReport: null,
      currentResultReport: null,
      
      // 表单数据
      progressText: '',
      rejectReason: '',
      
      // 处理行动相关
      selectedAction: '',
      actionParams: {
        creditDeduction: CREDIT_SCORE_CONFIG.DEFAULT,
        banDuration: BAN_DURATION_OPTIONS[2],
        note: ''
      },
      actionFeedback: {
        [ACTION_TYPES.WARNING]: false,
        [ACTION_TYPES.TEMP_BAN]: false,
        [ACTION_TYPES.PERM_BAN]: false,
        [ACTION_TYPES.REMOVE_PRODUCT]: false
      },
      isExecuting: false,
      
      // 滚动控制
      scrollTop: 0,
      
      // 举报数据
      reports: []
    }
  },
  
  computed: {
    // 统计卡片数据
    statCardsData() {
      // 如果没有数据，返回全0的统计
      if (!this.reports || this.reports.length === 0) {
        return [
          { title: '待处理', value: '0', icon: 'el-icon-time', status: 'pending', iconBg: 'bg-warning' },
          { title: '处理中', value: '0', icon: 'el-icon-loading', status: 'processing', iconBg: 'bg-info' },
          { title: '已完成', value: '0', icon: 'el-icon-circle-check', status: 'approved', iconBg: 'bg-success' },
          { title: '已驳回', value: '0', icon: 'el-icon-circle-close', status: 'rejected', iconBg: 'bg-danger' }
        ];
      }
      return ReportStats.generateStatCards(this.reports)
    },
    
    // 筛选后的举报数据
    filteredReports() {
      return ReportFilter.filter(this.reports, {
        status: this.filterStatus,
        type: this.filterType,
        targetType: this.filterTarget,
        search: this.searchQuery
      })
    },
    
    // 分页后的数据
    paginatedReports() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredReports.slice(start, end)
    }
  },
  
  watch: {
    // 监听信誉分数变化
    'actionParams.creditDeduction'(newValue) {
      if (newValue !== null && newValue !== undefined) {
        const validation = CreditScoreValidator.validate(newValue)
        if (!validation.isValid) {
          this.$nextTick(() => {
            this.actionParams.creditDeduction = validation.correctedValue
          })
          const type = newValue < CREDIT_SCORE_CONFIG.MIN ? 'low' : 'high'
          OperationMessageHelper.showCreditScoreValidation(this, type)
        }
      }
    }
  },
  
  async mounted() {
    await this.fetchReports()
  },
  
  beforeDestroy() {
    document.body.classList.remove('dialog-open')
    document.documentElement.classList.remove('dialog-open')
    // 移除滚轮事件监听
    document.removeEventListener('wheel', this.preventScroll, { passive: false })
    document.removeEventListener('touchmove', this.preventScroll, { passive: false })
  },
  
  methods: {
    // 数据加载
    async fetchReports() {
      try {
        const res = await this.$axios.get('/api/reports/page', {
          params: { pageNum: 1, pageSize: 1000 }
        })
        let pageData = res
        if (res && res.code && res.data) {
          pageData = res.data
        }
        const rawList = pageData.records || []
        this.reports = rawList.map(item => this.normalizeReport(item))
      } catch (e) {
        console.error('获取举报数据失败:', e)
        this.$root.$emit('show-island-message', '获取举报数据失败', 'danger')
      }
    },

    normalizeReport(raw) {
      const parseImages = (img) => {
        if (!img) return []
        if (Array.isArray(img)) return img
        try {
          if (typeof img === 'string' && img.startsWith('[')) return JSON.parse(img)
          return img.split(',')
        } catch (e) { return [] }
      }

      const report = {
        id: raw.reportId,
        reportNumber: raw.reportNumber,
        title: raw.title,
        description: raw.description,
        detailContent: raw.detailContent || '',
        evidenceImages: parseImages(raw.evidenceImages),
        type: raw.reportType,
        status: raw.status,
        targetType: raw.targetType,
        targetId: raw.targetId,
        targetName: raw.targetName || '',
        reporterId: raw.reporterId,
        reporterName: raw.reporterName || ('用户' + raw.reporterId),
        createdAt: raw.createdAt ? new Date(raw.createdAt) : null,
        processedAt: raw.handledAt ? new Date(raw.handledAt) : null,
        handlerId: raw.handlerId,
        rejectReason: raw.rejectReason || '',
        creditDeduction: raw.creditDeduction,
        banDuration: raw.banDuration,
        banUntil: raw.banUntil,
        actionDescription: raw.handleResult || '',
        progressHistory: []
      }

      if (raw.handleAction) {
        report.actionResult = {
          action: raw.handleAction,
          actionLabel: this.getActionLabel(raw.handleAction),
          params: {
            creditDeduction: raw.creditDeduction,
            banDuration: raw.banDuration,
            note: raw.handleResult
          },
          executedAt: raw.handledAt ? new Date(raw.handledAt) : null
        }
      }

      return report
    },

    loadReports() {
      this.fetchReports()
    },
    
    // 筛选方法
    handleTargetFilter(target) {
      this.filterTarget = target
      this.currentPage = 1
    },
    
    handleTypeFilter(type) {
      this.filterType = type
      this.currentPage = 1
    },
    
    handleStatusFilter(status) {
      this.filterStatus = status
      this.currentPage = 1
    },
    
    // 弹窗控制
    handleDialogOpen() {
      this.scrollTop = document.documentElement.scrollTop || document.body.scrollTop
      document.body.classList.add('dialog-open')
      document.documentElement.classList.add('dialog-open')
      document.body.style.top = `-${this.scrollTop}px`
      
      // 阻止滚轮穿透
      document.addEventListener('wheel', this.preventScroll, { passive: false })
      document.addEventListener('touchmove', this.preventScroll, { passive: false })
    },
    
    handleDialogClose() {
      document.body.classList.remove('dialog-open')
      document.documentElement.classList.remove('dialog-open')
      document.body.style.top = ''
      
      // 移除滚轮事件监听
      document.removeEventListener('wheel', this.preventScroll, { passive: false })
      document.removeEventListener('touchmove', this.preventScroll, { passive: false })
      
      if (this.scrollTop !== undefined) {
        window.scrollTo(0, this.scrollTop)
        this.scrollTop = 0
      }
      
      this.currentReportDetail = null
    },
    
    // 阻止滚轮穿透的方法
    preventScroll(e) {
      // 检查事件目标是否在对话框内
      const dialogElement = e.target.closest('.el-dialog')
      if (!dialogElement) {
        e.preventDefault()
        e.stopPropagation()
        return false
      }
      
      // 如果在对话框内，检查是否可以滚动
      const scrollableElement = e.target.closest('.el-dialog__body, .modern-textarea, .textarea-container')
      if (scrollableElement) {
        const { scrollTop, scrollHeight, clientHeight } = scrollableElement
        const isScrollable = scrollHeight > clientHeight
        
        if (!isScrollable) {
          e.preventDefault()
          e.stopPropagation()
          return false
        }
        
        // 检查滚动边界
        if (e.deltaY < 0 && scrollTop === 0) {
          e.preventDefault()
          e.stopPropagation()
          return false
        }
        
        if (e.deltaY > 0 && scrollTop + clientHeight >= scrollHeight) {
          e.preventDefault()
          e.stopPropagation()
          return false
        }
      }
    },
    
    // 举报操作
    handleViewDetail(report) {
      this.currentReportDetail = report
      this.detailDialogVisible = true
      this.handleDialogOpen()
    },
    
    async handleSetProcessing(report) {
      try {
        const res = await this.$axios.put(`/api/reports/${report.id}`, {
          status: 'processing'
        })
        if (isSuccess(res)) {
          await this.fetchReports()
          this.updateTrigger++
          OperationMessageHelper.showReportOperationSuccess(this, 'REPORT_SET_PROCESSING')
        } else {
          this.$root.$emit('show-island-message', res.message || '操作失败', 'danger')
        }
      } catch (e) {
        console.error(e)
        this.$root.$emit('show-island-message', '操作失败', 'danger')
      }
    },
    
    async handleCompleteProcessing(report) {
      try {
        const res = await this.$axios.put(`/api/reports/${report.id}`, {
          status: 'approved'
        })
        if (isSuccess(res)) {
          await this.fetchReports()
          this.updateTrigger++
          OperationMessageHelper.showReportOperationSuccess(this, 'PROCESSING_COMPLETED')
        } else {
          this.$root.$emit('show-island-message', res.message || '操作失败', 'danger')
        }
      } catch (e) {
        console.error(e)
        this.$root.$emit('show-island-message', '操作失败', 'danger')
      }
    },
    
    // 驳回处理
    handleShowRejectDialog(report) {
      this.currentRejectReport = report
      this.rejectReason = ''
      this.rejectDialogVisible = true
      this.handleDialogOpen()
    },
    
    async handleConfirmReject() {
      if (!this.rejectReason.trim()) {
        this.$root.$emit('show-island-message', '请输入驳回理由', 'warning')
        return
      }

      try {
        const res = await this.$axios.post(`/api/reports/${this.currentRejectReport.id}/reject`, {
          rejectReason: this.rejectReason.trim()
        })
        if (isSuccess(res)) {
          this.rejectReason = ''
          this.rejectDialogVisible = false
          this.handleDialogClose()
          await this.fetchReports()
          this.updateTrigger++
          this.$root.$emit('show-island-message', '举报已驳回', 'success')
        } else {
          this.$root.$emit('show-island-message', res.message || '驳回失败', 'danger')
        }
      } catch (e) {
        console.error(e)
        this.$root.$emit('show-island-message', '驳回失败', 'danger')
      }
    },
    
    closeRejectDialog() {
      this.rejectDialogVisible = false
      this.handleDialogClose()
    },
    
    // 处理进度
    handleShowProgress(report) {
      this.currentProgressReport = report
      this.progressText = ''
      this.progressDialogVisible = true
      this.handleDialogOpen()
      
      if (!report.progressHistory) {
        this.$set(report, 'progressHistory', [])
      }
    },
    
    handleSaveProgress() {
      if (!this.progressText.trim()) {
        OperationMessageHelper.showRequiredFieldWarning(this, 'progressContent')
        return
      }
      
      // 确保progressHistory存在
      if (!this.currentProgressReport.progressHistory) {
        this.$set(this.currentProgressReport, 'progressHistory', [])
      }
      
      // 添加新的进度记录
      const progressRecord = {
        time: new Date(),
        content: this.progressText.trim()
      }
      
      this.currentProgressReport.progressHistory.unshift(progressRecord)
      
      // 触发强制更新
      this.updateTrigger++
      
      this.progressText = ''
      this.progressDialogVisible = false
      this.handleDialogClose()
      
      // 强制更新视图
      this.$forceUpdate()
      
      OperationMessageHelper.showReportOperationSuccess(this, 'PROGRESS_SAVED')
    },
    
    closeProgressDialog() {
      this.progressDialogVisible = false
      this.handleDialogClose()
    },
    
    // 处理结果
    handleShowResult(report) {
      this.currentResultReport = report
      this.resultDialogVisible = true
      this.handleDialogOpen()
    },
    
    closeResultDialog() {
      this.resultDialogVisible = false
      this.handleDialogClose()
    },
    
    // 处理行动
    handleShowActionDialog(report) {
      this.currentActionReport = report
      this.selectedAction = ''
      this.actionParams = {
        creditDeduction: CREDIT_SCORE_CONFIG.DEFAULT,
        banDuration: BAN_DURATION_OPTIONS[2],
        note: ''
      }
      
      Object.keys(this.actionFeedback).forEach(key => {
        this.actionFeedback[key] = false
      })
      
      this.isExecuting = false
      this.actionDialogVisible = true
      this.handleDialogOpen()
    },
    
    selectActionWithFeedback(action) {
      Object.keys(this.actionFeedback).forEach(key => {
        this.actionFeedback[key] = false
      })
      
      this.selectedAction = action
      
      this.$nextTick(() => {
        this.actionFeedback[action] = true
        // 移除弹窗提示
        // OperationMessageHelper.showActionSelected(this, action)
      })
    },
    
    selectDuration(duration) {
      this.actionParams.banDuration = duration
      // 移除弹窗提示
      // OperationMessageHelper.showBanDurationSet(this, duration)
    },
    
    validateCreditDeduction(value) {
      const validation = CreditScoreValidator.validate(value)
      
      if (!validation.isValid) {
        this.$nextTick(() => {
          this.actionParams.creditDeduction = validation.correctedValue
        })
        const type = value < CREDIT_SCORE_CONFIG.MIN ? 'low' : 'high'
        OperationMessageHelper.showCreditScoreValidation(this, type)
      } else {
        this.actionParams.creditDeduction = validation.correctedValue
      }
    },
    
    cancelAction() {
      this.actionDialogVisible = false
      this.selectedAction = ''
      this.isExecuting = false
      this.handleDialogClose()
    },
    
    async executeActionWithAnimation() {
      if (!this.selectedAction || this.isExecuting) {
        return
      }

      this.isExecuting = true

      try {
        await new Promise(resolve => setTimeout(resolve, 1500))

        const reportId = this.currentActionReport.id
        let res = null
        const actionNote = this.actionParams.note || ''

        if (this.selectedAction === 'warning') {
          res = await this.$axios.post(`/api/reports/${reportId}/handle/warning`, {
            handleResult: actionNote
          })
        } else if (this.selectedAction === 'tempBan') {
          res = await this.$axios.post(`/api/reports/${reportId}/handle/temp-ban`, {
            banDuration: this.actionParams.banDuration,
            creditDeduction: this.actionParams.creditDeduction,
            handleResult: actionNote
          })
        } else if (this.selectedAction === 'permBan') {
          res = await this.$axios.post(`/api/reports/${reportId}/handle/perm-ban`, {
            creditDeduction: this.actionParams.creditDeduction,
            handleResult: actionNote
          })
        } else if (this.selectedAction === 'removeProduct') {
          res = await this.$axios.post(`/api/reports/${reportId}/handle/remove-product`, {
            handleResult: actionNote
          })
        }

        if (isSuccess(res)) {
          this.actionDialogVisible = false
          this.handleDialogClose()
          await this.fetchReports()
          this.updateTrigger++
          OperationMessageHelper.showActionCompleted(this, this.selectedAction)
        } else {
          this.$root.$emit('show-island-message', (res && res.message) || '处理失败', 'danger')
        }
      } catch (error) {
        console.error(error)
        OperationMessageHelper.showOperationFailed(this)
      } finally {
        this.isExecuting = false
      }
    },
    
    // 获取行动标签的辅助方法
    getActionLabel(action) {
      const labels = {
        'warning': '警告处理',
        'tempBan': '临时封禁',
        'permBan': '永久封禁',
        'removeProduct': '下架商品'
      }
      return labels[action] || '未知处理'
    },
    
    // 其他操作
    handleExport() {
      OperationMessageHelper.showDevelopingFeature(this, '数据导出功能')
    },
    
    // 分页
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    
    handleCurrentChange(val) {
      this.currentPage = val
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';
@import '@/assets/css/components/pagination.scss';

// 防止弹窗滚轮穿透的全局样式
:global(.dialog-open) {
  overflow: hidden !important;
  position: fixed !important;
  width: 100% !important;
  height: 100% !important;
}

:global(.dialog-open body) {
  overflow: hidden !important;
  position: fixed !important;
  width: 100% !important;
}

// 对话框滚轮控制
:deep(.glass-dialog) {
  .el-dialog {
    // 确保对话框可以独立滚动
    .el-dialog__body {
      overflow-y: auto;
      overscroll-behavior: contain;
      
      // 自定义滚动条样式
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: rgba($color-border, 0.1);
        border-radius: 3px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: rgba($color-primary, 0.3);
        border-radius: 3px;
        
        &:hover {
          background: rgba($color-primary, 0.5);
        }
      }
    }
  }
}

// 文本域滚轮控制
.modern-textarea,
.textarea-container {
  overscroll-behavior: contain;
}

.illegal-page {
  padding: 20px;
  min-height: 100vh;
}

.page-header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
  
  .header-actions {
    display: flex;
    gap: 12px;
    align-items: center;
    
    .glass-btn.btn-export {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 14px 28px;
      background: linear-gradient(135deg, 
        rgba(var(--color-secondary-success-rgb), 0.15), 
        rgba(var(--color-secondary-success-rgb), 0.08));
      backdrop-filter: blur(15px);
      border: 2px solid rgba($color-secondary-success, 0.25);
      border-radius: 16px;
      color: $color-secondary-success;
      font-size: 15px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      text-decoration: none;
      white-space: nowrap;
      letter-spacing: 0.5px;
      position: relative;
      overflow: hidden;
      box-shadow: 0 4px 15px rgba(var(--color-secondary-success-rgb), 0.2);
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, 
          transparent, 
          rgba(255, 255, 255, 0.2), 
          transparent);
        transition: left 0.6s ease;
      }
      
      &::after {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 2px;
        background: linear-gradient(90deg, 
          rgba(var(--color-secondary-success-rgb), 0.8), 
          rgba(var(--color-secondary-success-rgb), 0.4),
          rgba(var(--color-secondary-success-rgb), 0.8));
        border-radius: 2px;
        opacity: 0;
        transition: opacity 0.3s ease;
      }
      
      &:hover {
        background: linear-gradient(135deg, 
          rgba(var(--color-secondary-success-rgb), 0.25), 
          rgba(var(--color-secondary-success-rgb), 0.15));
        border-color: rgba($color-secondary-success, 0.4);
        transform: translateY(-4px);
        box-shadow: 0 8px 30px rgba(var(--color-secondary-success-rgb), 0.35);
        
        &::before {
          left: 100%;
        }
        
        &::after {
          opacity: 1;
        }
        
        i {
          transform: scale(1.2) rotate(10deg);
        }
      }
      
      &:active {
        transform: translateY(-2px);
      }
      
      i {
        font-size: 18px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }
      
      span {
        font-weight: 700;
        letter-spacing: 0.5px;
      }
    }
  }
}

.section-container {
  margin-bottom: 32px;
}

.stats-row {
  display: flex;
  gap: 16px;
  justify-content: space-between;
  
  .stat-item {
    flex: 1;
    min-width: 0;
  }
}

.active-stat-card {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(var(--color-primary-rgb), 0.3);
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: inherit;
    border: 2px solid rgba(var(--color-primary-rgb), 0.5);
    pointer-events: none;
  }
}
</style>