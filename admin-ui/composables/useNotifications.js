/**
 * 通知中心 Composable
 * 直接从业务表聚合通知数据，无需独立通知表
 */

/** 通知类型配置 */
const NOTIFICATION_TYPE_MAP = {
  goods_audit: {
    icon: 'el-icon-goods',
    color: 'warning'
  },
  order_process: {
    icon: 'el-icon-shopping-cart-2',
    color: 'success'
  },
  user_register: {
    icon: 'el-icon-user',
    color: 'info'
  }
}

/**
 * 格式化通知时间为相对时间
 */
function formatRelativeTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return dateStr.substring(0, 10)
}

/**
 * 通知管理组合式函数
 * @param {Object} vm - Vue 组件实例
 * @returns {Object} 通知管理方法集合
 */
export function useNotifications(vm) {
  return {
    /**
     * 获取通知列表
     */
    async fetchNotifications(limit = 10) {
      try {
        const res = await vm.$axios.get('/api/admin/notifications', {
          params: { limit }
        })
        const data = res.data || res
        if (data && data.code === 200 && data.data) {
          const records = data.data || []
          return records.map(item => this.normalizeNotification(item))
        }
        return []
      } catch (e) {
        console.error('获取通知列表失败:', e)
        return []
      }
    },

    /**
     * 获取未读通知数量
     */
    async fetchUnreadCount() {
      try {
        const res = await vm.$axios.get('/api/admin/notifications/unread-count')
        const data = res.data || res
        if (data && data.code === 200) {
          return data.data || 0
        }
        return 0
      } catch (e) {
        console.error('获取未读数量失败:', e)
        return 0
      }
    },

    /**
     * 点击通知：跳转到对应页面
     */
    handleNotificationClick(notification) {
      if (notification.targetPath) {
        vm.$router.push(notification.targetPath)
      }
    },

    /**
     * 将后端数据转换为前端通知格式
     */
    normalizeNotification(raw) {
      const typeConfig = NOTIFICATION_TYPE_MAP[raw.type] || {
        icon: 'el-icon-info',
        color: 'info'
      }
      return {
        title: raw.title,
        description: raw.description,
        time: formatRelativeTime(raw.createdAt),
        icon: typeConfig.icon,
        type: typeConfig.color,
        targetPath: raw.targetPath
      }
    }
  }
}
