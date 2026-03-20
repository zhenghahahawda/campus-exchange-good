<template>
  <div class="me-page">
    <div class="page-header">
      <div class="header-text">
        <h1>个人中心</h1>
        <p>管理你的收藏、换物与偏好设置</p>
      </div>
    </div>

    <div class="profile-card">
      <div class="avatar" @click="triggerAvatarUpload" style="cursor:pointer; position:relative;">
        <img :src="avatarUrl" alt="User" />
        <div class="avatar-overlay">
          <i class="el-icon-camera"></i>
        </div>
        <input 
          ref="avatarInput" 
          type="file" 
          accept="image/*" 
          style="display:none"
          @change="handleAvatarChange"
        />
      </div>
      <div class="profile-info" v-if="userInfo">
        <div class="name">{{ userInfo.username }}</div>
        <div class="sub">{{ userInfo.school || '校园换物平台用户' }}</div>
        <div class="user-details">
          <span class="detail-item">
            <i class="el-icon-user"></i>
            用户ID: {{ userInfo.userId }}
          </span>
          <span class="detail-item">
            <i class="el-icon-medal"></i>
            {{ getUserTypeText(userInfo.userType) }}
          </span>
          <span class="detail-item" :class="getStatusClass(userInfo.status)">
            <i class="el-icon-circle-check"></i>
            {{ getStatusText(userInfo.status) }}
          </span>
        </div>
      </div>
      <div class="profile-info" v-else>
        <div class="name">{{ displayName }}</div>
        <div class="sub">{{ subtitle }}</div>
      </div>
      <!-- 根据登录状态显示不同按钮 -->
      <el-button 
        v-if="!isLoggedIn" 
        type="primary" 
        class="action-btn" 
        @click="goToLogin"
      >
        <i class="el-icon-user"></i>
        登录
      </el-button>
      <el-button 
        v-else 
        type="primary" 
        plain 
        class="action-btn" 
        @click="goToTheme"
      >
        <i class="el-icon-brush"></i>
        主题
      </el-button>
    </div>

    <div class="quick-grid">
      <div class="quick-card" @click="goToFavorites">
        <div class="icon">
          <i class="el-icon-collection-tag"></i>
        </div>
        <div class="content">
          <div class="title">我的收藏</div>
          <div class="desc">查看你收藏的物品</div>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>

      <div class="quick-card" @click="goToExchanges">
        <div class="icon">
          <i class="el-icon-refresh"></i>
        </div>
        <div class="content">
          <div class="title">我的换物</div>
          <div class="desc">查看交换记录与进度</div>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>

      <div class="quick-card" @click="goToMessages">
        <div class="icon">
          <i class="el-icon-chat-dot-round"></i>
        </div>
        <div class="content">
          <div class="title">消息</div>
          <div class="desc">进入聊天与通知</div>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>

      <div class="quick-card" @click="goToSettings">
        <div class="icon">
          <i class="el-icon-setting"></i>
        </div>
        <div class="content">
          <div class="title">设置</div>
          <div class="desc">账号与通用设置</div>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>

      <div class="quick-card" @click="goToViolations">
        <div class="icon" style="background: rgba(245,108,108,0.12); color: #F56C6C;">
          <i class="el-icon-warning"></i>
        </div>
        <div class="content">
          <div class="title">我的举报</div>
          <div class="desc">查看违规举报记录</div>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>
    </div>

    <!-- 如果未登录，显示登出按钮 -->
    <div v-if="isLoggedIn" class="logout-section">
      <button class="logout-btn" @click="handleLogout">
        <i class="el-icon-switch-button"></i>
        退出登录
      </button>
    </div>
  </div>
</template>

<script>
import { getToken, clearAuth } from '@/utils/auth'
import { addUserQueryToRoute } from '@/utils/urlHelper'

export default {
  name: 'MePage',
  data() {
    return {
      avatarUrl: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
      displayName: '未登录用户',
      subtitle: '欢迎来到校园换物平台',
      isLoggedIn: false,
      userInfo: null,
      loading: false
    }
  },
  mounted() {
    // 立即从localStorage读取头像，避免等待API
    try {
      const stored = JSON.parse(localStorage.getItem('userInfo') || '{}')
      if (stored.avatar) this.avatarUrl = stored.avatar
    } catch (e) {}
    this.checkLoginStatus()
  },
  methods: {
    triggerAvatarUpload() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录');
        return;
      }
      this.$refs.avatarInput.click();
    },
    
    async handleAvatarChange(e) {
      const file = e.target.files[0];
      if (!file) return;
      
      if (file.size > 5 * 1024 * 1024) {
        this.$message.error('图片大小不能超过5MB');
        return;
      }
      
      try {
        this.$message.info('正在上传头像...');
        
        const formData = new FormData();
        formData.append('avatar', file);  // 参数名是 avatar
        
        const res = await this.$axios.post('/user/avatar', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        
        if (res.code === 20000 && res.data) {
          // 更新本地userInfo
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
          userInfo.avatar = res.data;
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          
          // 更新页面显示
          this.avatarUrl = res.data;
          this.userInfo = { ...this.userInfo, avatar: res.data };
          
          // 通知AppHeader更新头像
          window.dispatchEvent(new Event('avatar-updated'));
          
          this.$message.success('头像更新成功');
        }
      } catch (error) {
        console.error('头像上传失败:', error);
        this.$message.error('头像上传失败，请稍后重试');
      }
      
      e.target.value = '';
    },
    
    // 检查登录状态 - 调用后端接口验证
    async checkLoginStatus() {
      const token = getToken()
      
      if (!token) {
        this.isLoggedIn = false
        this.userInfo = null
        this.displayName = '未登录用户'
        this.subtitle = '点击登录按钮开始使用'
        return
      }

      this.loading = true
      
      try {
        // 调用 /user/info 获取完整用户信息（含头像）
        const response = await this.$axios.get('/user/info')
        
        if (response.code === 20000 && response.data) {
          this.isLoggedIn = true
          this.userInfo = response.data
          
          // 更新头像
          const avatarFromApi = response.data.avatar || response.data.avatarUrl || response.data.avatar_url
          if (avatarFromApi) {
            this.avatarUrl = avatarFromApi
            // 同步到localStorage
            const stored = JSON.parse(localStorage.getItem('userInfo') || '{}')
            stored.avatar = avatarFromApi
            localStorage.setItem('userInfo', JSON.stringify(stored))
            // 通知header更新
            window.dispatchEvent(new Event('avatar-updated'))
          } else {
            // 接口没返回头像，从localStorage读取
            try {
              const stored = JSON.parse(localStorage.getItem('userInfo') || '{}')
              if (stored.avatar) this.avatarUrl = stored.avatar
            } catch (e) {}
          }
        } else {
          this.handleVerifyFailed()
        }
      } catch (error) {
        console.error('❌ 获取用户信息失败:', error)
        this.handleVerifyFailed()
      } finally {
        this.loading = false
      }
    },
    
    // 验证失败处理
    handleVerifyFailed() {
      this.isLoggedIn = false
      this.userInfo = null
      this.displayName = '未登录用户'
      this.subtitle = '登录已过期，请重新登录'
      clearAuth()
    },
    
    // 获取用户类型文本
    getUserTypeText(userType) {
      return userType === 1 ? '管理员' : '普通用户'
    },
    
    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        0: '已封禁',
        1: '正常',
        2: '活跃'
      }
      return statusMap[status] || '未知'
    },
    
    // 获取状态样式类
    getStatusClass(status) {
      return {
        'status-banned': status === 0,
        'status-normal': status === 1,
        'status-active': status === 2
      }
    },
    
    // 跳转到登录页面
    goToLogin() {
      this.$router.push('/login-page/login')
    },
    
    // 退出登录
    async handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端登出接口
          await this.$axios.post('/auth/logout')
        } catch (error) {
          console.warn('登出接口调用失败:', error)
        } finally {
          // 无论接口是否成功，都清除本地认证信息
          clearAuth()
          
          // 重置页面状态
          this.userInfo = null
          this.isLoggedIn = false
          this.displayName = '未登录用户'
          this.subtitle = '点击登录按钮开始使用'
          this.avatarUrl = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
          
          // 提示用户
          this.$message({
            type: 'success',
            message: '已退出登录'
          })
        }
      }).catch(() => {
        // 用户取消
      })
    },
    
    goToFavorites() { 
      if (!this.isLoggedIn) {
        this.$message({ type: 'warning', message: '请先登录' })
        this.goToLogin()
        return
      }
      this.$router.push(addUserQueryToRoute('/favorites'))
    },
    
    goToExchanges() { 
      if (!this.isLoggedIn) {
        this.$message({ type: 'warning', message: '请先登录' })
        this.goToLogin()
        return
      }
      this.$router.push(addUserQueryToRoute('/exchanges'))
    },
    
    goToMessages() { 
      if (!this.isLoggedIn) {
        this.$message({ type: 'warning', message: '请先登录' })
        this.goToLogin()
        return
      }
      this.$router.push(addUserQueryToRoute('/chat'))
    },
    
    goToTheme() { 
      this.$router.push(addUserQueryToRoute('/theme-selector'))
    },
    
    goToSettings() {
      if (!this.isLoggedIn) {
        this.$message({ type: 'warning', message: '请先登录' })
        this.goToLogin()
        return
      }
      this.$router.push('/settings')
    },
    goToViolations() {
      if (!this.isLoggedIn) {
        this.$message({ type: 'warning', message: '请先登录' })
        this.goToLogin()
        return
      }
      this.$router.push('/violations')
    }
  },
  head() {
    return { title: '个人中心 - 校园换物平台' }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.me-page {
  padding: 20px;
  min-height: 100vh;
  color: $color-text-primary;
  max-width: 960px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  .header-text {
    h1 {
      font-size: 32px;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin-bottom: 8px;
    }
    p {
      font-size: 16px;
      color: $color-text-secondary;
      margin: 0;
    }
  }
}

.profile-card {
  @include glass-card;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  .avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    overflow: hidden;
    border: 2px solid rgba(var(--color-primary-rgb), 0.25);
    flex-shrink: 0;
    position: relative;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .avatar-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.2s;
      border-radius: 50%;
      
      i {
        color: white;
        font-size: 18px;
      }
    }
    
    &:hover .avatar-overlay {
      opacity: 1;
    }
  }

  .profile-info {
    flex: 1;
    min-width: 0;
    .name {
      font-size: 18px;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .sub {
      margin-top: 4px;
      font-size: 13px;
      color: $color-text-secondary;
    }
    .user-details {
      margin-top: 8px;
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      
      .detail-item {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: $color-text-secondary;
        padding: 2px 8px;
        border-radius: 6px;
        background: rgba(var(--color-primary-rgb), 0.05);
        
        i {
          font-size: 12px;
        }
        
        &.status-banned {
          color: #f56c6c;
          background: rgba(245, 108, 108, 0.1);
        }
        
        &.status-normal {
          color: #67c23a;
          background: rgba(103, 194, 58, 0.1);
        }
        
        &.status-active {
          color: #409eff;
          background: rgba(64, 158, 255, 0.1);
        }
      }
    }
  }

  .action-btn {
    height: 40px;
    border-radius: 10px;
    border-color: rgba(var(--color-primary-rgb), 0.35);
    color: $color-primary;
    background: rgba(var(--color-primary-rgb), 0.08);
    i { margin-right: 6px; }
  }
}

.quick-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.quick-card {
  @include glass-card;
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
  }

  .icon {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(var(--color-primary-rgb), 0.12);
    color: $color-primary;
    flex-shrink: 0;
    i { font-size: 18px; }
  }

  .content {
    flex: 1;
    min-width: 0;
    .title {
      font-size: 15px;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
    }
    .desc {
      margin-top: 4px;
      font-size: 13px;
      color: $color-text-secondary;
    }
  }

  .el-icon-arrow-right {
    color: $color-text-disabled;
    font-size: 16px;
  }
}

@media (max-width: $breakpoint-sm) {
  .profile-card {
    flex-direction: column;
    align-items: flex-start;
    .action-btn { width: 100%; }
  }
}

.logout-section {
  margin-top: 24px;
  text-align: center;

  .logout-btn {
    width: 100%;
    max-width: 400px;
    height: 48px;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    border: 1.5px solid rgba(var(--color-secondary-danger-rgb, 245, 54, 92), 0.5);
    background: rgba(var(--color-secondary-danger-rgb, 245, 54, 92), 0.08);
    color: var(--color-secondary-danger, #f5365c);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    letter-spacing: 0.5px;

    i { margin-right: 8px; }

    &:hover {
      background: rgba(var(--color-secondary-danger-rgb, 245, 54, 92), 0.18);
      border-color: var(--color-secondary-danger, #f5365c);
      box-shadow: 0 4px 16px rgba(var(--color-secondary-danger-rgb, 245, 54, 92), 0.25);
    }

    &:active {
      background: rgba(var(--color-secondary-danger-rgb, 245, 54, 92), 0.25);
    }
  }
}
</style>

