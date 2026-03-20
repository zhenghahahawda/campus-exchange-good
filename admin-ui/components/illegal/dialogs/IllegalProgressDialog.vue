<template>
  <el-dialog
    title="处理进度"
    :visible.sync="dialogVisible"
    width="750px"
    custom-class="glass-dialog progress-dialog"
    :modal="false"
    :close-on-click-modal="false"
    append-to-body
    center
    top="5vh"
    @open="$emit('dialog-open')"
    @close="$emit('dialog-close')"
  >
    <div v-if="report" class="progress-content">
      <!-- 举报基本信息卡片 -->
      <div class="progress-info-card">
        <div class="info-header">
          <div class="info-icon">
            <i class="el-icon-document"></i>
          </div>
          <div class="info-content">
            <h4 class="progress-title">{{ report.title }}</h4>
            <div class="progress-meta">
              <span class="meta-tag" :class="getTypeBadgeClass(report.type)">
                <i :class="getTypeIcon(report.type)"></i>
                {{ getTypeLabel(report.type) }}
              </span>
              <span class="meta-time">
                <i class="el-icon-time"></i>
                {{ formatDate(report.createdAt) }}
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 处理进度输入区域 -->
      <div class="progress-input-section">
        <div class="section-header">
          <div class="section-icon">
            <i class="el-icon-edit"></i>
          </div>
          <h5 class="section-label">处理进度更新</h5>
          <div class="section-decoration"></div>
        </div>
        
        <div class="input-wrapper">
          <el-input
            type="textarea"
            :value="progressText"
            @input="$emit('update:progress-text', $event)"
            placeholder="请输入当前处理进度、遇到的问题或需要的协助..."
            :rows="6"
            maxlength="500"
            show-word-limit
            class="progress-textarea"
          />
        </div>
      </div>
      
      <!-- 历史处理记录 -->
      <div class="progress-history" v-if="report.progressHistory && report.progressHistory.length > 0">
        <div class="section-header">
          <div class="section-icon">
            <i class="el-icon-time"></i>
          </div>
          <h5 class="section-label">处理历史</h5>
          <div class="section-decoration"></div>
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

    <span slot="footer" class="dialog-footer">
      <button 
        class="glass-btn btn-cancel" 
        @click="$emit('close')"
      >
        <i class="el-icon-close"></i>
        <span>取消</span>
      </button>
      <button 
        class="glass-btn btn-save" 
        @click="$emit('save-progress')"
        :disabled="!progressText || !progressText.trim()"
      >
        <i class="el-icon-check"></i>
        <span>保存进度</span>
      </button>
    </span>
  </el-dialog>
</template>

<script>
import { ContentFormatter } from '@/utils/illegalManager'
import { StyleHelper } from '@/utils/illegalStyleHelper'
import { REPORT_TYPE_LABELS } from '@/utils/illegalConstants'

export default {
  name: 'IllegalProgressDialog',
  
  props: {
    visible: Boolean,
    report: Object,
    progressText: String
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
    
    getTypeBadgeClass(type) {
      return StyleHelper.getTypeBadgeClass(type)
    },
    
    getTypeIcon(type) {
      return StyleHelper.getTypeIcon(type)
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
      
      const scrollableElement = e.target.closest('.el-dialog__body, .progress-textarea, .textarea-container')
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

.progress-content {
  padding: 0;
  
  // 举报信息卡片
  .progress-info-card {
    background: linear-gradient(135deg, 
      rgba($color-bg-surface, 0.8), 
      rgba($color-bg-surface, 0.6));
    backdrop-filter: blur(20px);
    border: 2px solid rgba($color-border, 0.25);
    border-radius: 16px;
    padding: 20px;
    margin-bottom: 24px;
    position: relative;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, 
        rgba(var(--color-primary-rgb), 0.9), 
        rgba(var(--color-primary-rgb), 0.6),
        rgba(var(--color-primary-rgb), 0.9));
      border-radius: 3px 3px 0 0;
    }
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
      border-color: rgba(var(--color-primary-rgb), 0.4);
    }
    
    .info-header {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      
      .info-icon {
        width: 48px;
        height: 48px;
        border-radius: 14px;
        background: linear-gradient(135deg, 
          rgba(var(--color-primary-rgb), 0.2), 
          rgba(var(--color-primary-rgb), 0.1));
        border: 2px solid rgba(var(--color-primary-rgb), 0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        
        i {
          font-size: 22px;
          color: $color-primary;
        }
      }
      
      .info-content {
        flex: 1;
        
        .progress-title {
          font-size: 18px;
          font-weight: 700;
          color: $color-text-primary;
          margin: 0 0 12px 0;
          line-height: 1.4;
          letter-spacing: 0.5px;
        }
        
        .progress-meta {
          display: flex;
          align-items: center;
          gap: 16px;
          flex-wrap: wrap;
          
          .meta-tag {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 700;
            letter-spacing: 0.5px;
            
            // 动态样式会通过getTypeBadgeClass应用
            background: rgba(var(--color-secondary-warning-rgb), 0.15);
            color: $color-secondary-warning;
            border: 1px solid rgba(var(--color-secondary-warning-rgb), 0.3);
            
            i {
              font-size: 12px;
            }
          }
          
          .meta-time {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            color: $color-text-secondary;
            font-weight: 600;
            
            i {
              font-size: 14px;
              opacity: 0.8;
            }
          }
        }
      }
    }
  }
  
  // 区域标题样式
  .section-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    
    .section-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: linear-gradient(135deg, 
        rgba(var(--color-secondary-info-rgb), 0.15), 
        rgba(var(--color-secondary-info-rgb), 0.08));
      border: 1px solid rgba(var(--color-secondary-info-rgb), 0.25);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      i {
        font-size: 18px;
        color: $color-secondary-info;
      }
    }
    
    .section-label {
      font-size: 16px;
      font-weight: 700;
      color: $color-text-primary;
      margin: 0;
      letter-spacing: 0.5px;
    }
    
    .section-decoration {
      flex: 1;
      height: 2px;
      background: linear-gradient(90deg, 
        rgba(var(--color-secondary-info-rgb), 0.3), 
        transparent);
      border-radius: 2px;
      margin-left: 8px;
    }
  }
  
  // 输入区域
  .progress-input-section {
    margin-bottom: 24px;
    
    .input-wrapper {
      background: linear-gradient(135deg, 
        rgba($color-bg-surface, 0.6), 
        rgba($color-bg-surface, 0.4));
      backdrop-filter: blur(15px);
      border: 2px solid rgba($color-border, 0.2);
      border-radius: 14px;
      padding: 16px;
      position: relative;
      overflow: hidden;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 2px;
        background: linear-gradient(90deg, 
          rgba(var(--color-secondary-info-rgb), 0.8), 
          rgba(var(--color-secondary-info-rgb), 0.4),
          rgba(var(--color-secondary-info-rgb), 0.8));
      }
      
      ::v-deep .progress-textarea {
        .el-textarea__inner {
          background: rgba(255, 255, 255, 0.05);
          backdrop-filter: blur(10px);
          border: 2px solid rgba($color-border, 0.2);
          border-radius: 12px;
          color: $color-text-primary;
          font-size: 14px;
          line-height: 1.6;
          padding: 16px;
          resize: vertical;
          min-height: 120px;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          
          &:hover {
            background: rgba(255, 255, 255, 0.08);
            border-color: rgba(var(--color-primary-rgb), 0.3);
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          }
          
          &:focus {
            background: rgba(255, 255, 255, 0.1);
            border-color: $color-primary;
            box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.15);
            outline: none;
            transform: translateY(-2px);
          }
          
          &::placeholder {
            color: $color-text-disabled;
            font-style: italic;
          }
        }
        
        .el-input__count {
          background: transparent !important;
          color: $color-text-disabled;
          font-size: 12px;
          font-weight: 600;
          right: 12px;
          bottom: 8px;
        }
      }
    }
  }
  
  // 历史记录时间线
  .progress-history {
    .history-timeline {
      position: relative;
      padding-left: 24px;
      
      &::before {
        content: '';
        position: absolute;
        left: 8px;
        top: 0;
        bottom: 0;
        width: 2px;
        background: linear-gradient(180deg, 
          rgba(var(--color-secondary-success-rgb), 0.6), 
          rgba(var(--color-secondary-success-rgb), 0.3),
          rgba(var(--color-secondary-success-rgb), 0.6));
        border-radius: 2px;
      }
      
      .timeline-item {
        position: relative;
        padding-bottom: 20px;
        
        &:last-child {
          padding-bottom: 0;
        }
        
        .timeline-dot {
          position: absolute;
          left: -20px;
          top: 6px;
          width: 16px;
          height: 16px;
          border-radius: 50%;
          background: linear-gradient(135deg, 
            rgba(var(--color-secondary-success-rgb), 0.9), 
            rgba(var(--color-secondary-success-rgb), 0.7));
          border: 2px solid rgba(255, 255, 255, 0.8);
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 2px 8px rgba(var(--color-secondary-success-rgb), 0.3);
          
          .dot-inner {
            width: 6px;
            height: 6px;
            border-radius: 50%;
            background: white;
          }
        }
        
        .timeline-content {
          background: linear-gradient(135deg, 
            rgba($color-bg-surface, 0.7), 
            rgba($color-bg-surface, 0.5));
          backdrop-filter: blur(12px);
          border: 1px solid rgba($color-border, 0.2);
          border-radius: 12px;
          padding: 14px 16px;
          position: relative;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          
          &::before {
            content: '';
            position: absolute;
            left: -8px;
            top: 12px;
            width: 0;
            height: 0;
            border-top: 6px solid transparent;
            border-bottom: 6px solid transparent;
            border-right: 8px solid rgba($color-bg-surface, 0.7);
          }
          
          &:hover {
            transform: translateX(4px);
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
            border-color: rgba(var(--color-secondary-success-rgb), 0.3);
          }
          
          .timeline-time {
            font-size: 12px;
            color: $color-text-secondary;
            font-weight: 600;
            margin-bottom: 6px;
            display: flex;
            align-items: center;
            gap: 4px;
            
            &::before {
              content: '🕒';
              font-size: 12px;
            }
          }
          
          .timeline-text {
            font-size: 14px;
            color: $color-text-primary;
            line-height: 1.5;
            font-weight: 500;
          }
        }
      }
    }
  }
}

// 底部按钮样式
.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 0;
  
  .glass-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 14px 28px;
    border: none;
    border-radius: 14px;
    font-size: 15px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    backdrop-filter: blur(15px);
    white-space: nowrap;
    min-width: 120px;
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
      transition: left 0.6s ease;
    }
    
    &:hover:not(:disabled)::before {
      left: 100%;
    }
    
    i {
      font-size: 16px;
      transition: transform 0.3s ease;
    }
    
    &:hover:not(:disabled) i {
      transform: scale(1.1);
    }
    
    &.btn-cancel {
      background: linear-gradient(135deg, 
        rgba($color-text-secondary, 0.15), 
        rgba($color-text-secondary, 0.08));
      color: $color-text-secondary;
      border: 2px solid rgba($color-text-secondary, 0.25);
      
      &:hover {
        background: linear-gradient(135deg, 
          rgba($color-text-secondary, 0.25), 
          rgba($color-text-secondary, 0.15));
        border-color: rgba($color-text-secondary, 0.4);
        color: $color-text-primary;
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba($color-text-secondary, 0.2);
      }
    }
    
    &.btn-save {
      background: linear-gradient(135deg, 
        rgba(var(--color-primary-rgb), 0.95), 
        rgba(var(--color-primary-rgb), 0.85));
      color: white;
      border: 2px solid rgba(var(--color-primary-rgb), 0.8);
      box-shadow: 0 4px 15px rgba(var(--color-primary-rgb), 0.3);
      
      &:hover:not(:disabled) {
        background: linear-gradient(135deg, 
          $color-primary, 
          rgba(var(--color-primary-rgb), 0.9));
        border-color: $color-primary;
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba(var(--color-primary-rgb), 0.4);
      }
      
      &:disabled {
        background: linear-gradient(135deg, 
          rgba($color-text-disabled, 0.3), 
          rgba($color-text-disabled, 0.2));
        color: rgba(255, 255, 255, 0.6);
        border-color: rgba($color-text-disabled, 0.3);
        cursor: not-allowed;
        transform: none;
        box-shadow: none;
        
        &::before {
          display: none;
        }
      }
    }
  }
}

// 对话框特定样式
::v-deep .glass-dialog.progress-dialog {
  .el-dialog__header {
    background: linear-gradient(135deg, 
      rgba(var(--color-secondary-info-rgb), 0.08), 
      rgba(var(--color-secondary-info-rgb), 0.03));
    border-bottom: 1px solid rgba($color-border, 0.15);
    
    .el-dialog__title {
      color: $color-text-primary;
      font-weight: 800;
      letter-spacing: 0.5px;
      
      &::before {
        content: '📝';
        margin-right: 8px;
        font-size: 18px;
      }
    }
  }
  
  .el-dialog__body {
    background: rgba($color-bg-surface, 0.02);
  }
  
  .el-dialog__footer {
    background: linear-gradient(135deg, 
      rgba(var(--color-primary-rgb), 0.03), 
      rgba(var(--color-primary-rgb), 0.06));
    border-top: 1px solid rgba($color-border, 0.15);
  }
}

// 响应式适配
@media (max-width: 768px) {
  ::v-deep .glass-dialog.progress-dialog {
    width: 95vw !important;
    margin: 0 !important;
  }
  
  .progress-content {
    .progress-info-card .info-header {
      flex-direction: column;
      text-align: center;
      gap: 12px;
      
      .info-content .progress-meta {
        justify-content: center;
      }
    }
    
    .section-header {
      .section-label {
        font-size: 14px;
      }
    }
    
    .history-timeline {
      padding-left: 20px;
      
      .timeline-item .timeline-dot {
        left: -16px;
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