<template>
  <div class="dynamic-background">
    <!-- 图片壁纸 -->
    <img
      v-if="isImageWallpaper"
      :src="wallpaperUrl"
      class="wallpaper-bg"
      alt=""
    />
    <!-- 视频壁纸 -->
    <video
      v-else-if="isVideoWallpaper"
      :src="wallpaperUrl"
      class="wallpaper-bg"
      autoplay
      loop
      muted
      playsinline
    ></video>
    <!-- 默认渐变背景 -->
    <template v-else>
      <div class="bg-color"></div>
      <div class="blobs">
        <div class="blob blob-1"></div>
        <div class="blob blob-2"></div>
        <div class="blob blob-3"></div>
        <div class="blob blob-4"></div>
      </div>
    </template>
    <div class="noise-overlay"></div>
  </div>
</template>

<script>
export default {
  name: 'DynamicBackground',
  data() {
    return {
      wallpaperUrl: null,
      wallpaperType: null
    }
  },
  computed: {
    isImageWallpaper() {
      if (!this.wallpaperUrl) return false
      if (this.wallpaperType === 'image') return true
      return /\.(png|jpe?g|gif|webp|svg|bmp)(\?|$)/i.test(this.wallpaperUrl)
    },
    isVideoWallpaper() {
      if (!this.wallpaperUrl) return false
      if (this.wallpaperType === 'video') return true
      return /\.(mp4|webm|ogg)(\?|$)/i.test(this.wallpaperUrl)
    }
  },
  mounted() {
    const savedUrl = localStorage.getItem('selected-theme-wallpaper')
    const savedType = localStorage.getItem('selected-theme-wallpaper-type')
    if (savedUrl) {
      this.wallpaperUrl = savedUrl
      this.wallpaperType = savedType || null
    }

    this._wallpaperHandler = (e) => {
      this.wallpaperUrl = e.detail.url || null
      this.wallpaperType = e.detail.type || null
    }
    window.addEventListener('theme-wallpaper-change', this._wallpaperHandler)
  },
  beforeDestroy() {
    window.removeEventListener('theme-wallpaper-change', this._wallpaperHandler)
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.dynamic-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 0;
  overflow: hidden;
  background-color: $color-bg-page;
}

.wallpaper-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
  pointer-events: none;
}

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
  filter: blur(80px);
  opacity: 0.6;
}

.blob {
  position: absolute;
  border-radius: 50%;
  will-change: transform;
  animation: float 20s infinite ease-in-out alternate;
}

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
  background: color-mix(in srgb, var(--color-primary) 80%, white);
  animation-duration: 28s;
  opacity: 0.2;
}

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
  0% { transform: translate(0, 0) rotate(0deg) scale(1); }
  33% { transform: translate(30px, -50px) rotate(10deg) scale(1.1); }
  66% { transform: translate(-20px, 20px) rotate(-5deg) scale(0.9); }
  100% { transform: translate(0, 0) rotate(0deg) scale(1); }
}
</style>
