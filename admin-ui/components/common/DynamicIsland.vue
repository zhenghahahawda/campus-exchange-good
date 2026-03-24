<template>
  <div
    class="dynamic-island"
    :class="{
      'expanded': isIslandExpanded,
      'floating': floating,
      'standalone': standalone
    }"
    @mouseenter="expandIsland"
    @mouseleave="collapseIsland"
  >
    <div class="island-content">
      <!-- 默认状态：天气、日期时间、温度 -->
      <transition name="fade-scale">
        <div class="island-default" v-if="!isIslandExpanded && !islandMessage">
          <span class="weather-icon">{{ weatherIcon }}</span>
          <div class="datetime-info">
            <span class="current-date">{{ currentDate }}</span>
            <span class="current-time">{{ currentTime }}</span>
          </div>
          <span class="temperature">{{ currentTemperature }}</span>
        </div>
      </transition>

      <!-- 消息状态 -->
      <transition name="fade-scale">
        <div class="island-message" v-if="islandMessage">
          <i :class="getIconByType(islandMessageType)" :style="{ color: getColorByType(islandMessageType) }"></i>
          <span class="message-text">{{ islandMessage }}</span>
        </div>
      </transition>

      <!-- 展开状态：音乐播放器 -->
      <transition name="fade-scale">
        <div class="island-expanded" v-if="isIslandExpanded && !islandMessage">
          <div class="music-wave">
            <span></span><span></span><span></span><span></span>
          </div>
          <div class="music-info">
            <span class="music-title">{{ expandedTitle }}</span>
            <span class="music-artist">{{ expandedSubtitle }}</span>
          </div>
          <div class="music-controls" v-if="showControls">
            <i class="el-icon-video-pause" />
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DynamicIsland',
  props: {
    // 是否独立模式（登录页面使用）
    standalone: {
      type: Boolean,
      default: false
    },
    // 是否浮动（脱离header）
    floating: {
      type: Boolean,
      default: false
    },
    // 展开状态的标题
    expandedTitle: {
      type: String,
      default: 'Playing Now'
    },
    // 展开状态的副标题
    expandedSubtitle: {
      type: String,
      default: 'Unknown Artist'
    },
    // 是否显示控制按钮
    showControls: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      isIslandExpanded: false,
      islandMessage: '',
      islandMessageType: 'info',
      currentTime: '',
      currentDate: '',
      currentTemperature: '22°C',
      weatherIcon: '☀️',
      timeInterval: null,
      messageTimer: null,
      weatherInterval: null
    }
  },
  mounted() {
    this.updateTime()
    this.timeInterval = setInterval(this.updateTime, 1000)

    // 监听全局消息事件
    this.$root.$on('show-island-message', this.showIslandMessage)

    this.fetchWeather()
    this.weatherInterval = setInterval(this.fetchWeather, 30 * 60 * 1000)
  },
  beforeDestroy() {
    if (this.timeInterval) {
      clearInterval(this.timeInterval)
    }
    if (this.weatherInterval) {
      clearInterval(this.weatherInterval)
    }
    if (this.messageTimer) {
      clearTimeout(this.messageTimer)
    }
    this.$root.$off('show-island-message', this.showIslandMessage)
  },
  methods: {
    expandIsland() {
      if (!this.islandMessage) {
        this.isIslandExpanded = true
      }
    },
    collapseIsland() {
      if (!this.islandMessage) {
        this.isIslandExpanded = false
      }
    },
    showIslandMessage(message, type = 'info') {
      this.islandMessage = message
      this.islandMessageType = type
      this.isIslandExpanded = true

      if (this.messageTimer) {
        clearTimeout(this.messageTimer)
      }

      this.messageTimer = setTimeout(() => {
        this.islandMessage = ''
        this.islandMessageType = 'info'
        this.isIslandExpanded = false
      }, 3000)
    },
    getIconByType(type) {
      const icons = {
        success: 'el-icon-success',
        warning: 'el-icon-warning',
        danger: 'el-icon-error',
        info: 'el-icon-info'
      }
      return icons[type] || icons.info
    },
    getColorByType(type) {
      const colors = {
        success: '#10b981',
        warning: '#f59e0b',
        danger: '#ef4444',
        info: '#3b82f6'
      }
      return colors[type] || colors.info
    },
    updateTime() {
      const now = new Date()
      this.currentTime = now.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      })
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const weekday = weekdays[now.getDay()]
      this.currentDate = `${month}/${day}${weekday}`
    },
    async fetchWeather() {
      try {
        const { getRealtimeWeather, getWeatherEmoji } = await import('@/api/weather')
        const result = await getRealtimeWeather()

        if (result.success) {
          const weather = result.data
          this.currentTemperature = `${weather.temperature}°`
          this.weatherIcon = getWeatherEmoji(weather.icon)
        } else {
          this.useFallbackWeather()
        }
      } catch (error) {
        this.useFallbackWeather()
      }
    },
    useFallbackWeather() {
      const hour = new Date().getHours()
      let temp, icon

      if (hour >= 6 && hour < 12) {
        temp = 7
        icon = '🌫️'
      } else if (hour >= 12 && hour < 18) {
        temp = 12
        icon = '⛅'
      } else if (hour >= 18 && hour < 22) {
        temp = 9
        icon = '☁️'
      } else {
        temp = 5
        icon = '🌫️'
      }

      this.currentTemperature = `${temp}°`
      this.weatherIcon = icon
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.dynamic-island {
  background: color-mix(in srgb, var(--color-bg-surface) 72%, rgba(0, 0, 0, 0.35));
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-radius: 40px;
  padding: 12px 24px;
  min-width: 200px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  border: 1px solid var(--glass-border);

  &.standalone {
    position: fixed;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 99999;
    min-width: 300px;
    height: 52px;
    padding: 14px 28px;

    &:hover {
      transform: translateX(-50%) scale(1.02);
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
    }

    &.expanded {
      min-width: 500px;
      height: 60px;
      padding: 16px 32px;
    }
  }

  &.floating {
    position: fixed;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 99999;
  }

  &.expanded {
    min-width: 320px;
    height: 56px;
    padding: 16px 28px;

    &.standalone {
      min-width: 500px;
    }
  }

  &:hover:not(.standalone) {
    transform: scale(1.02);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
  }
}

.island-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.island-default {
  display: flex;
  align-items: center;
  gap: 16px;
  color: var(--color-text-primary);
  font-size: 13px;
  width: 100%;
  justify-content: space-between;

  .weather-icon {
    font-size: 24px;
    flex-shrink: 0;
  }

  .datetime-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    flex: 1;
    align-items: center;

    .current-date {
      font-size: 11px;
      color: var(--color-text-secondary);
      font-weight: 400;
    }

    .current-time {
      font-size: 18px;
      font-weight: 600;
      letter-spacing: 0.5px;
    }
  }

  .temperature {
    font-size: 16px;
    font-weight: 500;
    color: var(--color-text-secondary);
    flex-shrink: 0;
  }
}

.island-message {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 500;

  i {
    font-size: 18px;
  }

  .message-text {
    flex: 1;
  }
}

.island-expanded {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  color: var(--color-text-primary);

  .music-wave {
    display: flex;
    align-items: center;
    gap: 3px;
    height: 24px;

    span {
      width: 3px;
      height: 100%;
      background: linear-gradient(180deg, var(--color-primary), var(--color-secondary-info));
      border-radius: 2px;
      animation: wave 1s ease-in-out infinite;

      &:nth-child(1) {
        animation-delay: 0s;
      }

      &:nth-child(2) {
        animation-delay: 0.2s;
      }

      &:nth-child(3) {
        animation-delay: 0.4s;
      }

      &:nth-child(4) {
        animation-delay: 0.6s;
      }
    }
  }

  .music-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .music-title {
      font-size: 14px;
      font-weight: 600;
    }

    .music-artist {
      font-size: 12px;
      color: var(--color-text-secondary);
    }
  }

  .music-controls {
    display: flex;
    align-items: center;
    gap: 8px;

    i {
      font-size: 18px;
      cursor: pointer;
      transition: transform 0.2s ease;

      &:hover {
        transform: scale(1.2);
      }
    }
  }
}

@keyframes wave {
  0%, 100% {
    transform: scaleY(0.5);
  }
  50% {
    transform: scaleY(1);
  }
}

.fade-scale-enter-active, .fade-scale-leave-active {
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.fade-scale-enter, .fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.8);
}
</style>
