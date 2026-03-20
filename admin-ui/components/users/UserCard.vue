<template>
  <div class="user-card">
    <!-- 用户头像区域 -->
    <div class="card-header">
      <div class="avatar-wrapper">
        <el-avatar :key="avatarUrl" :size="80" :src="avatarUrl" class="user-avatar">
          {{ user.username ? user.username.charAt(0).toUpperCase() : 'U' }}
        </el-avatar>
        <div v-if="user.isOnline" class="online-indicator" />
      </div>
      <!-- 用户名 -->
      <h3 class="header-username">{{ user.username || '未知用户' }}</h3>
      <!-- 状态徽标 -->
      <div class="status-badge" :class="userStatusClass">
        {{ userStatusLabel }}
      </div>
      <!-- 更多操作按钮 -->
      <div class="more-dropdown">
        <GlassDropdown @command="handleCommand">
          <template slot="trigger" slot-scope="{ isOpen }">
            <div class="more-btn" :class="{ 'is-open': isOpen }">
              <i class="el-icon-more" />
            </div>
          </template>
          <GlassDropdownItem command="reset-password">
            <i class="el-icon-key" /> 重置密码
          </GlassDropdownItem>
          <GlassDropdownItem command="delete" class="text-danger">
            <i class="el-icon-delete" /> 删除用户
          </GlassDropdownItem>
        </GlassDropdown>
      </div>
    </div>

    <!-- 用户信息区域 -->
    <div class="card-body">
      <h3 class="user-name">{{ user.realName || '未实名' }}</h3>
      <!-- 用户类型徽章：只在非管理员时显示 -->
      <div v-if="user.role !== 'admin'" class="user-type-badge" :class="typeBadgeClass">
        <i :class="typeIcon" />
        <span>{{ typeLabel }}</span>
      </div>
      
      <div class="user-info">
        <div class="info-item">
          <i class="el-icon-postcard" />
          <span>ID: {{ user.userId }}</span>
        </div>
        <div class="info-item">
          <i class="el-icon-location" />
          <span>{{ user.accountAddress }}</span>
        </div>
        <div class="info-item">
          <i class="el-icon-school" />
          <span>{{ user.school }}</span>
        </div>
        <div class="info-item">
          <i class="el-icon-phone" />
          <span>{{ user.phone }}</span>
        </div>
        <div class="info-item">
          <i class="el-icon-time" />
          <span>最近登录: {{ formatLastLogin(user.lastLoginAt) }}</span>
        </div>
        <div v-if="user.lockUntil" class="info-item text-danger">
          <i class="el-icon-lock" />
          <span>解封: {{ formatLockDate(user.lockUntil) }}</span>
        </div>
      </div>
      
      <div class="user-stats">
        <div class="stat-item">
          <div class="stat-value">{{ user.goodsCount || 0 }}</div>
          <div class="stat-label">发布商品</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ user.ordersCount || 0 }}</div>
          <div class="stat-label">交易订单</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ user.reputation || 100 }}</div>
          <div class="stat-label">信誉分</div>
        </div>
      </div>
    </div>
    
    <!-- 卡片底部操作栏 -->
    <div class="card-footer">
      <GlassSwitch 
        v-model="user.isActive" 
        active-text="正常" 
        inactive-text="封禁"
        @change="handleToggleStatus" 
      />
      <button class="glass-btn btn-edit" @click="handleEdit">
        <i class="el-icon-edit" />
        <span>编辑</span>
      </button>
    </div>
  </div>
</template>

<script>
import GlassDropdown from '@/components/ui/GlassDropdown.vue';
import GlassDropdownItem from '@/components/ui/GlassDropdownItem.vue';
import GlassSwitch from '@/components/ui/GlassSwitch.vue';
import { UserDisplay } from '@/utils/userManager';
import { USER_STATUS_LABELS, USER_STATUS } from '@/utils/userConstants';

export default {
  name: 'UserCard',
  components: {
    GlassDropdown,
    GlassDropdownItem,
    GlassSwitch
  },
  props: {
    user: {
      type: Object,
      required: true
    }
  },
  computed: {
    // 生成头像 URL，如果没有则返回 null 让 el-avatar 显示文字
    avatarUrl() {
      return this.user.avatar || null;
    },
    // 用户状态标签：右上角显示，status = 1 或 2 显示"正常"，status = 0 显示"封禁"
    userStatusLabel() {
      return this.user.status === 1 || this.user.status === 2 ? '正常' : '封禁';
    },
    // 用户状态样式类
    userStatusClass() {
      return this.user.status === 1 || this.user.status === 2 ? 'badge-success' : 'badge-banned';
    },
    statusLabel() {
      return USER_STATUS_LABELS[this.user.status] || this.user.status;
    },
    statusClass() {
      return this.user.status === USER_STATUS.ACTIVE ? 'badge-success' : 'badge-danger';
    },
    // 用户类型标签：根据 status 字段，0=封禁，1=普通用户，2=活跃用户
    typeLabel() {
      const typeMap = {
        0: '已封禁',
        1: '普通用户',
        2: '活跃用户'
      };
      return typeMap[this.user.status] || '未知状态';
    },
    // 用户类型图标
    typeIcon() {
      const iconMap = {
        0: 'el-icon-close',
        1: 'el-icon-user-solid',
        2: 'el-icon-star-on'
      };
      return iconMap[this.user.status] || 'el-icon-user-solid';
    },
    // 用户类型徽章样式
    typeBadgeClass() {
      const classMap = {
        0: 'badge-banned',
        1: 'badge-normal',
        2: 'badge-active'
      };
      return classMap[this.user.status] || 'badge-normal';
    },
    onlineStatusText() {
      return UserDisplay.getOnlineStatusText(this.user);
    }
  },
  methods: {
    handleToggleStatus() {
      this.$emit('toggle-status', this.user);
    },
    handleEdit() {
      this.$emit('edit', this.user);
    },
    handleCommand(command) {
      this.$emit('command', command, this.user);
    },
    formatLockDate(dateStr) {
      if (!dateStr) return '';
      // 简单的日期格式化，假设是 ISO 字符串或类似
      try {
        const date = new Date(dateStr);
        return date.toLocaleDateString();
      } catch (e) {
        return dateStr;
      }
    },
    formatLastLogin(dateStr) {
      if (!dateStr) return '从未登录';
      try {
        const date = new Date(dateStr);
        if (isNaN(date.getTime())) return dateStr;
        
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        
        return `${year}-${month}-${day} ${hours}:${minutes}`;
      } catch (e) {
        return dateStr;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.user-card {
  @include glass-card;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.08);
  
  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  }
  
  // 头部区域
  .card-header {
    position: relative;
    padding: 24px;
    background: linear-gradient(135deg,
      rgba(var(--color-primary-rgb), 0.1),
      rgba(var(--color-primary-rgb), 0.05));
    display: flex;
    flex-direction: column;
    align-items: center;

    .avatar-wrapper {
      position: relative;
      display: inline-block;

      .user-avatar {
        border: 4px solid rgba(255, 255, 255, 0.3);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        background: linear-gradient(135deg,
          rgba(var(--color-primary-rgb), 0.8),
          rgba(var(--color-primary-rgb), 0.6));
        color: #fff;
        font-size: 32px;
        font-weight: 700;
      }

      .online-indicator {
        position: absolute;
        bottom: 8px;
        right: 8px;
        width: 20px;
        height: 20px;
        background: $color-secondary-success;
        border: 3px solid #fff;
        border-radius: 50%;
        box-shadow: 0 2px 8px rgba(var(--color-secondary-success-rgb), 0.4);
        animation: pulse 2s infinite;
      }
    }

    .header-username {
      margin-top: 12px;
      font-size: 16px;
      font-weight: 600;
      color: $color-text-primary;
      text-align: center;
    }

    .status-badge {
      position: absolute;
      top: 16px;
      left: 16px;
      padding: 5px 12px;
      border-radius: 10px;
      font-size: 12px;
      font-weight: 600;
      backdrop-filter: blur(10px);
      color: #fff;
      border: 1px solid rgba(255, 255, 255, 0.3);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

      &.badge-success {
        background: rgba(var(--color-secondary-success-rgb), 0.9);
      }

      &.badge-danger {
        background: rgba(var(--color-secondary-danger-rgb), 0.9);
      }

      &.badge-banned {
        background: rgba(var(--color-secondary-danger-rgb), 0.9);
      }

      &.badge-normal {
        background: rgba(var(--color-primary-rgb), 0.9);
      }

      &.badge-active {
        background: rgba(var(--color-secondary-success-rgb), 0.9);
      }
    }
    
    .more-dropdown {
      position: absolute;
      top: 16px;
      right: 16px;
      
      .more-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 36px;
        height: 36px;
        background: rgba(0, 0, 0, 0.6);
        backdrop-filter: blur(10px);
        border-radius: 50%;
        border: 1px solid rgba(255, 255, 255, 0.2);
        color: #fff;
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover, &.is-open {
          background: rgba(var(--color-primary-rgb), 0.9);
          border-color: rgba(255, 255, 255, 0.4);
          transform: scale(1.1);
        }
        
        i {
          font-size: 18px;
        }
      }
    }
  }
  
  // 内容区域
  .card-body {
    padding: 20px;
    flex: 1;
    
    .user-name {
      font-size: 18px;
      font-weight: 700;
      margin: 0 0 8px 0;
      text-align: center;
      color: $color-text-primary;
    }
    
    .user-type-badge {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 13px;
      font-weight: 600;
      margin-bottom: 16px;
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.2);
      transition: all 0.3s ease;
      
      i {
        font-size: 14px;
      }
      
      &.badge-active {
        background: rgba(var(--color-secondary-success-rgb), 0.15);
        color: $color-secondary-success;
        border-color: rgba(var(--color-secondary-success-rgb), 0.3);
        box-shadow: 0 2px 8px rgba(var(--color-secondary-success-rgb), 0.2);
      }
      
      &.badge-normal {
        background: rgba(var(--color-primary-rgb), 0.15);
        color: $color-primary;
        border-color: rgba(var(--color-primary-rgb), 0.3);
        box-shadow: 0 2px 8px rgba(var(--color-primary-rgb), 0.2);
      }
      
      &.badge-banned {
        background: rgba(var(--color-secondary-danger-rgb), 0.15);
        color: $color-secondary-danger;
        border-color: rgba(var(--color-secondary-danger-rgb), 0.3);
        box-shadow: 0 2px 8px rgba(var(--color-secondary-danger-rgb), 0.2);
      }
    }
    
    .user-info {
      display: flex;
      flex-direction: column;
      gap: 10px;
      margin-bottom: 16px;
      padding: 16px;
      background: rgba(var(--color-primary-rgb), 0.03);
      border-radius: 12px;
      border: 1px solid rgba($color-border, 0.2);
      
      .info-item {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 13px;
        color: $color-text-secondary;
        
        i {
          font-size: 16px;
          color: $color-primary;
          width: 20px;
          flex-shrink: 0;
        }
        
        span {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        &.online-status {
          &.is-online {
            i {
              color: $color-secondary-success;
            }
            span {
              color: $color-secondary-success;
              font-weight: 600;
            }
          }
          
          &:not(.is-online) {
            i {
              color: $color-text-disabled;
            }
            span {
              color: $color-text-disabled;
            }
          }
        }
      }
    }
    
    .user-stats {
      display: flex;
      justify-content: space-around;
      padding-top: 16px;
      border-top: 1px solid rgba($color-border, 0.2);
      
      .stat-item {
        text-align: center;
        
        .stat-value {
          font-size: 20px;
          font-weight: 700;
          color: $color-primary;
          line-height: 1.2;
        }
        
        .stat-label {
          font-size: 12px;
          color: $color-text-secondary;
          margin-top: 4px;
        }
      }
    }
  }
  
  // 底部操作栏
  .card-footer {
    padding: 16px 20px;
    border-top: 1px solid rgba($color-border, 0.3);
    background: rgba($color-primary, 0.02);
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    
    .glass-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      padding: 10px 20px;
      border: none;
      border-radius: 10px;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      background: rgba(var(--color-primary-rgb), 0.1);
      border: 1px solid rgba(var(--color-primary-rgb), 0.3);
      color: $color-primary;
      
      i {
        font-size: 16px;
      }
      
      &:hover {
        background: rgba(var(--color-primary-rgb), 0.2);
        border-color: rgba(var(--color-primary-rgb), 0.5);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
  }
}

// 在线指示器脉搏动画
@keyframes pulse {
  0% {
    box-shadow: 0 2px 8px rgba(var(--color-secondary-success-rgb), 0.4),
                0 0 0 0 rgba(var(--color-secondary-success-rgb), 0.7);
  }
  70% {
    box-shadow: 0 2px 8px rgba(var(--color-secondary-success-rgb), 0.4),
                0 0 0 10px rgba(var(--color-secondary-success-rgb), 0);
  }
  100% {
    box-shadow: 0 2px 8px rgba(var(--color-secondary-success-rgb), 0.4),
                0 0 0 0 rgba(var(--color-secondary-success-rgb), 0);
  }
}

// 下拉菜单项样式
::v-deep {
  .text-danger {
    color: $color-secondary-danger !important;
    
    &:hover {
      background: rgba(var(--color-secondary-danger-rgb), 0.1) !important;
    }
  }
}
</style>