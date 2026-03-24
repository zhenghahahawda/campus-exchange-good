
// 引入 ThemeManager
import { ThemeManager } from '@/utils/themeManager'

const DEFAULT_THEME_KEY = 'ios-style'

export const state = () => ({
  theme: DEFAULT_THEME_KEY,
  isSidebarCollapsed: false,
  isUIHidden: false,
  userInfo: null,
  themeList: [], // 缓存从API获取的主题列表
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
  isUIHidden: (state) => state.isUIHidden,
  userInfo: (state) => state.userInfo,
  themeList: (state) => state.themeList,
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
  SET_UI_HIDDEN(state, isHidden) {
    state.isUIHidden = isHidden
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
  },
  SET_THEME_LIST(state, list) {
    state.themeList = list
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
  initApp({ commit, dispatch }) {
    if (process.client) {
      // 先从 localStorage 加载已保存的主题（快速恢复，避免白屏）
      const savedTheme = ThemeManager.loadSavedTheme()
      if (savedTheme) {
        commit('SET_THEME', savedTheme)
      }

      const savedSidebarState = localStorage.getItem('sidebarCollapsed')
      if (savedSidebarState !== null) {
        commit('SET_SIDEBAR_COLLAPSED', JSON.parse(savedSidebarState))
      }

      // 从 Cookie 恢复用户信息（页面刷新时）
      const { getUserInfo } = require('@/utils/auth')
      const userInfo = getUserInfo()
      if (userInfo) {
        commit('SET_USER_INFO', userInfo)
        // 如果用户有主题偏好，从主题列表中找到并应用
        if (userInfo.themePreference) {
          dispatch('applyUserThemePreference', userInfo.themePreference)
        }
      }

      // 异步加载主题列表
      dispatch('fetchThemeList')
    }
  },

  async fetchThemeList({ commit }) {
    if (process.client) {
      try {
        const { getActiveThemes } = require('@/api/theme')
        const axios = require('@/plugins/axios').default || window.$nuxt?.$axios
        if (!axios) return
        const res = await getActiveThemes(axios)
        const list = res?.data?.data || res?.data || []
        if (Array.isArray(list)) {
          commit('SET_THEME_LIST', list)
        }
      } catch (e) {
        console.warn('获取主题列表失败', e)
      }
    }
  },

  /**
   * 根据用户偏好的 themeKey 应用主题
   */
  applyUserThemePreference({ state, commit }, themeKey) {
    const normalizedThemeKey = ThemeManager.normalizeThemeKey(themeKey)

    // 先尝试从 Vuex 缓存的主题列表中查找
    let themeData = state.themeList.find(t => t.themeKey === normalizedThemeKey)

    // 如果列表还没加载，尝试从 localStorage 恢复
    if (!themeData) {
      themeData = ThemeManager.getCurrentThemeData()
      if (themeData && themeData.themeKey !== normalizedThemeKey) {
        themeData = null
      }
    }

    if (themeData) {
      ThemeManager.applyTheme(themeData)
      commit('SET_THEME', normalizedThemeKey)
      return
    }

    const fallbackThemeData = state.themeList.find(t => t.themeKey === DEFAULT_THEME_KEY)
    if (fallbackThemeData) {
      ThemeManager.applyTheme(fallbackThemeData)
      commit('SET_THEME', fallbackThemeData.themeKey)
      return
    }

    commit('SET_THEME', normalizedThemeKey)
    if (process.client) {
      document.documentElement.setAttribute('data-theme', normalizedThemeKey)
      localStorage.setItem('selected-theme', normalizedThemeKey)
      localStorage.removeItem('selected-theme-data')
    }
  },

  setUserInfo({ commit, dispatch }, userInfo) {
    commit('SET_USER_INFO', userInfo)
    if (process.client) {
      const { setUserInfo } = require('@/utils/auth')
      setUserInfo(userInfo)

      // 登录/刷新用户信息时，如果服务器有主题偏好则应用
      if (userInfo && userInfo.themePreference) {
        dispatch('applyUserThemePreference', userInfo.themePreference)
      }
    }
  },

  toggleTheme({ commit }, themeData) {
    if (themeData) {
      if (process.client) {
        ThemeManager.applyTheme(themeData)
      }
      commit('SET_THEME', themeData.themeKey || themeData)
    }
  },

  toggleSidebar({ commit, state }) {
    const newState = !state.isSidebarCollapsed
    commit('SET_SIDEBAR_COLLAPSED', newState)
    if (process.client) {
      localStorage.setItem('sidebarCollapsed', JSON.stringify(newState))
    }
  },

  toggleUI({ commit, state }) {
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
