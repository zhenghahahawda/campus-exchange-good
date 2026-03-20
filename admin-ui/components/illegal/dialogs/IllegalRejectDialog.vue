<template>
  <el-dialog
    title="驳回举报"
    :visible.sync="dialogVisible"
    width="520px"
    custom-class="glass-dialog reject-dialog"
    :modal="false"
    :close-on-click-modal="false"
    append-to-body
    center
    @open="$emit('dialog-open')"
    @close="$emit('dialog-close')"
  >
    <div v-if="report" class="reject-content">
      <!-- 举报信息卡片 -->
      <div class="report-info-card">
        <div class="report-header">
          <div class="report-icon">
            <i class="el-icon-warning-outline"></i>
          </div>
          <div class="report-details">
            <h4 class="report-title">商品信息虚假</h4>
            <div class="report-meta">
              <span class="meta-tag">商品-虚假信息</span>
              <span class="meta-time">2024/03/08 14:30</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 驳回理由输入区域 -->
      <div class="reject-reason-section">
        <div class="section-header">
          <div class="section-icon">
            <i class="el-icon-edit"></i>
          </div>
          <h5 class="section-title">驳回理由</h5>
        </div>
        
        <div class="textarea-container">
          <el-input
            type="textarea"
            :value="rejectReason"
            @input="$emit('update:reject-reason', $event)"
            placeholder="请输入驳回理由，说明为什么此举报不成立或不符合处理条件..."
            :rows="6"
            maxlength="300"
            show-word-limit
            class="reject-textarea"
            resize="none"
          />
          <div class="textarea-decoration">
            <div class="decoration-line"></div>
          </div>
        </div>
      </div>
    </div>

    <span slot="footer" class="dialog-footer">
      <button 
        class="glass-btn btn-cancel" 
        @click="$emit('close')"
      >
        <i class="el-icon-close"></i>
        <span>取消</span>
      </button>
      <button 
        class="glass-btn btn-confirm-reject" 
        @click="$emit('confirm-reject')"
        :disabled="!rejectReason || !rejectReason.trim()"
      >
        <i class="el-icon-check"></i>
        <span>确认驳回</span>
      </button>
    </span>
  </el-dialog>
</template>

<script>
import { ContentFormatter } from '@/utils/illegalManager'
import { REPORT_TYPE_LABELS } from '@/utils/illegalConstants'

export default {
  name: 'IllegalRejectDialog',
  
  props: {
    visible: Boolean,
    report: Object,
    rejectReason: String
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
    getTypeLabel(type) {
      return REPORT_TYPE_LABELS[type] || '未知类型'
    },
    
    formatDate(date) {
      return ContentFormatter.formatDate(date)
    },
    
    // 阻止滚轮穿透
    preventScroll(e) {
      const dialogElement = e.target.closest('.glass-dialog')
      if (!dialogElement) {
        e.preventDefault()
        e.stopPropagation()
        return false
      }
      
      const scrollableElement = e.target.closest('.el-dialog__body, .reject-textarea')
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

.reject-content {
  padding: 0;
  
  .report-info-card {
    background: rgba($color-primary, 0.03);
    border: 1px solid rgba($color-primary, 0.1);
    border-radius: 16px;
    padding: 20px;
    margin-bottom: 28px;
    position: relative;
    overflow: hidden;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, 
        rgba(var(--color-primary-rgb), 0.8), 
        rgba(var(--color-primary-rgb), 0.4),
        rgba(var(--color-primary-rgb), 0.8));
    }
    
    .report-header {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      
      .report-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        background: linear-gradient(135deg, 
          rgba(var(--color-secondary-warning-rgb), 0.15), 
          rgba(var(--color-secondary-warning-rgb), 0.08));
        border: 1px solid rgba($color-secondary-warning, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        
        i {
          font-size: 24px;
          color: $color-secondary-warning;
        }
      }
      
      .report-details {
        flex: 1;
        
        .report-title {
          font-size: 18px;
          font-weight: 700;
          color: $color-text-primary;
          margin: 0 0 12px 0;
          line-height: 1.3;
        }
        
        .report-meta {
          display: flex;
          align-items: center;
          gap: 16px;
          flex-wrap: wrap;
          
          .meta-tag {
            display: inline-flex;
            align-items: center;
            padding: 6px 14px;
            background: linear-gradient(135deg, 
              rgba(var(--color-secondary-danger-rgb), 0.15), 
              rgba(var(--color-secondary-danger-rgb), 0.08));
            color: $color-secondary-danger;
            border: 1px solid rgba($color-secondary-danger, 0.25);
            border-radius: 20px;
            font-size: 13px;
            font-weight: 600;
            letter-spacing: 0.5px;
          }
          
          .meta-time {
            color: $color-text-secondary;
            font-size: 14px;
            font-weight: 500;
            display: flex;
            align-items: center;
            
            &::before {
              content: '•';
              margin-right: 8px;
              color: rgba($color-text-secondary, 0.5);
            }
          }
        }
      }
    }
  }
  
  .reject-reason-section {
    .section-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;
      
      .section-icon {
        width: 36px;
        height: 36px;
        border-radius: 10px;
        background: linear-gradient(135deg, 
          rgba(var(--color-primary-rgb), 0.15), 
          rgba(var(--color-primary-rgb), 0.08));
        border: 1px solid rgba($color-primary, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        
        i {
          font-size: 18px;
          color: $color-primary;
        }
      }
      
      .section-title {
        font-size: 16px;
        font-weight: 700;
        color: $color-text-primary;
        margin: 0;
        letter-spacing: 0.5px;
      }
    }
    
    .textarea-container {
      position: relative;
      
      .textarea-decoration {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        pointer-events: none;
        border-radius: 12px;
        
        .decoration-line {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 2px;
          background: linear-gradient(90deg, 
            transparent, 
            rgba(var(--color-primary-rgb), 0.3), 
            transparent);
          border-radius: 2px;
          opacity: 0;
          transition: opacity 0.3s ease;
        }
      }
      
      &:focus-within .decoration-line {
        opacity: 1;
      }
    }
  }
}

// 自定义文本域样式
::v-deep .reject-textarea {
  .el-textarea__inner {
    background: rgba($color-bg-surface, 0.6);
    backdrop-filter: blur(10px);
    border: 2px solid rgba($color-border, 0.2);
    border-radius: 12px;
    color: $color-text-primary;
    font-size: 14px;
    line-height: 1.6;
    padding: 16px 20px;
    resize: none;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    min-height: 140px !important;
    
    &::placeholder {
      color: $color-text-disabled;
      font-style: italic;
    }
    
    &:focus {
      border-color: rgba($color-primary, 0.5);
      background: rgba($color-bg-surface, 0.8);
      box-shadow: 
        0 0 0 4px rgba(var(--color-primary-rgb), 0.1),
        0 8px 25px rgba(var(--color-primary-rgb), 0.15);
      transform: translateY(-2px);
    }
    
    &:hover:not(:focus) {
      border-color: rgba($color-border, 0.4);
      background: rgba($color-bg-surface, 0.7);
    }
  }
  
  .el-input__count {
    background: transparent !important;
    color: $color-text-disabled;
    font-size: 12px;
    right: 16px;
    bottom: 12px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 0;
  margin-top: 8px;
  
  .glass-btn {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 32px;
    border: none;
    border-radius: 14px;
    font-size: 15px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    backdrop-filter: blur(10px);
    white-space: nowrap;
    min-width: 140px;
    justify-content: center;
    letter-spacing: 0.5px;
    position: relative;
    overflow: hidden;
    
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
      transition: left 0.5s ease;
    }
    
    &:hover::before {
      left: 100%;
    }
    
    i {
      font-size: 18px;
      transition: transform 0.3s ease;
    }
    
    &:hover i {
      transform: scale(1.1);
    }
    
    &.btn-cancel {
      background: linear-gradient(135deg, 
        rgba($color-text-secondary, 0.08), 
        rgba($color-text-secondary, 0.05));
      color: $color-text-secondary;
      border: 2px solid rgba($color-text-secondary, 0.15);
      
      &:hover {
        background: linear-gradient(135deg, 
          rgba($color-text-secondary, 0.15), 
          rgba($color-text-secondary, 0.08));
        color: $color-text-primary;
        border-color: rgba($color-text-secondary, 0.25);
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba($color-text-secondary, 0.2);
      }
      
      &:active {
        transform: translateY(-1px);
      }
    }
    
    &.btn-confirm-reject {
      background: linear-gradient(135deg, 
        rgba(var(--color-secondary-danger-rgb), 0.95), 
        rgba(var(--color-secondary-danger-rgb), 0.85));
      color: white;
      border: 2px solid rgba($color-secondary-danger, 0.8);
      box-shadow: 0 4px 15px rgba(var(--color-secondary-danger-rgb), 0.3);
      
      &:hover:not(:disabled) {
        background: linear-gradient(135deg, 
          $color-secondary-danger, 
          rgba(var(--color-secondary-danger-rgb), 0.9));
        border-color: $color-secondary-danger;
        transform: translateY(-3px);
        box-shadow: 0 8px 30px rgba(var(--color-secondary-danger-rgb), 0.4);
      }
      
      &:active:not(:disabled) {
        transform: translateY(-1px);
      }
      
      &:disabled {
        background: linear-gradient(135deg, 
          rgba($color-text-disabled, 0.15), 
          rgba($color-text-disabled, 0.08));
        color: $color-text-disabled;
        border-color: rgba($color-text-disabled, 0.1);
        cursor: not-allowed;
        box-shadow: none;
        
        &:hover {
          transform: none;
          box-shadow: none;
        }
        
        &::before {
          display: none;
        }
      }
    }
  }
}

// 对话框特定样式
::v-deep .glass-dialog.reject-dialog {
  .el-dialog__header {
    padding: 28px 32px 24px;
    background: linear-gradient(135deg, 
      rgba(var(--color-primary-rgb), 0.03), 
      rgba(var(--color-primary-rgb), 0.01));
    border-bottom: 1px solid rgba($color-border, 0.15);
    
    .el-dialog__title {
      font-size: 20px;
      font-weight: 800;
      color: $color-text-primary;
      letter-spacing: 0.5px;
    }
    
    .el-dialog__headerbtn {
      top: 28px;
      right: 28px;
      width: 40px;
      height: 40px;
      border-radius: 10px;
      background: rgba($color-text-secondary, 0.05);
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba($color-text-secondary, 0.1);
        transform: scale(1.05);
      }
      
      .el-dialog__close {
        font-size: 22px;
        font-weight: 700;
        color: $color-text-secondary;
        
        &:hover {
          color: $color-primary;
        }
      }
    }
  }
  
  .el-dialog__body {
    padding: 28px 32px 24px;
    background: rgba($color-bg-surface, 0.02);
  }
  
  .el-dialog__footer {
    padding: 24px 32px 32px;
    background: linear-gradient(135deg, 
      rgba(var(--color-primary-rgb), 0.01), 
      rgba(var(--color-primary-rgb), 0.03));
    border-top: 1px solid rgba($color-border, 0.15);
  }
}

// 响应式适配
@media (max-width: 768px) {
  ::v-deep .glass-dialog.reject-dialog {
    width: 95vw !important;
    margin: 0 !important;
    
    .el-dialog__header,
    .el-dialog__body,
    .el-dialog__footer {
      padding-left: 20px;
      padding-right: 20px;
    }
  }
  
  .reject-content {
    .report-info-card {
      padding: 16px;
      
      .report-header {
        gap: 12px;
        
        .report-icon {
          width: 40px;
          height: 40px;
          
          i {
            font-size: 20px;
          }
        }
        
        .report-details .report-title {
          font-size: 16px;
        }
      }
    }
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 12px;
    
    .glass-btn {
      width: 100%;
      min-width: auto;
    }
  }
}
</style>