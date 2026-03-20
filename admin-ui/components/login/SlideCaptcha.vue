<template>
  <div class="captcha-container" :class="{ 'verified': isVerified, 'dragging': isDragging }">
    <div class="captcha-track">
      <div class="captcha-fill" :style="{ width: sliderPosition + 'px' }"></div>
      <div class="captcha-text">
        <span v-if="!isVerified" class="captcha-hint">{{ captchaText }}</span>
        <span v-else class="verified-text">
          <i class="check-icon">✓</i>
          <span>验证成功</span>
        </span>
      </div>
      <div
        class="captcha-slider"
        :class="{ 'verified': isVerified, 'dragging': isDragging }"
        :style="{ transform: `translate(${sliderPosition}px, -50%)` }"
        @mousedown="handleSliderStart"
        @touchstart="handleSliderStart"
      >
        <div class="slider-icon">
          <svg v-if="!isVerified" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M7 4L13 10L7 16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <svg v-else width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M4 10L8 14L16 6" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SlideCaptcha',
  data() {
    return {
      isVerified: false,
      sliderPosition: 0,
      isDragging: false,
      captchaText: '请向右滑动验证'
    }
  },
  methods: {
    handleSliderStart(e) {
      if (this.isVerified) return

      e.preventDefault()
      this.isDragging = true

      const startX = e.type === 'mousedown' ? e.clientX : e.touches[0].clientX
      const sliderElement = e.currentTarget
      const trackElement = sliderElement.parentElement
      const trackRect = trackElement.getBoundingClientRect()
      const sliderWidth = sliderElement.offsetWidth
      const maxWidth = trackRect.width - sliderWidth

      let lastX = startX

      const handleMove = (e) => {
        if (!this.isDragging) return

        const currentX = e.type === 'mousemove' ? e.clientX : e.touches[0].clientX
        const diff = currentX - lastX

        // 计算新位置
        let newPosition = this.sliderPosition + diff
        newPosition = Math.max(0, Math.min(newPosition, maxWidth))

        this.sliderPosition = newPosition
        lastX = currentX

        // 验证成功
        if (this.sliderPosition >= maxWidth - 2) {
          this.isVerified = true
          this.isDragging = false
          this.sliderPosition = maxWidth

          // 添加触觉反馈（如果支持）
          if (navigator.vibrate) {
            navigator.vibrate(50)
          }

          this.$emit('verified', true)
          this.$root.$emit('show-island-message', '验证成功！', 'success')
          removeListeners()
        }
      }

      const handleEnd = () => {
        if (!this.isVerified && this.isDragging) {
          // 验证失败，滑块平滑回到起点
          this.isDragging = false
          const startPos = this.sliderPosition
          const duration = 300
          const startTime = Date.now()

          const animate = () => {
            const elapsed = Date.now() - startTime
            const progress = Math.min(elapsed / duration, 1)

            // 使用缓动函数
            const easeOut = 1 - Math.pow(1 - progress, 3)
            this.sliderPosition = startPos * (1 - easeOut)

            if (progress < 1) {
              requestAnimationFrame(animate)
            } else {
              this.sliderPosition = 0
            }
          }

          animate()
          this.$root.$emit('show-island-message', '请完成验证', 'warning')
        }
        this.isDragging = false
        removeListeners()
      }

      const removeListeners = () => {
        document.removeEventListener('mousemove', handleMove)
        document.removeEventListener('mouseup', handleEnd)
        document.removeEventListener('touchmove', handleMove)
        document.removeEventListener('touchend', handleEnd)
      }

      document.addEventListener('mousemove', handleMove)
      document.addEventListener('mouseup', handleEnd)
      document.addEventListener('touchmove', handleMove, { passive: false })
      document.addEventListener('touchend', handleEnd)
    },
    reset() {
      this.isVerified = false
      this.sliderPosition = 0
      this.isDragging = false
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.captcha-container {
  margin-bottom: 24px;
  user-select: none;

  &.verified {
    .captcha-track {
      border-color: transparent;
      background: linear-gradient(90deg,
        rgba(var(--color-secondary-success-rgb), 0.1) 0%,
        rgba(var(--color-secondary-success-rgb), 0.15) 100%
      );
    }

    .captcha-fill {
      background: linear-gradient(90deg,
        rgba(var(--color-secondary-success-rgb), 0.3) 0%,
        $color-secondary-success 100%
      );
    }

    .captcha-slider {
      background: $color-secondary-success;
      box-shadow: 0 4px 16px rgba(var(--color-secondary-success-rgb), 0.4);
    }
  }
}

.captcha-track {
  position: relative;
  width: 100%;
  height: 44px;
  background: rgba(var(--color-primary-rgb), 0.04);
  border: 1.5px solid rgba(var(--color-border-rgb), 0.3);
  border-radius: 22px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
}

.captcha-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg,
    rgba(var(--color-primary-rgb), 0.08) 0%,
    rgba(var(--color-primary-rgb), 0.15) 100%
  );
  border-radius: 22px;
  transition: width 0.1s ease-out, background 0.4s ease;
  pointer-events: none;
}

.captcha-text {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  z-index: 1;

  .captcha-hint {
    font-size: 14px;
    font-weight: 500;
    color: $color-text-secondary;
    letter-spacing: 0.3px;
    transition: all 0.3s ease;
  }

  .verified-text {
    display: flex;
    align-items: center;
    gap: 8px;
    color: $color-secondary-success;
    font-weight: 600;
    font-size: 15px;
    animation: verifiedPop 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);

    .check-icon {
      font-style: normal;
      font-size: 20px;
      font-weight: bold;
      animation: checkRotate 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    }
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

.captcha-slider {
  position: absolute;
  left: 0;
  top: 50%;
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  box-shadow:
    0 2px 12px rgba(0, 0, 0, 0.08),
    0 8px 24px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    inset 0 -1px 0 rgba(0, 0, 0, 0.05);
  z-index: 2;
  will-change: transform;

  &::before {
    content: '';
    position: absolute;
    inset: -1px;
    border-radius: 50%;
    padding: 1px;
    background: linear-gradient(135deg,
      rgba(255, 255, 255, 0.8) 0%,
      rgba(255, 255, 255, 0.2) 50%,
      rgba(0, 0, 0, 0.05) 100%
    );
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask-composite: exclude;
    pointer-events: none;
  }

  &:hover:not(.verified):not(.dragging) {
    background: rgba(255, 255, 255, 0.98);
    box-shadow:
      0 4px 16px rgba(0, 0, 0, 0.1),
      0 12px 32px rgba(0, 0, 0, 0.08),
      inset 0 1px 0 rgba(255, 255, 255, 1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.08);

    .slider-icon {
      color: $color-primary;
      transform: scale(1.15);
    }
  }

  &.dragging {
    cursor: grabbing;
    box-shadow:
      0 6px 20px rgba(0, 0, 0, 0.12),
      0 16px 40px rgba(0, 0, 0, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 1),
      inset 0 -1px 0 rgba(0, 0, 0, 0.1);
  }

  &.verified {
    cursor: default;
    background: rgba(var(--color-secondary-success-rgb), 0.95);
    backdrop-filter: blur(20px) saturate(180%);
    border-color: rgba(var(--color-secondary-success-rgb), 0.8);

    &::before {
      background: linear-gradient(135deg,
        rgba(255, 255, 255, 0.6) 0%,
        rgba(255, 255, 255, 0.1) 50%,
        rgba(0, 0, 0, 0.1) 100%
      );
    }

    .slider-icon {
      color: white;
      animation: iconSuccess 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    }

    &:hover {
      box-shadow:
        0 6px 24px rgba(var(--color-secondary-success-rgb), 0.4),
        0 12px 48px rgba(var(--color-secondary-success-rgb), 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.5);
    }
  }

  .slider-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    color: $color-text-secondary;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

    svg {
      width: 18px;
      height: 18px;
      filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
    }
  }
}

@keyframes iconSuccess {
  0% {
    transform: scale(0) rotate(-180deg);
    opacity: 0;
  }
  50% {
    transform: scale(1.3) rotate(10deg);
  }
  100% {
    transform: scale(1) rotate(0deg);
    opacity: 1;
  }
}
</style>
