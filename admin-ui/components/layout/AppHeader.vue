<template>
  <header class="app-header">
    <div class="header-left">
      <!-- Back Button -->
      <div class="back-button" @click="goBack" v-if="showBackButton">
        <i class="el-icon-back" />
      </div>
      
      <!-- Notification -->
      <div class="notification-wrapper" @click="toggleNotification">
        <i class="el-icon-bell notification-icon" />
        <span class="badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
        
        <!-- Notification Panel -->
        <transition name="slide-fade">
          <div class="notification-panel" v-if="showNotification" @click.stop>
            <div class="panel-header">
              <h3>通知中心</h3>
              <span class="mark-read" @click="refreshNotifications">刷新</span>
            </div>

            <div class="notification-list">
              <div
                v-for="(item, index) in notifications"
                :key="index"
                class="notification-item unread"
                @click="handleNotificationClick(index)"
              >
                <div class="item-icon" :class="item.type">
                  <i :class="item.icon" />
                </div>
                <div class="item-content">
                  <div class="item-title">{{ item.title }}</div>
                  <div class="item-desc">{{ item.description }}</div>
                  <div class="item-time">{{ item.time }}</div>
                </div>
              </div>
              <div v-if="notifications.length === 0" style="text-align:center;padding:30px 0;color:rgba(255,255,255,0.4);font-size:13px;">
                暂无通知
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- Music Button -->
      <div class="music-wrapper" @click="goToMusic">
        <i class="el-icon-headset music-icon" />
      </div>
    </div>
    
    <div class="header-right">
      <!-- XiaoO Chat Button -->
      <div class="theme-wrapper" @click="goToXiaoOChat" title="小O助手">
        <i class="el-icon-service theme-icon" />
      </div>

      <!-- Clear Screen Button -->
      <div class="theme-wrapper" @click="toggleUI" title="清屏模式 (Esc退出)">
        <i class="el-icon-monitor theme-icon" />
      </div>

      <!-- Theme Switcher -->
      <div class="theme-wrapper" @click="goToThemeSelector">
        <i class="el-icon-brush theme-icon" ref="themeIcon" />
      </div>
    </div>

    <div 
      class="dynamic-island" 
      :class="{ 
        'expanded': isIslandExpanded,
        'floating': !isHeaderVisible && (isIslandExpanded || islandMessage)
      }" 
      @mouseenter="expandIsland" 
      @mouseleave="collapseIsland"
    >
      <div class="island-content">
        <!-- 默认状态：天气、日期时间、温度（仅当没有音乐播放时显示） -->
        <transition name="fade-scale">
          <div class="island-default" v-if="!isIslandExpanded && !islandMessage && (!currentSong || !isPlaying)">
            <span class="weather-icon">{{ weatherIcon }}</span>
            <div class="datetime-info">
              <span class="current-date">{{ currentDate }}</span>
              <span class="current-time">{{ currentTime }}</span>
            </div>
            <span class="temperature">{{ currentTemperature }}</span>
          </div>
        </transition>

        <transition name="fade-scale">
          <div class="island-message" v-if="isIslandExpanded && islandMessage">
            <i :class="getIconByType(islandMessageType)" :style="{ color: getColorByType(islandMessageType), marginRight: '8px', fontSize: '18px' }"></i>
            <span class="message-text">{{ islandMessage }}</span>
          </div>
        </transition>

        <!-- 音乐播放器：收起状态（仅当有歌曲且正在播放时显示） -->
        <transition name="fade-scale">
          <div class="island-music-compact" v-if="!isIslandExpanded && !islandMessage && currentSong && isPlaying">
            <img :src="currentSong.cover" class="music-cover-small" />
            <div class="datetime-info">
              <span class="current-date">{{ currentDate }}</span>
              <span class="current-time">{{ currentTime }}</span>
            </div>
            <div class="music-wave-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </transition>

        <!-- 音乐播放器：展开状态（悬停时显示） -->
        <transition name="fade-scale">
          <div class="island-expanded island-music-expanded" v-if="isIslandExpanded && !islandMessage">
            <img :src="currentSong ? currentSong.cover : defaultMusicCover" class="music-cover" />
            <div class="music-main">
              <div class="music-info">
                <span class="music-title">
                  {{ currentSong ? currentSong.name : '未选择歌曲' }}
                  <span class="music-separator" v-if="currentSong">-</span>
                  <span class="music-artist-inline">{{ currentSong ? currentSong.artist : '前往音乐页面选择' }}</span>
                </span>
              </div>
              <div class="music-progress-wrapper">
                <div class="progress-bar" @click="handleProgressClick">
                  <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
                </div>
              </div>
              <div class="music-controls">
                <i class="el-icon-d-arrow-left control-icon" :class="{ disabled: !currentSong }" @click.stop="playPrevious" />
                <i
                  :class="[isPlaying ? 'el-icon-video-pause' : 'el-icon-video-play', 'control-icon', 'play-icon', { disabled: !currentSong }]"
                  @click.stop="togglePlay"
                />
                <i class="el-icon-d-arrow-right control-icon" :class="{ disabled: !currentSong }" @click.stop="playNext" />
              </div>
            </div>
            <div class="volume-control">
              <i
                :class="musicVolume === 0 ? 'el-icon-turn-off-microphone' : 'el-icon-microphone'"
                class="volume-icon"
                @click.stop="toggleMute"
              />
              <input
                type="number"
                min="0"
                max="100"
                :value="musicVolume"
                @input="handleVolumeInput"
                @click.stop
                class="volume-input"
              />
            </div>
          </div>
        </transition>
      </div>
    </div>
  </header>
</template>

<script>
import { useNotifications } from '@/composables/useNotifications'

export default {
  name: 'AppHeader',
  data() {
    return {
      isTransitioning: false,
      showNotification: false,
      isIslandExpanded: false,
      isHoveringIsland: false,
      islandMessage: '',
      islandMessageType: 'info',
      islandTimer: null,
      islandCloseTimer: null,
      isHeaderVisible: true,
      observer: null,
      currentTime: '',
      currentDate: '',
      currentTemperature: '7°',
      weatherIcon: '🌫️',
      timeInterval: null,
      notificationPollTimer: null,
      musicVolume: 50, // 音量状态
      defaultMusicCover: '/images/wallpapers/ios.png', // 使用iOS风格图片作为默认音乐封面
      themeList: ['default', 'purple', 'yellow', 'green', 'red', 'orange', 'lightblue', 'white', 'rainbow', 'iced-americano', 'dark-gold'],
      themeNames: {
        'default': '深蓝色主题',
        'purple': '浅紫色主题',
        'yellow': '暖黄色主题',
        'green': '清新绿色主题',
        'red': '热情红色主题',
        'orange': '活力橙色主题',
        'lightblue': '清爽淡蓝主题',
        'white': '极简亮白主题',
        'rainbow': '梦幻彩虹主题',
        'iced-americano': '冰美式主题',
        'dark-gold': '暗黑金主题'
      },
      notifications: [],
      notificationManager: null
    }
  },
  computed: {
    showBackButton() {
      // 在非首页时显示返回按钮
      return this.$route.path !== '/'
    },
    unreadCount() {
      return this.notifications.length
    },
    // 音乐播放器状态
    currentSong() {
      return this.$store.getters.currentSong
    },
    isPlaying() {
      return this.$store.getters.isPlaying
    },
    musicCurrentTime() {
      return this.$store.getters.musicCurrentTime
    },
    musicDuration() {
      return this.$store.getters.musicDuration
    },
    musicPlaylist() {
      return this.$store.getters.musicPlaylist
    },
    progressPercent() {
      if (!this.musicDuration) return 0
      return (this.musicCurrentTime / this.musicDuration) * 100
    }
  },
  mounted() {
    document.addEventListener('click', this.handleClickOutside)

    // Use IntersectionObserver to track visibility
    this.observer = new IntersectionObserver((entries) => {
      this.isHeaderVisible = entries[0].isIntersecting;
    }, { threshold: 0 });
    this.observer.observe(this.$el);

    this.updateTime();
    this.timeInterval = setInterval(this.updateTime, 1000);
    this.$root.$on('show-island-message', this.showIslandMessage);

    // 初始化通知管理器
    this.notificationManager = useNotifications(this)
    // 加载通知数据
    this.loadNotifications()
    // 每30秒刷新通知
    this.notificationPollTimer = setInterval(() => this.loadNotifications(), 30000)

    // 获取武汉天气（由于跨域问题，暂时使用模拟数据）
    // 实际项目中应该通过后端代理或使用支持 CORS 的 API
    this.fetchWeather();
    // 每30分钟更新一次天气
    setInterval(this.fetchWeather, 30 * 60 * 1000);

    // 模拟初始展开动画
    setTimeout(() => {
      this.isIslandExpanded = true;
      setTimeout(() => {
        this.isIslandExpanded = false;
      }, 2000);
    }, 1000);

    // 从localStorage恢复音量设置
    if (process.client) {
      const savedVolume = localStorage.getItem('musicVolume')
      if (savedVolume !== null) {
        this.musicVolume = parseInt(savedVolume)
        // 通知音乐页面恢复音量
        this.$root.$emit('music-volume-change', this.musicVolume)
      }
    }
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside)
    if (this.observer) this.observer.disconnect();
    this.$root.$off('show-island-message', this.showIslandMessage);
    if (this.timeInterval) clearInterval(this.timeInterval);
    if (this.islandTimer) clearTimeout(this.islandTimer);
    if (this.islandCloseTimer) clearTimeout(this.islandCloseTimer);
    if (this.notificationPollTimer) clearInterval(this.notificationPollTimer);
  },
  methods: {
      // 修复：IntersectionObserver 回调中不要直接覆盖 isHeaderVisible
      // 而是使用 observer 来确保准确性
      // 这里已经使用了 observer，所以删除无用的 handleIntersection 方法
    updateTime() {
      const now = new Date();
      this.currentTime = now.toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit', 
        hour12: false 
      });
      this.currentDate = now.toLocaleDateString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        weekday: 'short'
      });
    },
    async fetchWeather() {
      try {
        // 使用和风天气 API 获取武汉江夏区实时天气
        const { getRealtimeWeather, getWeatherEmoji } = await import('@/api/weather')

        const result = await getRealtimeWeather()

        if (result.success) {
          const weather = result.data
          this.currentTemperature = `${weather.temperature}°`
          this.weatherIcon = getWeatherEmoji(weather.icon)

          console.log('天气已更新（和风天气）:', {
            温度: weather.temperature + '°',
            天气: weather.text,
            体感: weather.feelsLike + '°',
            湿度: weather.humidity + '%',
            更新时间: weather.updateTime
          })
        } else {
          // API 调用失败，使用降级方案（模拟数据）
          console.warn('和风天气API调用失败，使用模拟数据:', result.message)
          this.useFallbackWeather()
        }
      } catch (error) {
        console.error('获取天气失败:', error)
        // 使用降级方案
        this.useFallbackWeather()
      }
    },

    // 降级方案：使用模拟天气数据
    useFallbackWeather() {
      const hour = new Date().getHours()
      let temp, icon

      // 根据时间段模拟温度变化
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
      console.log('使用模拟天气数据:', temp + '°', icon)
    },
    getWeatherIcon(code, description) {
      // 优先根据描述判断
      const desc = description.toLowerCase();
      
      if (desc.includes('fog') || desc.includes('mist') || desc.includes('haze')) {
        return '🌫️';  // 雾
      }
      if (desc.includes('thunder') || desc.includes('storm')) {
        return '⛈️';  // 雷暴
      }
      if (desc.includes('snow') || desc.includes('blizzard')) {
        return desc.includes('heavy') ? '❄️' : '🌨️';  // 雪
      }
      if (desc.includes('rain') || desc.includes('drizzle') || desc.includes('shower')) {
        return desc.includes('heavy') ? '🌧️' : '🌦️';  // 雨
      }
      if (desc.includes('cloud')) {
        return desc.includes('partly') ? '⛅' : '☁️';  // 云
      }
      if (desc.includes('clear') || desc.includes('sunny')) {
        return '☀️';  // 晴
      }
      
      // 根据天气代码返回对应的 emoji
      const weatherMap = {
        '113': '☀️',  // 晴天
        '116': '⛅',  // 局部多云
        '119': '☁️',  // 多云
        '122': '☁️',  // 阴天
        '143': '🌫️',  // 雾
        '248': '🌫️',  // 大雾
        '260': '🌫️',  // 冻雾
        '176': '🌦️',  // 局部有雨
        '179': '🌨️',  // 局部有雪
        '182': '🌧️',  // 雨夹雪
        '185': '🌧️',  // 冻雨
        '200': '⛈️',  // 雷暴
        '227': '🌨️',  // 暴风雪
        '230': '❄️',  // 大雪
        '263': '🌦️',  // 小雨
        '266': '🌧️',  // 小雨
        '281': '🌧️',  // 冻雨
        '284': '🌧️',  // 重冻雨
        '293': '🌦️',  // 小雨
        '296': '🌧️',  // 小雨
        '299': '🌧️',  // 中雨
        '302': '🌧️',  // 中雨
        '305': '🌧️',  // 大雨
        '308': '🌧️',  // 暴雨
        '311': '🌧️',  // 冻雨
        '314': '🌧️',  // 重冻雨
        '317': '🌨️',  // 雨夹雪
        '320': '🌨️',  // 雨夹雪
        '323': '🌨️',  // 小雪
        '326': '🌨️',  // 小雪
        '329': '❄️',  // 中雪
        '332': '❄️',  // 中雪
        '335': '❄️',  // 大雪
        '338': '❄️',  // 暴雪
        '350': '🌧️',  // 冰雹
        '353': '🌦️',  // 小阵雨
        '356': '🌧️',  // 中阵雨
        '359': '🌧️',  // 大阵雨
        '362': '🌨️',  // 雨夹雪
        '365': '🌨️',  // 雨夹雪
        '368': '🌨️',  // 小阵雪
        '371': '❄️',  // 中阵雪
        '374': '🌧️',  // 冰雹
        '377': '🌧️',  // 冰雹
        '386': '⛈️',  // 雷阵雨
        '389': '⛈️',  // 雷暴大雨
        '392': '⛈️',  // 雷阵雪
        '395': '⛈️'   // 雷暴大雪
      };
      
      return weatherMap[code] || '☀️';
    },
    getIconByType(type) {
      switch(type) {
        case 'success': return 'el-icon-circle-check';
        case 'warning': return 'el-icon-warning-outline';
        case 'danger': return 'el-icon-circle-close';
        case 'info': default: return 'el-icon-info';
      }
    },
    getColorByType(type) {
      switch(type) {
        case 'success': return '#67C23A';
        case 'warning': return '#E6A23C';
        case 'danger': return '#F56C6C';
        case 'info': default: return '#909399';
      }
    },
    showIslandMessage(message, type = 'info') {
      if (this.islandTimer) clearTimeout(this.islandTimer);
      if (this.islandCloseTimer) clearTimeout(this.islandCloseTimer);
      
      this.islandMessage = message;
      this.islandMessageType = type;
      this.isIslandExpanded = true;
      
      // 如果头部不可见，则灵动岛会变为 fixed 悬浮状态
      // 此时即使不 hover 也要保持显示，直到时间结束
      
      if (this.isHoveringIsland) return;

      this.islandTimer = setTimeout(() => {
        this.isIslandExpanded = false;
        this.islandCloseTimer = setTimeout(() => {
          this.islandMessage = '';
          this.islandCloseTimer = null;
        }, 300); // Wait for transition
        this.islandTimer = null;
      }, 3000);
    },
    expandIsland() {
      this.isHoveringIsland = true;
      this.isIslandExpanded = true;
      if (this.islandTimer) {
        clearTimeout(this.islandTimer);
        this.islandTimer = null;
      }
      if (this.islandCloseTimer) {
        clearTimeout(this.islandCloseTimer);
        this.islandCloseTimer = null;
      }
    },
    collapseIsland() {
      this.isHoveringIsland = false;
      if (this.islandMessage) {
        this.islandTimer = setTimeout(() => {
          this.isIslandExpanded = false;
          this.islandCloseTimer = setTimeout(() => {
            this.islandMessage = '';
            this.islandCloseTimer = null;
          }, 300);
          this.islandTimer = null;
        }, 3000);
      } else {
        this.isIslandExpanded = false;
      }
    },
    goBack() {
      this.$router.back()
    },
    goToThemeSelector() {
      this.$router.push('/setting/select-theme');
    },
    goToMusic() {
      this.$router.push('/setting/music');
    },
    toggleNotification() {
      this.showNotification = !this.showNotification
      // 打开通知面板时刷新数据
      if (this.showNotification) {
        this.loadNotifications()
      }
    },
    handleClickOutside(e) {
      const notificationWrapper = this.$el.querySelector('.notification-wrapper')
      if (notificationWrapper && !notificationWrapper.contains(e.target)) {
        this.showNotification = false
      }
    },
    async loadNotifications() {
      if (!this.notificationManager) return
      const list = await this.notificationManager.fetchNotifications(10)
      this.notifications = list
    },
    refreshNotifications() {
      this.loadNotifications()
      this.showIslandMessage('通知已刷新', 'success')
    },
    handleNotificationClick(index) {
      const notification = this.notifications[index]
      if (!notification) return
      this.showNotification = false
      if (notification.targetPath) {
        this.$router.push(notification.targetPath)
      }
    },
    toggleTheme(event) {
      if (this.isTransitioning) return;
      
      // 获取根元素
      const root = document.documentElement;
      // 获取当前主题
      const currentTheme = root.getAttribute('data-theme') || 'default';
      
      // 计算下一个主题
      const currentIndex = this.themeList.indexOf(currentTheme);
      const nextIndex = (currentIndex + 1) % this.themeList.length;
      const newTheme = this.themeList[nextIndex];
      
      // 使用 View Transitions API
      if (!document.startViewTransition) {
        // 降级处理：不支持 View Transitions 的浏览器直接切换
        this.applyTheme(root, newTheme);
        return;
      }

      this.isTransitioning = true;
      
      // 获取点击位置
      const x = event.clientX;
      const y = event.clientY;
      
      // 计算需要扩散的半径（勾股定理：到最远角落的距离）
      const endRadius = Math.hypot(
        Math.max(x, window.innerWidth - x),
        Math.max(y, window.innerHeight - y)
      );

      // 开始过渡
      const transition = document.startViewTransition(() => {
        this.applyTheme(root, newTheme);
      });

      // 自定义动画：新视图从点击处扩散
      transition.ready.then(() => {
        document.documentElement.animate(
          {
            clipPath: [
              `circle(0px at ${x}px ${y}px)`,
              `circle(${endRadius}px at ${x}px ${y}px)`,
            ],
          },
          {
            duration: 500,
            easing: 'ease-in',
            // 关键：指定动画作用于“新视图”伪元素
            pseudoElement: '::view-transition-new(root)',
          }
        );
      });

      transition.finished.then(() => {
        this.isTransitioning = false;
      });
    },

    applyTheme(root, newTheme) {
      if (newTheme === 'default') {
        root.removeAttribute('data-theme');
      } else {
        root.setAttribute('data-theme', newTheme);
      }
      this.showIslandMessage(`已切换至 ${this.themeNames[newTheme]}`, 'success');
    },

    toggleUI() {
      this.$store.dispatch('toggleUI');
      this.showIslandMessage('已进入清屏模式，按 ESC 退出', 'info');
    },

    goToXiaoOChat() {
      this.$router.push('/setting/xiaoO');
      this.showIslandMessage('正在打开小O助手...', 'success');
    },

    handleUserCommand(command) {
      switch (command) {
        case 'profile':
          this.showIslandMessage('个人中心功能开发中...', 'info')
          break
        case 'settings':
          this.$router.push('/setting/setuser')
          break
        case 'logout':
          this.handleLogout()
          break
      }
    },

    handleLogout() {
      this.$confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 清除认证信息
        const { clearAuth } = require('@/utils/auth')
        clearAuth()

        this.showIslandMessage('已退出登录', 'success')

        // 跳转到登录页
        setTimeout(() => {
          this.$router.push('/login-page/login')
        }, 1000)
      }).catch(() => {
        // 取消退出
      })
    },

    // 音乐播放器控制方法
    togglePlay() {
      this.$root.$emit('music-toggle-play')
    },
    playPrevious() {
      this.$root.$emit('music-previous')
    },
    playNext() {
      this.$root.$emit('music-next')
    },
    handleVolumeChange(event) {
      const volume = parseInt(event.target.value)
      this.musicVolume = volume
      this.$root.$emit('music-volume-change', volume)
      // 保存到localStorage
      if (process.client) {
        localStorage.setItem('musicVolume', volume)
      }
    },
    handleVolumeInput(event) {
      let volume = parseInt(event.target.value)
      if (isNaN(volume)) volume = 0
      if (volume < 0) volume = 0
      if (volume > 100) volume = 100
      this.musicVolume = volume
      this.$root.$emit('music-volume-change', volume)
      // 保存到localStorage
      if (process.client) {
        localStorage.setItem('musicVolume', volume)
      }
    },
    handleProgressClick(event) {
      if (!this.currentSong || !this.musicDuration) return
      const progressBar = event.currentTarget
      const rect = progressBar.getBoundingClientRect()
      const clickX = event.clientX - rect.left
      const percent = clickX / rect.width
      const newTime = percent * this.musicDuration
      this.$root.$emit('music-seek', newTime)
    },
    toggleMute() {
      if (this.musicVolume > 0) {
        this.musicVolume = 0
      } else {
        this.musicVolume = 50
      }
      this.$root.$emit('music-volume-change', this.musicVolume)
      // 保存到localStorage
      if (process.client) {
        localStorage.setItem('musicVolume', this.musicVolume)
      }
    },
    formatMusicTime(seconds) {
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

.app-header {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background-color: transparent;
  position: relative;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.dynamic-island {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  @include glass-effect;
  border-radius: 20px;
  height: 48px;
  min-width: 400px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  color: $color-text-primary;
  overflow: hidden;
  z-index: 10000;

  // 当有音乐播放时，收起状态更紧凑
  &:not(.expanded) {
    &:has(.island-music-compact) {
      min-width: 320px;
    }

    &:has(.island-default) {
      min-width: 400px;
    }
  }

  &.expanded {
    width: 700px;
    height: 56px;
    border-radius: 28px;
    background-color: var(--glass-bg); // 保持一致的玻璃背景
  }
  
  &.floating {
    position: fixed;
    top: 24px;
    left: 50%;
    transform: translateX(-50%) !important;
    z-index: 99999 !important; // 提高到最高层级
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.4);
    animation: slide-down-bounce 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    min-width: 200px;
    height: 44px;
    
    .island-message {
      .message-text {
        font-size: 15px;
      }
    }
  }
  
  &:hover {
    background-color: var(--color-bg-surface);
    box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.2);
  }
}

@keyframes slide-down-bounce {
  0% { top: -60px; opacity: 0; }
  60% { top: 30px; opacity: 1; }
  100% { top: 24px; opacity: 1; }
}

.island-content {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 音乐播放器收起状态
.island-music-compact {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0 20px;

  .music-cover-small {
    width: 36px;
    height: 36px;
    border-radius: 6px;
    object-fit: cover;
    flex-shrink: 0;
  }

  .datetime-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    flex: 1;

    .current-date {
      font-size: 12px;
      font-weight: 500;
      color: $color-text-secondary;
      letter-spacing: 0.3px;
    }

    .current-time {
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 0.5px;
    }
  }

  .music-wave-indicator {
    display: flex;
    align-items: flex-end;
    gap: 3px;
    height: 20px;
    flex-shrink: 0;

    span {
      width: 3px;
      background: $color-primary;
      border-radius: 2px;
      animation: wave 1s infinite ease-in-out;

      &:nth-child(1) { height: 60%; animation-delay: 0s; }
      &:nth-child(2) { height: 100%; animation-delay: 0.15s; }
      &:nth-child(3) { height: 80%; animation-delay: 0.3s; }
    }
  }

  .play-icon-compact {
    font-size: 24px;
    color: $color-primary;
    cursor: pointer;
    transition: all 0.3s;
    flex-shrink: 0;

    &:hover {
      transform: scale(1.1);
    }
  }
}

@keyframes wave {
  0%, 100% { transform: scaleY(0.5); }
  50% { transform: scaleY(1); }
}


.island-default {
    position: absolute;
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding: 0 20px;
    
    .weather-icon {
      font-size: 24px;
      line-height: 1;
      flex-shrink: 0;
    }
    
    .datetime-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    flex: 1;
    
    .current-date {
      font-size: 12px;
      font-weight: 500;
      color: $color-text-secondary;
      letter-spacing: 0.3px;
    }
    
    .current-time {
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 0.5px;
    }
  }
  
  .temperature {
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 0.5px;
    flex-shrink: 0;
  }
}

.island-message {
  position: absolute;
  width: 100%;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .message-text {
    font-size: 14px;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.island-expanded {
  position: absolute;
  width: 100%;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;

  .music-cover {
    width: 44px;
    height: 44px;
    border-radius: 8px;
    object-fit: cover;
    flex-shrink: 0;
  }

  .music-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
  }

  .music-info {
    display: flex;
    align-items: center;
    min-width: 0;

    .music-title {
      font-size: 12px;
      font-weight: 600;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      display: flex;
      align-items: center;
      gap: 6px;

      .music-separator {
        color: $color-text-secondary;
        font-weight: 400;
      }

      .music-artist-inline {
        color: $color-text-secondary;
        font-weight: 400;
        font-size: 11px;
      }
    }
  }

  .music-progress-wrapper {
    width: 100%;

    .progress-bar {
      width: 100%;
      height: 4px;
      background: rgba(255, 255, 255, 0.15);
      border-radius: 2px;
      overflow: visible;
      position: relative;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        height: 5px;

        .progress-fill {
          box-shadow: 0 0 8px rgba(var(--color-primary-rgb), 0.5);
        }
      }

      .progress-fill {
        height: 100%;
        background: linear-gradient(90deg,
          rgba(var(--color-primary-rgb), 0.8) 0%,
          rgba(var(--color-primary-rgb), 1) 100%);
        border-radius: 2px;
        transition: width 0.3s ease, box-shadow 0.2s;
        position: relative;

        &::after {
          content: '';
          position: absolute;
          right: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 8px;
          height: 8px;
          background: $color-primary;
          border-radius: 50%;
          box-shadow: 0 0 4px rgba(var(--color-primary-rgb), 0.6);
          opacity: 0;
          transition: opacity 0.2s;
        }
      }

      &:hover .progress-fill::after {
        opacity: 1;
      }
    }
  }

  .music-controls {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;

    .control-icon {
      font-size: 16px;
      color: $color-text-secondary;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        color: $color-primary;
        transform: scale(1.1);
      }

      &.play-icon {
        font-size: 22px;
        color: $color-primary;
      }

      &.disabled {
        opacity: 0.3;
        cursor: not-allowed;
        pointer-events: none;
      }
    }
  }

  .volume-control {
    display: flex;
    align-items: center;
    gap: 6px;
    flex-shrink: 0;

    .volume-icon {
      font-size: 18px;
      color: $color-text-secondary;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        color: $color-primary;
      }
    }

    .volume-input {
      width: 45px;
      height: 28px;
      background: rgba(255, 255, 255, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.2);
      border-radius: 6px;
      color: $color-text-primary;
      text-align: center;
      font-size: 12px;
      font-weight: 600;
      outline: none;
      transition: all 0.3s;

      &:focus {
        background: rgba(255, 255, 255, 0.15);
        border-color: $color-primary;
      }

      &::-webkit-inner-spin-button,
      &::-webkit-outer-spin-button {
        -webkit-appearance: none;
        appearance: none;
        margin: 0;
      }

      -moz-appearance: textfield;
      appearance: textfield;
    }
  }
}

@keyframes wave {
  0%, 100% { transform: scaleY(0.5); }
  50% { transform: scaleY(1); }
}

.fade-scale-enter-active, .fade-scale-leave-active {
  transition: all 0.3s ease;
}
.fade-scale-enter, .fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.back-button {
  @include glass-effect;
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  
  i {
    font-size: 24px;
    color: $color-text-secondary;
    transition: all 0.3s ease;
  }
  
  &:hover {
    background-color: rgba(255, 255, 255, 0.15);
    transform: translateY(-1px) translateX(-2px);
    box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.2);
    
    i {
      color: $color-primary;
      transform: translateX(-2px);
    }
  }
  
  &:active {
    transform: translateY(0) translateX(0);
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 30px;
}

.filter-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: $color-text-secondary;
  font-weight: $font-weight-medium;
  
  .filter-text {
    margin-right: 8px;
  }
}

.notification-wrapper {
  position: relative;
  cursor: pointer;
  @include glass-effect;
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10000;
  
  &:hover {
    background-color: rgba(255, 255, 255, 0.15);
    transform: translateY(-1px);
    box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.2);
    
    .notification-icon {
      color: $color-primary;
    }
  }
  
  .notification-icon {
    font-size: 24px;
    color: $color-text-secondary;
    transition: all 0.3s ease;
  }
  
  &:hover .notification-icon {
    animation: bell-shake 0.8s cubic-bezier(.36,.07,.19,.97) both;
  }

  @keyframes bell-shake {
    0%, 100% { transform: rotate(0); }
    15% { transform: rotate(15deg); }
    30% { transform: rotate(-15deg); }
    45% { transform: rotate(10deg); }
    60% { transform: rotate(-10deg); }
    75% { transform: rotate(5deg); }
    90% { transform: rotate(-5deg); }
  }
  
  .badge {
    position: absolute;
    top: -4px;
    right: -4px;
    min-width: 20px;
    height: 20px;
    padding: 0 4px;
    background: $color-primary;
    border-radius: 99px;
    font-size: 13px;
    font-weight: 700;
    line-height: 20px;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid $color-bg-page;
    box-shadow: 0 0 0 2px rgba(var(--color-primary-rgb), 0.2);
    z-index: 2;
    white-space: nowrap;
  }
}

.music-wrapper {
  position: relative;
  cursor: pointer;
  @include glass-effect;
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10000;
  
  &:hover {
    background-color: rgba(255, 255, 255, 0.15);
    transform: translateY(-1px);
    box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.2);
    
    .music-icon {
      color: $color-primary;
    }
  }
  
  .music-icon {
    font-size: 24px;
    color: $color-text-secondary;
    transition: all 0.3s ease;
  }
  
  &:hover .music-icon {
    animation: music-bounce 1s ease infinite;
  }
}

@keyframes music-bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.notification-panel {
  position: absolute;
  top: calc(100% + 12px);
  left: 0;
  width: 380px;
  max-height: 500px;
  padding: 0;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  
  // 使用完全不透明的背景，不穿透后面的图层
  background: $color-bg-surface;
  border: 1px solid $color-border;
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(0, 0, 0, 0.05);
  border-radius: 20px;
  transform: translateZ(0);
  will-change: transform;
  contain: layout style;
  
  .panel-header {
    padding: 20px 24px;
    border-bottom: 1px solid $color-border;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h3 {
      font-size: 18px;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin: 0;
    }
    
    .mark-read {
      font-size: 13px;
      color: $color-primary;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        opacity: 0.8;
        text-decoration: underline;
      }
    }
  }
  
  .notification-list {
    flex: 1;
    overflow-y: auto;
    max-height: 360px;
    
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(var(--color-primary-rgb), 0.3);
      border-radius: 3px;
      
      &:hover {
        background: rgba(var(--color-primary-rgb), 0.5);
      }
    }
  }
  
  .notification-item {
    padding: 16px 24px;
    display: flex;
    gap: 12px;
    cursor: pointer;
    transition: all 0.3s;
    position: relative;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    
    &.unread {
      background: rgba(var(--color-primary-rgb), 0.05);
    }
    
    &:hover {
      background: rgba(var(--color-primary-rgb), 0.1);
    }
    
    .item-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      i {
        font-size: 20px;
      }
      
      &.success {
        background: rgba(var(--color-secondary-success), 0.1);
        color: $color-secondary-success;
      }
      
      &.warning {
        background: rgba(var(--color-secondary-warning-rgb), 0.1);
        color: $color-secondary-warning;
      }
      
      &.danger {
        background: rgba(var(--color-secondary-danger), 0.1);
        color: $color-secondary-danger;
      }
      
      &.info {
        background: rgba(var(--color-secondary-info), 0.1);
        color: $color-secondary-info;
      }
    }
    
    .item-content {
      flex: 1;
      min-width: 0;
      
      .item-title {
        font-size: 14px;
        font-weight: $font-weight-semibold;
        color: $color-text-primary;
        margin-bottom: 4px;
      }
      
      .item-desc {
        font-size: 13px;
        color: $color-text-secondary;
        margin-bottom: 6px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .item-time {
        font-size: 12px;
        color: $color-text-disabled;
      }
    }
    
    .unread-dot {
      position: absolute;
      top: 20px;
      right: 24px;
      width: 8px;
      height: 8px;
      background: $color-primary;
      border-radius: 50%;
      box-shadow: 0 0 8px rgba(var(--color-primary-rgb), 0.6);
    }
  }
  
  .panel-footer {
    padding: 16px 24px;
    border-top: 1px solid $color-border;
    text-align: center;
    
    span {
      font-size: 14px;
      color: $color-primary;
      cursor: pointer;
      font-weight: $font-weight-medium;
      transition: all 0.3s;
      
      &:hover {
        opacity: 0.8;
        text-decoration: underline;
      }
    }
  }
}

// 向左滑入动画
.slide-fade-enter-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.slide-fade-enter {
  transform: translateX(-20px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

// 上滑切换动画
.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.slide-up-enter {
  transform: translateY(20px);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

.theme-wrapper {
  position: relative;
  cursor: pointer;
  @include glass-effect;
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10000;
  
  &:hover {
    background-color: rgba(255, 255, 255, 0.15);
    transform: translateY(-1px);
    box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.2);
    
    .theme-icon {
      color: $color-primary;
    }
  }
  
  .theme-icon {
    font-size: 24px;
    color: $color-text-secondary;
    transition: all 0.3s ease;
  }
  
  &:hover .theme-icon {
    transform: rotate(30deg) scale(1.1);
  }
}

.weather-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 9999px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: translateY(-1px);
  }
  
  .temperature {
    font-weight: 600;
    color: $color-text-primary;
    font-size: 15px;
  }
  
  .weather-icon {
    font-size: 18px;
    line-height: 1;
  }

  .weather-icon-dynamic {
    width: 24px;
    height: 24px;
    object-fit: contain;
    filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
  }
}

.user-profile {
    cursor: pointer;
    display: flex;
    align-items: center;

    .avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      overflow: hidden;
      border: 2px solid rgba(var(--color-primary-rgb), 0.3);
      transition: all 0.3s ease;

      &:hover {
        border-color: $color-primary;
        transform: scale(1.05);
      }

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
  }
</style>

<style lang="scss">
@import '@/assets/css/themes/blue.scss';

// 用户信息头部样式
.user-info-header {
  padding: 16px 24px;
  border-bottom: 1px solid $color-border;

  .user-name {
    font-size: 16px;
    font-weight: 600;
    color: $color-text-primary;
    margin-bottom: 4px;
  }

  .user-role {
    font-size: 13px;
    color: $color-text-secondary;
  }
}

// 全局覆盖 Element UI 下拉菜单样式以实现毛玻璃效果
.el-dropdown-menu.glass-dropdown {
  @include glass-effect;
  padding: 8px 0;
  border-radius: 12px;
  margin-top: 12px !important; // 增加一点间距

  .el-dropdown-menu__item {
    color: $color-text-secondary;
    font-weight: 500;
    font-size: 14px;
    padding: 10px 24px;
    line-height: 1.5;

    i {
      margin-right: 8px;
      font-size: 16px;
    }

    &:hover, &:focus {
      background-color: rgba(255, 255, 255, 0.5);
      color: $color-primary;
    }
  }

  // 隐藏原本的箭头，因为毛玻璃上半透明箭头会很难看
  .popper__arrow {
    display: none;
  }
}
</style>
