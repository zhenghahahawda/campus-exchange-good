<template>
  <el-dialog
    title="举报详情"
    :visible.sync="dialogVisible"
    width="900px"
    custom-class="glass-dialog detail-dialog"
    :modal="false"
    :close-on-click-modal="false"
    append-to-body
    center
    top="3vh"
    @open="$emit('dialog-open')"
    @close="$emit('dialog-close')"
  >
    <div v-if="report" class="report-detail-content">
      <!-- 基本信息卡片 -->
      <div class="detail-section info-section">
        <div class="section-header">
          <div class="section-icon">
            <i class="el-icon-info"></i>
          </div>
          <h3 class="section-title">基本信息</h3>
          <div class="section-decoration"></div>
        </div>
        
        <div class="info-grid">
          <div class="info-card">
            <div class="info-item primary-info">
              <div class="info-icon">
                <i class="el-icon-document-copy"></i>
              </div>
              <div class="info-content">
                <label>举报标题</label>
                <span class="info-value">{{ report.title }}</span>
              </div>
            </div>
            
            <div class="info-item">
              <div class="info-icon">
                <i class="el-icon-collection-tag"></i>
              </div>
              <div class="info-content">
                <label>举报类型</label>
                <span class="type-badge" :class="getTypeBadgeClass(report.type)">
                  <i :class="getTypeIcon(report.type)"></i>
                  {{ getTypeLabel(report.type) }}
                </span>
              </div>
            </div>
          </div>
          
          <div class="info-card">
            <div class="info-item">
              <div class="info-icon">
                <i class="el-icon-user"></i>
              </div>
              <div class="info-content">
                <label>举报人</label>
                <span class="info-value">{{ report.reporterName }}</span>
              </div>
            </div>

            <div class="info-item" v-if="report.targetType === 'product'">
              <div class="info-icon">
                <i class="el-icon-goods"></i>
              </div>
              <div class="info-content">
                <label>商品编号</label>
                <span class="info-value">{{ report.targetId }}</span>
              </div>
            </div>

            <div class="info-item" v-else>
              <div class="info-icon">
                <i class="el-icon-user-solid"></i>
              </div>
              <div class="info-content">
                <label>被举报用户</label>
                <span class="info-value">{{ report.targetName }}</span>
              </div>
            </div>
          </div>

          <div class="info-card" v-if="report.targetType === 'product' && report.targetName">
            <div class="info-item">
              <div class="info-icon">
                <i class="el-icon-shopping-bag-2"></i>
              </div>
              <div class="info-content">
                <label>商品名称</label>
                <span class="info-value">{{ report.targetName }}</span>
              </div>
            </div>
          </div>
          
          <div class="info-card">
            <div class="info-item">
              <div class="info-icon">
                <i class="el-icon-time"></i>
              </div>
              <div class="info-content">
                <label>举报时间</label>
                <span class="info-value">{{ formatDate(report.createdAt) }}</span>
              </div>
            </div>
            
            <div class="info-item">
              <div class="info-icon">
                <i class="el-icon-flag"></i>
              </div>
              <div class="info-content">
                <label>处理状态</label>
                <span class="status-badge" :class="getStatusClass(report.status)">
                  {{ getStatusLabel(report.status) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 举报内容 -->
      <div class="detail-section content-section">
        <div class="section-header">
          <div class="section-icon">
            <i class="el-icon-document"></i>
          </div>
          <h3 class="section-title">举报内容</h3>
          <div class="section-decoration"></div>
        </div>
        
        <div class="content-card">
          <div class="content-text">
            <div class="content-inner" v-html="formatContentToHtml(report.detailContent || report.description)">
            </div>
          </div>
        </div>
      </div>

      <!-- 证据图片 -->
      <div class="detail-section evidence-section" v-if="report.evidenceImages && report.evidenceImages.length > 0">
        <div class="section-header">
          <div class="section-icon">
            <i class="el-icon-picture"></i>
          </div>
          <h3 class="section-title">证据图片</h3>
          <div class="section-decoration"></div>
        </div>
        
        <div class="evidence-grid">
          <div
            v-for="(image, index) in report.evidenceImages"
            :key="index"
            class="evidence-item"
            @click="previewImage(image)"
          >
            <div class="image-container">
              <img :src="image" :alt="`证据图片${index + 1}`" />
              <div class="image-overlay">
                <i class="el-icon-zoom-in"></i>
              </div>
            </div>
            <div class="image-info">
              <p class="image-title">证据图片 {{ index + 1 }}</p>
              <small class="image-filename">{{ image.split('/').pop() }}</small>
            </div>
          </div>
        </div>
      </div>
    </div>

    <span slot="footer" class="dialog-footer">
      <button 
        v-if="report && report.status === 'pending'"
        class="glass-btn btn-reject" 
        @click="handleRejectClick"
      >
        <i class="el-icon-close"></i>
        <span>驳回举报</span>
      </button>
      <button 
        v-if="report && report.status === 'pending'"
        class="glass-btn btn-processing" 
        @click="handleProcessingClick"
      >
        <i class="el-icon-loading"></i>
        <span>转为处理</span>
      </button>
      <button 
        v-if="report && report.status === 'pending'"
        class="glass-btn btn-approve" 
        @click="handleActionClick"
      >
        <i class="el-icon-s-tools"></i>
        <span>选择处理行动</span>
      </button>
    </span>
  </el-dialog>
</template>

<script>
import { StyleHelper } from '@/utils/illegalStyleHelper'
import { ContentFormatter } from '@/utils/illegalManager'
import { REPORT_TYPE_LABELS, REPORT_STATUS_LABELS } from '@/utils/illegalConstants'

export default {
  name: 'IllegalDetailDialog',
  
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
    },
    
    formatContentToHtml(content) {
      return ContentFormatter.formatContentToHtml(content)
    },

    previewImage(url) {
      window.open(url, '_blank')
    },
    
    // 按钮点击处理方法
    handleRejectClick() {
      this.$emit('show-reject-dialog', this.report)
      this.dialogVisible = false
    },
    
    handleProcessingClick() {
      this.$emit('set-processing', this.report)
      this.dialogVisible = false
    },
    
    handleActionClick() {
      this.$emit('show-action-dialog', this.report)
      this.dialogVisible = false
    },
    
    // 阻止滚轮穿透
    preventScroll(e) {
      const dialogElement = e.target.closest('.glass-dialog')
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

.report-detail-content {
  padding: 0;
  
  .detail-section {
    margin-bottom: 24px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .section-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;
      position: relative;
      
      .section-icon {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        background: linear-gradient(135deg, 
          rgba(var(--color-primary-rgb), 0.15), 
          rgba(var(--color-primary-rgb), 0.08));
        border: 1px solid rgba($color-primary, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        
        i {
          font-size: 20px;
          color: $color-primary;
        }
      }
      
      .section-title {
        font-size: 18px;
        font-weight: 700;
        color: $color-text-primary;
        margin: 0;
        letter-spacing: 0.5px;
      }
      
      .section-decoration {
        flex: 1;
        height: 2px;
        background: linear-gradient(90deg, 
          rgba(var(--color-primary-rgb), 0.3), 
          transparent);
        border-radius: 2px;
        margin-left: 12px;
      }
    }
  }
  
  // 基本信息区域
  .info-section {
    .info-grid {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    
    .info-card {
      background: rgba($color-bg-surface, 0.6);
      backdrop-filter: blur(10px);
      border: 1px solid rgba($color-border, 0.2);
      border-radius: 14px;
      padding: 16px;
      display: flex;
      gap: 20px;
      position: relative;
      overflow: hidden;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 2px;
        background: linear-gradient(90deg, 
          rgba(var(--color-primary-rgb), 0.6), 
          rgba(var(--color-primary-rgb), 0.2),
          rgba(var(--color-primary-rgb), 0.6));
        opacity: 0;
        transition: opacity 0.3s ease;
      }
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
        border-color: rgba($color-primary, 0.3);
        
        &::before {
          opacity: 1;
        }
      }
      
      .info-item {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 10px;
        
        &.primary-info {
          flex: 2;
        }
        
        .info-icon {
          width: 36px;
          height: 36px;
          border-radius: 8px;
          background: linear-gradient(135deg, 
            rgba(var(--color-secondary-info-rgb), 0.15), 
            rgba(var(--color-secondary-info-rgb), 0.08));
          border: 1px solid rgba($color-secondary-info, 0.2);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          
          i {
            font-size: 16px;
            color: $color-secondary-info;
          }
        }
        
        .info-content {
          flex: 1;
          
          label {
            display: block;
            font-size: 12px;
            font-weight: 600;
            color: $color-text-secondary;
            margin-bottom: 3px;
            letter-spacing: 0.5px;
          }
          
          .info-value {
            font-size: 14px;
            font-weight: 600;
            color: $color-text-primary;
            line-height: 1.3;
          }
          
          .type-badge,
          .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 5px;
            padding: 4px 10px;
            border-radius: 16px;
            font-size: 12px;
            font-weight: 700;
            letter-spacing: 0.5px;
            
            i {
              font-size: 12px;
            }
          }
          
          .type-badge {
            background: linear-gradient(135deg, 
              rgba(var(--color-secondary-warning-rgb), 0.15), 
              rgba(var(--color-secondary-warning-rgb), 0.08));
            color: $color-secondary-warning;
            border: 1px solid rgba($color-secondary-warning, 0.25);
          }
          
          .status-badge {
            background: linear-gradient(135deg, 
              rgba(var(--color-secondary-success-rgb), 0.15), 
              rgba(var(--color-secondary-success-rgb), 0.08));
            color: $color-secondary-success;
            border: 1px solid rgba($color-secondary-success, 0.25);
          }
        }
      }
    }
  }
  
  // 举报内容区域
  .content-section {
    .content-card {
      background: rgba($color-bg-surface, 0.6);
      backdrop-filter: blur(10px);
      border: 1px solid rgba($color-border, 0.2);
      border-radius: 14px;
      padding: 18px;
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
      
      .content-text {
        .content-inner {
          font-size: 14px;
          line-height: 1.6;
          color: $color-text-primary;
          font-weight: 500;
          
          ::v-deep {
            p {
              margin: 0 0 12px 0;
              
              &:last-child {
                margin-bottom: 0;
              }
            }
            
            ol, ul {
              margin: 12px 0;
              padding-left: 20px;
              
              li {
                margin-bottom: 6px;
                color: $color-text-primary;
                font-weight: 500;
              }
            }
          }
        }
      }
    }
  }
  
  // 证据图片区域
  .evidence-section {
    .evidence-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: 16px;
    }

    .evidence-item {
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-3px);

        .image-overlay {
          opacity: 1;
        }
      }

      .image-container {
        position: relative;
        width: 100%;
        aspect-ratio: 1;
        border-radius: 14px;
        overflow: hidden;
        background: rgba($color-bg-surface, 0.6);
        backdrop-filter: blur(10px);
        border: 2px solid rgba($color-border, 0.2);
        transition: all 0.3s ease;

        &:hover {
          border-color: rgba($color-primary, 0.4);
          box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
        }

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .image-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.5);
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          transition: opacity 0.3s ease;

          i {
            font-size: 32px;
            color: #fff;
          }
        }
      }

      .image-info {
        margin-top: 8px;
        text-align: center;

        .image-title {
          font-size: 13px;
          font-weight: 500;
          color: $color-text-primary;
          margin-bottom: 4px;
        }

        .image-filename {
          font-size: 11px;
          color: $color-text-secondary;
          word-break: break-all;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 0;
  
  .glass-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    border: none;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    backdrop-filter: blur(10px);
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
      transition: left 0.5s ease;
    }
    
    &:hover::before {
      left: 100%;
    }
    
    i {
      font-size: 16px;
      transition: transform 0.3s ease;
    }
    
    &:hover i {
      transform: scale(1.1);
    }
    
    &.btn-reject {
      background: linear-gradient(135deg, 
        rgba(var(--color-secondary-danger-rgb), 0.15), 
        rgba(var(--color-secondary-danger-rgb), 0.08));
      color: $color-secondary-danger;
      border: 2px solid rgba($color-secondary-danger, 0.25);
      
      &:hover {
        background: linear-gradient(135deg, 
          rgba(var(--color-secondary-danger-rgb), 0.25), 
          rgba(var(--color-secondary-danger-rgb), 0.15));
        border-color: rgba($color-secondary-danger, 0.4);
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(var(--color-secondary-danger-rgb), 0.3);
      }
    }
    
    &.btn-processing {
      background: linear-gradient(135deg, 
        rgba(var(--color-secondary-info-rgb), 0.15), 
        rgba(var(--color-secondary-info-rgb), 0.08));
      color: $color-secondary-info;
      border: 2px solid rgba($color-secondary-info, 0.25);
      
      &:hover {
        background: linear-gradient(135deg, 
          rgba(var(--color-secondary-info-rgb), 0.25), 
          rgba(var(--color-secondary-info-rgb), 0.15));
        border-color: rgba($color-secondary-info, 0.4);
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(var(--color-secondary-info-rgb), 0.3);
      }
    }
    
    &.btn-approve {
      background: linear-gradient(135deg, 
        rgba(var(--color-primary-rgb), 0.95), 
        rgba(var(--color-primary-rgb), 0.85));
      color: white;
      border: 2px solid rgba($color-primary, 0.8);
      box-shadow: 0 4px 15px rgba(var(--color-primary-rgb), 0.3);
      
      &:hover {
        background: linear-gradient(135deg, 
          $color-primary, 
          rgba(var(--color-primary-rgb), 0.9));
        border-color: $color-primary;
        transform: translateY(-2px);
        box-shadow: 0 6px 25px rgba(var(--color-primary-rgb), 0.4);
      }
    }
  }
}

// 对话框特定样式
::v-deep .glass-dialog.detail-dialog {
  max-height: 94vh;
  display: flex;
  flex-direction: column;
  
  .el-dialog__header {
    padding: 24px 32px 20px;
    background: linear-gradient(135deg, 
      rgba(var(--color-primary-rgb), 0.05), 
      rgba(var(--color-primary-rgb), 0.02));
    border-bottom: 1px solid rgba($color-border, 0.15);
    flex-shrink: 0;
    
    .el-dialog__title {
      font-size: 20px;
      font-weight: 800;
      color: $color-text-primary;
      letter-spacing: 0.5px;
    }
    
    .el-dialog__headerbtn {
      top: 24px;
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
        font-size: 20px;
        font-weight: 700;
        color: $color-text-secondary;
        
        &:hover {
          color: $color-primary;
        }
      }
    }
  }
  
  .el-dialog__body {
    padding: 24px 32px 20px;
    background: rgba($color-bg-surface, 0.02);
    flex: 1;
    overflow-y: auto;
    min-height: 0;
    
    // 自定义滚动条
    &::-webkit-scrollbar {
      width: 8px;
    }
    
    &::-webkit-scrollbar-track {
      background: rgba($color-border, 0.05);
      border-radius: 8px;
      margin: 4px 0;
    }
    
    &::-webkit-scrollbar-thumb {
      background: linear-gradient(180deg, 
        rgba(var(--color-primary-rgb), 0.4), 
        rgba(var(--color-primary-rgb), 0.3));
      border-radius: 8px;
      transition: all 0.3s ease;
      
      &:hover {
        background: linear-gradient(180deg, 
          rgba(var(--color-primary-rgb), 0.6), 
          rgba(var(--color-primary-rgb), 0.5));
      }
    }
  }
  
  .el-dialog__footer {
    padding: 20px 32px 28px;
    background: linear-gradient(135deg, 
      rgba(var(--color-primary-rgb), 0.02), 
      rgba(var(--color-primary-rgb), 0.05));
    border-top: 1px solid rgba($color-border, 0.15);
    flex-shrink: 0;
  }
}

// 响应式适配
@media (max-width: 768px) {
  ::v-deep .glass-dialog.detail-dialog {
    width: 95vw !important;
    margin: 0 !important;
    
    .el-dialog__header,
    .el-dialog__body,
    .el-dialog__footer {
      padding-left: 20px;
      padding-right: 20px;
    }
  }
  
  .report-detail-content {
    .info-section .info-card {
      flex-direction: column;
      gap: 16px;
      
      .info-item {
        &.primary-info {
          flex: 1;
        }
      }
    }
    
    .evidence-section .evidence-grid {
      grid-template-columns: 1fr;
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