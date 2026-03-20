/**
 * 首次登录检测工具
 */

/**
 * 检查是否需要显示信息补全引导
 * 注册时已填写：用户名、手机、邮箱、学校、性别、地址（选填）
 * 需要补全：头像、真实姓名、生日
 * @param {Object} userInfo - 用户信息
 * @returns {boolean} true 表示需要引导补全
 */
export function isFirstLogin(userInfo) {
  if (!userInfo) {
    return false
  }

  // 检查头像是否已上传（非默认头像）
  const hasAvatar = userInfo.avatar &&
                    userInfo.avatar !== '' &&
                    !userInfo.avatar.includes('default') &&
                    !userInfo.avatar.includes('placeholder')

  // 检查真实姓名和生日是否已填写
  const hasRealName = userInfo.realName && userInfo.realName.trim() !== ''
  const hasBirthday = userInfo.birthday && userInfo.birthday !== ''

  return !hasAvatar || !hasRealName || !hasBirthday
}

/**
 * 检查用户信息是否完整
 * @param {Object} userInfo - 用户信息
 * @returns {Object} { isComplete: boolean, missingFields: string[] }
 */
export function checkUserInfoComplete(userInfo) {
  if (!userInfo) {
    return { isComplete: false, missingFields: ['all'] }
  }

  const requiredFields = [
    { key: 'avatar', label: '头像' },
    { key: 'realName', label: '真实姓名' },
    { key: 'birthday', label: '生日' },
    { key: 'email', label: '邮箱' },
    { key: 'phone', label: '手机号' }
  ]

  const missingFields = []

  requiredFields.forEach(field => {
    const value = userInfo[field.key]
    if (!value || value === '' ||
        (field.key === 'avatar' && (value.includes('default') || value.includes('placeholder')))) {
      missingFields.push(field.label)
    }
  })

  return {
    isComplete: missingFields.length === 0,
    missingFields
  }
}
