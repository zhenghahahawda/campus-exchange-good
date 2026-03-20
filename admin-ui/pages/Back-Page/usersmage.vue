<template>
  <div class="users-page">
    <!-- 页面头部 -->
    <div class="page-header-wrapper">
      <PageHeader title="用户管理" description="管理平台用户信息与权限" />
      <button class="glass-btn btn-add-user" @click="handleAddUser">
        <i class="el-icon-plus"></i>
        <span>新增用户</span>
      </button>
    </div>

    <!-- 统计卡片 -->
    <div class="row section-container">
      <div class="col-xl-3 col-md-6" v-for="(card, index) in statCardsData" :key="index">
        <StatCard v-bind="card" @click.native="filterStatus = card.status" 
          :class="{ 'active-stat-card': filterStatus === card.status }" />
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-bar">
      <div class="search-wrapper">
        <el-input
          placeholder="搜索用户名/ID/地址/学校/手机号..."
          v-model="searchQuery"
          prefix-icon="el-icon-search"
          clearable
          class="capsule-input"
        />
      </div>
      
      <div class="sort-wrapper">
        <GlassDropdown @command="handleSortCommand">
          <template slot="trigger" slot-scope="{ isOpen }">
            <div class="filter-dropdown">
              <span>{{ getSortLabel(sortBy) }}</span>
              <i class="el-icon-arrow-down" :class="{ 'rotate-180': isOpen }" />
            </div>
          </template>
          <GlassDropdownItem 
            v-for="(label, key) in USER_SORT_LABELS" 
            :key="key"
            :command="key"
          >
            {{ label }}
          </GlassDropdownItem>
        </GlassDropdown>
      </div>
    </div>

    <!-- 用户列表 -->
    <div v-if="paginatedUsers.length > 0" class="users-grid">
      <transition-group name="list-fade" tag="div" class="users-grid-inner">
        <UserCard
          v-for="user in paginatedUsers"
          :key="user.userId || user.id"
          :user="user"
          @toggle-status="handleToggleStatus"
          @edit="handleEdit"
          @command="handleCommand"
        />
      </transition-group>
    </div>

    <!-- 空状态 -->
    <EmptyState 
      v-else 
      icon="el-icon-user" 
      message="暂无符合条件的用户" 
      action-text="清除筛选条件" 
      :show-action="true"
      @action="handleClearFilters" 
    />

    <!-- 分页 -->
    <div v-if="filteredUsers.length > 0" class="pagination-wrapper">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredUsers.length"
        :page-sizes="[8, 16, 24, 32]"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        class="glass-pagination"
      />
    </div>

    <!-- 编辑用户弹窗 -->
    <UserFormDialog
      :visible="dialogVisible"
      :is-edit-mode="isEditMode"
      :form-data="currentForm"
      @close="closeDialog"
      @save="handleSave"
    />
  </div>
</template>

<script>
import PageHeader from '@/components/common/PageHeader.vue';
import EmptyState from '@/components/ui/EmptyState.vue';
import GlassDropdown from '@/components/ui/GlassDropdown.vue';
import GlassDropdownItem from '@/components/ui/GlassDropdownItem.vue';
import StatCard from '@/components/dashboard/StatCard.vue';
import UserCard from '@/components/users/UserCard.vue';
import UserFormDialog from '@/components/users/UserFormDialog.vue';

import { 
  UserFilter, 
  UserSorter, 
  UserStats, 
  UserOperations, 
  UserValidator,
  UserDisplay 
} from '@/utils/userManager';
import { MessageHelper, ConfirmHelper } from '@/utils/messageHelper';
import { 
  USER_STATUS_LABELS, 
  USER_SORT_LABELS,
  USER_STATUS,
  USER_SORT_OPTIONS,
  PAGINATION_CONFIG,
  DEFAULT_AVATAR,
  USER_TYPES
} from '@/utils/userConstants';
import { getMockUsers } from '@/mock/userData';

export default {
  name: 'UsersManagePage',
  middleware: 'admin-only', // 仅管理员可访问
  components: {
    PageHeader,
    EmptyState,
    GlassDropdown,
    GlassDropdownItem,
    StatCard,
    UserCard,
    UserFormDialog
  },
  
  data() {
    return {
      // 响应式数据
      usersList: getMockUsers(),
      searchQuery: '',
      filterStatus: USER_STATUS.ALL,
      sortBy: USER_SORT_OPTIONS.DEFAULT,
      currentPage: PAGINATION_CONFIG.DEFAULT_PAGE,
      pageSize: PAGINATION_CONFIG.PAGE_SIZE,
      
      // 表单相关
      dialogVisible: false,
      isEditMode: false,
      currentForm: {
        id: null,
        name: '',
        email: '',
        phone: '',
        role: 'user',
        avatar: DEFAULT_AVATAR,
        userId: '',
        accountAddress: '',
        school: '',
        userType: USER_TYPES.NORMAL,
        reputation: 100,
        lockUntil: '',
        isAdmin: false // 默认不是管理员
      },
      usersList: [], // 从API获取数据

      // 常量
      USER_STATUS_LABELS,
      USER_SORT_LABELS,
      USER_STATUS
    };
  },

  computed: {
    statCardsData() {
      return UserStats.generateStatCards(this.usersList);
    },

    filteredUsers() {
      // 先过滤掉管理员（userType === 1），然后再应用其他筛选条件
      const nonAdminUsers = this.usersList.filter(user => user.userType !== 1);
      return UserFilter.filter(nonAdminUsers, this.filterStatus, this.searchQuery);
    },

    sortedUsers() {
      return UserSorter.sort(this.filteredUsers, this.sortBy);
    },

    paginatedUsers() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.sortedUsers.slice(start, end);
    }
  },

  watch: {
    searchQuery() {
      this.currentPage = PAGINATION_CONFIG.DEFAULT_PAGE;
    },
    filterStatus() {
      this.currentPage = PAGINATION_CONFIG.DEFAULT_PAGE;
    },
    sortBy() {
      this.currentPage = PAGINATION_CONFIG.DEFAULT_PAGE;
    }
  },

  created() {
    // 从API获取用户数据
    this.fetchUsers();
  },

  methods: {
    // 获取用户列表
    async fetchUsers() {
      try {
        const response = await this.$axios.get('/api/user/list');
        // 兼容多种成功状态码：20000、200、0
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          // 处理后端返回的数据，保持原始 status 值
          this.usersList = (response.data || []).map(user => {
            // 后端 status 字段：0=封禁，1=普通用户，2=活跃用户
            // 保持原始数字值，用于统计和显示
            return {
              ...user,
              // 映射后端字段
              realName: user.realName || user.real_name || '',
              // 映射后端字段 avatarUrl 到前端使用的 avatar
              avatar: user.avatarUrl || user.avatar || user.headImg || '',
              // 映射后端 address 字段到前端使用的 accountAddress
              accountAddress: user.address || user.accountAddress || '未设置地址',
              reputation: user.reputation || 100,
              lastLoginAt: user.lastLoginAt,
              lastLoginTime: user.lastLoginAt, // 兼容排序逻辑
              status: user.status,
              isActive: user.status === 1 || user.status === 2,
              goodsCount: user.goodsCount || 0,
              ordersCount: user.orderCount || user.ordersCount || 0
            };
          });
        } else {
          console.error('获取用户数据失败:', response.message);
          this.$root.$emit('show-island-message', response.message || '获取用户数据失败', 'danger');
        }
      } catch (error) {
        console.error('获取用户数据失败:', error);
      }
    },

    // 工具方法
    getSortLabel(sortOption) {
      return USER_SORT_LABELS[sortOption] || USER_SORT_LABELS[USER_SORT_OPTIONS.DEFAULT];
    },

    // 用户状态相关方法
    getUserStatusLabel(status) {
      return USER_STATUS_LABELS[status] || status;
    },
    
    getUserStatusClass(status) {
      return status === USER_STATUS.ACTIVE ? 'badge-success' : 'badge-danger';
    },
    
    // 用户类型相关方法（使用工具类）
    getUserTypeLabel(user) {
      return UserDisplay.getUserTypeLabel(user);
    },
    
    getUserTypeIcon(user) {
      return UserDisplay.getUserTypeIcon(user);
    },
    
    getUserTypeBadgeClass(user) {
      return UserDisplay.getUserTypeBadgeClass(user);
    },
    
    getOnlineStatusText(user) {
      return UserDisplay.getOnlineStatusText(user);
    },

    // 事件处理方法
    handleSortCommand(command) {
      this.sortBy = command;
    },

    handlePageChange(page) {
      this.currentPage = page;
      // 滚动到页面顶部
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
    },

    handleClearFilters() {
      this.searchQuery = '';
      this.filterStatus = USER_STATUS.ALL;
      this.sortBy = USER_SORT_OPTIONS.DEFAULT;
      this.currentPage = PAGINATION_CONFIG.DEFAULT_PAGE;
    },

    handleAddUser() {
      this.isEditMode = false;
      this.currentForm = {
        id: null,
        name: '',
        password: '', // 新增时由用户填写
        email: '',
        phone: '',
        role: 'user',
        avatar: DEFAULT_AVATAR,
        accountAddress: '',
        school: '',
        userType: USER_TYPES.NORMAL,
        isAdmin: false // 默认不是管理员
      };
      this.dialogVisible = true;
    },
    
    handleEdit(user) {
      this.isEditMode = true;
      this.currentForm = {
        id: user.id,
        name: user.username || user.name, // 兼容 username 和 name 字段
        email: user.email,
        phone: user.phone || '',
        role: user.role,
        avatar: user.avatar,
        userId: user.userId,
        accountAddress: user.address || user.accountAddress, // 兼容 address 和 accountAddress 字段
        school: user.school,
        reputation: user.reputation,
        lockUntil: user.lockUntil,
        userType: user.status === 2 ? USER_TYPES.ACTIVE : USER_TYPES.NORMAL,
        goodsCount: user.goodsCount || 0,
        ordersCount: user.ordersCount || 0
      };
      this.dialogVisible = true;
    },
    
    async handleSave(formData) {
      // 验证表单
      const validation = UserValidator.validateUserForm(formData, this.isEditMode);
      if (!validation.isValid) {
        // 显示详细的验证错误
        validation.errors.forEach(error => {
          this.$root.$emit('show-island-message', error, 'warning');
        });
        return;
      }

      try {
        if (this.isEditMode) {
          // 编辑模式：使用管理员接口提交到后端（只提交修改的字段）
          const updateData = {};

          // 只添加有值的字段
          if (formData.name && formData.name.trim()) {
            updateData.username = formData.name;
          }
          if (formData.email && formData.email.trim()) {
            updateData.email = formData.email;
          }
          if (formData.phone && formData.phone.trim()) {
            updateData.phone = formData.phone;
          }
          if (formData.accountAddress && formData.accountAddress.trim()) {
            updateData.address = formData.accountAddress;
          }
          if (formData.school && formData.school.trim()) {
            updateData.school = formData.school;
          }
          if (formData.avatar) {
            updateData.avatar = formData.avatar;
          }
          // 添加信誉分和封禁时间
          if (formData.reputation !== undefined && formData.reputation !== null) {
            updateData.reputation = formData.reputation;
          }
          if (formData.lockUntil) {
            updateData.lockUntil = formData.lockUntil;
          }
          // status 字段：根据 userType 设置（1=正常, 2=活跃用户）
          updateData.status = formData.userType === USER_TYPES.ACTIVE ? 2 : 1;

          const response = await this.$axios.put(`/api/user/admin/${formData.userId}`, updateData);
          if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
            MessageHelper.success(this, '更新成功');
            // 重新获取用户列表
            await this.fetchUsers();
            this.closeDialog();
          } else {
            MessageHelper.error(this, response.message || '更新失败');
          }
        } else {
          // 新增模式：使用管理员创建用户接口
          const createData = {
            username: formData.name,
            password: formData.password, // 使用表单中的密码
            email: formData.email,
            phone: formData.phone,
            userType: formData.isAdmin ? 1 : 2, // 1=管理员, 2=普通用户
            status: formData.userType === USER_TYPES.ACTIVE ? 2 : 1
          };

          // 添加可选字段
          if (formData.accountAddress && formData.accountAddress.trim()) {
            createData.address = formData.accountAddress;
          }
          if (formData.school && formData.school.trim()) {
            createData.school = formData.school;
          }
          if (formData.avatar) {
            createData.avatar = formData.avatar;
          }

          const response = await this.$axios.post('/api/user/admin/create', createData);
          if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
            MessageHelper.success(this, '新增用户成功');
            // 重新获取用户列表
            await this.fetchUsers();
            this.closeDialog();
          } else {
            MessageHelper.error(this, response.message || '新增失败');
          }
        }
      } catch (error) {
        // 获取详细的错误信息
        let errorMessage = '操作失败';
        if (error.response && error.response.data) {
          errorMessage = error.response.data.message || error.response.data.msg || errorMessage;
        } else if (error.message) {
          errorMessage = error.message;
        }
        MessageHelper.error(this, errorMessage);
        console.error('Save user error:', error);
      }
    },
    
    async handleToggleStatus(user) {
      try {
        // 准备提交的数据
        // user.isActive 已经是切换后的状态
        // true(开) -> status=1 (正常)
        // false(关) -> status=0 (封禁)
        const updateData = {
          status: user.isActive ? 1 : 0
        };

        // 提交到后端
        // 使用管理员更新用户信息的通用接口，替代可能的 /api/user/status
        const response = await this.$axios.put(`/api/user/admin/${user.userId}`, updateData);
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          const message = UserOperations.toggleUserStatus(user);
          const messageType = user.isActive ? 'success' : 'warning';
          MessageHelper[messageType](this, message);

          // 重新获取用户列表
          await this.fetchUsers();
        } else {
          // 如果后端失败，恢复开关状态
          user.isActive = !user.isActive;
          MessageHelper.error(this, response.message || '状态更新失败');
        }
      } catch (error) {
        // 如果请求失败，恢复开关状态
        user.isActive = !user.isActive;
        MessageHelper.error(this, '状态更新失败，请重试');
        console.error('Toggle status error:', error);
      }
    },
    
    async handleCommand(command, user) {
      try {
        if (command === 'reset-password') {
          const userName = user.name || user.username || '该用户';
          await ConfirmHelper.confirmResetPassword(this, userName);
          
          const response = await this.$axios.post(`/api/user/admin/${user.userId}/reset-password`);
          
          if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
            MessageHelper.success(this, `已重置 ${userName} 的密码为 123456`);
          } else {
            MessageHelper.error(this, response.message || '重置密码失败');
          }
        } else if (command === 'delete') {
          const userName = user.name || user.username || '该用户';
          await ConfirmHelper.confirmDelete(this, `用户 ${userName}`);
          
          const response = await this.$axios.delete(`/api/user/admin/${user.userId}`);
          
          if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
            MessageHelper.success(this, '删除成功');
            // 重新获取用户列表
            await this.fetchUsers();
          } else {
            MessageHelper.error(this, response.message || '删除失败');
          }
        }
      } catch (error) {
        // 忽略用户取消操作
        if (error === 'cancel') return;
        
        console.error('Operation error:', error);
        let errorMessage = '操作失败';
        if (error.response && error.response.data) {
          errorMessage = error.response.data.message || error.response.data.msg || errorMessage;
        } else if (error.message) {
          errorMessage = error.message;
        }
        MessageHelper.error(this, errorMessage);
      }
    },

    // 表单相关方法
    closeDialog() {
      this.dialogVisible = false;
      this.currentForm = {
        id: null,
        name: '',
        password: '',
        email: '',
        phone: '',
        role: 'user',
        avatar: DEFAULT_AVATAR,
        userId: '',
        accountAddress: '',
        school: '',
        userType: USER_TYPES.NORMAL,
        isAdmin: false
      };
    },

    generateUserId() {
      return UserOperations.generateUserId(this.usersList);
    }
  }
};
</script>
<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.users-page {
  padding: 10px 20px;
  min-height: 100vh;
  color: $color-text-primary;
}

// 页面头部包装器
.page-header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
  
  .glass-btn.btn-add-user {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px 24px;
    border: none;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    outline: none;
    backdrop-filter: blur(10px);
    background: linear-gradient(135deg, 
      rgba(var(--color-primary-rgb), 0.95), 
      rgba(var(--color-primary-rgb), 0.85));
    border: 1px solid rgba(var(--color-primary-rgb), 0.3);
    color: #fff;
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
    
    i {
      font-size: 16px;
      font-weight: bold;
    }
    
    &:hover {
      background: linear-gradient(135deg, 
        rgba(var(--color-primary-rgb), 1), 
        rgba(var(--color-primary-rgb), 0.9));
      border-color: rgba(var(--color-primary-rgb), 0.5);
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(var(--color-primary-rgb), 0.4);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

// 优化渲染性能
.section-container {
  transform: translateZ(0);
  margin-bottom: 32px;
}

// Simple grid system
.row {
  display: flex;
  flex-wrap: wrap;
  margin-right: -10px;
  margin-left: -10px;
}

.col-xl-3, .col-md-6 {
  position: relative;
  width: 100%;
  padding-right: 10px;
  padding-left: 10px;
}

@media (min-width: 768px) {
  .col-md-6 {
    flex: 0 0 50%;
    max-width: 50%;
  }
}

@media (min-width: 1200px) {
  .col-xl-3 {
    flex: 0 0 25%;
    max-width: 25%;
  }
}

// 活跃统计卡片样式
::v-deep .active-stat-card {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(var(--color-primary-rgb), 0.15) !important;
  border: 2px solid rgba(var(--color-primary-rgb), 0.3);
}

// 筛选工具栏
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px;
  margin-bottom: 32px;
  
  .search-wrapper {
    flex: 1;
    max-width: 400px;
  }
  
  .sort-wrapper {
    .filter-dropdown {
      background: transparent;
      border: none;
      box-shadow: none;
      
      @include glass-effect;
      padding: 10px 20px;
      border-radius: 50px;
      cursor: pointer;
      font-size: 14px;
      font-weight: 500;
      color: $color-text-secondary;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: all 0.3s ease;
      
      &:hover {
        color: $color-primary;
        background: var(--color-bg-surface);
      }
      
      i {
        font-size: 12px;
        transition: transform 0.3s;
        
        &.rotate-180 {
          transform: rotate(180deg);
        }
      }
    }
  }
}

// 用户网格
.users-grid-inner {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;

  @media (max-width: 1600px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

// 动画
.list-fade-enter-active, .list-fade-leave-active {
  transition: all 0.5s;
}
.list-fade-enter, .list-fade-leave-to {
  opacity: 0;
  transform: translateY(30px);
}
.list-fade-move {
  transition: transform 0.5s;
}

// 输入框样式
::v-deep {
  .capsule-input {
    .el-input__inner {
      border-radius: 50px;
      background: var(--color-pill-bg);
      border: 1px solid rgba(255, 255, 255, 0.15);
      height: 46px;
      line-height: 46px;
      padding-left: 44px;
      padding-right: 40px;
      color: $color-text-primary;
      transition: all 0.3s ease;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
      
      &:focus {
        background: var(--color-bg-surface);
        border-color: $color-primary;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      }
      
      &::placeholder {
        color: $color-text-secondary;
        opacity: 0.8;
      }
    }
    
    .el-input__prefix {
      left: 14px;
      display: flex;
      align-items: center;
      
      .el-input__icon {
        font-size: 18px;
        line-height: 46px;
        color: $color-text-primary;
        opacity: 0.8;
      }
    }
    
    .el-input__suffix {
      right: 12px;
      display: flex;
      align-items: center;
      
      .el-input__icon {
        font-size: 16px;
        line-height: 46px;
        color: $color-text-disabled;
        cursor: pointer;
        
        &:hover {
          color: $color-text-primary;
        }
      }
    }
  }
}

// 分页样式
@import '@/assets/css/components/pagination.scss';
</style>