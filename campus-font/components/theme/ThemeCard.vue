<template>
  <div
    class="theme-card"
    :class="{ active: isActive }"
    @click="$emit('select', theme.id, $event)"
  >
    <div class="theme-preview" :style="previewStyle">
      <!-- 壁纸背景（图片） -->
      <img
        v-if="hasImageWallpaper"
        :src="theme.wallpaper"
        class="preview-wallpaper"
        alt=""
      />
      <!-- 壁纸背景（视频） -->
      <video
        v-else-if="hasVideoWallpaper"
        :src="theme.wallpaper"
        class="preview-wallpaper preview-video"
        autoplay
        loop
        muted
        playsinline
      ></video>
      <!-- 模拟UI叠加层 -->
      <div class="preview-content">
        <div class="preview-sidebar" :style="{ background: theme.sidebarBg || 'rgba(0,0,0,0.3)' }">
          <div class="preview-logo" :style="{ background: theme.primaryColor }"></div>
          <div class="preview-menu-item" :style="{ background: theme.primaryColor }"></div>
          <div class="preview-menu-item"></div>
          <div class="preview-menu-item"></div>
        </div>
        <div class="preview-main">
          <div class="preview-header"></div>
          <div class="preview-cards">
            <div class="preview-card" :style="{ borderColor: theme.primaryColor }"></div>
            <div class="preview-card" :style="{ borderColor: theme.primaryColor }"></div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="theme-info">
      <div class="theme-name">{{ theme.name }}</div>
      <div class="theme-desc">{{ theme.description }}</div>
      <div class="theme-tags">
        <span v-for="tag in theme.tags" :key="tag" class="tag">{{ tag }}</span>
      </div>
    </div>

    <div v-if="isActive" class="active-badge">
      <i class="el-icon-check"></i>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ThemeCard',
  props: {
    theme: {
      type: Object,
      required: true
    },
    isActive: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    hasImageWallpaper() {
      if (!this.theme.wallpaper) return false
      if (this.theme.wallpaperType === 'image') return true
      return /\.(png|jpe?g|gif|webp|svg|bmp)(\?|$)/i.test(this.theme.wallpaper)
    },
    hasVideoWallpaper() {
      if (!this.theme.wallpaper) return false
      if (this.theme.wallpaperType === 'video') return true
      return /\.(mp4|webm|ogg)(\?|$)/i.test(this.theme.wallpaper)
    },
    previewStyle() {
      if (this.hasImageWallpaper || this.hasVideoWallpaper) {
        return {}
      }
      return { background: this.theme.gradient || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.theme-card {
  @include glass-card;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  }
  
  &.active {
    border: 2px solid $color-primary;
    box-shadow: 0 8px 32px rgba(var(--color-primary-rgb), 0.3);
  }
}

.theme-preview {
  height: 180px;
  border-radius: 16px 16px 0 0;
  overflow: hidden;
  position: relative;
}

.preview-wallpaper {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
}

.preview-video {
  pointer-events: none;
}

.preview-content {
  display: flex;
  height: 100%;
  padding: 12px;
  gap: 8px;
  position: relative;
  z-index: 1;
}

.preview-sidebar {
  width: 60px;
  border-radius: 8px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.preview-logo {
  width: 100%;
  height: 20px;
  border-radius: 4px;
  opacity: 0.8;
}

.preview-menu-item {
  width: 100%;
  height: 12px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.2);
}

.preview-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-header {
  height: 24px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
}

.preview-cards {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.preview-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  border: 1px solid transparent;
}

.theme-info {
  padding: 20px;
  
  .theme-name {
    font-size: 18px;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin-bottom: 6px;
  }
  
  .theme-desc {
    font-size: 14px;
    color: $color-text-secondary;
    margin-bottom: 12px;
  }
  
  .theme-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
  
  .tag {
    padding: 4px 12px;
    background: rgba(var(--color-primary-rgb), 0.1);
    color: $color-primary;
    border-radius: 12px;
    font-size: 12px;
    font-weight: $font-weight-medium;
  }
}

.active-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  background: $color-primary;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.4);
}

// 响应式设计
@media (max-width: $breakpoint-xs) {
  .theme-card {
    .theme-preview {
      height: 140px;
    }
    
    .theme-info {
      padding: 16px;
      
      .theme-name {
        font-size: 16px;
      }
      
      .theme-desc {
        font-size: 13px;
      }
    }
  }
}
</style>