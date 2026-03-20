import { ref } from 'vue'
import { isFirstLogin } from '@/utils/firstLogin'
import { uploadAvatar, updateUserInfo } from '@/api/auth'
import { MessageHelper } from '@/utils/messageHelper'
import { getUserInfo } from '@/utils/auth'

/**
 * 构建带身份信息的 query 参数
 */
const buildAuthQuery = () => {
  const userInfo = getUserInfo()
  return {
    uid: userInfo?.userId,
    username: userInfo?.username,
    userType: userInfo?.userType
  }
}

/**
 * 首次登录引导管理
 */
export const useOnboarding = (router, root) => {
  const vm = { $root: root }
  const showOnboarding = ref(false)
  const currentUserInfo = ref(null)

  /**
   * 检查并显示引导
   */
  const checkAndShowOnboarding = (userInfo, onComplete) => {
    currentUserInfo.value = userInfo

    if (isFirstLogin(userInfo)) {
      showOnboarding.value = true
    } else {
      onComplete?.()
    }
  }

  /**
   * 处理引导提交
   */
  const handleOnboardingSubmit = async (formData, axios) => {
    try {
      // 上传头像
      if (formData.avatarFile) {
        const avatarFormData = new FormData()
        avatarFormData.append('avatar', formData.avatarFile)
        await uploadAvatar(axios, avatarFormData)
      }

      // 更新用户信息
      await updateUserInfo(axios, {
        realName: formData.realName,
        birthday: formData.birthday,
        address: formData.address,
        email: formData.email,
        phone: formData.phone
      })

      MessageHelper.info(vm, '个人信息已完善！')
      showOnboarding.value = false

      const redirect = router.currentRoute.query.redirect || '/'
      setTimeout(() => {
        router.push({ path: redirect, query: buildAuthQuery() })
      }, 500)
    } catch (error) {
      MessageHelper.error(vm, '信息提交失败，请稍后重试')
      console.error('Onboarding submit error:', error)
    }
  }

  /**
   * 跳过引导
   */
  const handleOnboardingSkip = () => {
    MessageHelper.info(vm, '您可以稍后在个人中心完善信息')
    showOnboarding.value = false

    const redirect = router.currentRoute.query.redirect || '/'
    setTimeout(() => {
      router.push({ path: redirect, query: buildAuthQuery() })
    }, 500)
  }

  /**
   * 引导错误处理
   */
  const handleOnboardingError = (message) => {
    MessageHelper.error(vm, message)
  }

  return {
    showOnboarding,
    currentUserInfo,
    checkAndShowOnboarding,
    handleOnboardingSubmit,
    handleOnboardingSkip,
    handleOnboardingError
  }
}
