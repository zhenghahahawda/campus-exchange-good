<template>
  <div class="app-container">
    <!-- 动态背景组件 -->
    <DynamicBackground />

    <Sidebar :style="{ opacity: isUIHidden ? 0 : 1, pointerEvents: isUIHidden ? 'none' : 'auto', transition: 'opacity 0.5s ease' }" />
    <main class="main-content" :class="{ 'collapsed': isSidebarCollapsed }" :style="{ opacity: isUIHidden ? 0 : 1, pointerEvents: isUIHidden ? 'none' : 'auto', transition: 'opacity 0.5s ease' }">
      <AppHeader />
      <div class="page-content" ref="pageContent">
        <Nuxt />
      </div>
    </main>

    <!-- 全局音频播放器 -->
    <audio
      ref="globalAudioPlayer"
      @timeupdate="updateMusicTime"
      @loadedmetadata="updateMusicDuration"
      @ended="playNextSong"
      @play="onAudioPlay"
      @pause="onAudioPause"
    ></audio>
  </div>
</template>

<script>
import Sidebar from '@/components/layout/Sidebar.vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import DynamicBackground from '@/components/ui/DynamicBackground.vue'

export default {
  name: 'DefaultLayout',
  components: {
    Sidebar,
    AppHeader,
    DynamicBackground
  },
  methods: {
    // 监听键盘事件以退出清屏模式
    handleKeydown(e) {
      if (e.key === 'Escape' && this.isUIHidden) {
        this.$store.commit('SET_UI_HIDDEN', false);
      }
    },
    // 点击背景退出清屏模式
    handleBackgroundClick(e) {
      if (this.isUIHidden) {
        this.$store.commit('SET_UI_HIDDEN', false);
      }
    },
    // 音频播放器控制方法
    updateMusicTime() {
      const audio = this.$refs.globalAudioPlayer
      if (audio && !isNaN(audio.currentTime) && !isNaN(audio.duration)) {
        this.$store.dispatch('updateMusicTime', {
          currentTime: audio.currentTime,
          duration: audio.duration
        })
      }
    },
    updateMusicDuration() {
      const audio = this.$refs.globalAudioPlayer
      if (audio && !isNaN(audio.currentTime) && !isNaN(audio.duration)) {
        this.$store.dispatch('updateMusicTime', {
          currentTime: audio.currentTime,
          duration: audio.duration
        })
      }
    },
    playNextSong() {
      this.$root.$emit('music-ended')
    },
    onAudioPlay() {
      this.$store.commit('SET_PLAYING', true)
    },
    onAudioPause() {
      this.$store.commit('SET_PLAYING', false)
    },
    handleMusicTogglePlay() {
      const audio = this.$refs.globalAudioPlayer
      if (!audio) return

      if (this.$store.getters.isPlaying) {
        audio.pause()
      } else {
        audio.play()
      }
    },
    handleMusicVolumeChange(volume) {
      const audio = this.$refs.globalAudioPlayer
      if (audio) {
        audio.volume = volume / 100
      }
    },
    handleMusicSeek(time) {
      const audio = this.$refs.globalAudioPlayer
      if (audio) {
        audio.currentTime = time
      }
    },
    async handlePlaySong(song) {
      const audio = this.$refs.globalAudioPlayer
      if (!audio) return

      try {
        // 获取歌曲播放地址
        const urlRes = await fetch(`http://106.52.174.132:3015/song/url?id=${song.id}`).then(r => r.json())

        if (urlRes.data && urlRes.data[0] && urlRes.data[0].url) {
          audio.src = urlRes.data[0].url
          audio.play()
        } else {
          this.$root.$emit('show-island-message', '该歌曲暂无播放资源', 'warning')
        }
      } catch (error) {
        console.error('播放失败:', error)
        this.$root.$emit('show-island-message', '播放失败', 'danger')
      }
    }
  },
  async mounted() {
    window.addEventListener('keydown', this.handleKeydown);
    // 可以在这里添加点击事件监听，但由于 content 被隐藏，可能需要加在 app-container 上

    this.$store.dispatch('initApp')
    // 从后端拉取最新用户信息（含头像），确保 Sidebar 显示正确
    try {
      const res = await this.$axios.get('/api/user/info')
      if (res && res.data) {
        const { getUserInfo, setUserInfo } = require('@/utils/auth')
        const cached = getUserInfo()
        if (cached) {
          const updated = { ...cached, avatar: res.data.avatar, username: res.data.username }
          setUserInfo(updated)
          this.$store.commit('SET_USER_INFO', updated)
        }
      }
    } catch (e) {
      // 静默失败，不影响页面加载
    }

    // 监听全局音乐控制事件
    this.$root.$on('music-toggle-play', this.handleMusicTogglePlay)
    this.$root.$on('music-volume-change', this.handleMusicVolumeChange)
    this.$root.$on('music-seek', this.handleMusicSeek)
    this.$root.$on('music-play-song', this.handlePlaySong)

    // 恢复音量设置
    if (process.client) {
      const savedVolume = localStorage.getItem('musicVolume')
      if (savedVolume !== null) {
        const audio = this.$refs.globalAudioPlayer
        if (audio) {
          audio.volume = parseInt(savedVolume) / 100
        }
      }
    }
  },
  beforeDestroy() {
    window.removeEventListener('keydown', this.handleKeydown);
    // 移除音乐控制事件监听
    this.$root.$off('music-toggle-play', this.handleMusicTogglePlay)
    this.$root.$off('music-volume-change', this.handleMusicVolumeChange)
    this.$root.$off('music-seek', this.handleMusicSeek)
    this.$root.$off('music-play-song', this.handlePlaySong)
  },
  computed: {
    isSidebarCollapsed() {
      return this.$store.getters.isSidebarCollapsed;
    },
    isUIHidden() {
      return this.$store.getters.isUIHidden;
    }
  }
}
</script>

<style lang="scss">
@import '@/assets/css/themes/index.scss';

// 全局定义 hidden-ui 样式，避免 scoped 限制导致无法应用到组件根元素
.hidden-ui {
  opacity: 0 !important;
  pointer-events: none !important;
  transition: opacity 0.5s ease;
}
</style>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.app-container {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden; // 防止水平溢出
}

// UI 隐藏时的样式
// .hidden-ui { ... } 移至全局样式中

// 确保 DynamicBackground 始终可见且可以接收点击（如果需要点击背景恢复）
// 但 DynamicBackground 默认 z-index: 0，如果 main-content 隐藏，点击会穿透到 body
// 我们可以给 app-container 加一个点击事件，或者在 UI 隐藏时显示一个透明遮罩层来接收点击

.main-content {
  flex: 1;
  margin-left: calc(#{$sidebar-width} + 24px); // 250px + gap
  display: flex;
  flex-direction: column;
  transition: margin-left 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  will-change: margin-left;
  z-index: 10; // 确保在视频之上
  position: relative;
  
  &.collapsed {
    margin-left: 104px; // 80px + 12px + 12px
  }
}

.page-content {
  flex: 1;
  padding: 10px 40px 30px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
</style>
