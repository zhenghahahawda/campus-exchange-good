<template>
  <div class="animated-register-page">
    <!-- 注册容器 -->
    <transition name="fade-out">
      <div v-if="!registerSuccess" class="register-container">
        <!-- Register Form -->
        <div class="form-section">
          <div class="form-card">
            <div class="form-header">
              <h1>注册新账号</h1>
              <p>加入以物换物平台</p>
            </div>

            <RegisterForm
              ref="registerForm"
              :is-loading="isLoading"
              :is-verified="isVerified"
              @focus="handleFocus"
              @blur="handleBlur"
              @typing="handleTyping"
              @toggle-password="handleTogglePassword"
              @focus-change="handleFocusChange"
              @error="handleError"
              @submit="handleSubmit"
              @go-to-login="handleGoToLogin"
            >
              <template #captcha>
                <HumanVerification
                  ref="captcha"
                  :provider="captchaProvider"
                  :config="captchaConfig"
                  @verified="handleVerified"
                />
              </template>
            </RegisterForm>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import RegisterForm from '@/components/login/RegisterForm.vue'
import HumanVerification from '@/components/login/HumanVerification.vue'
import { useRegister } from '@/composables/useRegister'
import { useLoginUI } from '@/composables/useLoginUI'
import { useCaptcha } from '@/composables/useCaptcha'
import { MessageHelper } from '@/utils/messageHelper'
import { parseLoginError, ERROR_MESSAGES } from '@/utils/loginError'

export default {
  name: 'RegisterPage',
  layout: 'loginLayout',
  components: {
    RegisterForm,
    HumanVerification
  },
  setup() {
    // 注册逻辑
    const { isLoading, hasError, performRegister } = useRegister()

    // UI 状态管理
    const {
      focusType,
      showPassword,
      isTyping,
      loginSuccess: registerSuccess,
      handleFocus: onFocus,
      handleBlur: onBlur,
      handleTyping: onTyping,
      handleTogglePassword: onTogglePassword,
      handleFocusChange: onFocusChange,
      setLoginSuccess: setRegisterSuccess
    } = useLoginUI()

    // 验证码管理
    const {
      isVerified,
      captchaToken,
      handleVerified: onVerified,
      resetCaptcha: onResetCaptcha
    } = useCaptcha()

    return {
      // 注册
      isLoading,
      hasError,
      performRegister,

      // UI
      focusType,
      showPassword,
      isTyping,
      registerSuccess,
      onFocus,
      onBlur,
      onTyping,
      onTogglePassword,
      onFocusChange,
      setRegisterSuccess,

      // 验证码
      isVerified,
      captchaToken,
      onVerified,
      onResetCaptcha
    }
  },
  data() {
    return {
      // 验证码配置
      captchaProvider: 'simple',
      captchaConfig: {}
    }
  },
  methods: {
    // UI 交互处理
    handleFocus(type) {
      this.onFocus(type)
      this.hasError = false
    },

    handleBlur() {
      this.onBlur()
    },

    handleTyping(isTyping) {
      this.onTyping(isTyping)
    },

    handleTogglePassword(show) {
      this.onTogglePassword(show)
    },

    handleFocusChange(type) {
      this.onFocusChange(type)
    },

    handleError(hasError) {
      this.hasError = hasError
    },

    // 验证码处理
    handleVerified(verified, token) {
      this.onVerified(verified, token)
    },

    resetCaptcha() {
      this.onResetCaptcha(this.$refs.captcha)
    },

    // 注册处理
    async handleSubmit(formData) {
      // 验证人机验证
      if (!this.isVerified) {
        MessageHelper.warning(this, ERROR_MESSAGES.VERIFICATION_REQUIRED)
        return
      }

      try {
        // 执行注册
        await this.performRegister(this.$axios, formData)

        // 显示成功消息
        MessageHelper.success(this, '注册成功！即将跳转到登录页面...')

        // 触发消散动画
        this.setRegisterSuccess(true)

        // 等待动画完成后跳转到登录页
        setTimeout(() => {
          this.$router.push('/login-page/login')
        }, 1500)
      } catch (error) {
        this.handleRegisterError(error)
      }
    },

    // 处理注册错误
    handleRegisterError(error) {
      const { message, field } = parseLoginError(error)

      MessageHelper.error(this, message)

      if (field && this.$refs.registerForm) {
        this.$refs.registerForm.setError(field, message)
      }

      this.resetCaptcha()
    },

    // 跳转到登录页
    handleGoToLogin() {
      this.$router.push('/login-page/login')
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.animated-register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  z-index: 10;
}

.register-container {
  display: flex;
  justify-content: center;
  max-width: 750px;
  width: 100%;
  @include glass-card;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.form-section {
  width: 100%;
  padding: 50px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(var(--color-primary-rgb), 0.02);
  overflow-y: auto;
  max-height: 90vh;

  @media (max-width: 968px) {
    padding: 30px 20px;
  }
}

.form-card {
  width: 100%;
}

.form-header {
  margin-bottom: 24px;
  text-align: center;

  h1 {
    font-size: 28px;
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

// 注册成功消散动画
.fade-out-enter-active,
.fade-out-leave-active {
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-out-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(-20px);
  filter: blur(10px);
}
</style>
