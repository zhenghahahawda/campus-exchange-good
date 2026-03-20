<template>
  <aside 
    class="sidebar" 
    :class="{ 'collapsed': isCollapsed }"
  >
    <!-- Toggle Button -->
    <div class="toggle-btn" @click="toggleSidebar">
        <i :class="isCollapsed ? 'el-icon-arrow-right' : 'el-icon-arrow-left'" />
    </div>

    <div class="logo-area">
      <div class="logo-icon-img">
        <!-- 直接使用 SVG 代码以支持 CSS 变量控制颜色 -->
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">
          <g class="theme-svg-group">
            <g>
              <path d="M5.053,15.158c0,1.395-1.131,2.526-2.526,2.526C1.131,17.684,0,16.553,0,15.158c0-1.395,1.131-2.526,2.526-2.526h2.526 V15.158z"/>
              <path d="M6.316,15.158c0-1.395,1.131-2.526,2.526-2.526s2.526,1.131,2.526,2.526v6.316c0,1.395-1.131,2.526-2.526,2.526 s-2.526-1.131-2.526-2.526V15.158z"/>
            </g>
            <g>
              <path d="M8.842,5.053c-1.395,0-2.526-1.131-2.526-2.526C6.316,1.131,7.447,0,8.842,0s2.526,1.131,2.526,2.526v2.526H8.842z"/>
              <path d="M8.842,6.316c1.395,0,2.526,1.131,2.526,2.526s-1.131,2.526-2.526,2.526H2.526C1.131,11.368,0,10.237,0,8.842 s1.131-2.526,2.526-2.526H8.842z"/>
            </g>
            <g>
              <path d="M18.947,8.842c0-1.395,1.131-2.526,2.526-2.526C22.869,6.316,24,7.447,24,8.842s-1.131,2.526-2.526,2.526h-2.526V8.842z"/>
              <path d="M17.684,8.842c0,1.395-1.131,2.526-2.526,2.526s-2.526-1.131-2.526-2.526V2.526C12.632,1.131,13.763,0,15.158,0 s2.526,1.131,2.526,2.526V8.842z"/>
            </g>
            <g>
              <path d="M15.158,18.947c1.395,0,2.526,1.131,2.526,2.526c0,1.395-1.131,2.526-2.526,2.526s-2.526-1.131-2.526-2.526v-2.526H15.158 z"/>
              <path d="M15.158,17.684c-1.395,0-2.526-1.131-2.526-2.526c0-1.395,1.131-2.526,2.526-2.526h6.316c1.395,0,2.526,1.131,2.526,2.526 c0,1.395-1.131,2.526-2.526,2.526H15.158z"/>
            </g>
          </g>
        </svg>
      </div>
      <transition name="fade">
        <span v-if="!isCollapsed" class="logo-text">校园换物管理平台</span>
      </transition>
    </div>

    <nav class="nav-menu">
      <div 
        v-for="(item, index) in menuItems" 
        :key="index"
        class="nav-item"
        :class="{ active: activeIndex === index }"
        @click="handleNavClick(index, item)"
      >
        <el-tooltip 
          :content="item.label" 
          placement="right" 
          :disabled="!isCollapsed"
          effect="dark"
        >
          <div class="icon-wrapper">
            <i :class="item.icon" class="nav-icon" />
          </div>
        </el-tooltip>
        <transition name="fade">
          <span v-if="!isCollapsed" class="nav-text">{{ item.label }}</span>
        </transition>
        <div v-if="activeIndex === index" class="active-indicator" />
      </div>
    </nav>
    
    <div class="current-user" @click="goToUserCenter">
       <div class="avatar">
         <img :src="userAvatar" alt="User" />
       </div>
       <transition name="fade">
         <div v-if="!isCollapsed" class="info">
           <div class="name">{{ userName }}</div>
           <div class="role">{{ userRole }}</div>
         </div>
       </transition>
    </div>

  </aside>
</template>

<script>
export default {
  name: 'Sidebar',
  data() {
    return {
      activeIndex: 0,
      menuItems: [
        { label: '仪表盘', icon: 'el-icon-s-data', path: '/' },
        { label: '商品管理', icon: 'el-icon-shopping-bag-1', path: '/Back-Page/good' },
        { label: '商品分类', icon: 'el-icon-collection-tag', path: '/Back-Page/category-manage' },
        { label: '订单管理', icon: 'el-icon-s-order', path: '/Back-Page/order' },
        { label: '用户管理', icon: 'el-icon-user-solid', path: '/Back-Page/usersmage' },
        { label: '登录日志', icon: 'el-icon-monitor', path: '/Back-Page/login-log' },
        { label: '系统会话', icon: 'el-icon-chat-dot-square', path: '/Back-Page/session-manage' },
        { label: '违规举报', icon: 'el-icon-warning', path: '/Back-Page/illegal' },
      ]
    }
  },
  computed: {
    isCollapsed() {
      return this.$store.getters.isSidebarCollapsed
    },
    userName() {
      return this.$store.getters.userInfo?.username || '未登录'
    },
    userRole() {
      const info = this.$store.getters.userInfo
      if (!info) return '游客'
      return info.userType === 1 || info.userType === '1' || info.role === 'admin' ? '管理员' : '普通用户'
    },
    userAvatar() {
      const info = this.$store.getters.userInfo
      return info?.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
    }
  },
  created() {
    this.updateActiveIndex();
  },
  watch: {
    '$route'() {
      this.updateActiveIndex();
    }
  },
  methods: {
    updateActiveIndex() {
      const path = this.$route.path;
      const index = this.menuItems.findIndex(item => item.path === path);
      if (index !== -1) {
        this.activeIndex = index;
      }
    },
    handleNavClick(index, item) {
      this.activeIndex = index;
      if (item.path) {
        this.$router.push(item.path);
      }
    },
    toggleSidebar() {
      this.$store.dispatch('toggleSidebar');
    },
    goToUserCenter() {
      this.$router.push('/setting/setuser');
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.sidebar {
  width: $sidebar-width;
  height: calc(100vh - 24px); // 悬浮留空
  position: fixed;
  left: 12px; // 左侧悬浮间距
  top: 12px;  // 顶部悬浮间距
  display: flex;
  flex-direction: column;
  padding: 30px 20px;
  z-index: 1000;
  transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1), padding 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  
  // 使用 Mixin 实现深色玻璃效果
  @include glass-card;
  // 覆盖默认 glass-card 的圆角
  border-radius: 24px;
  
  &.collapsed {
    width: 80px;
    padding: 30px 10px;
    
    .nav-item {
      padding: 0;
      justify-content: center;
      
      .nav-icon {
        margin-right: 0;
      }
    }
    
    .logo-area {
      justify-content: center;
      padding-left: 0;
      .logo-icon-img { margin-right: 0; }
    }
    
    .current-user {
      justify-content: center;
      .avatar { margin-right: 0; }
    }
  }
}

.toggle-btn {
  position: absolute;
  right: -12px; // 悬浮在边框上
  top: 50%;     // 垂直居中
  transform: translateY(-50%);
  width: 12px;
  height: 96px; // 胶囊形状 - 加长高度
  background: $color-bg-surface; // 使用表面背景色
  border: 1px solid $color-border;
  border-left: none; // 与侧边栏融合
  border-radius: 0 12px 12px 0; // 半圆角
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 4px 0 8px rgba(0, 0, 0, 0.05); // 仅右侧阴影
  color: $color-text-secondary;
  font-size: 12px;
  z-index: 1001; // 确保在最上层
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  opacity: 0; // 默认隐藏
  
  &:hover {
    color: $color-primary;
    background: $color-bg-page;
    width: 24px; // 悬停时稍微变宽
    opacity: 1; // 悬停显示
  }
}

.sidebar:hover .toggle-btn {
  opacity: 1; // 悬停在sidebar上时也显示
}

// 收起状态下的按钮样式微调
.sidebar.collapsed .toggle-btn {
  opacity: 0; // 收起时默认隐藏
  width: 24px; // 收起时宽度
  
  &:hover {
      opacity: 1; // 悬停时显示
  }
}

// 当 sidebar collapsed 且 hover 时显示按钮
.sidebar.collapsed:hover .toggle-btn {
    opacity: 1;
}

.logo-area {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
  padding-left: 10px;
  height: 40px;
  overflow: hidden;
  
  .logo-icon-img {
    min-width: 36px;
    width: 36px;
    height: 36px;
    background: #ffffff;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    transition: transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    
    svg {
      width: 24px;
      height: 24px;
      fill: $color-primary; // 默认填充色为主色
      transition: fill 0.3s ease;
      
      path {
        fill: $color-primary;
      }
    }
  }
  
  &:hover .logo-icon-img {
    transform: rotate(15deg) scale(1.1);
  }
  
  .logo-text {
    font-size: 18px;
    font-weight: $font-weight-bold;
    color: $color-text-primary; // 使用主题文字颜色
    white-space: nowrap;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); // 添加轻微文字阴影
  }
}

.nav-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  /* overflow-x: hidden; 移除此属性以显示完整阴影 */
}

.nav-item {
  display: flex;
  align-items: center;
  height: 50px;
  padding: 0 16px;
  border-radius: 16px;
  cursor: pointer;
  color: $color-text-secondary;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  white-space: nowrap;

  &:hover:not(.active) {
    background-color: rgba(255, 255, 255, 0.05);
    color: $color-text-primary;
    
    .nav-icon {
      transform: translateX(2px) scale(1.1);
      color: $color-text-primary;
    }
  }

  &.active {
    background: linear-gradient(135deg,
      rgba(var(--color-primary-rgb), 0.95),
      rgba(var(--color-primary-rgb), 0.85));
    color: $color-nav-active-text;
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3),
                0 0 20px rgba(var(--color-primary-rgb), 0.2);
    
    .nav-icon {
      color: $color-nav-active-text;
      transform: scale(1.05);
    }

    .nav-text {
      font-weight: $font-weight-bold;
    }
  }

  .nav-icon {
    font-size: 20px;
    margin-right: 16px;
    color: $color-text-secondary;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .nav-text {
    font-weight: $font-weight-medium;
    font-size: 15px;
    transition: all 0.3s ease;
  }
}

.current-user {
    display: flex;
    align-items: center;
    padding-top: 20px;
    border-top: 1px solid rgba(255,255,255,0.1); // 分割线
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      .avatar {
        border-color: $color-primary;
        transform: scale(1.05);
      }
    }

    .avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      overflow: hidden;
      margin-right: 12px;
      border: 2px solid rgba(255,255,255,0.2);
      transition: all 0.3s ease;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .info {
      white-space: nowrap;
      .name {
        font-size: 14px;
        font-weight: $font-weight-bold;
        color: $color-text-primary;
      }
      .role {
        font-size: 12px;
        color: $color-text-secondary;
      }
    }
  }

// Transitions
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}
</style>
