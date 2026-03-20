import { ref } from 'vue'

/**
 * 验证码管理
 */
export const useCaptcha = () => {
  const isVerified = ref(false)
  const captchaToken = ref(null)

  const handleVerified = (verified, token) => {
    isVerified.value = verified
    captchaToken.value = token
  }

  const resetCaptcha = (captchaRef) => {
    if (captchaRef && captchaRef.reset) {
      captchaRef.reset()
      isVerified.value = false
      captchaToken.value = null
    }
  }

  return {
    isVerified,
    captchaToken,
    handleVerified,
    resetCaptcha
  }
}
