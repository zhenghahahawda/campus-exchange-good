<template>
  <div
    ref="card"
    class="theme-card"
    :class="{ active: isActive }"
    @click="$emit('select', theme.id, $event)"
  >
    <div class="theme-preview" :style="{ background: theme.gradient }">
      <!-- 静态壁纸 -->
      <div
        v-if="theme.wallpaper && isVisible"
        class="preview-wallpaper"
        :style="{ backgroundImage: `url(${theme.wallpaper})` }"
      ></div>
      <!-- 视频预览：可视区域内自动播放 -->
      <video
        v-if="theme.video && shouldLoadVideo"
        ref="previewVideo"
        class="preview-video"
        :class="{ 'is-playing': isVideoPlaying }"
        loop
        muted
        playsinline
        preload="auto"
        :src="theme.video"
        @canplay="onVideoCanPlay"
      ></video>
      <div class="preview-content">
        <div class="preview-sidebar" :style="{ background: theme.sidebarBg }">
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
      <!-- 视频加载指示器 -->
      <div v-if="theme.video && shouldLoadVideo && !isVideoReady" class="video-loading">
        <i class="el-icon-loading"></i>
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
// 全局视频加载队列：控制同时加载视频的数量
const videoLoadQueue = {
  maxConcurrent: 3,
  loading: 0,
  queue: [],
  enqueue(fn) {
    if (this.loading < this.maxConcurrent) {
      this.loading++
      fn(() => { this.loading--; this.dequeue() })
    } else {
      this.queue.push(fn)
    }
  },
  dequeue() {
    if (this.queue.length > 0 && this.loading < this.maxConcurrent) {
      this.loading++
      const fn = this.queue.shift()
      fn(() => { this.loading--; this.dequeue() })
    }
  },
  clear() {
    this.queue = []
  }
}

export function clearVideoQueue() {
  videoLoadQueue.clear()
}

export default {
  name: 'ThemeCard',
  data() {
    return {
      isVisible: false,
      shouldLoadVideo: false,
      isVideoReady: false,
      isVideoPlaying: false,
      observer: null,
      _destroyed: false
    }
  },
  props: {
    theme: { type: Object, required: true },
    isActive: { type: Boolean, default: false }
  },
  mounted() {
    // 双向 IntersectionObserver：进入可视区域自动播放，离开时暂停
    this.observer = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            this.isVisible = true
            if (this.theme.video && !this.shouldLoadVideo) {
              videoLoadQueue.enqueue((done) => {
                if (this._destroyed) { done(); return }
                this.shouldLoadVideo = true
                this._queueDone = done
              })
            } else if (this.theme.video && this.isVideoReady) {
              // 重新进入可视区域，恢复播放
              this.playVideo()
            }
          } else {
            // 离开可视区域，暂停视频释放GPU资源
            if (this.theme.video && this.isVideoPlaying) {
              this.pauseVideo()
            }
          }
        })
      },
      {
        rootMargin: '100px',
        threshold: 0
      }
    )
    if (this.$refs.card) {
      this.observer.observe(this.$refs.card)
    }
  },
  beforeDestroy() {
    this._destroyed = true
    if (this.observer) {
      this.observer.disconnect()
    }
    if (this._queueDone) {
      this._queueDone()
      this._queueDone = null
    }
    const video = this.$refs.previewVideo
    if (video) {
      video.pause()
      video.removeAttribute('src')
      video.load()
    }
  },
  methods: {
    playVideo() {
      const video = this.$refs.previewVideo
      if (!video) return
      video.play().then(() => {
        this.isVideoPlaying = true
      }).catch(() => {})
    },
    pauseVideo() {
      const video = this.$refs.previewVideo
      if (!video) return
      video.pause()
      this.isVideoPlaying = false
    },
    onVideoCanPlay() {
      this.isVideoReady = true
      // 释放队列位置
      if (this._queueDone) {
        this._queueDone()
        this._queueDone = null
      }
      // 自动播放
      this.playVideo()
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

    .preview-video {
      transform: scale(1.05);
    }

    .preview-wallpaper {
      transform: scale(1.05);
    }
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

.preview-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
  pointer-events: none;
  transition: transform 0.5s ease, opacity 0.5s ease;
  opacity: 0;

  &.is-playing {
    opacity: 1;
  }
}

.preview-wallpaper {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  z-index: 0;
  transition: transform 0.5s ease;
}

.video-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 3;
  color: rgba(255, 255, 255, 0.9);
  font-size: 24px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
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
</style>
