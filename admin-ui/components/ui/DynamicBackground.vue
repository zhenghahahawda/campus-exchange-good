<template>
  <div class="dynamic-background">
    <div class="bg-color"></div>
    <div class="blobs">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
      <div class="blob blob-4"></div>
    </div>
    <div class="noise-overlay"></div>
    <!-- 专门为需要图片壁纸的主题准备的图层 -->
    <div class="wallpaper-layer" :class="{ 'is-active': hasWallpaper }"></div>
    
    <!-- 专门为视频壁纸准备的图层 -->
    <video 
      ref="bgVideo"
      class="video-layer" 
      autoplay 
      loop 
      muted 
      playsinline
      v-show="videoSrc"
    >
      <source :src="videoSrc" type="video/mp4">
    </video>
  </div>
</template>

<script>
// 引入主题配置以获取视频映射关系
import { themes } from '@/assets/css/themes/themes-config.js'

export default {
  name: 'DynamicBackground',
  data() {
    return {
      videoSrc: '',
      hasWallpaper: false,
      observer: null
    }
  },
  mounted() {
    // 初始化视频
    this.checkThemeVideo()
    
    // 监听 html 上的 data-theme 属性变化
    this.observer = new MutationObserver((mutations) => {
      for (const mutation of mutations) {
        if (mutation.type === 'attributes' && mutation.attributeName === 'data-theme') {
          this.checkThemeVideo()
        }
      }
    })
    
    this.observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['data-theme']
    })
  },
  beforeDestroy() {
    if (this.observer) {
      this.observer.disconnect()
    }
  },
  methods: {
    checkThemeVideo() {
      const currentThemeId = document.documentElement.getAttribute('data-theme')
      const themeConfig = themes.find(t => t.id === currentThemeId)
      
      if (themeConfig && themeConfig.video) {
        // 如果当前主题有视频且与正在播放的不同
        if (this.videoSrc !== themeConfig.video) {
          this.videoSrc = themeConfig.video
          // 强制重新加载视频
          this.$nextTick(() => {
            if (this.$refs.bgVideo) {
              this.$refs.bgVideo.load()
              this.$refs.bgVideo.play().catch(e => console.log('Auto-play prevented:', e))
            }
          })
        }
      } else {
        this.videoSrc = ''
      }
      
      // 检查是否为静态壁纸
      if (themeConfig && themeConfig.wallpaper) {
        this.hasWallpaper = true
      } else {
        this.hasWallpaper = false
      }
    }
  }
}
</script>

<style lang="scss">
/* 注意：将这里的 scoped 移除，改为全局样式或者修改选择器逻辑 */
@import '@/assets/css/themes/index.scss';

.dynamic-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 0;
  overflow: hidden;
  background-color: var(--color-bg-page);
}

// 视频图层
.video-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1; // 在背景色之上，壁纸之下(如果有的话)
  object-fit: cover; // 确保完整覆盖屏幕，不留黑边
  pointer-events: none;
}

// 专门处理带有图片的背景
.wallpaper-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2; // 在动态光球之上
  pointer-events: none;
  // 默认隐藏
  opacity: 0;
  transition: opacity 0.5s ease;
  
  // 从 CSS 变量中读取背景 (如果有的话)
  background: var(--theme-wallpaper, none);
  
  &.is-active {
    opacity: 1;
  }
}

// 移除之前的硬编码选择器
// html[data-theme='anime-ghoul'] .wallpaper-layer,
// html[data-theme='anime-sky'] .wallpaper-layer {
//   opacity: 1;
// }

.bg-color {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at center, color-mix(in srgb, var(--color-bg-page), white 10%), var(--color-bg-page));
}

.blobs {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  filter: blur(80px); // 核心：高斯模糊融合
  opacity: 0.6; // 调整强度
}

.blob {
  position: absolute;
  border-radius: 50%;
  will-change: transform;
  animation: float 20s infinite ease-in-out alternate;
}

// 定义几个流动的球体
.blob-1 {
  top: -10%;
  left: -10%;
  width: 50vw;
  height: 50vw;
  background: var(--color-primary);
  animation-duration: 25s;
  opacity: 0.4;
}

.blob-2 {
  bottom: -10%;
  right: -10%;
  width: 60vw;
  height: 60vw;
  // 使用 color-mix 替代 mix
  background: color-mix(in srgb, var(--color-primary), var(--color-secondary-info));
  animation-duration: 30s;
  animation-direction: reverse;
  opacity: 0.3;
}

.blob-3 {
  top: 20%;
  right: 20%;
  width: 40vw;
  height: 40vw;
  background: var(--color-secondary-info);
  animation-duration: 22s;
  opacity: 0.3;
}

.blob-4 {
  bottom: 10%;
  left: 20%;
  width: 35vw;
  height: 35vw;
  // 使用 color-mix 替代 mix
  background: color-mix(in srgb, var(--color-primary) 80%, white);
  animation-duration: 28s;
  opacity: 0.2;
}

// 噪点遮罩，增加质感
.noise-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  opacity: 0.03;
  pointer-events: none;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
}

@keyframes float {
  0% {
    transform: translate(0, 0) rotate(0deg) scale(1);
  }
  33% {
    transform: translate(30px, -50px) rotate(10deg) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) rotate(-5deg) scale(0.9);
  }
  100% {
    transform: translate(0, 0) rotate(0deg) scale(1);
  }
}
</style>
