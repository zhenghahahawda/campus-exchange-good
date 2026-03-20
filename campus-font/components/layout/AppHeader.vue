<template>
  <header class="app-header" :class="{ 'scrolled': isScrolled }">
    <div class="header-container">
      <!-- Logo 区域 -->
      <div class="logo-section" @click="goToHome">
        <div class="logo-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="28" height="28">
            <g class="theme-svg-group">
              <path d="M5.053,15.158c0,1.395-1.131,2.526-2.526,2.526C1.131,17.684,0,16.553,0,15.158c0-1.395,1.131-2.526,2.526-2.526h2.526 V15.158z" fill="currentColor"/>
              <path d="M6.316,15.158c0-1.395,1.131-2.526,2.526-2.526s2.526,1.131,2.526,2.526v6.316c0,1.395-1.131,2.526-2.526,2.526 s-2.526-1.131-2.526-2.526V15.158z" fill="currentColor"/>
              <path d="M8.842,5.053c-1.395,0-2.526-1.131-2.526-2.526C6.316,1.131,7.447,0,8.842,0s2.526,1.131,2.526,2.526v2.526H8.842z" fill="currentColor"/>
              <path d="M8.842,6.316c1.395,0,2.526,1.131,2.526,2.526s-1.131,2.526-2.526,2.526H2.526C1.131,11.368,0,10.237,0,8.842 s1.131-2.526,2.526-2.526H8.842z" fill="currentColor"/>
            </g>
          </svg>
        </div>
        <span class="logo-text">校园换物</span>
      </div>

      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <nuxt-link to="/" class="nav-item" exact-active-class="active">首页</nuxt-link>
        <nuxt-link to="/goods" class="nav-item" active-class="active">探索物品</nuxt-link>
        <nuxt-link to="/exchanges" class="nav-item" active-class="active">我的换物</nuxt-link>
        <nuxt-link to="/chat" class="nav-item" active-class="active">消息</nuxt-link>
        <nuxt-link to="/favorites" class="nav-item" active-class="active">我的收藏</nuxt-link>
        <nuxt-link to="/me" class="nav-item" active-class="active">个人中心</nuxt-link>
      </nav>

      <!-- 右侧工具栏 -->
      <div class="header-tools">
        <!-- 发布按钮 -->
        <el-button type="primary" size="medium" round icon="el-icon-plus" class="publish-btn" @click="handlePublish">
          发布物品
        </el-button>

        <!-- 通知 -->
        <div class="tool-item notification" @click.stop="toggleNotification" ref="notificationRef">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge-item">
            <i class="el-icon-bell" />
          </el-badge>
          
          <!-- 通知面板 -->
          <transition name="el-zoom-in-top">
            <div class="notification-panel" v-if="showNotification" @click.stop>
              <div class="panel-header">
                <h3>通知中心 ({{ unreadCount }})</h3>
                <span class="mark-read" @click="markAllRead">全部已读</span>
              </div>
              <div class="notification-list" v-if="notifications.length > 0">
                <div 
                  v-for="(item, index) in notifications" 
                  :key="index"
                  class="notification-item"
                  :class="{ unread: !item.read }"
                  @click="markAsRead(index)"
                >
                  <div class="item-icon-wrapper" :class="item.type">
                    <i :class="item.icon" />
                  </div>
                  <div class="item-content">
                    <div class="item-header">
                      <span class="item-title">{{ item.title }}</span>
                      <span class="item-time">{{ item.time }}</span>
                    </div>
                    <div class="item-desc">{{ item.description }}</div>
                  </div>
                  <div class="unread-dot" v-if="!item.read"></div>
                </div>
              </div>
              <div class="empty-notification" v-else>
                <i class="el-icon-bell-off"></i>
                <p>暂无新通知</p>
              </div>
            </div>
          </transition>
        </div>

        <!-- 主题切换 -->
        <div class="tool-item theme" @click="goToThemeSelector">
          <i class="el-icon-brush" />
        </div>

        <!-- 用户头像 -->
        <div class="tool-item profile">
          <div class="avatar-wrapper">
            <img :src="headerAvatar" alt="User" />
          </div>
        </div>
      </div>
    </div>
    
    <!-- 发布商品弹窗 -->
    <PublishGoodsDialog :visible.sync="showPublishDialog" />
  </header>
</template>

<script>
import PublishGoodsDialog from '@/components/goods/publish/PublishGoodsDialog.vue'

export default {
  name: 'AppHeader',
  components: {
    PublishGoodsDialog
  },
  data() {
    return {
      isScrolled: false,
      showNotification: false,
      showPublishDialog: false,
      ticking: false,
      notifications: [],
      unreadCount: 0,
      pollingTimer: null,
      headerAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
    }
  },
  computed: {},
  mounted() {
    window.addEventListener('scroll', this.throttledScroll);
    document.addEventListener('click', this.handleClickOutside);
    
    // 加载通知
    this.loadNotifications();
    this.loadUnreadCount();
    
    // 从localStorage读取头像
    this.loadHeaderAvatar();
    
    // 监听头像更新事件
    window.addEventListener('avatar-updated', this.loadHeaderAvatar);
    
    // 每30秒轮询一次未读数
    this.pollingTimer = setInterval(() => {
      this.loadUnreadCount();
    }, 30000);
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.throttledScroll);
    document.removeEventListener('click', this.handleClickOutside);
    window.removeEventListener('avatar-updated', this.loadHeaderAvatar);
    
    // 清除轮询
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer);
    }
  },
  methods: {
    // 从localStorage读取头像
    loadHeaderAvatar() {
      try {
        const userInfo = localStorage.getItem('userInfo');
        if (userInfo) {
          const parsed = JSON.parse(userInfo);
          if (parsed.avatar) {
            this.headerAvatar = parsed.avatar;
          }
        }
      } catch (e) {}
    },
    
    // API方法
    async loadNotifications() {
      try {
        const response = await this.$axios.get('/notifications', {
          params: { page: 1, limit: 20 }
        });
        
        if (response.code === 20000 && response.data) {
          this.notifications = response.data.list.map(item => ({
            id: item.id,
            title: item.title,
            description: item.content,
            time: this.formatTime(item.createdAt),
            icon: this.getNotificationIcon(item.type),
            type: this.getNotificationType(item.type),
            read: item.isRead === 1,
            relatedId: item.relatedId,
            relatedType: item.relatedType
          }));
        }
      } catch (error) {
        console.error('加载通知失败:', error);
        // 如果是401错误，不显示提示（用户可能未登录）
        if (error.response?.status !== 401) {
          this.notifications = [];
        }
      }
    },
    
    async loadUnreadCount() {
      try {
        const response = await this.$axios.get('/notifications/unread-count');
        if (response.code === 20000) {
          this.unreadCount = response.data || 0;
        }
      } catch (error) {
        // 静默失败，不影响用户体验
        if (error.response?.status !== 401) {
          this.unreadCount = 0;
        }
      }
    },
    
    async markAsRead(index) {
      const notification = this.notifications[index];
      if (notification.read) return;
      
      try {
        await this.$axios.put(`/notifications/${notification.id}/read`);
        notification.read = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
        
        // 根据通知类型跳转
        this.handleNotificationClick(notification);
      } catch (error) {
        console.error('标记已读失败:', error);
      }
    },
    
    async markAllRead() {
      try {
        await this.$axios.put('/notifications/read-all');
        this.notifications.forEach(n => n.read = true);
        this.unreadCount = 0;
        this.$message.success('已全部标记为已读');
      } catch (error) {
        console.error('全部已读失败:', error);
        this.$message.error('操作失败');
      }
    },
    
    // 辅助方法
    getNotificationIcon(type) {
      const iconMap = {
        'new_goods': 'el-icon-goods',
        'exchange_request': 'el-icon-refresh',
        'exchange_accepted': 'el-icon-success',
        'exchange_rejected': 'el-icon-error',
        'system': 'el-icon-bell'
      };
      return iconMap[type] || 'el-icon-bell';
    },
    
    getNotificationType(type) {
      const typeMap = {
        'new_goods': 'success',
        'exchange_request': 'info',
        'exchange_accepted': 'success',
        'exchange_rejected': 'error',
        'system': 'info'
      };
      return typeMap[type] || 'info';
    },
    
    formatTime(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      const now = new Date();
      const diff = now - date;
      const minutes = Math.floor(diff / (1000 * 60));
      const hours = Math.floor(diff / (1000 * 60 * 60));
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      
      if (minutes < 1) return '刚刚';
      if (minutes < 60) return `${minutes}分钟前`;
      if (hours < 24) return `${hours}小时前`;
      if (days < 7) return `${days}天前`;
      return date.toLocaleDateString();
    },
    
    handleNotificationClick(notification) {
      // 根据通知类型跳转到相应页面
      if (notification.relatedType === 'goods' && notification.relatedId) {
        this.$router.push(`/goods/${notification.relatedId}`);
      } else if (notification.relatedType === 'exchange' && notification.relatedId) {
        this.$router.push('/exchanges');
      }
      this.showNotification = false;
    },
    
    // UI交互方法
    handleClickOutside(event) {
      const notificationEl = this.$refs.notificationRef;
      if (this.showNotification && notificationEl && !notificationEl.contains(event.target)) {
        this.showNotification = false;
      }
    },
    throttledScroll() {
      if (!this.ticking) {
        window.requestAnimationFrame(() => {
          this.handleScroll();
          this.ticking = false;
        });
        this.ticking = true;
      }
    },
    handleScroll() {
      const scrolled = window.scrollY > 20;
      if (this.isScrolled !== scrolled) {
        this.isScrolled = scrolled;
      }
    },
    goToHome() { this.$router.push('/'); },
    goToThemeSelector() { this.$router.push('/theme-selector'); },
    handlePublish() { this.showPublishDialog = true; },
    toggleNotification(event) {
      this.showNotification = !this.showNotification;
      if (this.showNotification) {
        // 打开通知面板时刷新列表
        this.loadNotifications();
      }
      if (event) event.stopPropagation();
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.app-header {
  height: 72px;
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
  padding: 0 40px;
  
  &.scrolled {
    background: var(--card-bg);
    backdrop-filter: blur(var(--glass-blur));
    -webkit-backdrop-filter: blur(var(--glass-blur));
    border-bottom: 1px solid var(--card-border);
    box-shadow: var(--card-shadow);
  }
}

.header-container {
  max-width: 1440px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  
  .logo-icon {
    color: $color-primary;
    display: flex;
    align-items: center;
  }
  
  .logo-text {
    font-size: 18px;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    letter-spacing: -0.5px;
  }
}

.nav-menu {
  display: flex;
  gap: 32px;
  align-items: center;
  
  .nav-item {
      text-decoration: none;
      font-size: 15px;
      font-weight: $font-weight-medium;
      color: rgba(255, 255, 255, 0.85); /* 提高非激活状态的文字亮度 */
      transition: all 0.3s ease;
      position: relative;
      padding: 8px 0;
      text-shadow: 0 1px 2px rgba(0,0,0,0.3); /* 增加文字阴影提高可读性 */
      
      &:hover {
        color: #ffffff;
        text-shadow: 0 0 8px rgba(var(--color-primary-rgb), 0.6);
      }
      
      &.active {
        color: $color-primary;
        font-weight: 600;
        text-shadow: 0 0 12px rgba(var(--color-primary-rgb), 0.5);
        
        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 0;
          right: 0;
          height: 3px;
          background: $color-primary;
          border-radius: 2px;
          box-shadow: 0 0 8px rgba(var(--color-primary-rgb), 0.6);
        }
      }
    }
}

.header-tools {
  display: flex;
  align-items: center;
  gap: 16px;
}

.tool-item {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(var(--color-primary-rgb), 0.05);
  color: $color-text-secondary;
  
  &:hover {
    background: rgba(var(--color-primary-rgb), 0.1);
    color: $color-primary;
    transform: translateY(-2px);
  }
  
  i { font-size: 20px; }
}

.avatar-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid rgba(var(--color-primary-rgb), 0.2);
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.notification-panel {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 16px;
  width: 360px;
  @include glass-card;
  border-radius: 16px;
  overflow: hidden;
  z-index: 2000;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);
  
  .panel-header {
    padding: 16px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    
    h3 { font-size: 15px; font-weight: 600; color: var(--color-text-primary); margin: 0; }
    .mark-read { 
      font-size: 13px; 
      color: var(--color-primary); 
      cursor: pointer;
      opacity: 0.8;
      &:hover { opacity: 1; }
    }
  }
  
  .notification-list {
    max-height: 400px;
    overflow-y: auto;
    
    // 自定义滚动条
    &::-webkit-scrollbar {
      width: 4px;
    }
    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 2px;
    }
  }
  
  .notification-item {
    padding: 16px 20px;
    display: flex;
    gap: 16px;
    cursor: pointer;
    transition: all 0.2s ease;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    position: relative;
    
    &:hover { background: rgba(var(--color-primary-rgb), 0.08); }
    
    &.unread {
      background: rgba(var(--color-primary-rgb), 0.03);
      .item-title { color: var(--color-text-primary); font-weight: 600; }
    }
    
    .item-icon-wrapper {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      flex-shrink: 0;
      
      &.success { background: rgba(var(--color-secondary-success-rgb), 0.1); color: var(--color-secondary-success); }
      &.warning { background: rgba(var(--color-secondary-warning-rgb), 0.1); color: var(--color-secondary-warning); }
      &.info { background: rgba(var(--color-secondary-info-rgb), 0.1); color: var(--color-secondary-info); }
      &.error { background: rgba(var(--color-secondary-danger-rgb), 0.1); color: var(--color-secondary-danger); }
    }
    
    .item-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .item-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .item-title { font-size: 14px; color: var(--color-text-primary); }
        .item-time { font-size: 12px; color: var(--color-text-disabled); }
      }
      
      .item-desc { 
        font-size: 13px; 
        color: var(--color-text-secondary); 
        line-height: 1.4;
      }
    }
    
    .unread-dot {
      position: absolute;
      top: 16px;
      right: 16px;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--color-secondary-danger);
    }
  }
  
  .empty-notification {
    padding: 40px 0;
    text-align: center;
    color: var(--color-text-disabled);
    
    i { font-size: 48px; margin-bottom: 12px; opacity: 0.5; }
    p { margin: 0; font-size: 14px; }
  }
}

@media (max-width: 768px) {
  .nav-menu, .publish-btn { display: none; }
  .app-header { padding: 0 16px; }
}
</style>
