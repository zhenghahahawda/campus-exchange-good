
// 引入 ThemeManager
import { ThemeManager } from '@/utils/themeManager'

export const state = () => ({
  theme: 'blue',
  isSidebarCollapsed: false,
  isUIHidden: false, // 新增：控制 UI 显示状态
  userInfo: null,
  // 音乐播放器状态
  musicPlayer: {
    currentSong: null,
    isPlaying: false,
    currentTime: 0,
    duration: 0,
    playlist: []
  }
})

export const getters = {
  currentTheme: (state) => state.theme,
  isSidebarCollapsed: (state) => state.isSidebarCollapsed,
  isUIHidden: (state) => state.isUIHidden, // 新增
  userInfo: (state) => state.userInfo,
  // 音乐播放器 getters
  currentSong: (state) => state.musicPlayer.currentSong,
  isPlaying: (state) => state.musicPlayer.isPlaying,
  musicCurrentTime: (state) => state.musicPlayer.currentTime,
  musicDuration: (state) => state.musicPlayer.duration,
  musicPlaylist: (state) => state.musicPlayer.playlist
}

export const mutations = {
  SET_THEME(state, theme) {
    state.theme = theme
  },
  SET_SIDEBAR_COLLAPSED(state, isCollapsed) {
    state.isSidebarCollapsed = isCollapsed
  },
  SET_UI_HIDDEN(state, isHidden) { // 新增
    state.isUIHidden = isHidden
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
  },
  // 音乐播放器 mutations
  SET_CURRENT_SONG(state, song) {
    state.musicPlayer.currentSong = song
  },
  SET_PLAYING(state, isPlaying) {
    state.musicPlayer.isPlaying = isPlaying
  },
  SET_MUSIC_TIME(state, { currentTime, duration }) {
    if (currentTime !== undefined) state.musicPlayer.currentTime = currentTime
    if (duration !== undefined) state.musicPlayer.duration = duration
  },
  SET_PLAYLIST(state, playlist) {
    state.musicPlayer.playlist = playlist
  }
}

export const actions = {
  initApp({ commit }) {
    if (process.client) {
      const savedTheme = ThemeManager.loadSavedTheme()
      if (savedTheme) {
        ThemeManager.applyTheme(savedTheme)
        commit('SET_THEME', savedTheme)
      }

      const savedSidebarState = localStorage.getItem('sidebarCollapsed')
      if (savedSidebarState !== null) {
        commit('SET_SIDEBAR_COLLAPSED', JSON.parse(savedSidebarState))
      }
      
      // 不持久化 UI 隐藏状态，刷新后默认显示 UI

      // 从 Cookie 恢复用户信息（页面刷新时）
      const { getUserInfo } = require('@/utils/auth')
      const userInfo = getUserInfo()
      if (userInfo) {
        commit('SET_USER_INFO', userInfo)
      }
    }
  },

  setUserInfo({ commit }, userInfo) {
    commit('SET_USER_INFO', userInfo)
    if (process.client) {
      const { setUserInfo } = require('@/utils/auth')
      setUserInfo(userInfo)
    }
  },

  toggleTheme({ commit, state }, theme) {
    if (theme) {
      if (process.client) {
        ThemeManager.applyTheme(theme)
      }
      commit('SET_THEME', theme)
    }
  },

  toggleSidebar({ commit, state }) {
    const newState = !state.isSidebarCollapsed
    commit('SET_SIDEBAR_COLLAPSED', newState)
    if (process.client) {
      localStorage.setItem('sidebarCollapsed', JSON.stringify(newState))
    }
  },

  toggleUI({ commit, state }) { // 新增 action
    commit('SET_UI_HIDDEN', !state.isUIHidden)
  },

  // 音乐播放器 actions
  playSong({ commit }, song) {
    commit('SET_CURRENT_SONG', song)
    commit('SET_PLAYING', true)
  },
  togglePlayPause({ commit, state }) {
    commit('SET_PLAYING', !state.musicPlayer.isPlaying)
  },
  updateMusicTime({ commit }, { currentTime, duration }) {
    commit('SET_MUSIC_TIME', { currentTime, duration })
  },
  setPlaylist({ commit }, playlist) {
    commit('SET_PLAYLIST', playlist)
  }
}
