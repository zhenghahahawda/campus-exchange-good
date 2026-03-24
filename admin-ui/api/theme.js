/**
 * 主题管理 API
 */

/**
 * 获取启用的主题列表（普通用户）
 */
export function getActiveThemes(axios) {
  return axios({
    url: '/api/themes',
    method: 'get'
  })
}

/**
 * 获取全部主题列表（管理员，含禁用）
 */
export function getAllThemes(axios) {
  return axios({
    url: '/api/themes/all',
    method: 'get'
  })
}

/**
 * 获取主题详情
 */
export function getThemeById(axios, themeId) {
  return axios({
    url: `/api/themes/${themeId}`,
    method: 'get'
  })
}

/**
 * 创建主题
 */
export function createTheme(axios, data) {
  return axios({
    url: '/api/themes',
    method: 'post',
    data
  })
}

/**
 * 更新主题
 */
export function updateTheme(axios, themeId, data) {
  return axios({
    url: `/api/themes/${themeId}`,
    method: 'put',
    data
  })
}

/**
 * 删除主题
 */
export function deleteTheme(axios, themeId) {
  return axios({
    url: `/api/themes/${themeId}`,
    method: 'delete'
  })
}

/**
 * 启用/禁用主题
 */
export function toggleTheme(axios, themeId) {
  return axios({
    url: `/api/themes/${themeId}/toggle`,
    method: 'post'
  })
}

/**
 * 上传主题壁纸
 */
export function uploadWallpaper(axios, formData) {
  return axios({
    url: '/api/themes/upload/wallpaper',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 上传主题预览图
 */
export function uploadPreview(axios, formData) {
  return axios({
    url: '/api/themes/upload/preview',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
