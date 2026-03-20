<template>
  <el-dialog
    title="处理结果"
    :visible.sync="dialogVisible"
    width="750px"
    custom-class="glass-dialog result-dialog"
    :modal="false"
    :close-on-click-modal="false"
    append-to-body
    center
    top="5vh"
    @open="$emit('dialog-open')"
    @close="$emit('dialog-close')"
  >
    <div v-if="report" class="result-content">
      <!-- 举报基本信息卡片 -->
      <div class="result-info-card">
        <div class="info-header">
          <div class="info-icon" :class="getStatusIconClass(report.status)">
            <i :class="getStatusIcon(report.status)"></i>
          </div>
          <div class="info-content">
            <h4 class="result-title">{{ report.title }}</h4>
            <div class="result-meta">
              <span class="meta-tag" :class="getTypeBadgeClass(report.type)">
                <i :class="getTypeIcon(report.type)"></i>
                {{ getTypeLabel(report.type) }}
              </span>
              <span class="status-badge" :class="getStatusClass(report.status)">
                <i :class="getStatusIcon(report.status)"></i>
                {{ getStatusLabel(report.status) }}
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 处理结果详情 -->
      <div class="result-details">
        <!-- 已处理状态 -->
        <div v-if="report.status === 'approved'" class="result-section approved-section">
          <div class="section-header">
            <div class="section-icon success">
              <i class="el-icon-circle-check"></i>
            </div>
            <h5 class="section-title">处理完成</h5>
            <div class="section-decoration success"></div>
          </div>
          
          <!-- 处理行动结果 -->
          <div v-if="report.actionResult" class="action-result-card">
            <div class="card-header">
              <div class="card-icon">
                <i class="el-icon-s-tools"></i>
              </div>
              <h6 class="card-title">执行的处理行动</h6>
            </div>
            
            <div class="action-summary">
              <div class="action-badge" :class="getActionBadgeClass(report.actionResult.action)">
                <i :class="getActionIcon(report.actionResult.action)"></i>
                <span>{{ report.actionResult.actionLabel }}</span>
              </div>
              
              <div class="action-details">
                <div v-if="report.actionDescription" class="action-description">
                  <div class="desc-label">
                    <i class="el-icon-document"></i>
                    <span>处理详情</span>
                  </div>
                  <div class="desc-content">{{ report.actionDescription }}</div>
                </div>
                
                <div class="action-params" v-if="report.actionResult.params">
                  <div class="param-item" v-if="report.actionResult.params.creditDeduction && (report.actionResult.action === 'warning' || report.actionResult.action === 'tempBan')">
                    <span class="param-label">信誉分数扣除：</span>
                    <span class="param-value credit-value">{{ report.actionResult.params.creditDeduction }}分</span>
                  </div>
                  <div class="param-item" v-if="report.actionResult.params.banDuration && report.actionResult.action === 'tempBan'">
                    <span class="param-label">封禁时长：</span>
                    <span class="param-value ban-value">{{ report.actionResult.params.banDuration }}天</span>
                  </div>
                  <div class="param-item" v-if="report.actionResult.params.note">
                    <span class="param-label">处理说明：</span>
                    <span class="param-value note-value">{{ report.actionResult.params.note }}</span>
                  </div>
                </div>
                
                <div class="action-time">
                  <i class="el-icon-time"></i>
                  <span>执行时间：{{ formatDate(report.actionResult.executedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 处理历史时间线 -->
          <div v-if="report.progressHistory && report.progressHistory.length > 0" class="progress-history-card">
            <div class="card-header">
              <div class="card-icon">
                <i class="el-icon-time"></i>
              </div>
              <h6 class="card-title">处理过程记录</h6>
            </div>
            
            <div class="history-timeline">
              <div 
                v-for="(record, index) in report.progressHistory" 
                :key="index"
                class="timeline-item"
              >
                <div class="timeline-dot">
                  <div class="dot-inner"></div>
                </div>
                <div class="timeline-content">
                  <div class="timeline-time">{{ formatDate(record.time) }}</div>
                  <div class="timeline-text">{{ record.content }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 已驳回状态 -->
        <div v-if="report.status === 'rejected'" class="result-section rejected-section">
          <div class="section-header">
            <div class="section-icon rejected">
              <i class="el-icon-circle-close"></i>
            </div>
            <h5 class="section-title">举报已驳回</h5>
            <div class="section-decoration rejected"></div>
          </div>
          
          <div v-if="report.rejectReason" class="reject-reason-card">
            <div class="card-header">
              <div class="card-icon">
                <i class="el-icon-warning-outline"></i>
              </div>
              <h6 class="card-title">驳回理由</h6>
            </div>
            
            <div class="reason-content">
              <div class="reason-text">{{ report.rejectReason }}</div>
            </div>
          </div>
        </div>
        
        <!-- 时间信息卡片 -->
        <div class="time-info-card">
          <div class="card-header">
            <div class="card-icon">
              <i class="el-icon-date"></i>
            </div>
            <h6 class="card-title">时间信息</h6>
          </div>
          
          <div class="time-grid">
            <div class="time-item">
              <div class="time-icon">
                <i class="el-icon-plus"></i>
              </div>
              <div class="time-content">
                <span class="time-label">举报时间</span>
                <span class="time-value">{{ formatDate(report.createdAt) }}</span>
              </div>
            </div>
            
            <div class="time-item" v-if="report.processedAt">
              <div class="time-icon">
                <i class="el-icon-check"></i>
              </div>
              <div class="time-content">
                <span class="time-label">处理时间</span>
                <span class="time-value">{{ formatDate(report.processedAt) }}</span>
              </div>
            </div>
            
            <div class="time-item" v-if="report.processedAt && report.createdAt">
              <div class="time-icon">
                <i class="el-icon-timer"></i>
              </div>
              <div class="time-content">
                <span class="time-label">处理耗时</span>
                <span class="time-value">{{ calculateProcessingTime(report.createdAt, report.processedAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <span slot="footer" class="dialog-footer">
      <button 
        class="glass-btn btn-close" 
        @click="$emit('close')"
      >
        <i class="el-icon-close"></i>
        <span>关闭</span>
      </button>
    </span>
  </el-dialog>
</template>

<script>
import { StyleHelper } from '@/utils/illegalStyleHelper'
import { ContentFormatter } from '@/utils/illegalManager'
import { REPORT_TYPE_LABELS, REPORT_STATUS_LABELS } from '@/utils/illegalConstants'

export default {
  name: 'IllegalResultDialog',
  
  props: {
    visible: Boolean,
    report: Object
  },
  
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    }
  },
  
  mounted() {
    // 阻止滚轮穿透
    this.$nextTick(() => {
      if (this.visible) {
        document.addEventListener('wheel', this.preventScroll, { passive: false })
        document.addEventListener('touchmove', this.preventScroll, { passive: false })
      }
    })
  },
  
  beforeDestroy() {
    // 移除滚轮事件监听
    document.removeEventListener('wheel', this.preventScroll, { passive: false })
    document.removeEventListener('touchmove', this.preventScroll, { passive: false })
  },
  
  watch: {
    visible(newVal) {
      if (newVal) {
        this.$nextTick(() => {
          document.addEventListener('wheel', this.preventScroll, { passive: false })
          document.addEventListener('touchmove', this.preventScroll, { passive: false })
        })
      } else {
        document.removeEventListener('wheel', this.preventScroll, { passive: false })
        document.removeEventListener('touchmove', this.preventScroll, { passive: false })
      }
    }
  },
  
  methods: {
    getTypeBadgeClass(type) {
      return StyleHelper.getTypeBadgeClass(type)
    },
    
    getStatusClass(status) {
      return StyleHelper.getStatusClass(status)
    },
    
    getStatusIconClass(status) {
      const classes = {
        'approved': 'success',
        'rejected': 'rejected',
        'processing': 'processing',
        'pending': 'pending'
      }
      return classes[status] || 'default'
    },
    
    getStatusIcon(status) {
      const icons = {
        'approved': 'el-icon-circle-check',
        'rejected': 'el-icon-circle-close',
        'processing': 'el-icon-loading',
        'pending': 'el-icon-time'
      }
      return icons[status] || 'el-icon-info'
    },
    
    getTypeIcon(type) {
      return StyleHelper.getTypeIcon(type)
    },
    
    getActionIcon(action) {
      const icons = {
        'warning': 'el-icon-warning-outline',
        'tempBan': 'el-icon-time',
        'permBan': 'el-icon-circle-close',
        'removeProduct': 'el-icon-delete'
      }
      return icons[action] || 'el-icon-s-tools'
    },
    
    getActionBadgeClass(action) {
      const classes = {
        'warning': 'action-warning',
        'tempBan': 'action-temp-ban',
        'permBan': 'action-perm-ban',
        'removeProduct': 'action-remove'
      }
      return classes[action] || 'action-default'
    },
    
    getTypeLabel(type) {
      return REPORT_TYPE_LABELS[type] || '未知类型'
    },
    
    getStatusLabel(status) {
      return REPORT_STATUS_LABELS[status] || '未知状态'
    },
    
    formatDate(date) {
      return ContentFormatter.formatDate(date)
    },
    
    calculateProcessingTime(startDate, endDate) {
      if (!startDate || !endDate) return '未知'
      
      const start = new Date(startDate)
      const end = new Date(endDate)
      const diffMs = end - start
      
      const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
      const diffHours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
      const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
      
      if (diffDays > 0) {
        return `${diffDays}天${diffHours}小时`
      } else if (diffHours > 0) {
        return `${diffHours}小时${diffMinutes}分钟`
      } else {
        return `${diffMinutes}分钟`
      }
    },
    
    // 阻止滚轮穿透
    preventScroll(e) {
      const dialogElement = e.target.closest('.result-dialog')
      if (!dialogElement) {
        e.preventDefault()
        e.stopPropagation()
        return false
      }
      
      const scrollableElement = e.target.closest('.el-dialog__body')
      if (scrollableElement) {
        const { scrollTop, scrollHeight, clientHeight } = scrollableElement
        const isScrollable = scrollHeight > clientHeight
        
        if (!isScrollable) {
          e.preventDefault()
          e.stopPropagation()
          return false
        }
        
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
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

// 结果对话框样式
:deep(.result-dialog) {
  .el-dialog {
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(20px);
    border: 1px solid rgba($color-border, 0.2);
    border-radius: 20px;
    box-shadow: 
      0 20px 40px rgba(0, 0, 0, 0.1),
      0 0 0 1px rgba(255, 255, 255, 0.1) inset;
    
    // 防止滚轮穿透
    overscroll-behavior: contain;
  }
  
  .el-dialog__header {
    padding: 24px 32px 16px;
    border-bottom: 1px solid rgba($color-border, 0.1);
    
    .el-dialog__title {
      color: $color-text-primary;
      font-size: 20px;
      font-weight: 600;
    }
  }
  
  .el-dialog__body {
    padding: 24px 32px;
    max-height: 70vh;
    overflow-y: auto;
    overscroll-behavior: contain;
    
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
  
  .el-dialog__footer {
    padding: 16px 32px 24px;
    border-top: 1px solid rgba($color-border, 0.1);
    text-align: center;
  }
}

// 结果内容容器
.result-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// 举报信息卡片
.result-info-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border: 1px solid rgba($color-border, 0.15);
  border-radius: 16px;
  padding: 20px;
  
  .info-header {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .info-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      
      &.success {
        background: rgba($color-secondary-success, 0.15);
        color: $color-secondary-success;
      }
      
      &.rejected {
        background: rgba($color-secondary-danger, 0.15);
        color: $color-secondary-danger;
      }
      
      &.processing {
        background: rgba($color-secondary-info, 0.15);
        color: $color-secondary-info;
      }
      
      &.pending {
        background: rgba($color-secondary-warning, 0.15);
        color: $color-secondary-warning;
      }
    }
    
    .info-content {
      flex: 1;
      
      .result-title {
        margin: 0 0 8px 0;
        color: $color-text-primary;
        font-size: 18px;
        font-weight: 600;
        line-height: 1.4;
      }
      
      .result-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        flex-wrap: wrap;
        
        .meta-tag, .status-badge {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          padding: 6px 12px;
          border-radius: 20px;
          font-size: 12px;
          font-weight: 500;
          
          i {
            font-size: 14px;
          }
        }
        
        .meta-tag {
          background: rgba($color-primary, 0.1);
          color: $color-primary;
          border: 1px solid rgba($color-primary, 0.2);
        }
        
        .status-badge {
          &.status-approved {
            background: rgba($color-secondary-success, 0.1);
            color: $color-secondary-success;
            border: 1px solid rgba($color-secondary-success, 0.2);
          }
          
          &.status-rejected {
            background: rgba($color-secondary-danger, 0.1);
            color: $color-secondary-danger;
            border: 1px solid rgba($color-secondary-danger, 0.2);
          }
          
          &.status-processing {
            background: rgba($color-secondary-info, 0.1);
            color: $color-secondary-info;
            border: 1px solid rgba($color-secondary-info, 0.2);
          }
          
          &.status-pending {
            background: rgba($color-secondary-warning, 0.1);
            color: $color-secondary-warning;
            border: 1px solid rgba($color-secondary-warning, 0.2);
          }
        }
      }
    }
  }
}

// 结果详情区域
.result-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// 结果区段
.result-section {
  .section-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;
    position: relative;
    
    .section-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      
      &.success {
        background: rgba($color-secondary-success, 0.15);
        color: $color-secondary-success;
      }
      
      &.rejected {
        background: rgba($color-secondary-danger, 0.15);
        color: $color-secondary-danger;
      }
    }
    
    .section-title {
      margin: 0;
      color: $color-text-primary;
      font-size: 16px;
      font-weight: 600;
    }
    
    .section-decoration {
      flex: 1;
      height: 2px;
      border-radius: 1px;
      margin-left: 16px;
      
      &.success {
        background: linear-gradient(90deg, 
          rgba($color-secondary-success, 0.3) 0%, 
          transparent 100%);
      }
      
      &.rejected {
        background: linear-gradient(90deg, 
          rgba($color-secondary-danger, 0.3) 0%, 
          transparent 100%);
      }
    }
  }
}

// 通用卡片样式
.action-result-card,
.progress-history-card,
.reject-reason-card,
.time-info-card {
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(8px);
  border: 1px solid rgba($color-border, 0.1);
  border-radius: 14px;
  padding: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    background: rgba(255, 255, 255, 0.04);
    border-color: rgba($color-border, 0.2);
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  }
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    
    .card-icon {
      width: 32px;
      height: 32px;
      border-radius: 8px;
      background: rgba($color-primary, 0.1);
      color: $color-primary;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
    }
    
    .card-title {
      margin: 0;
      color: $color-text-primary;
      font-size: 14px;
      font-weight: 600;
    }
  }
}

// 处理行动结果
.action-summary {
  .action-badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 16px;
    
    &.action-warning {
      background: rgba($color-secondary-warning, 0.15);
      color: $color-secondary-warning;
      border: 1px solid rgba($color-secondary-warning, 0.3);
    }
    
    &.action-temp-ban {
      background: rgba($color-secondary-info, 0.15);
      color: $color-secondary-info;
      border: 1px solid rgba($color-secondary-info, 0.3);
    }
    
    &.action-perm-ban {
      background: rgba($color-secondary-danger, 0.15);
      color: $color-secondary-danger;
      border: 1px solid rgba($color-secondary-danger, 0.3);
    }
    
    &.action-remove {
      background: rgba($color-secondary-danger, 0.15);
      color: $color-secondary-danger;
      border: 1px solid rgba($color-secondary-danger, 0.3);
    }
    
    &.action-default {
      background: rgba($color-primary, 0.15);
      color: $color-primary;
      border: 1px solid rgba($color-primary, 0.3);
    }
    
    i {
      font-size: 16px;
    }
  }
  
  .action-details {
    .action-description {
      margin-bottom: 16px;
      
      .desc-label {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        color: $color-text-secondary;
        font-size: 13px;
        font-weight: 500;
        
        i {
          font-size: 14px;
        }
      }
      
      .desc-content {
        color: $color-text-primary;
        font-size: 14px;
        line-height: 1.6;
        padding: 12px 16px;
        background: rgba($color-bg-surface, 0.3);
        border-radius: 10px;
        border-left: 3px solid $color-primary;
      }
    }
    
    .action-params {
      margin-bottom: 16px;
      
      .param-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 8px 0;
        border-bottom: 1px solid rgba($color-border, 0.1);
        
        &:last-child {
          border-bottom: none;
        }
        
        .param-label {
          color: $color-text-secondary;
          font-size: 13px;
        }
        
        .param-value {
          font-size: 14px;
          font-weight: 600;
          
          &.credit-value {
            color: $color-secondary-warning;
          }
          
          &.ban-value {
            color: $color-secondary-danger;
          }
          
          &.note-value {
            color: $color-text-primary;
            max-width: 200px;
            text-align: right;
            word-break: break-word;
          }
        }
      }
    }
    
    .action-time {
      display: flex;
      align-items: center;
      gap: 8px;
      color: $color-text-secondary;
      font-size: 13px;
      
      i {
        font-size: 14px;
      }
    }
  }
}

// 处理历史时间线
.history-timeline {
  .timeline-item {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .timeline-dot {
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background: rgba($color-primary, 0.2);
      border: 3px solid $color-primary;
      flex-shrink: 0;
      margin-top: 2px;
      position: relative;
      
      .dot-inner {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: $color-primary;
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
      }
      
      &::after {
        content: '';
        position: absolute;
        top: 20px;
        left: 50%;
        transform: translateX(-50%);
        width: 2px;
        height: 30px;
        background: linear-gradient(180deg, 
          rgba($color-primary, 0.3) 0%, 
          transparent 100%);
      }
    }
    
    &:last-child .timeline-dot::after {
      display: none;
    }
    
    .timeline-content {
      flex: 1;
      
      .timeline-time {
        color: $color-text-secondary;
        font-size: 12px;
        margin-bottom: 4px;
      }
      
      .timeline-text {
        color: $color-text-primary;
        font-size: 14px;
        line-height: 1.5;
      }
    }
  }
}

// 驳回理由
.reason-content {
  .reason-text {
    color: $color-text-primary;
    font-size: 14px;
    line-height: 1.6;
    padding: 16px;
    background: rgba($color-secondary-danger, 0.05);
    border: 1px solid rgba($color-secondary-danger, 0.15);
    border-radius: 10px;
    border-left: 4px solid $color-secondary-danger;
  }
}

// 时间信息网格
.time-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  
  .time-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: rgba($color-bg-surface, 0.2);
    border-radius: 10px;
    border: 1px solid rgba($color-border, 0.1);
    transition: all 0.3s ease;
    
    &:hover {
      background: rgba($color-bg-surface, 0.3);
      border-color: rgba($color-border, 0.2);
    }
    
    .time-icon {
      width: 32px;
      height: 32px;
      border-radius: 8px;
      background: rgba($color-primary, 0.1);
      color: $color-primary;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
    }
    
    .time-content {
      flex: 1;
      
      .time-label {
        display: block;
        color: $color-text-secondary;
        font-size: 12px;
        margin-bottom: 2px;
      }
      
      .time-value {
        display: block;
        color: $color-text-primary;
        font-size: 14px;
        font-weight: 500;
      }
    }
  }
}

// 底部按钮
.dialog-footer {
  .glass-btn {
    padding: 12px 32px;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    border: none;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    display: inline-flex;
    align-items: center;
    gap: 8px;
    
    &.btn-close {
      background: rgba($color-text-secondary, 0.1);
      border: 1px solid rgba($color-border, 0.3);
      color: $color-text-secondary;
      
      &:hover {
        background: rgba($color-text-secondary, 0.15);
        border-color: rgba($color-border, 0.5);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
    }
    
    i {
      font-size: 16px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  :deep(.result-dialog) {
    .el-dialog {
      width: 95% !important;
      margin: 0 auto;
    }
    
    .el-dialog__header,
    .el-dialog__body,
    .el-dialog__footer {
      padding-left: 20px;
      padding-right: 20px;
    }
  }
  
  .result-info-card {
    padding: 16px;
    
    .info-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
      
      .info-icon {
        align-self: center;
      }
      
      .info-content {
        text-align: center;
        
        .result-meta {
          justify-content: center;
        }
      }
    }
  }
  
  .action-result-card,
  .progress-history-card,
  .reject-reason-card,
  .time-info-card {
    padding: 16px;
  }
  
  .time-grid {
    grid-template-columns: 1fr;
  }
  
  .action-params .param-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
    
    .param-value.note-value {
      max-width: none;
      text-align: left;
    }
  }
}

@media (max-width: 480px) {
  .result-section .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    
    .section-decoration {
      width: 100%;
      margin-left: 0;
    }
  }
  
  .action-badge {
    width: 100%;
    justify-content: center;
  }
  
  .timeline-item {
    gap: 12px;
    
    .timeline-dot {
      width: 16px;
      height: 16px;
      
      .dot-inner {
        width: 4px;
        height: 4px;
      }
      
      &::after {
        height: 24px;
      }
    }
  }
}
</style>