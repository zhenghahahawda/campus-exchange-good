<template>
  <el-dialog
    title="商品交换凭证"
    :visible="visible"
    @update:visible="$emit('update:visible', $event)"
    width="500px"
    custom-class="glass-dialog exchange-code-dialog"
    append-to-body
    :modal="false"
    :close-on-click-modal="false"
  >
    <div class="code-content">
      <div class="code-header">
        <i class="el-icon-success"></i>
        <h3>审核通过！</h3>
        <p>您的商品已成功上架，以下是交换凭证码</p>
      </div>
      
      <div class="code-display">
        <div class="code-box">
          <span class="code-text">{{ exchangeCode }}</span>
        </div>
        <button class="copy-btn" @click="copyCode">
          <i class="el-icon-document-copy"></i>
          <span>复制凭证码</span>
        </button>
      </div>
      
      <div class="code-info">
        <div class="info-item">
          <i class="el-icon-info"></i>
          <div class="info-text">
            <strong>使用说明：</strong>
            <p>线下交易时，请向交换方出示此凭证码以验证商品真实性</p>
          </div>
        </div>
        <div class="info-item">
          <i class="el-icon-warning"></i>
          <div class="info-text">
            <strong>注意事项：</strong>
            <p>请妥善保管凭证码，不要随意泄露给他人</p>
          </div>
        </div>
      </div>
    </div>
    
    <span slot="footer" class="dialog-footer">
      <button class="glass-btn btn-primary" @click="$emit('update:visible', false)">
        <i class="el-icon-check"></i>
        <span>我知道了</span>
      </button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'ExchangeCodeDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    exchangeCode: {
      type: String,
      default: ''
    }
  },
  methods: {
    copyCode() {
      // 复制到剪贴板
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(this.exchangeCode).then(() => {
          this.$root.$emit('show-island-message', '凭证码已复制到剪贴板', 'success');
        }).catch(() => {
          this.fallbackCopy();
        });
      } else {
        this.fallbackCopy();
      }
    },
    fallbackCopy() {
      // 降级方案
      const textarea = document.createElement('textarea');
      textarea.value = this.exchangeCode;
      textarea.style.position = 'fixed';
      textarea.style.opacity = '0';
      document.body.appendChild(textarea);
      textarea.select();
      try {
        document.execCommand('copy');
        this.$root.$emit('show-island-message', '凭证码已复制到剪贴板', 'success');
      } catch (err) {
        this.$root.$emit('show-island-message', '复制失败，请手动复制', 'error');
      }
      document.body.removeChild(textarea);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.code-content {
  padding: 10px;
  
  .code-header {
    text-align: center;
    margin-bottom: 30px;
    
    i {
      font-size: 64px;
      color: $color-secondary-success;
      margin-bottom: 16px;
    }
    
    h3 {
      font-size: 24px;
      font-weight: 700;
      color: $color-text-primary;
      margin: 0 0 8px 0;
    }
    
    p {
      font-size: 14px;
      color: $color-text-secondary;
      margin: 0;
    }
  }
  
  .code-display {
    margin-bottom: 30px;
    
    .code-box {
      background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.1), rgba(var(--color-primary-rgb), 0.05));
      border: 2px dashed $color-primary;
      border-radius: 12px;
      padding: 24px;
      text-align: center;
      margin-bottom: 16px;
      
      .code-text {
        font-size: 28px;
        font-weight: 700;
        color: $color-primary;
        font-family: 'Courier New', monospace;
        letter-spacing: 2px;
      }
    }
    
    .copy-btn {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 12px 24px;
      background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.95), rgba(var(--color-primary-rgb), 0.85));
      border: 1px solid rgba(var(--color-primary-rgb), 0.3);
      border-radius: 12px;
      color: #fff;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      outline: none;
      
      i {
        font-size: 16px;
      }
      
      &:hover {
        background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 1), rgba(var(--color-primary-rgb), 0.9));
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(var(--color-primary-rgb), 0.4);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
  }
  
  .code-info {
    display: flex;
    flex-direction: column;
    gap: 16px;
    
    .info-item {
      display: flex;
      gap: 12px;
      padding: 14px;
      background: rgba($color-bg-surface, 0.5);
      border-radius: 10px;
      border: 1px solid rgba($color-border, 0.2);
      
      i {
        font-size: 20px;
        color: $color-primary;
        flex-shrink: 0;
        margin-top: 2px;
      }
      
      .info-text {
        flex: 1;
        
        strong {
          display: block;
          font-size: 14px;
          color: $color-text-primary;
          margin-bottom: 4px;
        }
        
        p {
          font-size: 13px;
          color: $color-text-secondary;
          line-height: 1.5;
          margin: 0;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: center;
  padding-top: 10px;
  
  .glass-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px 40px;
    border: none;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    outline: none;
    
    i {
      font-size: 16px;
    }
    
    &.btn-primary {
      background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.95), rgba(var(--color-primary-rgb), 0.85));
      border: 1px solid rgba(var(--color-primary-rgb), 0.3);
      color: #fff;
      box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
      
      &:hover {
        background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 1), rgba(var(--color-primary-rgb), 0.9));
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(var(--color-primary-rgb), 0.4);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
  }
}

::v-deep {
  .glass-dialog.exchange-code-dialog {
    @include glass-card;
    border: 1px solid rgba($color-border, 0.5);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    
    .el-dialog__header {
      padding: 24px 28px 20px;
      border-bottom: 1px solid rgba($color-border, 0.2);
      background: rgba($color-primary, 0.02);
      
      .el-dialog__title {
        font-size: 18px;
        font-weight: 700;
        color: $color-text-primary;
      }
      
      .el-dialog__headerbtn {
        top: 24px;
        right: 24px;
        
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
      padding: 24px 28px;
    }
    
    .el-dialog__footer {
      padding: 16px 28px 24px;
      background: rgba($color-primary, 0.02);
      border-top: 1px solid rgba($color-border, 0.2);
    }
  }
}
</style>
