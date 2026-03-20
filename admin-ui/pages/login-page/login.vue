<template>
  <div class="animated-login-page">
    <!-- 登录容器 - 添加消散动画 -->
    <transition name="fade-out">
      <div v-if="!loginSuccess" class="login-container">
        <!-- Left Side - Character -->
        <div class="characters-section">
          <div class="character-wrapper">
            <AnimatedCharacter
              :focusType="focusType"
              :isTyping="isTyping"
              :showPassword="showPassword"
              :isError="hasError"
              class="main-character"
            />
          </div>
        </div>

        <!-- Right Side - Login Form -->
        <div class="form-section">
          <div class="form-card">
            <div class="form-header">
              <h1>以物换物平台登录</h1>
            </div>

            <LoginForm
              ref="loginForm"
              :is-loading="isLoading"
              :is-verified="isVerified"
              @focus="handleFocus"
              @blur="handleBlur"
              @typing="handleTyping"
              @toggle-password="handleTogglePassword"
              @focus-change="handleFocusChange"
              @error="handleError"
              @submit="handleSubmit"
              @forgot-password="handleForgotPassword"
              @sign-up="handleSignUp"
            >
              <template #captcha>
                <HumanVerification
                  ref="captcha"
                  :provider="captchaProvider"
                  :config="captchaConfig"
                  @verified="handleVerified"
                />
              </template>
            </LoginForm>
          </div>
        </div>
      </div>
    </transition>

    <!-- 首次登录引导 - 页面内流程 -->
    <transition name="slide-in">
      <div v-if="onboardingManager.showOnboarding.value" class="onboarding-wrapper">
        <OnboardingGuide
          :visible="onboardingManager.showOnboarding.value"
          :user-info="onboardingManager.currentUserInfo.value"
          @submit="handleOnboardingSubmit"
          @skip="handleOnboardingSkip"
          @error="handleOnboardingError"
        />
      </div>
    </transition>
  </div>
</template>

<script>
import AnimatedCharacter from '@/components/login/AnimatedCharacter.vue'
import LoginForm from '@/components/login/LoginForm.vue'
import HumanVerification from '@/components/login/HumanVerification.vue'
import OnboardingGuide from '@/components/onboarding/OnboardingGuide.vue'
import { useLogin } from '@/composables/useLogin'
import { useLoginUI } from '@/composables/useLoginUI'
import { useCaptcha } from '@/composables/useCaptcha'
import { useOnboarding } from '@/composables/useOnboarding'
import { MessageHelper } from '@/utils/messageHelper'
import { parseLoginError, ERROR_MESSAGES } from '@/utils/loginError'
import { saveRememberMe, clearRememberMe } from '@/utils/rememberMe'

export default {
  name: 'AnimatedLoginPage',
  layout: 'loginLayout',
  components: {
    AnimatedCharacter,
    LoginForm,
    HumanVerification,
    OnboardingGuide
  },
  setup() {
    // 登录逻辑
    const { isLoading, hasError, performLogin } = useLogin()

    // UI 状态管理
    const {
      focusType,
      showPassword,
      isTyping,
      loginSuccess,
      handleFocus: onFocus,
      handleBlur: onBlur,
      handleTyping: onTyping,
      handleTogglePassword: onTogglePassword,
      handleFocusChange: onFocusChange,
      setLoginSuccess
    } = useLoginUI()

    // 验证码管理
    const {
      isVerified,
      captchaToken,
      handleVerified: onVerified,
      resetCaptcha: onResetCaptcha
    } = useCaptcha()

    return {
      // 登录
      isLoading,
      hasError,
      performLogin,

      // UI
      focusType,
      showPassword,
      isTyping,
      loginSuccess,
      onFocus,
      onBlur,
      onTyping,
      onTogglePassword,
      onFocusChange,
      setLoginSuccess,

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
  computed: {
    // 引导管理
    onboardingManager() {
      return useOnboarding(this.$router, this.$root)
    }
  },
  methods: {
    // UI 交互处理（简化版）
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

    // 登录处理
    async handleSubmit(formData) {
      // 验证人机验证
      if (!this.isVerified) {
        MessageHelper.warning(this, ERROR_MESSAGES.VERIFICATION_REQUIRED)
        return
      }

      try {
        // 执行登录
        const result = await this.performLogin(this.$axios, {
          account: formData.account,
          password: formData.password
        })

        // 获取后端返回的用户数据
        // result.data 是后端响应体 { code: 20000, data: { ... } }
        // 所以用户信息在 result.data.data 中
        const userData = result.data.data

        // 检查是否为管理员 (仅允许 userType 为 1 的用户登录)
        // 使用宽松比较以兼容字符串 "1"
        const isAdmin = userData && (userData.userType == 1 || userData.role === 'admin')

        // 仅允许管理员登录
        if (!isAdmin) {
          MessageHelper.error(this, '抱歉，此系统仅限管理员访问')
          // 如果不是管理员，应该清除已保存的 token，防止状态残留
          const { clearAuth } = require('@/utils/auth')
          clearAuth()
          this.resetCaptcha()
          return
        }

        // 处理"记住我"（仅保存账号，不保存密码）
        formData.remember
          ? saveRememberMe(formData.account)
          : clearRememberMe()

        // 显示成功消息
        MessageHelper.loginSuccess(this, result.data.username)

        // 登录成功后，将用户身份信息写入 URL query 用于校验
        const authQuery = {
          ...this.$route.query,
          uid: result.data.userId,
          username: result.data.username,
          userType: result.data.userType
        }
        
        // 同时保存 Token 到 Cookie，双重保险
        if (result.data.token) {
          const { setToken } = require('@/utils/auth')
          setToken(result.data.token, result.data.expiresIn)
        }
        this.$router.replace({ path: this.$route.path, query: authQuery }).catch(() => {})

        // 同时用 history API 确保 URL 立即更新
        if (process.client) {
          const params = new URLSearchParams(window.location.search)
          params.set('uid', result.data.userId)
          params.set('username', result.data.username)
          params.set('userType', result.data.userType)
          const newUrl = `${window.location.pathname}?${params.toString()}`
          window.history.replaceState({}, '', newUrl)
        }

        // 触发消散动画
        this.setLoginSuccess(true)

        // 等待动画完成后检查首次登录
        setTimeout(() => {
          this.onboardingManager.checkAndShowOnboarding(
            result.data,
            () => this.redirectAfterLogin()
          )
        }, 600)
      } catch (error) {
        this.handleLoginError(error)
      }
    },

    // 处理登录错误
    handleLoginError(error) {
      const { message, field } = parseLoginError(error)

      MessageHelper.error(this, message)

      if (field && this.$refs.loginForm) {
        this.$refs.loginForm.setError(field, message)
      }

      this.resetCaptcha()
    },

    // 登录成功后跳转
    redirectAfterLogin() {
      const { getUserInfo } = require('@/utils/auth')
      const userInfo = getUserInfo()
      const authQuery = {
        uid: userInfo?.userId,
        username: userInfo?.username,
        userType: userInfo?.userType
      }

      // 判断是否为管理员：检查 userType=1
      const isAdmin = userInfo && userInfo.userType === 1

      if (isAdmin) {
        // 管理员跳转到首页
        setTimeout(() => {
          this.$router.push({ path: '/', query: authQuery })
        }, 500)
      } else {
        // 非管理员不应登录成功，但作为兜底跳转到首页或显示错误
        showError(this.$root, '权限不足，无法访问后台')
        setTimeout(() => {
            this.$router.push({ path: '/login-page/login' })
        }, 1500)
      }
    },

    // 引导相关方法
    handleOnboardingSubmit(formData) {
      return this.onboardingManager.handleOnboardingSubmit(formData, this.$axios)
    },

    handleOnboardingSkip() {
      this.onboardingManager.handleOnboardingSkip()
    },

    handleOnboardingError(message) {
      this.onboardingManager.handleOnboardingError(message)
    },

    // 其他操作
    handleForgotPassword() {
      MessageHelper.info(this, '密码重置功能开发中...')
    },

    handleSignUp() {
      this.$router.push('/login-page/register')
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.animated-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  z-index: 10;
}

.login-container {
  display: flex;
  max-width: 1100px;
  width: 100%;
  @include glass-card;
  overflow: hidden;
  position: relative;
  z-index: 1;

  @media (max-width: 968px) {
    flex-direction: column;
  }
}

.characters-section {
  flex: 1;
  background: $color-bg-surface;
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  border-right: 1px solid var(--glass-border);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background:
      radial-gradient(circle at 20% 30%, rgba(var(--color-primary-rgb), 0.08) 0%, transparent 50%),
      radial-gradient(circle at 80% 70%, rgba(var(--color-primary-rgb), 0.05) 0%, transparent 50%);
    pointer-events: none;
  }

  @media (max-width: 968px) {
    padding: 40px 20px;
    border-right: none;
    border-bottom: 1px solid var(--glass-border);
  }
}

.character-wrapper {
  margin-bottom: 40px;
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;

  .main-character {
    transform: scale(1.2);
    filter: drop-shadow(0 10px 30px rgba(var(--color-primary-rgb), 0.3));
  }
}

.welcome-text {
  text-align: center;
  color: $color-text-primary;

  h2 {
    font-size: 32px;
    font-weight: 700;
    margin: 0 0 8px 0;
    color: $color-primary;
  }

  p {
    font-size: 16px;
    color: $color-text-secondary;
    margin: 0;
  }
}

.form-section {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(var(--color-primary-rgb), 0.02);

  @media (max-width: 968px) {
    padding: 40px 30px;
  }
}

.form-card {
  width: 100%;
  max-width: 420px;
}

.form-header {
  margin-bottom: 32px;
  text-align: center;

  h1 {
    font-size: 32px;
    font-weight: 700;
    color: $color-text-primary;
    margin: 0;
  }
}

// 登录成功消散动画
.fade-out-enter-active,
.fade-out-leave-active {
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-out-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(-20px);
  filter: blur(10px);
}

// 引导页面滑入动画
.onboarding-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.slide-in-enter-active {
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: 0.3s;
}

.slide-in-enter {
  opacity: 0;
  transform: translateY(30px) scale(0.95);
}

.slide-in-enter-to {
  opacity: 1;
  transform: translateY(0) scale(1);
}
</style>
