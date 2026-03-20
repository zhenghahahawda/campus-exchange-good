/**
 * 心知天气 API 服务
 * 文档：https://seniverse.yuque.com/hyper_data/api_v3
 */

// 心知天气 API 配置
const WEATHER_CONFIG = {
  apiKey: 'Sb5dkFZamkvOCCVCU',
  baseUrl: 'https://api.seniverse.com/v3'
}

// 武汉
const LOCATION = 'wuhan'

/**
 * 获取实时天气
 * @returns {Promise}
 */
export function getRealtimeWeather() {
  const url = `${WEATHER_CONFIG.baseUrl}/weather/now.json?key=${WEATHER_CONFIG.apiKey}&location=${LOCATION}&language=zh-Hans&unit=c`

  return fetch(url)
    .then(response => response.json())
    .then(data => {
      if (data.results && data.results.length > 0) {
        const result = data.results[0]
        const now = result.now
        return {
          success: true,
          data: {
            temperature: now.temperature,
            feelsLike: now.feels_like || now.temperature,
            text: now.text,
            icon: now.code,
            windDir: now.wind_direction || '',
            windScale: now.wind_scale || '',
            humidity: now.humidity || '',
            pressure: now.pressure || '',
            vis: now.visibility || '',
            updateTime: result.last_update
          }
        }
      } else {
        return {
          success: false,
          message: '获取天气失败'
        }
      }
    })
    .catch(error => {
      return {
        success: false,
        message: error.message
      }
    })
}

/**
 * 根据心知天气代码返回对应的 emoji
 * 文档：https://seniverse.yuque.com/hyper_data/api_v3/nyiu3t
 */
export function getWeatherEmoji(code) {
  const iconMap = {
    '0': '☀️', '1': '☀️', '2': '☀️', '3': '☀️',
    '4': '🌤️', '5': '🌤️', '6': '🌤️', '7': '🌤️', '8': '🌤️',
    '9': '☁️', '10': '🌧️', '11': '⛈️', '12': '⛈️',
    '13': '🌦️', '14': '🌧️', '15': '🌧️', '16': '🌧️', '17': '🌧️',
    '18': '🌧️', '19': '🌧️',
    '20': '🌨️', '21': '🌨️', '22': '❄️', '23': '❄️', '24': '❄️', '25': '❄️',
    '26': '🌨️', '27': '❄️', '28': '❄️',
    '29': '💨', '30': '💨', '31': '💨',
    '32': '🌫️', '33': '🌫️', '34': '🌫️', '35': '🌫️',
    '36': '🌫️', '37': '🌫️', '38': '🌫️'
  }
  return iconMap[code] || '☀️'
}

/**
 * 获取天气状况的中文描述颜色
 */
export function getWeatherColor(text) {
  if (text.includes('晴')) return '#FFB800'
  if (text.includes('云')) return '#94A3B8'
  if (text.includes('阴')) return '#64748B'
  if (text.includes('雨')) return '#3B82F6'
  if (text.includes('雪')) return '#E0F2FE'
  if (text.includes('雾') || text.includes('霾')) return '#9CA3AF'
  if (text.includes('雷')) return '#8B5CF6'
  return '#94A3B8'
}
