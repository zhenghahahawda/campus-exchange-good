/**
 * 图片处理工具函数
 */

/**
 * 获取商品的第一张图片URL
 * 兼容后端返回的images数组和前端mock的image字符串
 * @param {Object} good - 商品对象
 * @returns {String} 图片URL
 */
export function getGoodImage(good) {
  if (!good) {
    return getDefaultImage()
  }

  // 如果有images数组（后端返回格式）
  if (good.images && Array.isArray(good.images) && good.images.length > 0) {
    return good.images[0]
  }

  // 如果有image字符串（mock数据格式）
  if (good.image && typeof good.image === 'string') {
    return good.image
  }

  // 如果images是JSON字符串，尝试解析
  if (good.images && typeof good.images === 'string') {
    try {
      const parsed = JSON.parse(good.images)
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed[0]
      }
    } catch (e) {
      console.warn('解析images JSON失败:', e)
    }
  }

  // 返回默认图片
  return getDefaultImage()
}

/**
 * 获取商品的所有图片URL数组
 * @param {Object} good - 商品对象
 * @returns {Array} 图片URL数组
 */
export function getGoodImages(good) {
  if (!good) {
    return [getDefaultImage()]
  }

  // 如果有images数组
  if (good.images && Array.isArray(good.images) && good.images.length > 0) {
    return good.images
  }

  // 如果有image字符串
  if (good.image && typeof good.image === 'string') {
    return [good.image]
  }

  // 如果images是JSON字符串
  if (good.images && typeof good.images === 'string') {
    try {
      const parsed = JSON.parse(good.images)
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed
      }
    } catch (e) {
      console.warn('解析images JSON失败:', e)
    }
  }

  return [getDefaultImage()]
}

/**
 * 获取默认商品图片
 * @returns {String} 默认图片URL
 */
export function getDefaultImage() {
  // 使用可靠的图片源
  return 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
}

/**
 * 处理图片加载错误
 * @param {Event} event - 错误事件
 */
export function handleImageError(event) {
  console.warn('图片加载失败:', event.target.src)
  // 使用Element UI的默认图片
  event.target.src = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
  // 防止无限循环
  event.target.onerror = null
}

/**
 * 获取用户头像URL
 * @param {Object} user - 用户对象
 * @returns {String} 头像URL
 */
export function getUserAvatar(user) {
  if (!user) {
    return getDefaultAvatar()
  }

  if (user.avatar && typeof user.avatar === 'string') {
    return user.avatar
  }

  return getDefaultAvatar()
}

/**
 * 获取默认用户头像
 * @returns {String} 默认头像URL
 */
export function getDefaultAvatar() {
  return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
}
