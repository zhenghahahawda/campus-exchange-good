<template>
  <div class="animated-login-page">
    <transition name="fade-out">
      <div v-if="!loginSuccess" class="login-container">
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
import { showLoginSuccess, showWarning, showError, showInfo } from '@/utils/message'
import { parseLoginError, ERROR_MESSAGES } from '@/utils/loginError'
import { setRememberMe as saveRememberMe, clearRememberMe } from '@/utils/rememberMe'
import { setToken, setRefreshToken, setUserInfo, getToken, getUserInfo } from '@/utils/auth'

export default {
  name: 'AnimatedLoginPage',
  layout: 'loginLayout',
  components: { AnimatedCharacter, LoginForm, HumanVerification, OnboardingGuide },
  setup() {
    const { isLoading, hasError, performLogin } = useLogin()
    const {
      focusType, showPassword, isTyping, loginSuccess,
      handleFocus: onFocus, handleBlur: onBlur, handleTyping: onTyping,
      handleTogglePassword: onTogglePassword, handleFocusChange: onFocusChange,
      setLoginSuccess
    } = useLoginUI()
    const { isVerified, captchaToken, handleVerified: onVerified, resetCaptcha: onResetCaptcha } = useCaptcha()

    return {
      isLoading, hasError, performLogin,
      focusType, showPassword, isTyping, loginSuccess,
      onFocus, onBlur, onTyping, onTogglePassword, onFocusChange, setLoginSuccess,
      isVerified, captchaToken, onVerified, onResetCaptcha
    }
  },
  data() {
    return {
      captchaProvider: 'simple',
      captchaConfig: {}
    }
  },
  computed: {
    onboardingManager() {
      return useOnboarding(this.$router, this.$root)
    }
  },
  methods: {
    handleFocus(type) { this.onFocus(type); this.hasError = false },
    handleBlur() { this.onBlur() },
    handleTyping(isTyping) { this.onTyping(isTyping) },
    handleTogglePassword(show) { this.onTogglePassword(show) },
    handleFocusChange(type) { this.onFocusChange(type) },
    handleError(hasError) { this.hasError = hasError },
    handleVerified(verified, token) { this.onVerified(verified, token) },
    resetCaptcha() { this.onResetCaptcha(this.$refs.captcha) },

    async handleSubmit(formData) {
      if (!this.isVerified) {
        showWarning(this.$root, ERROR_MESSAGES.VERIFICATION_REQUIRED)
        return
      }

      try {
        const result = await this.performLogin(this.$axios, {
          account: formData.account,
          password: formData.password
        })

        // performLogin 返回 { success: true, data: res }
        // res = { code, message, data } , res.data = 用户数据
        const res = result.data
        const userData = (res && res.token) ? res : (res && res.data)

        if (!userData) {
          throw new Error('后端未返回用户数据')
        }

        if (userData.token) setToken(userData.token)
        if (userData.refreshToken) setRefreshToken(userData.refreshToken)

        // 重置 axios 登出状态，防止登录后请求被拦截
        if (this.$axios.resetAuthState) this.$axios.resetAuthState()

        setUserInfo({
          userId: userData.userId,
          username: userData.username,
          userType: userData.userType,
          status: userData.status,
          role: userData.role,
          email: userData.email,
          phone: userData.phone,
          school: userData.school,
          avatar: userData.avatar
        })

        formData.remember ? saveRememberMe(formData.account) : clearRememberMe()

        showLoginSuccess(this.$root, userData.username)
        this.setLoginSuccess(true)

        setTimeout(() => {
          this.onboardingManager.checkAndShowOnboarding(
            userData,
            () => this.redirectAfterLogin()
          )
        }, 600)
      } catch (error) {
        console.error('登录失败:', error)
        this.handleLoginError(error)
      }
    },

    handleLoginError(error) {
      const { message, field } = parseLoginError(error)
      showError(this.$root, message)
      if (field && this.$refs.loginForm) {
        this.$refs.loginForm.setError(field, message)
      }
      this.resetCaptcha()
    },

    redirectAfterLogin() {
      const userInfo = getUserInfo()
      setTimeout(() => {
        this.$router.push({
          path: '/',
          query: { userId: userInfo?.userId, username: userInfo?.username, userType: userInfo?.userType }
        })
      }, 500)
    },

    handleOnboardingSubmit(formData) {
      return this.onboardingManager.handleOnboardingSubmit(formData, this.$axios)
    },
    handleOnboardingSkip() { this.onboardingManager.handleOnboardingSkip() },
    handleOnboardingError(message) { this.onboardingManager.handleOnboardingError(message) },
    handleForgotPassword() {
      this.$prompt('请输入注册时的邮箱或手机号', '忘记密码', {
        confirmButtonText: '下一步',
        cancelButtonText: '取消',
        inputPlaceholder: '邮箱或手机号',
        inputValidator: (v) => !!v || '请输入账号',
        lockScroll: false
      }).then(({ value: account }) => {
        this.$prompt('请设置新密码', '重置密码', {
          confirmButtonText: '确认重置',
          cancelButtonText: '取消',
          inputType: 'password',
          inputPlaceholder: '请输入新密码（至少6位）',
          inputValidator: (v) => (v && v.length >= 6) || '密码至少6位',
          lockScroll: false
        }).then(async ({ value: newPassword }) => {
          try {
            await this.$axios.post('/auth/reset-password', { account, newPassword })
            this.$message.success('密码重置成功，请用新密码登录')
          } catch (e) {
            const msg = e.message || ''
            if (msg.includes('不存在') || msg.includes('找不到')) {
              this.$message.error('账号不存在')
            } else {
              this.$message.error('重置失败，请稍后重试')
            }
          }
        }).catch(() => {})
      }).catch(() => {})
    },
    handleSignUp() { this.$router.push('/login-page/register') }
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

.fade-out-enter-active,
.fade-out-leave-active {
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-out-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(-20px);
  filter: blur(10px);
}

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
