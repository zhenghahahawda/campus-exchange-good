<template>
  <div class="characters-group" @mousemove="handleMouseMove" @mouseleave="handleMouseLeave">
    <!-- 主角色 - 只触发动画不切换 -->
    <div class="main-character" :class="characterClass">
      <svg viewBox="0 0 200 260" class="character-svg">
        <defs>
          <filter id="shadow">
            <feDropShadow dx="0" dy="8" stdDeviation="8" flood-opacity="0.2"/>
          </filter>
        </defs>

        <!-- 主角色身体 -->
        <g class="main-body">
          <rect x="20" y="20" width="160" height="220" rx="20" :fill="themeColors.primary" filter="url(#shadow)" class="body-shape" />

          <!-- 眼睛 -->
          <g class="eyes" :class="{ 'blink': isBlinking, 'look-down': focusType === 'account', 'vigilant': focusType === 'password', 'closed': showPassword && focusType === 'password' }">
            <!-- 睁开的眼睛 -->
            <g v-if="!showPassword || focusType !== 'password'">
              <g class="eye-left">
                <circle cx="70" cy="70" r="18" fill="white" />
                <circle :cx="70 + eyeOffsetX" :cy="70 + eyeOffsetY" r="10" fill="#2C3E50" class="pupil" />
                <circle :cx="73 + eyeOffsetX" :cy="67 + eyeOffsetY" r="4" fill="white" class="shine" />
              </g>
              <g class="eye-right">
                <circle cx="130" cy="70" r="18" fill="white" />
                <circle :cx="130 + eyeOffsetX" :cy="70 + eyeOffsetY" r="10" fill="#2C3E50" class="pupil" />
                <circle :cx="133 + eyeOffsetX" :cy="67 + eyeOffsetY" r="4" fill="white" class="shine" />
              </g>
            </g>

            <!-- 闭上的眼睛（显示密码时） -->
            <g v-else class="closed-eyes">
              <path d="M52 70 Q70 75 88 70" stroke="#2C3E50" stroke-width="3.5" stroke-linecap="round" fill="none" class="closed-eye-left" />
              <path d="M112 70 Q130 75 148 70" stroke="#2C3E50" stroke-width="3.5" stroke-linecap="round" fill="none" class="closed-eye-right" />
            </g>
          </g>

          <!-- 嘴巴 -->
          <g class="mouth">
            <path v-if="!isError && focusType !== 'password'"
                  d="M70 180 Q100 200 130 180"
                  fill="none" stroke="#2C3E50" stroke-width="4" stroke-linecap="round"
                  class="smile" />
            <path v-else-if="focusType === 'password' && !isError"
                  d="M70 185 Q100 183 130 185"
                  fill="none" stroke="#2C3E50" stroke-width="4" stroke-linecap="round"
                  class="nervous" />
            <path v-else
                  d="M70 190 Q100 175 130 190"
                  fill="none" stroke="#2C3E50" stroke-width="4" stroke-linecap="round"
                  class="sad" />
          </g>

          <!-- 装饰点 -->
          <circle cx="40" cy="120" r="3" :fill="themeColors.primaryLight" opacity="0.6" class="deco-dot" />
          <circle cx="160" cy="140" r="3" :fill="themeColors.primaryLight" opacity="0.6" class="deco-dot" />

          <!-- 腮红 -->
          <ellipse cx="50" cy="110" rx="12" ry="8" fill="#FF69B4" :opacity="blushOpacity" class="blush-left" />
          <ellipse cx="150" cy="110" rx="12" ry="8" fill="#FF69B4" :opacity="blushOpacity" class="blush-right" />
        </g>
      </svg>

      <!-- 浮动装饰元素 -->
      <div class="floating-elements">
        <div v-for="(particle, index) in particles" :key="index"
             class="particle"
             :style="particle.style">
          {{ particle.emoji }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AnimatedCharacter',
  props: {
    focusType: {
      type: String,
      default: '' // 'email', 'password', ''
    },
    isTyping: {
      type: Boolean,
      default: false
    },
    showPassword: {
      type: Boolean,
      default: false
    },
    isError: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      mouseX: 0,
      mouseY: 0,
      eyeOffsetX: 0,
      eyeOffsetY: 0,
      isBlinking: false,
      blinkInterval: null,
      themeColors: {
        primary: '#7C3AED',
        primaryLight: '#A78BFA',
        dark: '#2D3748'
      },
      particles: [],
      particleInterval: null
    }
  },
  computed: {
    characterClass() {
      return {
        'is-typing': this.isTyping,
        'is-error': this.isError,
        'is-focused': this.focusType,
        'is-password-focus': this.focusType === 'password',
        'is-email-focus': this.focusType === 'email',
        'show-password': this.showPassword
      }
    },
    blushOpacity() {
      if (this.focusType === 'password') return 0.7
      if (this.isTyping) return 0.5
      if (this.focusType) return 0.4
      return 0.25
    }
  },
  mounted() {
    this.startBlinking()
    this.startParticles()
    this.updateThemeColors()

    // 监听主题变化
    if (typeof window !== 'undefined') {
      this.themeObserver = new MutationObserver(() => {
        this.updateThemeColors()
      })

      this.themeObserver.observe(document.documentElement, {
        attributes: true,
        attributeFilter: ['data-theme', 'class']
      })
    }
  },
  beforeDestroy() {
    if (this.blinkInterval) {
      clearInterval(this.blinkInterval)
    }
    if (this.particleInterval) {
      clearInterval(this.particleInterval)
    }
    if (this.themeObserver) {
      this.themeObserver.disconnect()
    }
  },
  methods: {
    updateThemeColors() {
      if (typeof window === 'undefined') return

      const rootStyles = getComputedStyle(document.documentElement)
      const primaryColor = rootStyles.getPropertyValue('--color-primary').trim()
      const primaryRgb = rootStyles.getPropertyValue('--color-primary-rgb').trim()
      const textPrimary = rootStyles.getPropertyValue('--color-text-primary').trim()

      if (primaryColor && primaryRgb) {
        this.themeColors = {
          primary: primaryColor,
          primaryLight: `rgba(${primaryRgb}, 0.6)`,
          dark: textPrimary || '#2D3748'
        }
      }
    },
    handleMouseMove(event) {
      if (this.focusType) return

      const rect = event.currentTarget.getBoundingClientRect()
      const centerX = rect.left + rect.width / 2
      const centerY = rect.top + rect.height / 2

      const deltaX = event.clientX - centerX
      const deltaY = event.clientY - centerY

      const maxOffset = 6
      this.eyeOffsetX = Math.max(-maxOffset, Math.min(maxOffset, deltaX / 20))
      this.eyeOffsetY = Math.max(-maxOffset, Math.min(maxOffset, deltaY / 20))
    },
    handleMouseLeave() {
      if (!this.focusType) {
        this.eyeOffsetX = 0
        this.eyeOffsetY = 0
      }
    },
    startBlinking() {
      this.blinkInterval = setInterval(() => {
        // 只在非特殊状态下眨眼
        if (!this.showPassword && Math.random() > 0.6) {
          this.isBlinking = true
          setTimeout(() => {
            this.isBlinking = false
          }, 200)
        }
      }, 4000)
    },
    startParticles() {
      this.particleInterval = setInterval(() => {
        if (this.isTyping && this.particles.length < 5) {
          this.addParticle()
        }
      }, 800)
    },
    addParticle() {
      const emojis = ['✨', '💫', '⭐', '🌟', '💝', '🎉']
      const particle = {
        emoji: emojis[Math.floor(Math.random() * emojis.length)],
        style: {
          left: `${Math.random() * 80 + 10}%`,
          animationDuration: `${Math.random() * 2 + 2}s`,
          animationDelay: `${Math.random() * 0.5}s`
        }
      }
      this.particles.push(particle)

      setTimeout(() => {
        this.particles.shift()
      }, 4000)
    }
  },
  watch: {
    focusType(newVal) {
      // 更丝滑的眼睛移动动画
      if (newVal === 'account') {
        // 账号输入框：眼睛温和地向下看
        this.eyeOffsetX = 0
        this.eyeOffsetY = 6
      } else if (newVal === 'password') {
        // 密码输入框：眼睛稍微向下，表现出专注
        this.eyeOffsetX = 0
        this.eyeOffsetY = 4
      } else {
        // 失去焦点：眼睛回到中心
        this.eyeOffsetX = 0
        this.eyeOffsetY = 0
      }
    },
    isError(newVal) {
      if (newVal) {
        // 错误时的轻微摇晃
        const el = this.$el.querySelector('.main-character')
        if (el) {
          el.classList.add('gentle-shake')
          setTimeout(() => {
            el.classList.remove('gentle-shake')
          }, 500)
        }
      }
    },
    showPassword(newVal) {
      if (newVal && this.focusType === 'password') {
        // 显示密码时的轻微反应
        const el = this.$el.querySelector('.main-character')
        if (el) {
          el.classList.add('gentle-bounce')
          setTimeout(() => {
            el.classList.remove('gentle-bounce')
          }, 400)
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.characters-group {
  width: 100%;
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  perspective: 1000px;
}

.main-character {
  width: 200px;
  height: 260px;
  position: relative;
  animation: characterEnter 1.2s cubic-bezier(0.34, 1.56, 0.64, 1),
             floatAnimation 6s ease-in-out infinite 1.2s;
  transition: transform 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: pointer;
  will-change: transform;

  &:hover {
    transform: scale(1.05) translateY(-3px);
  }

  &.is-focused {
    animation: floatAnimation 6s ease-in-out infinite;
  }

  &.is-typing {
    animation: floatAnimation 6s ease-in-out infinite;
  }

  &.is-error {
    animation: floatAnimation 6s ease-in-out infinite;
  }
}

.character-svg {
  width: 100%;
  height: 100%;
  overflow: visible;
  filter: drop-shadow(0 10px 30px rgba(0, 0, 0, 0.15));
  position: relative;
  z-index: 1;
}

// ========== 主体动画 ==========
.main-body {
  animation: gentleBreathe 8s ease-in-out infinite;
  transform-origin: center;
  will-change: transform;

  .body-shape {
    transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
}

// ========== 眼睛动画 ==========
.eyes {
  transition: transform 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);

  &.blink {
    circle:not(.shine) {
      transform: scaleY(0.15);
      transform-origin: center;
      transition: transform 0.12s ease-in-out;
    }
  }

  &.look-down {
    animation: gentleLookDown 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
  }

  &.vigilant {
    animation: gentleVigilantLook 3s ease-in-out infinite;
  }

  &.closed {
    .closed-eyes {
      animation: gentleEyesClosed 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
    }
  }
}

.closed-eyes {
  .closed-eye-left,
  .closed-eye-right {
    animation: closedEyeTwitch 2s ease-in-out infinite;
  }

  .closed-eye-right {
    animation-delay: 1s;
  }
}

.pupil {
  transition: cx 0.5s cubic-bezier(0.34, 1.56, 0.64, 1),
              cy 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.shine {
  animation: gentleShine 3s ease-in-out infinite;
}

// ========== 嘴巴动画 ==========
.mouth {
  path {
    transition: d 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);

    &.smile {
      animation: gentleSmileWave 4s ease-in-out infinite;
    }

    &.nervous {
      animation: gentleNervousTwitch 1s ease-in-out infinite;
    }

    &.sad {
      animation: gentleSadShake 0.6s ease-in-out;
    }
  }
}

// ========== 装饰元素动画 ==========
.deco-dot {
  animation: dotFloat 3s ease-in-out infinite;

  &:nth-child(1) {
    animation-delay: 0s;
  }

  &:nth-child(2) {
    animation-delay: 1.5s;
  }
}

.blush-left, .blush-right {
  transition: opacity 0.4s ease;
  animation: blushPulse 3s ease-in-out infinite;
}

.blush-right {
  animation-delay: 0.5s;
}

// ========== 浮动粒子 ==========
.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: visible;
  z-index: 10;
}

.particle {
  position: absolute;
  font-size: 24px;
  animation: particleFloat 3s ease-out forwards;
  opacity: 0;
  z-index: 10;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

// ========== 关键帧动画 ==========
@keyframes characterEnter {
  0% {
    opacity: 0;
    transform: scale(0.8) translateY(-30px);
  }
  60% {
    opacity: 1;
    transform: scale(1.05) translateY(5px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

@keyframes floatAnimation {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-8px);
  }
}

@keyframes gentleFocusPulse {
  0% {
    transform: scale(1);
  }
  40% {
    transform: scale(1.03);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes gentleTypingBounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

@keyframes gentleBreathe {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.01);
  }
}

@keyframes gentleShine {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

@keyframes gentleLookDown {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(3px);
  }
}

@keyframes gentleVigilantLook {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-2px);
  }
}

@keyframes gentleEyesClosed {
  0% {
    opacity: 0;
    transform: scaleY(0.3);
  }
  100% {
    opacity: 1;
    transform: scaleY(1);
  }
}

@keyframes closedEyeTwitch {
  0%, 100% {
    transform: scaleX(1);
  }
  50% {
    transform: scaleX(0.97);
  }
}

@keyframes gentleSmileWave {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-1px);
  }
}

@keyframes gentleNervousTwitch {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-1px);
  }
  75% {
    transform: translateX(1px);
  }
}

@keyframes gentleSadShake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-2px);
  }
  75% {
    transform: translateX(2px);
  }
}

@keyframes dotFloat {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-8px) scale(1.3);
    opacity: 1;
  }
}

@keyframes blushPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

@keyframes particleFloat {
  0% {
    opacity: 0;
    transform: translateY(0) scale(0);
  }
  20% {
    opacity: 1;
    transform: translateY(-20px) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(-100px) scale(0.5) rotate(180deg);
  }
}

// 特殊动画类
.gentle-shake {
  animation: gentleShakeCharacter 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) !important;
}

@keyframes gentleShakeCharacter {
  0%, 100% {
    transform: translateX(0);
  }
  20%, 60% {
    transform: translateX(-8px);
  }
  40%, 80% {
    transform: translateX(8px);
  }
}

.gentle-bounce {
  animation: gentleBounceCharacter 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) !important;
}

@keyframes gentleBounceCharacter {
  0%, 100% {
    transform: scale(1) translateY(0);
  }
  50% {
    transform: scale(1.05) translateY(-10px);
  }
}

// 响应式
@media (max-width: 968px) {
  .main-character {
    width: 160px;
    height: 220px;
  }

  .characters-group {
    height: 240px;
  }
}
</style>
