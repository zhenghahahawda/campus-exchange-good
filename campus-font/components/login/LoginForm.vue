<template>
  <form @submit.prevent="handleSubmit" class="login-form">
    <!-- 邮箱/手机号输入 -->
    <div class="form-group" :class="{ 'has-error': errors.account }">
      <label for="account">
        <svg class="icon-account" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
        邮箱/手机号
      </label>
      <div class="input-wrapper">
        <input
          id="account"
          v-model="formData.account"
          type="text"
          placeholder="请输入邮箱或手机号"
          @focus="handleFocus('account')"
          @blur="handleBlur"
          @input="handleInput"
          :class="{ 'error': errors.account }"
        />
      </div>
      <span v-if="errors.account" class="error-message">{{ errors.account }}</span>
    </div>

    <!-- 密码输入 -->
    <div class="form-group" :class="{ 'has-error': errors.password }">
      <label for="password">
        <svg class="icon-lock" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
          <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
        </svg>
        密码
      </label>
      <div class="input-wrapper password-wrapper">
        <input
          id="password"
          v-model="formData.password"
          :type="showPassword ? 'text' : 'password'"
          placeholder="请输入密码"
          autocomplete="current-password"
          @focus="handleFocus('password')"
          @blur="handleBlur"
          @input="handleInput"
          :class="{ 'error': errors.password }"
        />
        <button
          type="button"
          class="password-toggle"
          @click="togglePassword"
          tabindex="-1"
          aria-label="切换密码显示"
        >
          <svg v-if="!showPassword" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
            <circle cx="12" cy="12" r="3"></circle>
          </svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
            <line x1="1" y1="1" x2="23" y2="23"></line>
          </svg>
        </button>
      </div>
      <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
    </div>

    <!-- 记住密码 & 忘记密码 -->
    <div class="form-options">
      <label class="checkbox-wrapper">
        <input type="checkbox" v-model="formData.remember" />
        <span class="checkmark"></span>
        <span class="label-text">记住我</span>
      </label>
      <a href="#" class="forgot-link" @click.prevent="$emit('forgot-password')">
        忘记密码？
      </a>
    </div>

    <!-- 滑块验证 -->
    <slot name="captcha"></slot>

    <!-- 登录按钮 -->
    <button
      type="submit"
      class="btn-submit"
      :class="{ 'loading': isLoading }"
      :disabled="isLoading || !isVerified || !formData.account || !formData.password"
    >
      <span v-if="!isLoading" class="btn-content">
        <span>登录</span>
        <span class="arrow">→</span>
      </span>
      <span v-else class="btn-loading">
        <span class="spinner"></span>
        登录中...
      </span>
    </button>

    <!-- 注册提示 -->
    <div class="signup-prompt">
      还没有账号？
      <a href="#" @click.prevent="$emit('sign-up')">立即注册</a>
    </div>
  </form>
</template>

<script>
import { getRememberMe } from '@/utils/rememberMe'

export default {
  name: 'LoginForm',
  props: {
    isLoading: {
      type: Boolean,
      default: false
    },
    isVerified: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      formData: {
        account: '',
        password: '',
        remember: false
      },
      showPassword: false,
      errors: {
        account: '',
        password: ''
      },
      focusType: '',
      isTyping: false,
      typingTimeout: null
    }
  },
  mounted() {
    // 加载记住的账号密码
    this.loadRememberMe()
  },
  methods: {
    // 加载记住的账号密码
    loadRememberMe() {
      const remembered = getRememberMe()
      if (remembered) {
        this.formData.account = remembered.account
        this.formData.password = remembered.password
        this.formData.remember = remembered.remember
      }
    },
    handleFocus(type) {
      this.focusType = type
      // 清除该字段的错误信息
      this.errors[type] = ''
      this.$emit('focus', type)
    },
    handleBlur() {
      // 失焦时进行实时验证
      this.validateField(this.focusType)

      setTimeout(() => {
        this.focusType = ''
        this.isTyping = false
        this.$emit('blur')
      }, 100)
    },
    handleInput() {
      this.isTyping = true
      this.$emit('typing', true)

      // 如果字段有错误，在输入时清除错误
      if (this.focusType && this.errors[this.focusType]) {
        this.errors[this.focusType] = ''
      }

      clearTimeout(this.typingTimeout)
      this.typingTimeout = setTimeout(() => {
        this.isTyping = false
        this.$emit('typing', false)
      }, 500)
    },
    validateField(field) {
      if (!field) return

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      const phoneRegex = /^1[3-9]\d{9}$/

      if (field === 'account') {
        const account = (this.formData.account || '').trim()
        if (account && !emailRegex.test(account) && !phoneRegex.test(account)) {
          this.errors.account = '请输入有效的邮箱或手机号'
        }
      } else if (field === 'password') {
        const password = this.formData.password || ''
        if (password && password.length > 0 && password.length < 6) {
          this.errors.password = '密码至少需要6个字符'
        } else if (password && password.length > 32) {
          this.errors.password = '密码不能超过32个字符'
        }
      }
    },
    togglePassword() {
      this.showPassword = !this.showPassword
      this.$emit('toggle-password', this.showPassword)
    },
    validateForm() {
      let isValid = true
      this.errors = { account: '', password: '' }

      // 邮箱或手机号验证
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      const phoneRegex = /^1[3-9]\d{9}$/
      const account = (this.formData.account || '').trim()

      if (!account) {
        this.errors.account = '请填写此字段'
        isValid = false
      } else if (!emailRegex.test(account) && !phoneRegex.test(account)) {
        this.errors.account = '请输入有效的邮箱或手机号'
        isValid = false
      }

      // 密码验证 - 更合理的规则
      if (!this.formData.password) {
        this.errors.password = '请填写此字段'
        isValid = false
      } else if (this.formData.password.length < 6) {
        this.errors.password = '密码至少需要6个字符'
        isValid = false
      } else if (this.formData.password.length > 32) {
        this.errors.password = '密码不能超过32个字符'
        isValid = false
      }

      if (!isValid) {
        this.$emit('error', true)
      }

      return isValid
    },
    handleSubmit() {
      if (!this.validateForm()) {
        return
      }

      this.$emit('submit', this.formData)
    },
    setError(field, message) {
      this.errors[field] = message
    }
  },
  watch: {
    focusType(val) {
      this.$emit('focus-change', val)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 24px;

  label {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: $color-text-primary;
    margin-bottom: 8px;

    .icon-account, .icon-lock {
      width: 16px;
      height: 16px;
      color: $color-text-secondary;
    }
  }

  &.has-error {
    .input-wrapper input {
      border-color: $color-secondary-danger;
      animation: shake 0.3s ease-in-out;
    }
  }
}

.input-wrapper {
  position: relative;

  input {
    width: 100%;
    padding: 14px 16px;
    font-size: 15px;
    border: 2px solid $color-border;
    border-radius: 12px;
    background: rgba(var(--color-primary-rgb), 0.03);
    color: $color-text-primary;
    transition: all 0.3s ease;
    font-family: inherit;

    &::placeholder {
      color: $color-text-disabled;
    }

    &:focus {
      outline: none;
      border-color: $color-primary;
      background: rgba(var(--color-primary-rgb), 0.05);
      box-shadow: 0 0 0 4px rgba(var(--color-primary-rgb), 0.1);
    }

    &.error {
      border-color: $color-secondary-danger;
    }
  }

  &.password-wrapper {
    input {
      padding-right: 50px;

      // 隐藏 IE/Edge 的密码显示按钮
      &::-ms-reveal,
      &::-ms-clear {
        display: none;
      }

      // 隐藏 Chrome/Safari 的密码显示按钮
      &::-webkit-credentials-auto-fill-button,
      &::-webkit-contacts-auto-fill-button {
        visibility: hidden;
        pointer-events: none;
        position: absolute;
        right: 0;
      }
    }
  }
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-text-secondary;
  border-radius: 6px;

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover {
    color: $color-primary;
    background: rgba(var(--color-primary-rgb), 0.1);
  }

  &:active {
    transform: translateY(-50%) scale(0.95);
  }
}

.error-message {
  display: block;
  color: $color-secondary-danger;
  font-size: 13px;
  margin-top: 6px;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 14px;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;

  input[type="checkbox"] {
    position: absolute;
    opacity: 0;

    &:checked ~ .checkmark {
      background: $color-primary;
      border-color: $color-primary;

      &::after {
        display: block;
      }
    }
  }

  .checkmark {
    width: 20px;
    height: 20px;
    border: 2px solid $color-border;
    border-radius: 6px;
    margin-right: 8px;
    position: relative;
    transition: all 0.2s ease;
    background: rgba(var(--color-primary-rgb), 0.05);

    &::after {
      content: '';
      position: absolute;
      display: none;
      left: 6px;
      top: 2px;
      width: 5px;
      height: 10px;
      border: solid white;
      border-width: 0 2px 2px 0;
      transform: rotate(45deg);
    }
  }

  .label-text {
    color: $color-text-secondary;
  }
}

.forgot-link {
  color: $color-primary;
  text-decoration: none;
  font-weight: 600;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 0.8;
  }
}

.btn-submit {
  width: 100%;
  padding: 16px;
  background: $color-primary;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    transform: translate(-50%, -50%);
    transition: width 0.6s, height 0.6s;
  }

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(var(--color-primary-rgb), 0.4);

    &::before {
      width: 300px;
      height: 300px;
    }
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  .btn-content {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    position: relative;
    z-index: 1;

    .arrow {
      transition: transform 0.3s ease;
    }
  }

  &:hover .arrow {
    transform: translateX(4px);
  }

  .btn-loading {
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

@keyframes spin {
  to { transform: rotate(360deg); }
}

.signup-prompt {
  text-align: center;
  font-size: 14px;
  color: $color-text-secondary;

  a {
    color: $color-primary;
    font-weight: 600;
    text-decoration: none;
    margin-left: 4px;

    &:hover {
      text-decoration: underline;
    }
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-8px); }
  75% { transform: translateX(8px); }
}
</style>
