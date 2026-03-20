<template>
  <div v-if="visible" class="onboarding-container">
    <!-- 进度指示器 -->
    <div class="progress-bar">
          <div
            v-for="(step, index) in steps"
            :key="index"
            class="progress-step"
            :class="{
              'active': index === currentStep,
              'completed': index < currentStep
            }"
          >
            <div class="step-circle">
              <svg v-if="index < currentStep" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              <span v-else>{{ index + 1 }}</span>
            </div>
            <span class="step-label">{{ step.label }}</span>
          </div>
        </div>

        <!-- 步骤内容 -->
        <transition :name="slideDirection" mode="out-in">
          <div :key="currentStep" class="step-content">
            <!-- 步骤 1: 欢迎 -->
            <div v-if="currentStep === 0" class="step-welcome">
              <div class="welcome-icon">👋</div>
              <h2>欢迎，{{ userInfo && (userInfo.username || userInfo.nickname) }}！</h2>
              <p>注册成功！让我们花几分钟完善您的个人信息，方便交易时更好地与其他用户沟通。</p>
            </div>

            <!-- 步骤 2: 上传头像 -->
            <div v-if="currentStep === 1" class="step-avatar">
              <h2>上传您的头像</h2>
              <p>选择一张照片作为您的个人头像</p>
              <div class="avatar-upload">
                <div class="avatar-preview" @click="triggerFileInput">
                  <img v-if="formData.avatarPreview" :src="formData.avatarPreview" alt="头像预览" />
                  <div v-else class="avatar-placeholder">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    <span>点击上传</span>
                  </div>
                </div>
                <input
                  ref="fileInput"
                  type="file"
                  accept="image/*"
                  @change="handleFileChange"
                  style="display: none"
                />
              </div>
            </div>

            <!-- 步骤 3: 基本信息 -->
            <div v-if="currentStep === 2" class="step-basic-info">
              <h2>完善基本信息</h2>
              <p>填写您的真实姓名、地址和生日</p>
              <div class="form-group">
                <label>真实姓名 *</label>
                <input
                  v-model="formData.realName"
                  type="text"
                  placeholder="请输入真实姓名"
                  maxlength="50"
                />
              </div>
              <div class="form-group">
                <label>真实地址</label>
                <input
                  v-model="formData.address"
                  type="text"
                  placeholder="请输入真实地址"
                  maxlength="255"
                />
              </div>
              <div class="form-group">
                <label>生日</label>
                <input
                  v-model="formData.birthday"
                  type="date"
                  placeholder="请选择生日"
                />
              </div>
            </div>

            <!-- 步骤 4: 联系方式确认 -->
            <div v-if="currentStep === 3" class="step-contact">
              <h2>确认联系方式</h2>
              <p>请再次确认您的联系方式</p>
              <div class="form-group">
                <label>邮箱 *</label>
                <input
                  v-model="formData.email"
                  type="email"
                  placeholder="your@email.com"
                />
              </div>
              <div class="form-group">
                <label>手机号 *</label>
                <input
                  v-model="formData.phone"
                  type="tel"
                  placeholder="请输入手机号"
                  maxlength="11"
                />
              </div>
            </div>
          </div>
        </transition>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button
            v-if="currentStep > 0"
            class="btn-secondary"
            @click="prevStep"
            :disabled="isSubmitting"
          >
            上一步
          </button>
          <button
            v-if="currentStep < steps.length - 1"
            class="btn-primary"
            @click="nextStep"
            :disabled="!canProceed || isSubmitting"
          >
            下一步
          </button>
          <button
            v-if="currentStep === steps.length - 1"
            class="btn-primary"
            @click="complete"
            :disabled="!canProceed || isSubmitting"
          >
            <span v-if="!isSubmitting">完成</span>
            <span v-else class="loading">
              <span class="spinner"></span>
              提交中...
            </span>
          </button>
        </div>

        <!-- 跳过按钮 -->
        <button
          class="btn-skip"
          @click="skip"
          :disabled="isSubmitting"
        >
          跳过
        </button>
      </div>
  </div>
</template>

<script>
export default {
  name: 'OnboardingGuide',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    userInfo: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      currentStep: 0,
      slideDirection: 'slide-left',
      isSubmitting: false,
      steps: [
        { label: '欢迎', key: 'welcome' },
        { label: '头像', key: 'avatar' },
        { label: '基本信息', key: 'basic' },
        { label: '联系确认', key: 'contact' }
      ],
      formData: {
        avatarFile: null,
        avatarPreview: '',
        realName: '',
        birthday: '',
        address: '',
        email: '',
        phone: ''
      }
    }
  },
  computed: {
    canProceed() {
      switch (this.currentStep) {
        case 0:
          return true
        case 1:
          return !!this.formData.avatarPreview
        case 2:
          return (this.formData.realName || '').trim().length >= 2
        case 3:
          return !!this.formData.email && !!this.formData.phone
        default:
          return true
      }
    }
  },
  mounted() {
    // 预填充注册时已有的联系信息
    if (this.userInfo) {
      this.formData.email = this.userInfo.email || ''
      this.formData.phone = this.userInfo.phone || ''
      this.formData.address = this.userInfo.address || ''
    }
  },
  methods: {
    nextStep() {
      if (this.currentStep < this.steps.length - 1) {
        this.slideDirection = 'slide-left'
        this.currentStep++
      }
    },
    prevStep() {
      if (this.currentStep > 0) {
        this.slideDirection = 'slide-right'
        this.currentStep--
      }
    },
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    handleFileChange(event) {
      const file = event.target.files[0]
      if (file) {
        // 验证文件类型
        if (!file.type.startsWith('image/')) {
          this.$emit('error', '请选择图片文件')
          return
        }

        // 验证文件大小 (5MB)
        if (file.size > 5 * 1024 * 1024) {
          this.$emit('error', '图片大小不能超过 5MB')
          return
        }

        this.formData.avatarFile = file

        // 预览图片
        const reader = new FileReader()
        reader.onload = (e) => {
          this.formData.avatarPreview = e.target.result
        }
        reader.readAsDataURL(file)
      }
    },
    skip() {
      this.$emit('skip', this.formData)
    },
    async complete() {
      this.isSubmitting = true

      try {
        // 提交表单数据
        await this.$emit('submit', this.formData)
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        this.isSubmitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.onboarding-container {
  background: $color-bg-page;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 700px;
  width: 100%;
  padding: 48px;
  position: relative;
  margin: 0 auto;
}

.progress-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 48px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 16px;
    left: 32px;
    right: 32px;
    height: 2px;
    background: $color-border;
    z-index: 0;
  }
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
  position: relative;
  z-index: 1;

  .step-circle {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: $color-bg-surface;
    border: 2px solid $color-border;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    color: $color-text-secondary;
    transition: all 0.3s ease;
  }

  .step-label {
    font-size: 12px;
    color: $color-text-secondary;
    transition: all 0.3s ease;
  }

  &.active {
    .step-circle {
      background: $color-primary;
      border-color: $color-primary;
      color: white;
      transform: scale(1.1);
    }

    .step-label {
      color: $color-primary;
      font-weight: 600;
    }
  }

  &.completed {
    .step-circle {
      background: $color-primary;
      border-color: $color-primary;
      color: white;
    }

    .step-label {
      color: $color-text-primary;
    }
  }
}

.step-content {
  min-height: 300px;
  text-align: center;

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: $color-text-primary;
    margin: 0 0 12px 0;
  }

  p {
    font-size: 16px;
    color: $color-text-secondary;
    margin: 0 0 32px 0;
  }
}

.step-welcome {
  .welcome-icon {
    font-size: 80px;
    margin-bottom: 24px;
    animation: wave 1s ease-in-out infinite;
  }
}

@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(20deg); }
  75% { transform: rotate(-20deg); }
}

.step-avatar {
  .avatar-upload {
    display: flex;
    justify-content: center;
  }

  .avatar-preview {
    width: 160px;
    height: 160px;
    border-radius: 50%;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    border: 4px solid $color-border;

    &:hover {
      transform: scale(1.05);
      border-color: $color-primary;
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .avatar-placeholder {
    width: 100%;
    height: 100%;
    background: rgba(var(--color-primary-rgb), 0.1);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: $color-primary;

    span {
      font-size: 14px;
      font-weight: 600;
    }
  }
}

.step-basic-info,
.step-contact {
  text-align: left;

  .form-group {
    margin-bottom: 24px;
    position: relative;

    label {
      display: block;
      font-size: 14px;
      font-weight: 600;
      color: $color-text-primary;
      margin-bottom: 8px;
    }

    input,
    textarea {
      width: 100%;
      padding: 14px 16px;
      font-size: 15px;
      border: 2px solid $color-border;
      border-radius: 12px;
      background: rgba(var(--color-primary-rgb), 0.03);
      color: $color-text-primary;
      transition: all 0.3s ease;
      font-family: inherit;

      &:focus {
        outline: none;
        border-color: $color-primary;
        background: rgba(var(--color-primary-rgb), 0.05);
        box-shadow: 0 0 0 4px rgba(var(--color-primary-rgb), 0.1);
      }
    }

    textarea {
      resize: vertical;
      min-height: 100px;
    }

    .char-count {
      position: absolute;
      bottom: -20px;
      right: 0;
      font-size: 12px;
      color: $color-text-disabled;
    }
  }
}

.step-complete {
  .complete-icon {
    font-size: 80px;
    margin-bottom: 24px;
    animation: celebrate 0.6s ease-in-out;
  }
}

@keyframes celebrate {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2) rotate(10deg); }
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 32px;

  button {
    flex: 1;
    padding: 16px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    border: none;

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  .btn-primary {
    background: $color-primary;
    color: white;
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(var(--color-primary-rgb), 0.4);
    }

    .loading {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
    }

    .spinner {
      width: 16px;
      height: 16px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 0.6s linear infinite;
    }
  }

  .btn-secondary {
    background: $color-bg-surface;
    color: $color-text-primary;
    border: 2px solid $color-border;

    &:hover:not(:disabled) {
      background: rgba(var(--color-primary-rgb), 0.1);
      border-color: $color-primary;
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.btn-skip {
  position: absolute;
  top: 24px;
  right: 24px;
  background: none;
  border: none;
  color: $color-text-secondary;
  font-size: 14px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover:not(:disabled) {
    background: rgba(var(--color-primary-rgb), 0.1);
    color: $color-primary;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

// 步骤切换动画
.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s ease;
}

.slide-left-enter {
  transform: translateX(30px);
  opacity: 0;
}

.slide-left-leave-to {
  transform: translateX(-30px);
  opacity: 0;
}

.slide-right-enter {
  transform: translateX(-30px);
  opacity: 0;
}

.slide-right-leave-to {
  transform: translateX(30px);
  opacity: 0;
}
</style>
