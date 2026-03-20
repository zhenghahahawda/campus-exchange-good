<template>
  <form @submit.prevent="handleSubmit" class="register-form">
    <!-- 第一行：用户名 + 手机号码 -->
    <div class="form-row">
      <div class="form-group" :class="{ 'has-error': errors.username }">
        <label for="username">
          <svg class="icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
          用户名
        </label>
        <input
          id="username"
          v-model="formData.username"
          type="text"
          placeholder="请输入用户名"
          maxlength="50"
          @focus="handleFocus('username')"
          @blur="handleBlur"
          @input="handleInput"
          :class="{ 'error': errors.username }"
        />
        <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
      </div>

      <div class="form-group" :class="{ 'has-error': errors.phone }">
        <label for="phone">
          <svg class="icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
          </svg>
          手机号码
        </label>
        <input
          id="phone"
          v-model="formData.phone"
          type="tel"
          placeholder="请输入手机号码"
          maxlength="11"
          @focus="handleFocus('phone')"
          @blur="handleBlur"
          @input="handleInput"
          :class="{ 'error': errors.phone }"
        />
        <span v-if="errors.phone" class="error-message">{{ errors.phone }}</span>
      </div>
    </div>

    <!-- 第二行：邮箱地址 + 学校 -->
    <div class="form-row">
      <div class="form-group" :class="{ 'has-error': errors.email }">
        <label for="email">
          <svg class="icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
            <polyline points="22,6 12,13 2,6"></polyline>
          </svg>
          邮箱地址
        </label>
        <input
          id="email"
          v-model="formData.email"
          type="email"
          placeholder="请输入邮箱地址"
          maxlength="100"
          @focus="handleFocus('email')"
          @blur="handleBlur"
          @input="handleInput"
          :class="{ 'error': errors.email }"
        />
        <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
      </div>

      <div class="form-group" :class="{ 'has-error': errors.school }">
        <label for="school">
          <svg class="icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
            <polyline points="9 22 9 12 15 12 15 22"></polyline>
          </svg>
          学校
        </label>
        <input
          id="school"
          v-model="formData.school"
          type="text"
          placeholder="请输入学校名称"
          maxlength="100"
          @focus="handleFocus('school')"
          @blur="handleBlur"
          @input="handleInput"
          :class="{ 'error': errors.school }"
        />
        <span v-if="errors.school" class="error-message">{{ errors.school }}</span>
      </div>
    </div>

    <!-- 第三行：密码 + 确认密码 -->
    <div class="form-row">
      <div class="form-group" :class="{ 'has-error': errors.password }">
        <label for="password">
          <svg class="icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
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
            placeholder="请设置密码"
            maxlength="32"
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

      <div class="form-group" :class="{ 'has-error': errors.confirmPassword }">
        <label for="confirmPassword">
          <svg class="icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
          </svg>
          确认密码
        </label>
        <div class="input-wrapper password-wrapper">
          <input
            id="confirmPassword"
            v-model="formData.confirmPassword"
            :type="showConfirmPassword ? 'text' : 'password'"
            placeholder="请再次输入密码"
            maxlength="32"
            @focus="handleFocus('confirmPassword')"
            @blur="handleBlur"
            @input="handleInput"
            :class="{ 'error': errors.confirmPassword }"
          />
          <button
            type="button"
            class="password-toggle"
            @click="toggleConfirmPassword"
            tabindex="-1"
          >
            <svg v-if="!showConfirmPassword" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
              <line x1="1" y1="1" x2="23" y2="23"></line>
            </svg>
          </button>
        </div>
        <span v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</span>
      </div>
    </div>

    <!-- 第四行：性别 + 真实地址 -->
    <div class="form-row">
      <div class="form-group gender-group">
        <label>
          <svg class="icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
          </svg>
          性别
        </label>
        <div class="gender-options">
          <label class="radio-wrapper">
            <input type="radio" v-model="formData.gender" :value="1" />
            <span class="radio-mark"></span>
            <span class="label-text">男</span>
          </label>
          <label class="radio-wrapper">
            <input type="radio" v-model="formData.gender" :value="2" />
            <span class="radio-mark"></span>
            <span class="label-text">女</span>
          </label>
        </div>
      </div>

      <div class="form-group">
        <label for="address">
          <svg class="icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
            <circle cx="12" cy="10" r="3"></circle>
          </svg>
          真实地址
        </label>
        <input
          id="address"
          v-model="formData.address"
          type="text"
          placeholder="请输入真实地址（选填）"
          maxlength="255"
          @focus="handleFocus('address')"
          @blur="handleBlur"
          @input="handleInput"
        />
      </div>
    </div>

    <!-- 滑块验证 -->
    <slot name="captcha"></slot>

    <!-- 注册按钮 -->
    <button
      type="submit"
      class="btn-submit"
      :class="{ 'loading': isLoading }"
      :disabled="isLoading || !isVerified"
    >
      <span v-if="!isLoading" class="btn-content">
        <span>注册</span>
        <span class="arrow">→</span>
      </span>
      <span v-else class="btn-loading">
        <span class="spinner"></span>
        注册中...
      </span>
    </button>

    <!-- 登录提示 -->
    <div class="login-prompt">
      已有账号？
      <a href="#" @click.prevent="$emit('go-to-login')">立即登录</a>
    </div>
  </form>
</template>

<script>
export default {
  name: 'RegisterForm',
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
        username: '',
        phone: '',
        email: '',
        school: '',
        gender: 1,
        address: '',
        password: '',
        confirmPassword: ''
      },
      showPassword: false,
      showConfirmPassword: false,
      errors: {
        username: '',
        phone: '',
        email: '',
        school: '',
        password: '',
        confirmPassword: ''
      },
      focusType: '',
      isTyping: false,
      typingTimeout: null
    }
  },
  methods: {
    handleFocus(type) {
      this.focusType = type
      this.errors[type] = ''
      this.$emit('focus', type)
    },
    handleBlur() {
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

      switch (field) {
        case 'username':
          if (this.formData.username && this.formData.username.length < 3) {
            this.errors.username = '用户名至少需要3个字符'
          }
          break
        case 'phone':
          if (this.formData.phone && !phoneRegex.test(this.formData.phone)) {
            this.errors.phone = '请输入有效的手机号码'
          }
          break
        case 'email':
          if (this.formData.email && !emailRegex.test(this.formData.email)) {
            this.errors.email = '请输入有效的邮箱地址'
          }
          break
        case 'password':
          if (this.formData.password && this.formData.password.length < 6) {
            this.errors.password = '密码至少需要6个字符'
          }
          break
        case 'confirmPassword':
          if (this.formData.confirmPassword && this.formData.confirmPassword !== this.formData.password) {
            this.errors.confirmPassword = '两次输入的密码不一致'
          }
          break
      }
    },
    togglePassword() {
      this.showPassword = !this.showPassword
      this.$emit('toggle-password', this.showPassword)
    },
    toggleConfirmPassword() {
      this.showConfirmPassword = !this.showConfirmPassword
    },
    validateForm() {
      let isValid = true
      this.errors = {
        username: '',
        phone: '',
        email: '',
        school: '',
        password: '',
        confirmPassword: ''
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      const phoneRegex = /^1[3-9]\d{9}$/

      // 用户名验证
      if (!this.formData.username) {
        this.errors.username = '请填写此字段'
        isValid = false
      } else if (this.formData.username.length < 3) {
        this.errors.username = '用户名至少需要3个字符'
        isValid = false
      }

      // 手机号验证
      if (!this.formData.phone) {
        this.errors.phone = '请填写此字段'
        isValid = false
      } else if (!phoneRegex.test(this.formData.phone)) {
        this.errors.phone = '请输入有效的手机号码'
        isValid = false
      }

      // 邮箱验证
      if (!this.formData.email) {
        this.errors.email = '请填写此字段'
        isValid = false
      } else if (!emailRegex.test(this.formData.email)) {
        this.errors.email = '请输入有效的邮箱地址'
        isValid = false
      }

      // 学校验证
      if (!this.formData.school) {
        this.errors.school = '请填写此字段'
        isValid = false
      }

      // 密码验证
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

      // 确认密码验证
      if (!this.formData.confirmPassword) {
        this.errors.confirmPassword = '请填写此字段'
        isValid = false
      } else if (this.formData.confirmPassword !== this.formData.password) {
        this.errors.confirmPassword = '两次输入的密码不一致'
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

      const { confirmPassword, ...submitData } = this.formData
      this.$emit('submit', submitData)
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

.register-form {
  width: 100%;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;

  @media (max-width: 768px) {
    flex-direction: column;
    gap: 0;
  }
}

.form-group {
  flex: 1;
  min-width: 0;

  &.gender-group {
    margin-bottom: 0;
  }

  label {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    font-weight: 600;
    color: $color-text-primary;
    margin-bottom: 8px;

    .icon {
      width: 16px;
      height: 16px;
      color: $color-text-secondary;
    }
  }

  &.has-error {
    input {
      border-color: $color-secondary-danger;
      animation: shake 0.3s ease-in-out;
    }
  }

  input {
    width: 100%;
    padding: 10px 14px;
    font-size: 14px;
    border: 2px solid $color-border;
    border-radius: 10px;
    background: rgba(var(--color-primary-rgb), 0.03);
    color: $color-text-primary;
    transition: all 0.3s ease;
    font-family: inherit;

    &::placeholder {
      color: $color-text-disabled;
      font-size: 13px;
    }

    &:focus {
      outline: none;
      border-color: $color-primary;
      background: rgba(var(--color-primary-rgb), 0.05);
      box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
    }

    &.error {
      border-color: $color-secondary-danger;
    }
  }
}

.input-wrapper {
  position: relative;

  &.password-wrapper {
    input {
      padding-right: 50px;
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

.gender-options {
  display: flex;
  gap: 24px;
}

.radio-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;

  input[type="radio"] {
    position: absolute;
    opacity: 0;

    &:checked ~ .radio-mark {
      border-color: $color-primary;

      &::after {
        display: block;
      }
    }

    &:checked ~ .label-text {
      color: $color-primary;
    }
  }

  .radio-mark {
    width: 20px;
    height: 20px;
    border: 2px solid $color-border;
    border-radius: 50%;
    margin-right: 8px;
    position: relative;
    transition: all 0.2s ease;
    background: rgba(var(--color-primary-rgb), 0.05);

    &::after {
      content: '';
      position: absolute;
      display: none;
      left: 50%;
      top: 50%;
      transform: translate(-50%, -50%);
      width: 10px;
      height: 10px;
      border-radius: 50%;
      background: $color-primary;
    }
  }

  .label-text {
    color: $color-text-secondary;
    transition: color 0.2s ease;
  }
}

.btn-submit {
  width: 100%;
  padding: 14px;
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
  margin-bottom: 16px;
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

.login-prompt {
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
