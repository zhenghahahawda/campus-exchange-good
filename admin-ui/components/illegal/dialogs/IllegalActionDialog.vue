<template>
  <el-dialog
    title="选择处理行动"
    :visible.sync="dialogVisible"
    width="500px"
    custom-class="glass-dialog action-selection-dialog"
    :modal="false"
    :close-on-click-modal="false"
    append-to-body
    center
    @open="$emit('dialog-open')"
    @close="$emit('dialog-close')"
  >
    <div v-if="report" class="action-selection-content">
      <!-- 处理行动选项列表 -->
      <div class="action-list">
        <!-- 警告处理 -->
        <div 
          class="action-item warning-item" 
          :class="{ selected: selectedAction === 'warning', active: selectedAction === 'warning' }"
          @click="$emit('select-action', 'warning')"
        >
          <div class="item-icon">
            <i class="el-icon-warning-outline"></i>
          </div>
          <div class="item-content">
            <h4 class="item-title">警告处理</h4>
            <p class="item-desc">扣除信誉分数，记录违规行为</p>
          </div>
          <div class="item-selector">
            <div class="selector-radio" :class="{ checked: selectedAction === 'warning' }">
              <div v-if="selectedAction === 'warning'" class="radio-dot"></div>
            </div>
          </div>
        </div>
        
        <!-- 临时封禁 -->
        <div 
          class="action-item ban-item" 
          :class="{ selected: selectedAction === 'tempBan', active: selectedAction === 'tempBan' }"
          @click="$emit('select-action', 'tempBan')"
        >
          <div class="item-icon">
            <i class="el-icon-time"></i>
          </div>
          <div class="item-content">
            <h4 class="item-title">临时封禁</h4>
            <p class="item-desc">限制访问指定时长</p>
          </div>
          <div class="item-selector">
            <div class="selector-radio" :class="{ checked: selectedAction === 'tempBan' }">
              <div v-if="selectedAction === 'tempBan'" class="radio-dot"></div>
            </div>
          </div>
        </div>
        
        <!-- 永久封禁 -->
        <div 
          class="action-item danger-item" 
          :class="{ selected: selectedAction === 'permBan', active: selectedAction === 'permBan' }"
          @click="$emit('select-action', 'permBan')"
        >
          <div class="item-icon">
            <i class="el-icon-circle-close"></i>
          </div>
          <div class="item-content">
            <h4 class="item-title">永久封禁</h4>
            <p class="item-desc">永久禁止平台访问</p>
          </div>
          <div class="item-selector">
            <div class="selector-radio" :class="{ checked: selectedAction === 'permBan' }">
              <div v-if="selectedAction === 'permBan'" class="radio-dot"></div>
            </div>
          </div>
        </div>
        
        <!-- 下架商品 -->
        <div 
          v-if="report.targetType === 'product'"
          class="action-item product-item" 
          :class="{ selected: selectedAction === 'removeProduct', active: selectedAction === 'removeProduct' }"
          @click="$emit('select-action', 'removeProduct')"
        >
          <div class="item-icon">
            <i class="el-icon-delete"></i>
          </div>
          <div class="item-content">
            <h4 class="item-title">下架商品</h4>
            <p class="item-desc">移除违规商品</p>
          </div>
          <div class="item-selector">
            <div class="selector-radio" :class="{ checked: selectedAction === 'removeProduct' }">
              <div v-if="selectedAction === 'removeProduct'" class="radio-dot"></div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 参数配置区域 -->
      <div v-if="selectedAction" class="params-section">
        <!-- 扣除信誉分数 - 只对警告处理和临时封禁显示 -->
        <div v-if="selectedAction === 'warning' || selectedAction === 'tempBan'" class="param-group">
          <div class="param-header">
            <i class="el-icon-star-off"></i>
            <span class="param-title">扣除信誉分数</span>
            <el-tooltip content="可设置1-100分，根据违规严重程度调整" placement="top">
              <i class="el-icon-question param-help"></i>
            </el-tooltip>
          </div>
          <div class="param-control">
            <!-- 现代化输入框 -->
            <div class="modern-input-wrapper">
              <div class="input-container">
                <input
                  type="number"
                  :value="actionParams.creditDeduction"
                  @input="handleCreditInput"
                  :min="1"
                  :max="100"
                  class="modern-input"
                  placeholder="输入1-100"
                />
                <span class="input-suffix">{{ actionParams.creditDeduction }}/100</span>
              </div>
            </div>
            <!-- 滑块 -->
            <div class="slider-wrapper">
              <el-slider
                :value="actionParams.creditDeduction"
                @input="$emit('validate-credit', $event)"
                :min="1"
                :max="100"
                :step="1"
                class="modern-slider"
              />
            </div>
            <!-- 提示信息 -->
            <div class="param-hint" v-if="actionParams.creditDeduction <= 5">
              <i class="el-icon-info"></i>
              <span>轻微违规</span>
            </div>
            <div class="param-hint warning" v-else-if="actionParams.creditDeduction >= 50">
              <i class="el-icon-warning"></i>
              <span>严重违规</span>
            </div>
          </div>
        </div>
        
        <!-- 处理说明 - 对所有处理行动都显示 -->
        <div class="param-group">
          <div class="param-header">
            <i class="el-icon-edit"></i>
            <span class="param-title">处理说明</span>
          </div>
          <div class="modern-textarea-wrapper">
            <div class="textarea-container">
              <textarea
                :value="actionParams.note"
                @input="handleNoteInput"
                placeholder="请输入处理说明，将通知相关用户..."
                maxlength="200"
                class="modern-textarea"
                rows="3"
              ></textarea>
              <div class="textarea-counter">{{ (actionParams.note || '').length }}/200</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <span slot="footer" class="dialog-footer">
      <button 
        class="dialog-btn cancel-btn" 
        @click="$emit('cancel-action')"
      >
        <i class="el-icon-close"></i>
        <span>取消</span>
      </button>
      <button 
        class="dialog-btn confirm-btn" 
        :class="{ 
          disabled: !selectedAction,
          processing: isExecuting
        }"
        @click="$emit('execute-action')"
        :disabled="!selectedAction || isExecuting"
      >
        <i v-if="!isExecuting" class="el-icon-check"></i>
        <i v-else class="el-icon-loading"></i>
        <span>{{ isExecuting ? '执行中...' : '确认执行' }}</span>
      </button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'IllegalActionDialog',
  
  props: {
    visible: Boolean,
    report: Object,
    selectedAction: String,
    actionParams: Object,
    actionFeedback: Object,
    isExecuting: Boolean
  },
  
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    },
    
    actionLabel() {
      const labels = {
        'warning': '警告处理',
        'tempBan': '临时封禁',
        'permBan': '永久封禁',
        'removeProduct': '下架商品'
      }
      return labels[this.selectedAction] || this.selectedAction
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
    updateNote(value) {
      const newParams = { ...this.actionParams, note: value }
      this.$emit('update:action-params', newParams)
    },
    
    getActionLabel(action) {
      const labels = {
        'warning': '警告处理',
        'tempBan': '临时封禁',
        'permBan': '永久封禁',
        'removeProduct': '下架商品'
      }
      return labels[action] || action
    },
    
    handleCreditInput(event) {
      const value = parseInt(event.target.value) || 1
      this.$emit('validate-credit', value)
    },
    
    handleNoteInput(event) {
      this.updateNote(event.target.value)
    },
    
    // 阻止滚轮穿透
    preventScroll(e) {
      const dialogElement = e.target.closest('.action-selection-dialog')
      if (!dialogElement) {
        e.preventDefault()
        e.stopPropagation()
        return false
      }
      
      const scrollableElement = e.target.closest('.el-dialog__body, .modern-textarea, .textarea-container')
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
// 文本域滚轮控制
.modern-textarea,
.textarea-container {
  overscroll-behavior: contain;
}

.action-selection-content {
  padding: 0;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.action-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  border: 2px solid transparent;
  background: rgba(255, 255, 255, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  
  &:hover {
    background: rgba(255, 255, 255, 0.08);
    transform: translateY(-2px);
  }
  
  &.selected {
    border-color: #409EFF;
    background: rgba(64, 158, 255, 0.1);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  }
  
  &.warning-item.selected {
    border-color: #E6A23C;
    background: rgba(230, 162, 60, 0.1);
    box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
  }
  
  &.danger-item.selected {
    border-color: #F56C6C;
    background: rgba(245, 108, 108, 0.1);
    box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  }
  
  &.product-item.selected {
    border-color: #909399;
    background: rgba(144, 147, 153, 0.1);
    box-shadow: 0 4px 12px rgba(144, 147, 153, 0.3);
  }
}

.item-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  
  .warning-item & {
    background: rgba(230, 162, 60, 0.2);
    color: #E6A23C;
  }
  
  .ban-item & {
    background: rgba(64, 158, 255, 0.2);
    color: #409EFF;
  }
  
  .danger-item & {
    background: rgba(245, 108, 108, 0.2);
    color: #F56C6C;
  }
  
  .product-item & {
    background: rgba(144, 147, 153, 0.2);
    color: #909399;
  }
}

.item-content {
  flex: 1;
  
  .item-title {
    font-size: 16px;
    font-weight: 600;
    color: #FFFFFF;
    margin: 0 0 4px 0;
  }
  
  .item-desc {
    font-size: 14px;
    color: #B0B3B8;
    margin: 0;
    line-height: 1.4;
  }
}

.item-selector {
  margin-left: 16px;
}

.selector-radio {
  width: 20px;
  height: 20px;
  border: 2px solid #606266;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  
  &.checked {
    border-color: #409EFF;
    background: #409EFF;
  }
  
  .radio-dot {
    width: 8px;
    height: 8px;
    background: #FFFFFF;
    border-radius: 50%;
  }
}

.params-section {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 24px;
  margin-top: 24px;
}

.param-group {
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.param-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  
  i {
    color: #409EFF;
    margin-right: 8px;
    font-size: 16px;
  }
  
  .param-title {
    font-size: 14px;
    font-weight: 600;
    color: #FFFFFF;
  }
  
  .param-help {
    margin-left: 4px;
    color: #909399;
    cursor: help;
    font-size: 14px;
  }
}

.param-control {
  margin-bottom: 12px;
}

.modern-input-wrapper {
  margin-bottom: 16px;
}

.input-container {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.2);
  }
  
  &:focus-within {
    background: rgba(255, 255, 255, 0.1);
    border-color: #409EFF;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  }
}

.modern-input {
  background: transparent;
  border: none;
  outline: none;
  color: #FFFFFF;
  font-size: 16px;
  font-weight: 500;
  flex: 1;
  width: 100%;
  
  &::placeholder {
    color: #909399;
    font-weight: 400;
  }
  
  &::-webkit-outer-spin-button,
  &::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
  
  &[type=number] {
    -moz-appearance: textfield;
  }
}

.input-suffix {
  color: #909399;
  font-size: 14px;
  font-weight: 500;
  margin-left: 8px;
  white-space: nowrap;
}

.slider-wrapper {
  margin-bottom: 12px;
}

.modern-slider {
  width: 100%;
  
  ::v-deep .el-slider__runway {
    background-color: rgba(255, 255, 255, 0.1);
    height: 6px;
    border-radius: 3px;
  }
  
  ::v-deep .el-slider__bar {
    background-color: #409EFF;
    border-radius: 3px;
  }
  
  ::v-deep .el-slider__button {
    width: 20px;
    height: 20px;
    background-color: #409EFF;
    border: 2px solid #FFFFFF;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  }
  
  ::v-deep .el-slider__button:hover {
    transform: scale(1.1);
  }
}

.credit-input-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
  
  .credit-number-input {
    width: 120px;
    flex-shrink: 0;
  }
  
  .credit-slider {
    flex: 1;
    min-width: 100px;
  }
}

.param-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #67C23A;
  
  &.warning {
    color: #E6A23C;
  }
  
  i {
    font-size: 14px;
  }
}

.modern-textarea-wrapper {
  margin-bottom: 12px;
}

.textarea-container {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 12px 16px;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.2);
  }
  
  &:focus-within {
    background: rgba(255, 255, 255, 0.1);
    border-color: #409EFF;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  }
}

.modern-textarea {
  background: transparent;
  border: none;
  outline: none;
  color: #FFFFFF;
  font-size: 14px;
  line-height: 1.5;
  width: 100%;
  resize: none;
  font-family: inherit;
  
  &::placeholder {
    color: #909399;
  }
}

.textarea-counter {
  position: absolute;
  bottom: 8px;
  right: 12px;
  color: #909399;
  font-size: 12px;
  pointer-events: none;
}

.param-textarea {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.dialog-btn {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  
  &.cancel-btn {
    background: rgba(255, 255, 255, 0.1);
    color: #FFFFFF;
    
    &:hover {
      background: rgba(255, 255, 255, 0.15);
    }
  }
  
  &.confirm-btn {
    background: #409EFF;
    color: #FFFFFF;
    
    &:hover:not(.disabled) {
      background: #66B1FF;
    }
    
    &.disabled {
      background: #909399;
      cursor: not-allowed;
    }
    
    &.processing {
      background: #409EFF;
      cursor: wait;
      
      i {
        animation: spin 1s linear infinite;
      }
    }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>