<template>
  <div class="music-page">
    <PageHeader title="音乐中心" subtitle="享受音乐，放松心情" />

    <div class="music-container">
      <!-- 搜索栏 -->
      <div class="search-section">
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索歌曲、歌手、专辑..."
            prefix-icon="el-icon-search"
            @keyup.enter.native="handleSearch"
            clearable
          >
            <el-button slot="append" @click="handleSearch">搜索</el-button>
          </el-input>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="content-grid">
        <!-- 左侧：推荐和排行榜 -->
        <div class="sidebar-content">
          <!-- 推荐歌曲 -->
          <div class="music-card">
            <div class="card-header">
              <h3><i class="el-icon-star-on"></i> 推荐歌曲</h3>
              <el-button type="text" size="small" @click="loadRecommendations">
                <i class="el-icon-refresh"></i>
              </el-button>
            </div>
            <div class="song-list" v-loading="loadingRecommend">
              <div
                v-for="song in recommendedSongs"
                :key="song.id"
                class="song-item"
                @click="playSong(song)"
              >
                <img :src="song.cover" class="song-cover" />
                <div class="song-info">
                  <div class="song-name">{{ song.name }}</div>
                  <div class="song-artist">{{ song.artist }}</div>
                </div>
                <i class="el-icon-video-play play-icon"></i>
              </div>
              <EmptyState v-if="!loadingRecommend && recommendedSongs.length === 0" message="暂无推荐" />
            </div>
          </div>

          <!-- 排行榜 -->
          <div class="music-card">
            <div class="card-header">
              <h3><i class="el-icon-trophy"></i> 热门榜单</h3>
            </div>
            <div class="toplist" v-loading="loadingToplist">
              <div
                v-for="(list, index) in toplists"
                :key="index"
                class="toplist-item"
                @click="loadPlaylist(list.id)"
              >
                <img :src="list.cover" class="toplist-cover" />
                <div class="toplist-info">
                  <div class="toplist-name">{{ list.name }}</div>
                  <div class="toplist-update">{{ list.updateFrequency }}</div>
                </div>
              </div>
              <EmptyState v-if="!loadingToplist && toplists.length === 0" message="暂无榜单" />
            </div>
          </div>
        </div>

        <!-- 右侧：搜索结果/歌单详情 -->
        <div class="main-content">
          <div class="music-card main-card">
            <div class="card-header">
              <h3>
                <i :class="currentViewIcon"></i> {{ currentViewTitle }}
              </h3>
            </div>
            <div class="main-song-list" v-loading="loadingMain">
              <div
                v-for="(song, index) in mainSongs"
                :key="song.id"
                class="main-song-item"
                :class="{ active: currentSong && currentSong.id === song.id }"
                @click="playSong(song)"
              >
                <div class="song-index">{{ index + 1 }}</div>
                <img :src="song.cover" class="song-cover" />
                <div class="song-details">
                  <div class="song-name">{{ song.name }}</div>
                  <div class="song-artist">{{ song.artist }}</div>
                </div>
                <div class="song-album">{{ song.album }}</div>
                <div class="song-duration">{{ song.duration }}</div>
                <i class="el-icon-video-play play-icon"></i>
              </div>
              <EmptyState v-if="!loadingMain && mainSongs.length === 0" :message="emptyMessage" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 隐藏的音频元素已移到全局layout中 -->
  </div>
</template>

<script>
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

export default {
  name: 'MusicPage',
  components: {
    PageHeader,
    EmptyState
  },
  data() {
    return {
      searchKeyword: '',
      recommendedSongs: [],
      toplists: [],
      mainSongs: [],
      currentViewTitle: '推荐内容',
      currentViewIcon: 'el-icon-star-on',
      emptyMessage: '搜索或选择歌单以查看歌曲',

      // 播放器状态
      volume: 50,
      isMuted: false,

      // 加载状态
      loadingRecommend: false,
      loadingToplist: false,
      loadingMain: false
    }
  },
  computed: {
    currentSong() {
      return this.$store.getters.currentSong
    },
    isPlaying() {
      return this.$store.getters.isPlaying
    },
    currentTime() {
      return this.$store.getters.musicCurrentTime
    },
    duration() {
      return this.$store.getters.musicDuration
    }
  },
  mounted() {
    this.loadRecommendations()
    this.loadToplists()
    this.initAudioPlayer()

    // 监听来自灵动岛的控制事件
    this.$root.$on('music-toggle-play', this.togglePlay)
    this.$root.$on('music-previous', this.playPrevious)
    this.$root.$on('music-next', this.playNext)
    this.$root.$on('music-volume-change', this.handleVolumeChange)
    this.$root.$on('music-seek', this.handleSeek)
    this.$root.$on('music-ended', this.playNext)

    // 从localStorage恢复音量设置
    if (process.client) {
      const savedVolume = localStorage.getItem('musicVolume')
      if (savedVolume !== null) {
        this.volume = parseInt(savedVolume)
      }
    }
  },
  beforeDestroy() {
    // 移除事件监听
    this.$root.$off('music-toggle-play', this.togglePlay)
    this.$root.$off('music-previous', this.playPrevious)
    this.$root.$off('music-next', this.playNext)
    this.$root.$off('music-volume-change', this.handleVolumeChange)
    this.$root.$off('music-seek', this.handleSeek)
    this.$root.$off('music-ended', this.playNext)
  },
  methods: {
    // 初始化音频播放器（已移到全局，这里不需要了）
    initAudioPlayer() {
      // 音频播放器在全局layout中
    },

    // 加载推荐歌曲
    async loadRecommendations() {
      this.loadingRecommend = true
      try {
        const res = await fetch('http://106.52.174.132:3015/personalized/newsong').then(r => r.json())
        if (res.result) {
          this.recommendedSongs = res.result.slice(0, 10).map(item => ({
            id: item.id,
            name: item.name,
            artist: item.song.artists.map(a => a.name).join(' / '),
            album: item.song.album.name,
            cover: item.picUrl || item.song.album.picUrl,
            duration: this.formatTime(item.song.duration / 1000)
          }))
        }
      } catch (error) {
        console.error('加载推荐歌曲失败:', error)
        this.$root.$emit('show-island-message', '加载推荐失败', 'danger')
      } finally {
        this.loadingRecommend = false
      }
    },

    // 加载排行榜
    async loadToplists() {
      this.loadingToplist = true
      try {
        const res = await fetch('http://106.52.174.132:3015/toplist').then(r => r.json())
        if (res.list) {
          this.toplists = res.list.slice(0, 4).map(item => ({
            id: item.id,
            name: item.name,
            cover: item.coverImgUrl,
            updateFrequency: item.updateFrequency || '每日更新'
          }))
        }
      } catch (error) {
        console.error('加载排行榜失败:', error)
        this.$root.$emit('show-island-message', '加载榜单失败', 'danger')
      } finally {
        this.loadingToplist = false
      }
    },

    // 搜索歌曲
    async handleSearch() {
      if (!this.searchKeyword.trim()) {
        this.$root.$emit('show-island-message', '请输入搜索关键词', 'warning')
        return
      }

      this.loadingMain = true
      this.currentViewTitle = `搜索: ${this.searchKeyword}`
      this.currentViewIcon = 'el-icon-search'
      this.emptyMessage = '未找到相关歌曲'

      try {
        const res = await fetch(`http://106.52.174.132:3015/search?keywords=${encodeURIComponent(this.searchKeyword)}`).then(r => r.json())
        if (res.result && res.result.songs) {
          this.mainSongs = res.result.songs.map(item => ({
            id: item.id,
            name: item.name,
            artist: item.artists.map(a => a.name).join(' / '),
            album: item.album.name,
            cover: item.album.picUrl || item.album.artist.img1v1Url,
            duration: this.formatTime(item.duration / 1000)
          }))
          this.$root.$emit('show-island-message', `找到 ${this.mainSongs.length} 首歌曲`, 'success')
        }
      } catch (error) {
        console.error('搜索失败:', error)
        this.$root.$emit('show-island-message', '搜索失败', 'danger')
        this.mainSongs = []
      } finally {
        this.loadingMain = false
      }
    },

    // 加载歌单详情
    async loadPlaylist(playlistId) {
      this.loadingMain = true
      this.currentViewTitle = '歌单详情'
      this.currentViewIcon = 'el-icon-menu'
      this.emptyMessage = '歌单为空'

      try {
        const res = await fetch(`http://106.52.174.132:3015/playlist/detail?id=${playlistId}`).then(r => r.json())
        if (res.playlist && res.playlist.tracks) {
          this.mainSongs = res.playlist.tracks.map(item => ({
            id: item.id,
            name: item.name,
            artist: item.ar.map(a => a.name).join(' / '),
            album: item.al.name,
            cover: item.al.picUrl,
            duration: this.formatTime(item.dt / 1000)
          }))
          this.currentViewTitle = res.playlist.name
          this.$root.$emit('show-island-message', `加载了 ${this.mainSongs.length} 首歌曲`, 'success')
        }
      } catch (error) {
        console.error('加载歌单失败:', error)
        this.$root.$emit('show-island-message', '加载歌单失败', 'danger')
        this.mainSongs = []
      } finally {
        this.loadingMain = false
      }
    },

    // 播放歌曲
    async playSong(song) {
      // 更新全局状态
      this.$store.dispatch('playSong', song)
      this.$store.dispatch('setPlaylist', this.mainSongs)

      // 通知全局播放器播放歌曲
      this.$root.$emit('music-play-song', song)
    },

    // 播放控制（通过全局事件）
    togglePlay() {
      this.$root.$emit('music-toggle-play')
    },

    onPlay() {
      // 已在全局处理
    },

    onPause() {
      // 已在全局处理
    },

    playPrevious() {
      const currentIndex = this.mainSongs.findIndex(s => s.id === this.currentSong?.id)
      if (currentIndex > 0) {
        this.playSong(this.mainSongs[currentIndex - 1])
      } else {
        this.$root.$emit('show-island-message', '已经是第一首了', 'info')
      }
    },

    playNext() {
      const currentIndex = this.mainSongs.findIndex(s => s.id === this.currentSong?.id)
      if (currentIndex < this.mainSongs.length - 1) {
        this.playSong(this.mainSongs[currentIndex + 1])
      } else {
        this.$root.$emit('show-island-message', '已经是最后一首了', 'info')
      }
    },

    toggleMute() {
      // 已移到全局
    },

    handleVolumeChange(val) {
      this.volume = val
      // 音量控制已在全局layout处理
    },

    handleSeek() {
      // 进度跳转已在全局layout处理
    },

    updateTime() {
      // 已在全局处理
    },

    updateDuration() {
      // 已在全局处理
    },

    // 格式化时间
    formatTime(seconds) {
      if (!seconds || isNaN(seconds)) return '00:00'
      const mins = Math.floor(seconds / 60)
      const secs = Math.floor(seconds % 60)
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.music-page {
  padding-bottom: 40px;
}

.music-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.search-section {
  @include glass-card;
  padding: 24px;

  .search-bar {
    max-width: 800px;
    margin: 0 auto;
  }
}

.content-grid {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 24px;

  @media (max-width: 1200px) {
    grid-template-columns: 1fr;
  }
}

.sidebar-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.music-card {
  @include glass-card;
  padding: 0;
  overflow: hidden;

  &.main-card {
    min-height: 600px;
  }

  .card-header {
    padding: 20px 24px;
    border-bottom: 1px solid $color-border;
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      font-size: 16px;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin: 0;
      display: flex;
      align-items: center;
      gap: 8px;

      i {
        color: $color-primary;
      }
    }
  }
}

.song-list {
  padding: 12px;
  max-height: 400px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(var(--color-primary-rgb), 0.3);
    border-radius: 3px;
  }
}

.song-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  gap: 12px;

  &:hover {
    background: rgba(var(--color-primary-rgb), 0.1);

    .play-icon {
      opacity: 1;
    }
  }

  .song-cover {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    object-fit: cover;
    flex-shrink: 0;
  }

  .song-info {
    flex: 1;
    min-width: 0;

    .song-name {
      font-size: 14px;
      font-weight: $font-weight-medium;
      color: $color-text-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .song-artist {
      font-size: 12px;
      color: $color-text-secondary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .play-icon {
    font-size: 24px;
    color: $color-primary;
    opacity: 0;
    transition: opacity 0.3s;
  }
}

.toplist {
  padding: 12px;
}

.toplist-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  gap: 12px;
  margin-bottom: 8px;

  &:hover {
    background: rgba(var(--color-primary-rgb), 0.1);
  }

  .toplist-cover {
    width: 60px;
    height: 60px;
    border-radius: 8px;
    object-fit: cover;
  }

  .toplist-info {
    flex: 1;

    .toplist-name {
      font-size: 14px;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      margin-bottom: 4px;
    }

    .toplist-update {
      font-size: 12px;
      color: $color-text-secondary;
    }
  }
}

.main-song-list {
  padding: 12px;
  min-height: 400px;
}

.main-song-item {
  display: grid;
  grid-template-columns: 40px 60px 1fr 200px 80px 40px;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  gap: 16px;

  &:hover, &.active {
    background: rgba(var(--color-primary-rgb), 0.1);

    .play-icon {
      opacity: 1;
    }
  }

  &.active {
    .song-name {
      color: $color-primary;
    }
  }

  .song-index {
    text-align: center;
    font-size: 14px;
    color: $color-text-secondary;
    font-weight: $font-weight-medium;
  }

  .song-cover {
    width: 50px;
    height: 50px;
    border-radius: 8px;
    object-fit: cover;
  }

  .song-details {
    min-width: 0;

    .song-name {
      font-size: 14px;
      font-weight: $font-weight-medium;
      color: $color-text-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-bottom: 4px;
    }

    .song-artist {
      font-size: 12px;
      color: $color-text-secondary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .song-album {
    font-size: 13px;
    color: $color-text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .song-duration {
    font-size: 13px;
    color: $color-text-secondary;
    text-align: center;
  }

  .play-icon {
    font-size: 24px;
    color: $color-primary;
    opacity: 0;
    transition: opacity 0.3s;
    justify-self: center;
  }

  @media (max-width: 768px) {
    grid-template-columns: 40px 50px 1fr 40px;

    .song-album, .song-duration {
      display: none;
    }
  }
}

// Element UI 样式覆盖
::v-deep .el-input__inner {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid $color-border;
  color: $color-text-primary;
  border-radius: 12px;

  &:focus {
    border-color: $color-primary;
    background: rgba(255, 255, 255, 0.08);
  }
}

::v-deep .el-input-group__append {
  background: $color-primary;
  border: none;
  border-radius: 0 12px 12px 0;

  .el-button {
    color: white;
    font-weight: $font-weight-semibold;
  }
}

::v-deep .el-slider__runway {
  background: rgba(255, 255, 255, 0.1);
  height: 4px;
}

::v-deep .el-slider__bar {
  background: $color-primary;
  height: 4px;
}

::v-deep .el-slider__button {
  width: 12px;
  height: 12px;
  border: 2px solid $color-primary;
  background: white;
}

::v-deep .el-button--text {
  color: $color-text-secondary;

  &:hover {
    color: $color-primary;
  }
}
</style>
