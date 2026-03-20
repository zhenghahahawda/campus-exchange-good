<template>
  <div class="report-card glass-panel">
    <!-- 举报卡片头部 -->
    <div class="report-header">
      <div class="report-type-badge" :class="getTypeBadgeClass(report.type)">
        <i :class="getTypeIcon(report.type)"></i>
        <span>{{ getTypeLabel(report.type) }}</span>
      </div>
      <div class="report-status" :class="getStatusClass(report.status)">
        {{ getStatusLabel(report.status) }}
      </div>
    </div>

    <!-- 举报内容 -->
    <div class="report-content">
      <h4 class="report-title">{{ report.title }}</h4>
      <p class="report-description">{{ report.description }}</p>
      
      <div class="report-meta">
        <div class="meta-item">
          <i class="el-icon-user"></i>
          <span>举报人：{{ report.reporterName }}</span>
        </div>
        <div class="meta-item" v-if="report.targetType === 'product'">
          <i class="el-icon-goods"></i>
          <span>商品编号：{{ report.targetId }}</span>
        </div>
        <div class="meta-item" v-if="report.targetType === 'product' && report.targetName">
          <i class="el-icon-shopping-bag-1"></i>
          <span>商品名称：{{ report.targetName }}</span>
        </div>
        <div class="meta-item" v-if="report.targetType !== 'product'">
          <i class="el-icon-user-solid"></i>
          <span>被举报用户：{{ report.targetName }}</span>
        </div>
        <div class="meta-item">
          <i class="el-icon-time"></i>
          <span>{{ formatDate(report.createdAt) }}</span>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="report-actions">
      <button 
        class="glass-btn btn-view" 
        @click="$emit('view-detail', report)"
      >
        <i class="el-icon-view"></i>
        <span>查看详情</span>
      </button>
      
      <button 
        v-if="report.status === 'pending'"
        class="glass-btn btn-approve" 
        @click="$emit('show-action-dialog', report)"
      >
        <i class="el-icon-s-tools"></i>
        <span>选择处理行动</span>
      </button>
      
      <button 
        v-if="report.status === 'processing'"
        class="glass-btn btn-progress" 
        @click="$emit('show-progress', report)"
      >
        <i class="el-icon-edit"></i>
        <span>处理进度</span>
      </button>
      
      <button 
        v-if="report.status === 'processing'"
        class="glass-btn btn-complete" 
        @click="$emit('complete-processing', report)"
      >
        <i class="el-icon-check"></i>
        <span>结束处理</span>
      </button>
      
      <button 
        v-if="report.status === 'approved' || report.status === 'rejected'"
        class="glass-btn btn-result" 
        @click="$emit('show-result', report)"
      >
        <i class="el-icon-document"></i>
        <span>处理结果</span>
      </button>
      
      <button 
        v-if="report.status === 'pending'"
        class="glass-btn btn-reject" 
        @click="$emit('show-reject-dialog', report)"
      >
        <i class="el-icon-close"></i>
        <span>驳回</span>
      </button>
      
      <button 
        v-if="report.status === 'pending'"
        class="glass-btn btn-processing" 
        @click="$emit('set-processing', report)"
      >
        <i class="el-icon-loading"></i>
        <span>转处理</span>
      </button>
    </div>
  </div>
</template>

<script>
import { StyleHelper } from '@/utils/illegalStyleHelper'
import { ContentFormatter } from '@/utils/illegalManager'
import { REPORT_TYPE_LABELS, REPORT_STATUS_LABELS } from '@/utils/illegalConstants'

export default {
  name: 'IllegalReportCard',
  
  props: {
    report: {
      type: Object,
      required: true
    }
  },
  
  methods: {
    getTypeBadgeClass(type) {
      return StyleHelper.getTypeBadgeClass(type)
    },
    
    getStatusClass(status) {
      return StyleHelper.getStatusClass(status)
    },
    
    getTypeIcon(type) {
      return StyleHelper.getTypeIcon(type)
    },
    
    getTypeLabel(type) {
      return REPORT_TYPE_LABELS[type] || '未知类型'
    },
    
    getStatusLabel(status) {
      return REPORT_STATUS_LABELS[status] || '未知状态'
    },
    
    formatDate(date) {
      return ContentFormatter.formatDate(date)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.report-card {
  @include glass-card;
  padding: 24px;
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  height: 100%;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  }
  
  .report-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .report-type-badge {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      
      // 商品举报样式
      &.type-product-fake {
        background: rgba($color-secondary-warning-rgb, 0.15);
        color: $color-secondary-warning;
      }
      
      &.type-product-prohibited {
        background: rgba($color-secondary-danger-rgb, 0.15);
        color: $color-secondary-danger;
      }
      
      &.type-product-copyright {
        background: rgba(156, 39, 176, 0.15);
        color: #9c27b0;
      }
      
      &.type-product-price {
        background: rgba(255, 152, 0, 0.15);
        color: #ff9800;
      }
      
      &.type-product-quality {
        background: rgba(244, 67, 54, 0.15);
        color: #f44336;
      }
      
      &.type-product-description {
        background: rgba($color-secondary-info-rgb, 0.15);
        color: $color-secondary-info;
      }
      
      // 用户举报样式
      &.type-user-harassment {
        background: rgba($color-secondary-danger-rgb, 0.15);
        color: $color-secondary-danger;
      }
      
      &.type-user-fraud {
        background: rgba(220, 38, 127, 0.15);
        color: #dc267f;
      }
      
      &.type-user-spam {
        background: rgba($color-secondary-warning-rgb, 0.15);
        color: $color-secondary-warning;
      }
      
      &.type-user-impersonation {
        background: rgba(103, 58, 183, 0.15);
        color: #673ab7;
      }
      
      &.type-user-inappropriate {
        background: rgba(233, 30, 99, 0.15);
        color: #e91e63;
      }
      
      &.type-user-transaction {
        background: rgba($color-secondary-info-rgb, 0.15);
        color: $color-secondary-info;
      }
      
      &.type-other {
        background: rgba($color-text-secondary, 0.15);
        color: $color-text-secondary;
      }
    }
    
    .report-status {
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 600;
      
      &.status-pending {
        background: rgba($color-secondary-warning-rgb, 0.15);
        color: $color-secondary-warning;
      }
      
      &.status-approved {
        background: rgba($color-secondary-success-rgb, 0.15);
        color: $color-secondary-success;
      }
      
      &.status-rejected {
        background: rgba($color-secondary-danger-rgb, 0.15);
        color: $color-secondary-danger;
      }
      
      &.status-processing {
        background: rgba($color-secondary-info-rgb, 0.15);
        color: $color-secondary-info;
      }
    }
  }
  
  .report-content {
    margin-bottom: 20px;
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .report-title {
      font-size: 16px;
      font-weight: 600;
      color: $color-text-primary;
      margin-bottom: 8px;
      line-height: 1.4;
    }
    
    .report-description {
      color: $color-text-secondary;
      font-size: 14px;
      line-height: 1.5;
      margin-bottom: 16px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .report-meta {
      display: flex;
      flex-direction: column;
      gap: 6px;
      margin-top: auto;
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: $color-text-secondary;
        
        i {
          font-size: 14px;
          opacity: 0.7;
        }
      }
    }
  }
  
  .report-actions {
    display: flex;
    gap: 8px;
    flex-wrap: nowrap;
    justify-content: flex-start;
    align-items: center;
    margin-top: auto;
    flex-shrink: 0;
  }
}

// 按钮样式
.glass-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: none;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  white-space: nowrap;
  flex-shrink: 0;
  
  &.btn-view {
    background: rgba($color-secondary-info-rgb, 0.15);
    color: $color-secondary-info;
    border: 1px solid rgba($color-secondary-info-rgb, 0.3);
    
    &:hover {
      background: rgba($color-secondary-info-rgb, 0.25);
      transform: translateY(-1px);
    }
  }
  
  &.btn-approve {
    background: rgba($color-secondary-success-rgb, 0.15);
    color: $color-secondary-success;
    border: 1px solid rgba($color-secondary-success-rgb, 0.3);
    
    &:hover {
      background: rgba($color-secondary-success-rgb, 0.25);
      transform: translateY(-1px);
    }
  }
  
  &.btn-reject {
    background: rgba($color-secondary-danger-rgb, 0.15);
    color: $color-secondary-danger;
    border: 1px solid rgba($color-secondary-danger-rgb, 0.3);
    
    &:hover {
      background: rgba($color-secondary-danger-rgb, 0.25);
      transform: translateY(-1px);
    }
  }
  
  &.btn-processing {
    background: rgba($color-secondary-info-rgb, 0.15);
    color: $color-secondary-info;
    border: 1px solid rgba($color-secondary-info-rgb, 0.3);
    
    &:hover {
      background: rgba($color-secondary-info-rgb, 0.25);
      transform: translateY(-1px);
    }
    
    i {
      animation: rotate 1s linear infinite;
    }
  }
  
  &.btn-progress {
    background: rgba($color-secondary-warning-rgb, 0.15);
    color: $color-secondary-warning;
    border: 1px solid rgba($color-secondary-warning-rgb, 0.3);
    
    &:hover {
      background: rgba($color-secondary-warning-rgb, 0.25);
      transform: translateY(-1px);
    }
  }
  
  &.btn-complete {
    background: rgba($color-secondary-success-rgb, 0.15);
    color: $color-secondary-success;
    border: 1px solid rgba($color-secondary-success-rgb, 0.3);
    
    &:hover {
      background: rgba($color-secondary-success-rgb, 0.25);
      transform: translateY(-1px);
    }
  }
  
  &.btn-result {
    background: rgba($color-text-secondary, 0.1);
    color: $color-text-secondary;
    border: 1px solid rgba($color-border, 0.3);
    
    &:hover {
      background: rgba($color-text-secondary, 0.15);
      color: $color-text-primary;
      transform: translateY(-1px);
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>