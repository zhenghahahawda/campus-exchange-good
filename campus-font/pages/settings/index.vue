<template>
  <div class="settings-page">
    <div class="page-header">
      <el-button icon="el-icon-arrow-left" circle plain @click="$router.back()" class="back-btn" />
      <h1>设置</h1>
    </div>

    <!-- 账号设置 -->
    <div class="settings-group">
      <div class="group-title">账号</div>

      <div class="settings-item" @click="showEditUsername = true">
        <div class="item-left">
          <i class="el-icon-user"></i>
          <div>
            <div class="item-title">用户名</div>
            <div class="item-value">{{ userInfo.username || '—' }}</div>
          </div>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>

      <div class="settings-item" @click="showEditEmail = true">
        <div class="item-left">
          <i class="el-icon-message"></i>
          <div>
            <div class="item-title">邮箱</div>
            <div class="item-value">{{ userInfo.email || '未绑定' }}</div>
          </div>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>

      <div class="settings-item" @click="showEditPassword = true">
        <div class="item-left">
          <i class="el-icon-lock"></i>
          <div>
            <div class="item-title">修改密码</div>
            <div class="item-value">点击修改</div>
          </div>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>
    </div>

    <!-- 通用设置 -->
    <div class="settings-group">
      <div class="group-title">通用</div>

      <div class="settings-item" @click="$router.push('/theme-selector')">
        <div class="item-left">
          <i class="el-icon-brush"></i>
          <div>
            <div class="item-title">主题</div>
            <div class="item-value">切换界面主题</div>
          </div>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>
    </div>

    <!-- 关于 -->
    <div class="settings-group">
      <div class="group-title">关于</div>
      <div class="settings-item no-arrow">
        <div class="item-left">
          <i class="el-icon-info"></i>
          <div>
            <div class="item-title">版本</div>
            <div class="item-value">1.0.0</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section">
      <button class="logout-btn" @click="handleLogout">
        <i class="el-icon-switch-button"></i> 退出登录
      </button>
    </div>

    <!-- 修改用户名 Dialog -->
    <el-dialog title="修改用户名" :visible.sync="showEditUsername" width="400px" custom-class="glass-dialog" :modal="false" :close-on-click-modal="false" top="5vh">
      <el-input v-model="editForm.username" placeholder="请输入新用户名" maxlength="20" show-word-limit />
      <span slot="footer">
        <button class="glass-btn btn-cancel" @click="showEditUsername = false"><i class="el-icon-close"></i> 取消</button>
        <button class="glass-btn btn-save" :disabled="saving" @click="saveUsername"><i class="el-icon-check"></i> 保存</button>
      </span>
    </el-dialog>

    <!-- 修改邮箱 Dialog -->
    <el-dialog title="修改邮箱" :visible.sync="showEditEmail" width="400px" custom-class="glass-dialog" :modal="false" :close-on-click-modal="false" top="5vh">
      <el-input v-model="editForm.email" placeholder="请输入新邮箱" type="email" />
      <span slot="footer">
        <button class="glass-btn btn-cancel" @click="showEditEmail = false"><i class="el-icon-close"></i> 取消</button>
        <button class="glass-btn btn-save" :disabled="saving" @click="saveEmail"><i class="el-icon-check"></i> 保存</button>
      </span>
    </el-dialog>

    <!-- 修改密码 Dialog -->
    <el-dialog title="修改密码" :visible.sync="showEditPassword" width="400px" custom-class="glass-dialog" :modal="false" :close-on-click-modal="false" top="5vh">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <button class="glass-btn btn-cancel" @click="showEditPassword = false"><i class="el-icon-close"></i> 取消</button>
        <button class="glass-btn btn-save" :disabled="saving" @click="savePassword"><i class="el-icon-check"></i> 保存</button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { clearAuth } from '@/utils/auth'

export default {
  name: 'SettingsPage',
  data() {
    return {
      userInfo: {},
      showEditUsername: false,
      showEditEmail: false,
      showEditPassword: false,
      saving: false,
      editForm: { username: '', email: '' },
      pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' }
    }
  },
  mounted() {
    try {
      this.userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      this.editForm.username = this.userInfo.username || ''
      this.editForm.email = this.userInfo.email || ''
    } catch (e) {}
  },
  methods: {
    async saveUsername() {
      if (!this.editForm.username.trim()) return this.$message.warning('用户名不能为空')
      this.saving = true
      try {
        const res = await this.$axios.put('/user/info', { username: this.editForm.username })
        if (res.code === 20000) {
          this.userInfo = { ...this.userInfo, username: this.editForm.username }
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          this.$message.success('用户名已更新')
          this.showEditUsername = false
        } else {
          this.$message.error(res.message || '更新失败')
        }
      } catch (e) {
        this.$message.error('更新失败')
      } finally {
        this.saving = false
      }
    },
    async saveEmail() {
      if (!this.editForm.email.trim()) return this.$message.warning('邮箱不能为空')
      this.saving = true
      try {
        const res = await this.$axios.put('/user/info', { email: this.editForm.email })
        if (res.code === 20000) {
          this.userInfo = { ...this.userInfo, email: this.editForm.email }
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          this.$message.success('邮箱已更新')
          this.showEditEmail = false
        } else {
          this.$message.error(res.message || '更新失败')
        }
      } catch (e) {
        this.$message.error('更新失败')
      } finally {
        this.saving = false
      }
    },
    async savePassword() {
      const { oldPassword, newPassword, confirmPassword } = this.pwdForm
      if (!oldPassword || !newPassword) return this.$message.warning('请填写完整')
      if (newPassword.length < 6) return this.$message.warning('新密码至少6位')
      if (newPassword !== confirmPassword) return this.$message.warning('两次密码不一致')
      this.saving = true
      try {
        const res = await this.$axios.put('/user/password', { oldPassword, newPassword })
        if (res.code === 20000) {
          this.$message.success('密码已更新，请重新登录')
          this.showEditPassword = false
          this.pwdForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
          setTimeout(() => {
            clearAuth()
            this.$router.push('/login-page/login')
          }, 1500)
        } else {
          this.$message.error(res.message || '修改失败，请检查原密码')
        }
      } catch (e) {
        this.$message.error('修改失败')
      } finally {
        this.saving = false
      }
    },
    async handleLogout() {
      try {
        await this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
        })
      } catch {
        return // 点取消或 X，直接返回不做任何操作
      }
      try { await this.$axios.post('/auth/logout') } catch (e) {}
      clearAuth()
      this.$message.success('已退出登录')
      this.$router.push('/login-page/login')
    }
  },
  head() { return { title: '设置' } }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.settings-page {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px 20px 60px;
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;

  .back-btn {
    flex-shrink: 0;
    background: var(--glass-bg) !important;
    backdrop-filter: blur(var(--glass-blur, 20px));
    -webkit-backdrop-filter: blur(var(--glass-blur, 20px));
    border: 1px solid var(--glass-border) !important;
    color: var(--color-text-primary) !important;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    i { color: var(--color-text-primary) !important; }

    &:hover {
      background: rgba(var(--color-primary-rgb), 0.15) !important;
      border-color: var(--color-primary) !important;
      color: var(--color-primary) !important;
      i { color: var(--color-primary) !important; }
    }
  }

  h1 {
    font-size: 24px;
    font-weight: 700;
    color: $color-text-primary;
    margin: 0;
  }
}

.settings-group {
  margin-bottom: 24px;

  .group-title {
    font-size: 12px;
    font-weight: 600;
    color: $color-text-secondary;
    text-transform: uppercase;
    letter-spacing: 0.8px;
    padding: 0 4px;
    margin-bottom: 8px;
  }
}

.settings-item {
  @include glass-card;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: transform 0.15s;

  &:hover { transform: translateX(2px); }
  &.no-arrow { cursor: default; &:hover { transform: none; } }

  .item-left {
    display: flex;
    align-items: center;
    gap: 14px;

    > i {
      font-size: 20px;
      color: var(--color-primary);
      width: 24px;
      text-align: center;
    }

    .item-title {
      font-size: 15px;
      font-weight: 500;
      color: $color-text-primary;
    }
    .item-value {
      font-size: 12px;
      color: $color-text-secondary;
      margin-top: 2px;
    }
  }

  > .el-icon-arrow-right {
    color: $color-text-disabled;
    font-size: 15px;
  }
}

.logout-section {
  margin-top: 32px;
  text-align: center;

  .logout-btn {
    width: 100%;
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

<style>
::v-deep .glass-dialog {
  background: var(--glass-bg) !important;
  backdrop-filter: blur(var(--glass-blur, 20px));
  -webkit-backdrop-filter: blur(var(--glass-blur, 20px));
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  border-radius: 20px !important;
}
::v-deep .glass-dialog .el-dialog__header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--glass-border);
}
::v-deep .glass-dialog .el-dialog__title {
  color: var(--color-text-primary);
  font-size: 18px;
  font-weight: 600;
}
::v-deep .glass-dialog .el-dialog__headerbtn .el-dialog__close {
  color: var(--color-text-secondary);
  &:hover { color: var(--color-primary); }
}
::v-deep .glass-dialog .el-dialog__body {
  padding: 24px;
  background: transparent;
  color: var(--color-text-primary);
}
::v-deep .glass-dialog .el-dialog__footer {
  padding: 16px 24px 20px;
  border-top: 1px solid var(--glass-border);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
::v-deep .glass-dialog .el-input__inner {
  height: 48px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--glass-border);
  color: var(--color-text-primary);
  &:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
  }
}
::v-deep .glass-dialog .el-input__count { background: transparent; color: var(--color-text-disabled); }
::v-deep .glass-dialog .el-form-item__label { color: var(--color-text-secondary); }

.glass-btn {
  padding: 10px 24px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  gap: 6px;

  &.btn-cancel {
    background: rgba(var(--color-text-secondary, 203, 213, 225), 0.1);
    border: 1px solid var(--glass-border);
    color: var(--color-text-secondary);
    &:hover { background: rgba(var(--color-text-secondary, 203, 213, 225), 0.2); }
  }

  &.btn-save {
    background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.95), rgba(var(--color-primary-rgb), 0.8));
    color: #fff;
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
    &:hover { box-shadow: 0 6px 20px rgba(var(--color-primary-rgb), 0.45); transform: translateY(-1px); }
    &:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
  }
}
</style>
