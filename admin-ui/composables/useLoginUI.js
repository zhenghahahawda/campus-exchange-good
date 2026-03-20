import { ref } from 'vue'

/**
 * 登录页面UI状态管理
 */
export const useLoginUI = () => {
  // UI 状态
  const focusType = ref('')
  const showPassword = ref(false)
  const isTyping = ref(false)
  const loginSuccess = ref(false)

  // UI 交互处理
  const handleFocus = (type) => {
    focusType.value = type
  }

  const handleBlur = () => {
    setTimeout(() => {
      focusType.value = ''
      isTyping.value = false
    }, 100)
  }

  const handleTyping = (typing) => {
    isTyping.value = typing
  }

  const handleTogglePassword = (show) => {
    showPassword.value = show
  }

  const handleFocusChange = (type) => {
    focusType.value = type
  }

  const setLoginSuccess = (success) => {
    loginSuccess.value = success
  }

  return {
    // 状态
    focusType,
    showPassword,
    isTyping,
    loginSuccess,

    // 方法
    handleFocus,
    handleBlur,
    handleTyping,
    handleTogglePassword,
    handleFocusChange,
    setLoginSuccess
  }
}
