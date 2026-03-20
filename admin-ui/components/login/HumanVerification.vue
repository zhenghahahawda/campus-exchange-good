<template>
  <div class="human-verification">
    <!-- Google reCAPTCHA v3 (隐藏式) -->
    <div v-if="provider === 'recaptcha-v3'" class="recaptcha-v3-container">
      <div id="recaptcha-v3"></div>
    </div>

    <!-- Google reCAPTCHA v2 (复选框) -->
    <div v-else-if="provider === 'recaptcha-v2'" class="recaptcha-v2-container">
      <div id="recaptcha-v2"></div>
    </div>

    <!-- hCaptcha -->
    <div v-else-if="provider === 'hcaptcha'" class="hcaptcha-container">
      <div id="hcaptcha"></div>
    </div>

    <!-- 腾讯云验证码 -->
    <div v-else-if="provider === 'tencent'" class="tencent-captcha-container">
      <button
        type="button"
        class="captcha-button"
        :class="{ 'verified': isVerified }"
        @click="showTencentCaptcha"
      >
        <span v-if="!isVerified" class="button-content">
          <svg class="icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 2C5.58 2 2 5.58 2 10C2 14.42 5.58 18 10 18C14.42 18 18 14.42 18 10C18 5.58 14.42 2 10 2ZM10 16C6.69 16 4 13.31 4 10C4 6.69 6.69 4 10 4C13.31 4 16 6.69 16 10C16 13.31 13.31 16 10 16Z" fill="currentColor"/>
            <path d="M10 6C7.79 6 6 7.79 6 10C6 12.21 7.79 14 10 14C12.21 14 14 12.21 14 10C14 7.79 12.21 6 10 6Z" fill="currentColor"/>
          </svg>
          <span>点击完成人机验证</span>
        </span>
        <span v-else class="verified-content">
          <svg class="check-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M4 10L8 14L16 6" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>验证成功</span>
        </span>
      </button>
    </div>

    <!-- 阿里云验证码 -->
    <div v-else-if="provider === 'aliyun'" class="aliyun-captcha-container">
      <button
        type="button"
        class="captcha-button"
        :class="{ 'verified': isVerified }"
        @click="showAliyunCaptcha"
      >
        <span v-if="!isVerified" class="button-content">
          <svg class="icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 2C5.58 2 2 5.58 2 10C2 14.42 5.58 18 10 18C14.42 18 18 14.42 18 10C18 5.58 14.42 2 10 2ZM10 16C6.69 16 4 13.31 4 10C4 6.69 6.69 4 10 4C13.31 4 16 6.69 16 10C16 13.31 13.31 16 10 16Z" fill="currentColor"/>
            <path d="M10 6C7.79 6 6 7.79 6 10C6 12.21 7.79 14 10 14C12.21 14 14 12.21 14 10C14 7.79 12.21 6 10 6Z" fill="currentColor"/>
          </svg>
          <span>点击完成人机验证</span>
        </span>
        <span v-else class="verified-content">
          <svg class="check-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M4 10L8 14L16 6" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>验证成功</span>
        </span>
      </button>
    </div>

    <!-- 自定义简易验证（开发环境） -->
    <div v-else-if="provider === 'simple'" class="simple-captcha-container">
      <button
        type="button"
        class="captcha-button"
        :class="{ 'verified': isVerified }"
        @click="handleSimpleVerify"
      >
        <span v-if="!isVerified" class="button-content">
          <svg class="icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 2C5.58 2 2 5.58 2 10C2 14.42 5.58 18 10 18C14.42 18 18 14.42 18 10C18 5.58 14.42 2 10 2ZM10 16C6.69 16 4 13.31 4 10C4 6.69 6.69 4 10 4C13.31 4 16 6.69 16 10C16 13.31 13.31 16 10 16Z" fill="currentColor"/>
            <path d="M10 6C7.79 6 6 7.79 6 10C6 12.21 7.79 14 10 14C12.21 14 14 12.21 14 10C14 7.79 12.21 6 10 6Z" fill="currentColor"/>
          </svg>
          <span>点击完成人机验证</span>
        </span>
        <span v-else class="verified-content">
          <svg class="check-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M4 10L8 14L16 6" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>验证成功</span>
        </span>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HumanVerification',
  props: {
    // 验证服务提供商: 'recaptcha-v3', 'recaptcha-v2', 'hcaptcha', 'tencent', 'aliyun', 'simple'
    provider: {
      type: String,
      default: 'simple'
    },
    // 各服务的配置
    config: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      isVerified: false,
      verifyToken: null,
      scriptLoaded: false
    }
  },
  mounted() {
    this.initCaptcha()
  },
  methods: {
    async initCaptcha() {
      switch (this.provider) {
        case 'recaptcha-v3':
          await this.loadRecaptchaV3()
          break
        case 'recaptcha-v2':
          await this.loadRecaptchaV2()
          break
        case 'hcaptcha':
          await this.loadHCaptcha()
          break
        case 'tencent':
          await this.loadTencentCaptcha()
          break
        case 'aliyun':
          await this.loadAliyunCaptcha()
          break
        case 'simple':
          // 简易验证不需要加载外部脚本
          break
      }
    },

    // Google reCAPTCHA v3
    async loadRecaptchaV3() {
      const siteKey = this.config.siteKey || process.env.RECAPTCHA_V3_SITE_KEY
      if (!siteKey) {
        console.error('reCAPTCHA v3 site key not configured')
        return
      }

      if (window.grecaptcha) {
        this.executeRecaptchaV3()
        return
      }

      const script = document.createElement('script')
      script.src = `https://www.recaptcha.net/recaptcha/api.js?render=${siteKey}`
      script.async = true
      script.defer = true
      script.onload = () => {
        this.scriptLoaded = true
        this.executeRecaptchaV3()
      }
      document.head.appendChild(script)
    },

    executeRecaptchaV3() {
      const siteKey = this.config.siteKey || process.env.RECAPTCHA_V3_SITE_KEY
      window.grecaptcha.ready(() => {
        window.grecaptcha.execute(siteKey, { action: 'login' }).then((token) => {
          this.isVerified = true
          this.verifyToken = token
          this.$emit('verified', true, token)
          this.$root.$emit('show-island-message', '人机验证通过', 'success')
        })
      })
    },

    // Google reCAPTCHA v2
    async loadRecaptchaV2() {
      const siteKey = this.config.siteKey || process.env.RECAPTCHA_V2_SITE_KEY
      if (!siteKey) {
        console.error('reCAPTCHA v2 site key not configured')
        return
      }

      if (window.grecaptcha) {
        this.renderRecaptchaV2()
        return
      }

      const script = document.createElement('script')
      script.src = 'https://www.recaptcha.net/recaptcha/api.js'
      script.async = true
      script.defer = true
      script.onload = () => {
        this.scriptLoaded = true
        this.renderRecaptchaV2()
      }
      document.head.appendChild(script)
    },

    renderRecaptchaV2() {
      const siteKey = this.config.siteKey || process.env.RECAPTCHA_V2_SITE_KEY
      window.grecaptcha.render('recaptcha-v2', {
        sitekey: siteKey,
        callback: (token) => {
          this.isVerified = true
          this.verifyToken = token
          this.$emit('verified', true, token)
          this.$root.$emit('show-island-message', '人机验证通过', 'success')
        }
      })
    },

    // hCaptcha
    async loadHCaptcha() {
      const siteKey = this.config.siteKey || process.env.HCAPTCHA_SITE_KEY
      if (!siteKey) {
        console.error('hCaptcha site key not configured')
        return
      }

      if (window.hcaptcha) {
        this.renderHCaptcha()
        return
      }

      const script = document.createElement('script')
      script.src = 'https://js.hcaptcha.com/1/api.js'
      script.async = true
      script.defer = true
      script.onload = () => {
        this.scriptLoaded = true
        this.renderHCaptcha()
      }
      document.head.appendChild(script)
    },

    renderHCaptcha() {
      const siteKey = this.config.siteKey || process.env.HCAPTCHA_SITE_KEY
      window.hcaptcha.render('hcaptcha', {
        sitekey: siteKey,
        callback: (token) => {
          this.isVerified = true
          this.verifyToken = token
          this.$emit('verified', true, token)
          this.$root.$emit('show-island-message', '人机验证通过', 'success')
        }
      })
    },

    // 腾讯云验证码
    async loadTencentCaptcha() {
      if (window.TencentCaptcha) {
        return
      }

      const script = document.createElement('script')
      script.src = 'https://ssl.captcha.qq.com/TCaptcha.js'
      script.async = true
      script.onload = () => {
        this.scriptLoaded = true
      }
      document.head.appendChild(script)
    },

    showTencentCaptcha() {
      const appId = this.config.appId || process.env.TENCENT_CAPTCHA_APP_ID
      if (!appId) {
        console.error('Tencent Captcha app ID not configured')
        return
      }

      if (!window.TencentCaptcha) {
        this.$root.$emit('show-island-message', '验证码加载中，请稍候...', 'info')
        return
      }

      const captcha = new window.TencentCaptcha(appId, (res) => {
        if (res.ret === 0) {
          this.isVerified = true
          this.verifyToken = res.ticket
          this.$emit('verified', true, {
            ticket: res.ticket,
            randstr: res.randstr
          })
          this.$root.$emit('show-island-message', '人机验证通过', 'success')
        }
      })
      captcha.show()
    },

    // 阿里云验证码
    async loadAliyunCaptcha() {
      if (window.AWSC) {
        return
      }

      const script = document.createElement('script')
      script.src = 'https://g.alicdn.com/sd/nch5/index.js'
      script.async = true
      script.onload = () => {
        this.scriptLoaded = true
      }
      document.head.appendChild(script)
    },

    showAliyunCaptcha() {
      const appKey = this.config.appKey || process.env.ALIYUN_CAPTCHA_APP_KEY
      const scene = this.config.scene || 'nc_login'

      if (!appKey) {
        console.error('Aliyun Captcha app key not configured')
        return
      }

      if (!window.AWSC) {
        this.$root.$emit('show-island-message', '验证码加载中，请稍候...', 'info')
        return
      }

      window.AWSC.use('nc', (state, module) => {
        window.nc = module.init({
          appkey: appKey,
          scene: scene,
          renderTo: '#aliyun-captcha',
          success: (data) => {
            this.isVerified = true
            this.verifyToken = data.sessionId
            this.$emit('verified', true, {
              sessionId: data.sessionId,
              sig: data.sig,
              token: data.token
            })
            this.$root.$emit('show-island-message', '人机验证通过', 'success')
          }
        })
      })
    },

    // 简易验证（开发环境使用）
    handleSimpleVerify() {
      this.isVerified = true
      this.verifyToken = 'simple_verify_' + Date.now()
      this.$emit('verified', true, this.verifyToken)
      this.$root.$emit('show-island-message', '人机验证通过', 'success')
    },

    // 重置验证状态
    reset() {
      this.isVerified = false
      this.verifyToken = null

      // 重置各个验证服务
      if (this.provider === 'recaptcha-v2' && window.grecaptcha) {
        window.grecaptcha.reset()
      } else if (this.provider === 'hcaptcha' && window.hcaptcha) {
        window.hcaptcha.reset()
      }
    },

    // 获取验证token
    getToken() {
      return this.verifyToken
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.human-verification {
  margin-bottom: 24px;
}

.captcha-button {
  width: 100%;
  height: 48px;
  border: 2px solid $color-border;
  border-radius: 12px;
  background: rgba(var(--color-primary-rgb), 0.03);
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 500;
  color: $color-text-secondary;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-radius: 50%;
    background: rgba(var(--color-primary-rgb), 0.1);
    transform: translate(-50%, -50%);
    transition: width 0.6s, height 0.6s;
  }

  &:hover:not(.verified) {
    background: rgba(var(--color-primary-rgb), 0.05);
    border-color: $color-primary;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.15);

    &::before {
      width: 300px;
      height: 300px;
    }

    .icon {
      color: $color-primary;
      transform: scale(1.1) rotate(5deg);
    }
  }

  &:active:not(.verified) {
    transform: translateY(0);
  }

  &.verified {
    background: linear-gradient(135deg,
      rgba(var(--color-secondary-success-rgb), 0.08) 0%,
      rgba(var(--color-secondary-success-rgb), 0.12) 100%
    );
    border-color: $color-secondary-success;
    cursor: default;
    box-shadow: 0 4px 12px rgba(var(--color-secondary-success-rgb), 0.2);

    &::before {
      display: none;
    }

    .verified-content {
      color: $color-secondary-success;
      animation: verifiedPop 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    }

    .check-icon {
      animation: checkRotate 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    }
  }

  .button-content,
  .verified-content {
    display: flex;
    align-items: center;
    gap: 10px;
    position: relative;
    z-index: 1;
  }

  .icon {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    color: $color-text-secondary;
  }

  .check-icon {
    color: $color-secondary-success;
  }
}

@keyframes verifiedPop {
  0% {
    opacity: 0;
    transform: scale(0.5);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes checkRotate {
  0% {
    transform: rotate(-180deg) scale(0);
  }
  100% {
    transform: rotate(0deg) scale(1);
  }
}

// reCAPTCHA 和 hCaptcha 容器样式
.recaptcha-v2-container,
.hcaptcha-container {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
  padding: 12px;
  background: rgba(var(--color-primary-rgb), 0.03);
  border: 2px solid $color-border;
  border-radius: 12px;
  transition: all 0.3s ease;

  &:hover {
    border-color: rgba(var(--color-primary-rgb), 0.3);
    background: rgba(var(--color-primary-rgb), 0.05);
  }
}
</style>
