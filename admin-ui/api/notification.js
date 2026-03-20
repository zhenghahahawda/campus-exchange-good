/**
 * 通知中心 API 服务
 */
export default function ({ $axios }) {
  return {
    /**
     * 获取通知列表（分页）
     */
    getNotifications(params) {
      return $axios.$get('/api/admin/notifications', { params })
    },

    /**
     * 获取未读通知数量
     */
    getUnreadCount() {
      return $axios.$get('/api/admin/notifications/unread-count')
    },

    /**
     * 标记单条通知为已读
     */
    markAsRead(id) {
      return $axios.$put(`/api/admin/notifications/${id}/read`)
    },

    /**
     * 全部标记为已读
     */
    markAllAsRead() {
      return $axios.$put('/api/admin/notifications/read-all')
    },

    /**
     * 删除通知
     */
    deleteNotification(id) {
      return $axios.$delete(`/api/admin/notifications/${id}`)
    }
  }
}
