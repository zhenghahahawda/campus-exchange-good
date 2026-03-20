<template>
  <div class="user-settings-page">
    <PageHeader title="个人中心" subtitle="查看和管理您的个人信息" />

    <div class="settings-container">
      <div class="settings-card">
        <div class="card-header">
          <h3>个人信息</h3>
          <el-button type="primary" size="small" @click="handleSaveProfile">
            <i class="el-icon-check"></i> 保存修改
          </el-button>
        </div>
        <div class="card-body">
          <!-- 用户信息卡片 -->
          <UserInfoCard
            :avatar="profileForm.avatar"
            :username="profileForm.username"
            :uid="profileForm.uid"
            :phone="profileForm.phone"
            :email="profileForm.email"
            :goods-count="profileForm.goodsCount"
            :orders-count="profileForm.ordersCount"
            :upload-url="uploadUrl"
            @avatar-change="handleAvatarChange"
            class="user-info-section"
          />

          <!-- 表单区域 -->
          <el-form :model="profileForm" label-width="90px" class="profile-form">
            <div class="form-row">
              <el-form-item label="用户名" class="form-item-half">
                <el-input
                  v-model="profileForm.username"
                  placeholder="请输入用户名"
                  prefix-icon="el-icon-user"
                />
              </el-form-item>
              <el-form-item label="真实姓名" class="form-item-half">
                <el-input
                  v-model="profileForm.realName"
                  placeholder="请输入真实姓名"
                  prefix-icon="el-icon-user-solid"
                />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="学校" class="form-item-half">
                <el-input
                  v-model="profileForm.school"
                  placeholder="请输入学校名称"
                  prefix-icon="el-icon-school"
                />
              </el-form-item>
              <el-form-item label="个人地址" class="form-item-half">
                <el-input
                  v-model="profileForm.address"
                  placeholder="请输入个人地址"
                  prefix-icon="el-icon-location"
                />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="性别" class="form-item-half">
                <GenderSelector v-model="profileForm.gender" />
              </el-form-item>
              <el-form-item label="生日" class="form-item-half">
                <el-date-picker
                  v-model="profileForm.birthday"
                  type="date"
                  placeholder="选择生日"
                  format="yyyy-MM-dd"
                  value-format="yyyy-MM-dd"
                  :append-to-body="true"
                  popper-class="glass-date-picker"
                  style="width: 100%"
                />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="注册时间" class="form-item-half">
                <InfoDisplay
                  icon="el-icon-time"
                  :value="profileForm.registerTime"
                  placeholder="未知"
                />
              </el-form-item>
              <el-form-item label="上次登录" class="form-item-half">
                <InfoDisplay
                  icon="el-icon-timer"
                  :value="profileForm.lastLoginTime"
                  placeholder="未知"
                />
              </el-form-item>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import PageHeader from '@/components/common/PageHeader.vue'
import UserInfoCard from '@/components/setting/UserInfoCard.vue'
import GenderSelector from '@/components/setting/GenderSelector.vue'
import InfoDisplay from '@/components/setting/InfoDisplay.vue'
import { getUserInfo, setUserInfo } from '@/utils/auth'

export default {
  name: 'UserSettings',
  components: {
    PageHeader,
    UserInfoCard,
    GenderSelector,
    InfoDisplay
  },
  data() {
    return {
      uploadUrl: '/api/user/avatar',
      profileForm: {
        uid: '',
        avatar: '',
        username: '',
        email: '',
        phone: '',
        school: '',
        realName: '',
        address: '',
        gender: '',
        birthday: '',
        registerTime: '',
        lastLoginTime: '',
        goodsCount: 0,
        ordersCount: 0
      }
    }
  },
  mounted() {
    this.loadUserInfo()
  },
  methods: {
    // 从后端接口拉取用户信息，用 token 中的 userId 验证身份
    async loadUserInfo() {
      try {
        const res = await this.$axios.get('/api/user/info')
        // axios 拦截器已经返回了 res（即 response.data），所以 res.data 才是真正的数据
        const data = res.data
        if (data) {
          // 后端 gender 是 Integer（0=未知,1=男,2=女），前端需要转为 string
          const genderMap = { 1: 'male', 2: 'female', 0: 'other' }

          this.profileForm = {
            uid: data.userId || '',
            avatar: data.avatar || '',
            username: data.username || '',
            email: data.email || '',
            phone: data.phone || '',
            school: data.school || '',
            realName: data.realName || '',
            address: data.address || '',
            gender: genderMap[data.gender] || '',
            birthday: data.birthday || '',
            registerTime: data.registerTime || '',
            lastLoginTime: data.lastLoginTime || '',
            goodsCount: data.goodsCount || 0,
            ordersCount: data.ordersCount || 0
          }
        }
      } catch (e) {
        console.error('获取用户信息失败:', e)
        this.$root.$emit('show-island-message', '获取用户信息失败: ' + (e.message || '请检查网络连接'), 'danger')
      }
    },
    handleAvatarChange(url) {
      this.profileForm.avatar = url
      
      // 立即更新全局状态，使 Sidebar 等组件即时响应
      // 注意：这只是前端状态更新，用户仍需点击"保存修改"以持久化到服务器
      const cached = getUserInfo()
      if (cached) {
        const updated = { ...cached, avatar: url }
        setUserInfo(updated)
        this.$store.commit('SET_USER_INFO', updated)
      }
    },
    async handleSaveProfile() {
      if (!this.profileForm.username) {
        this.$root.$emit('show-island-message', '请输入用户名', 'warning')
        return
      }

      // 前端 gender string 转回后端 Integer
      const genderReverseMap = { male: 1, female: 2, other: 0 }

      const updateData = {
        username: this.profileForm.username,
        school: this.profileForm.school,
        realName: this.profileForm.realName,
        address: this.profileForm.address,
        gender: genderReverseMap[this.profileForm.gender] ?? null,
        birthday: this.profileForm.birthday,
        avatar: this.profileForm.avatar
      }

      try {
        await this.$axios.put('/api/user/info', updateData)
        // 同步头像到 Cookie 和 Vuex store，使 Sidebar 响应式更新
        const cached = getUserInfo()
        if (cached) {
          const updated = { ...cached, avatar: this.profileForm.avatar, username: this.profileForm.username }
          setUserInfo(updated)
          this.$store.commit('SET_USER_INFO', updated)
        }
        this.$root.$emit('show-island-message', '个人信息保存成功', 'success')
        await this.loadUserInfo()
      } catch (e) {
        this.$root.$emit('show-island-message', '保存失败: ' + (e.message || '未知错误'), 'danger')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.user-settings-page {
  padding-bottom: 40px;
}

.settings-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.settings-card {
  @include glass-card;
  padding: 0;
  overflow: hidden;

  .card-header {
    padding: 24px;
    border-bottom: 1px solid $color-border;
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      font-size: 18px;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin: 0;
    }
  }

  .card-body {
    padding: 32px;
  }
}

.user-info-section {
  margin-bottom: 32px;
}

.profile-form {
  .form-row {
    display: flex;
    gap: 24px;
    margin-bottom: 0;

    .form-item-half {
      flex: 1;
    }
  }
}

::v-deep .el-form-item {
  margin-bottom: 24px;

  .el-form-item__label {
    color: $color-text-secondary;
    font-weight: $font-weight-medium;
  }

  .el-form-item__content {
    line-height: normal;
  }
}

::v-deep .el-input__inner {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid $color-border;
  color: $color-text-primary;
  border-radius: 8px;
  transition: all 0.3s;

  &:focus {
    border-color: $color-primary;
    background: rgba(255, 255, 255, 0.08);
    box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
  }

  &::placeholder {
    color: $color-text-secondary;
  }
}

.birthday-picker {
  width: 100%;
}

::v-deep .el-date-editor {
  width: 100% !important;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;

  &.el-input {
    width: 100% !important;
    display: block !important;
  }

  .el-input__inner {
    cursor: pointer !important;
    pointer-events: auto !important;
    background: rgba(255, 255, 255, 0.05) !important;
    border: 1px solid $color-border !important;
    color: $color-text-primary !important;
    height: 40px !important;
    line-height: 40px !important;
    display: block !important;
    visibility: visible !important;
  }

  .el-input__prefix {
    color: $color-text-secondary;
    pointer-events: none;
    display: flex !important;
    align-items: center;
  }

  .el-input__icon {
    cursor: pointer !important;
    color: $color-text-secondary !important;
    line-height: 40px !important;
  }
}

::v-deep .el-picker-panel {
  background: var(--card-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  border-radius: 12px;

  .el-date-picker__header {
    color: $color-text-primary;
    border-bottom: 1px solid var(--glass-border);
    padding: 12px;

    .el-date-picker__header-label {
      color: $color-text-primary;
      font-weight: $font-weight-semibold;

      &:hover {
        color: $color-primary;
      }
    }
  }

  .el-picker-panel__icon-btn {
    color: $color-text-primary;
    width: 32px;
    height: 32px;
    border-radius: 8px;
    transition: all 0.3s;

    &:hover {
      color: $color-primary;
      background: rgba(102, 126, 234, 0.1);
    }
  }

  .el-date-table {
    th {
      color: $color-text-secondary;
      font-weight: $font-weight-medium;
      border-bottom: 1px solid var(--glass-border);
      padding: 8px 0;
    }

    td {
      color: $color-text-primary;
      padding: 4px 0;

      &.available {
        &:hover {
          span {
            background: rgba(102, 126, 234, 0.1);
            color: $color-primary;
            border-radius: 8px;
          }
        }
      }

      &.today span {
        color: $color-primary;
        font-weight: $font-weight-bold;
        position: relative;

        &::after {
          content: '';
          position: absolute;
          bottom: 2px;
          left: 50%;
          transform: translateX(-50%);
          width: 4px;
          height: 4px;
          background: $color-primary;
          border-radius: 50%;
        }
      }

      &.current:not(.disabled) span {
        background: $color-primary;
        color: white;
        border-radius: 8px;
        font-weight: $font-weight-semibold;
      }

      &.disabled {
        span {
          color: $color-text-disabled;
        }
      }

      span {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto;
        transition: all 0.3s;
      }
    }
  }

  .el-year-table,
  .el-month-table {
    td {
      padding: 8px;

      .cell {
        color: $color-text-primary;
        padding: 12px 8px;
        border-radius: 8px;
        transition: all 0.3s;

        &:hover {
          background: rgba(102, 126, 234, 0.1);
          color: $color-primary;
        }
      }

      &.current:not(.disabled) .cell {
        background: $color-primary;
        color: white;
        font-weight: $font-weight-semibold;
      }

      &.disabled .cell {
        color: $color-text-disabled;
      }
    }
  }

  .el-picker-panel__footer {
    border-top: 1px solid var(--glass-border);
    padding: 8px 12px;
    background: rgba(0, 0, 0, 0.1);

    .el-button {
      &.el-picker-panel__link-btn {
        color: $color-primary;
        font-weight: $font-weight-medium;

        &:hover {
          filter: brightness(1.2);
        }
      }
    }
  }
}

::v-deep .el-button--primary {
  display: flex;
  align-items: center;
  gap: 6px;
}

@media (max-width: 768px) {
  .profile-form {
    .form-row {
      flex-direction: column;
      gap: 0;
    }
  }
}
</style>
