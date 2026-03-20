<template>
  <el-dialog
    :title="isEditMode ? '编辑用户信息' : '新增用户'"
    :visible="visible"
    @update:visible="handleClose"
    width="650px"
    custom-class="glass-dialog user-dialog"
    top="5vh"
    :modal="true"
    :close-on-click-modal="false"
    :append-to-body="true"
  >
    <el-form
      ref="userForm"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      label-position="left"
      class="glass-form user-form"
    >
      <!-- 用户ID（仅编辑模式显示） -->
      <el-form-item v-if="isEditMode" label="用户ID">
        <el-input
          v-model="formData.userId"
          disabled
        >
          <i slot="prefix" class="el-icon-postcard" />
        </el-input>
      </el-form-item>

      <!-- 头像上传（仅编辑模式显示，新增用户无法上传头像） -->
      <el-form-item v-if="isEditMode" label="用户头像">
        <div class="avatar-upload-container">
          <div class="avatar-preview">
            <img :src="formData.avatar || defaultAvatar" alt="用户头像" />
          </div>
          <div class="avatar-actions">
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="handleBeforeUpload"
              :http-request="handleAvatarUpload"
              accept="image/*"
            >
              <button type="button" class="glass-btn btn-upload">
                <i class="el-icon-upload2" />
                <span>上传头像</span>
              </button>
            </el-upload>
            <button
              v-if="formData.avatar && formData.avatar !== defaultAvatar"
              type="button"
              class="glass-btn btn-reset"
              @click="handleResetAvatar"
            >
              <i class="el-icon-refresh-left" />
              <span>重置</span>
            </button>
          </div>
          <div class="avatar-tips">
            <i class="el-icon-info" />
            <span>支持 JPG、PNG 格式，文件大小不超过 2MB</span>
          </div>
        </div>
      </el-form-item>

      <!-- 用户名 -->
      <el-form-item label="用户名" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入用户名"
          :maxlength="NAME_MAX_LENGTH"
          show-word-limit
        >
          <i slot="prefix" class="el-icon-user" />
        </el-input>
      </el-form-item>

      <!-- 密码（仅新增模式显示） -->
      <el-form-item v-if="!isEditMode" label="密码" prop="password">
        <el-input
          v-model="formData.password"
          type="password"
          placeholder="请输入密码（至少6位）"
          :maxlength="50"
          show-password
        >
          <i slot="prefix" class="el-icon-lock" />
        </el-input>
      </el-form-item>

      <!-- 邮箱和手机 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="formData.email"
              type="email"
              placeholder="请输入邮箱账户"
            >
              <i slot="prefix" class="el-icon-message" />
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="formData.phone"
              placeholder="请输入电话号码"
            >
              <i slot="prefix" class="el-icon-phone" />
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 账号地址和学校 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="账号地址">
            <el-input
              v-model="formData.accountAddress"
              placeholder="请输入省市地址"
            >
              <i slot="prefix" class="el-icon-location" />
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学校">
            <el-input
              v-model="formData.school"
              placeholder="请输入学校名称"
            >
              <i slot="prefix" class="el-icon-school" />
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 信誉分和封禁日期 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="信誉分">
            <el-input 
              v-model.number="formData.reputation" 
              type="number"
              placeholder="请输入信誉分(0-100)"
              :min="0"
              :max="100"
              @blur="handleReputationBlur"
              class="no-spin-button"
            >
              <i slot="prefix" class="el-icon-medal" />
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="封禁至">
            <el-date-picker
              v-model="formData.lockUntil"
              type="datetime"
              placeholder="选择日期时间"
              style="width: 100%"
              value-format="yyyy-MM-dd HH:mm:ss"
              popper-class="glass-date-picker"
              :append-to-body="true"
            ></el-date-picker>
          </el-form-item>
        </el-col>
      </el-row>
      
      <!-- 用户类型 -->
      <el-form-item label="用户类型">
        <el-select
          v-model="formData.userType"
          placeholder="请选择用户类型"
          style="width: 100%"
          popper-class="glass-select-dropdown"
        >
          <el-option
            v-for="(label, value) in USER_TYPE_OPTIONS"
            :key="value"
            :label="label.text"
            :value="value"
          >
            <i :class="label.icon" />
            <span>{{ label.text }}</span>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 是否设置为管理员（仅新增模式显示） -->
      <el-form-item v-if="!isEditMode" label="设置为管理员">
        <el-switch
          v-model="formData.isAdmin"
          active-text="是"
          inactive-text="否"
          active-color="#13ce66"
          inactive-color="#dcdfe6"
        />
        <span style="margin-left: 12px; color: #909399; font-size: 12px;">
          管理员拥有系统所有权限
        </span>
      </el-form-item>

      <!-- 发布商品和交易订单（编辑模式下显示） -->
      <el-row v-if="isEditMode" :gutter="20">
        <el-col :span="12">
          <el-form-item label="发布商品">
            <el-input
              v-model="formData.goodsCount"
              disabled
            >
              <i slot="prefix" class="el-icon-goods" />
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="交易订单">
            <el-input
              v-model="formData.ordersCount"
              disabled
            >
              <i slot="prefix" class="el-icon-s-order" />
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    
    <span slot="footer" class="dialog-footer">
      <button class="glass-btn btn-cancel" @click="handleClose">
        <i class="el-icon-close" />
        <span>取消</span>
      </button>
      <button class="glass-btn btn-save" @click="handleSave">
        <i class="el-icon-check" />
        <span>保存</span>
      </button>
    </span>
  </el-dialog>
</template>

<script>
import {
  USER_TYPE_LABELS,
  USER_TYPES,
  VALIDATION_RULES,
  DEFAULT_AVATAR
} from '@/utils/userConstants';

export default {
  name: 'UserFormDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    isEditMode: {
      type: Boolean,
      default: false
    },
    formData: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      // 常量
      NAME_MAX_LENGTH: VALIDATION_RULES.MAX_NAME_LENGTH,
      USER_ID_MAX_LENGTH: VALIDATION_RULES.MAX_USER_ID_LENGTH,
      defaultAvatar: DEFAULT_AVATAR,
      USER_TYPE_OPTIONS: {
        [USER_TYPES.NORMAL]: {
          text: USER_TYPE_LABELS[USER_TYPES.NORMAL],
          icon: 'el-icon-user-solid'
        },
        [USER_TYPES.ACTIVE]: {
          text: USER_TYPE_LABELS[USER_TYPES.ACTIVE],
          icon: 'el-icon-star-on'
        }
      },
      // 表单验证规则
      formRules: {
        name: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 30, message: '用户名长度在 2 到 30 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码至少6位', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱账户', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
        ],
        phone: [
          { required: true, message: '请输入电话号码', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: ['blur', 'change'] }
        ]
      }
    };
  },
  methods: {
    handleClose() {
      this.$emit('close');
    },
    handleSave() {
      this.$refs.userForm.validate(valid => {
        if (valid) {
          this.$emit('save', { ...this.formData });
        }
      });
    },
    // 上传前验证
    handleBeforeUpload(file) {
      const isImage = file.type.startsWith('image/');
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isImage) {
        this.$root.$emit('show-island-message', '只能上传图片文件', 'danger');
        return false;
      }
      if (!isLt2M) {
        this.$root.$emit('show-island-message', '图片大小不能超过 2MB', 'danger');
        return false;
      }
      return true;
    },
    // 自定义上传
    async handleAvatarUpload(params) {
      const file = params.file;
      const formData = new FormData();
      formData.append('avatar', file);

      try {
        // 判断是编辑模式还是新增模式
        // 编辑模式：调用管理员修改指定用户头像接口
        // 新增模式：调用通用上传接口（返回URL）
        const url = (this.isEditMode && this.formData.userId) 
          ? `/api/user/admin/${this.formData.userId}/avatar` 
          : '/api/user/avatar';

        const response = await this.$axios.post(url, formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          let avatarUrl = response.data;
          // 兼容可能返回对象的情况
          if (avatarUrl && typeof avatarUrl === 'object' && avatarUrl.url) {
            avatarUrl = avatarUrl.url;
          }
          
          this.formData.avatar = avatarUrl;
          this.$root.$emit('show-island-message', '头像上传成功', 'success');
        } else {
          this.$root.$emit('show-island-message', response.message || '头像上传失败', 'danger');
        }
      } catch (error) {
        console.error('头像上传出错:', error);
        let errorMessage = '头像上传失败';
        if (error.response && error.response.data && error.response.data.message) {
          errorMessage = error.response.data.message;
        }
        this.$root.$emit('show-island-message', errorMessage, 'danger');
      }
    },
    // 重置头像
    handleResetAvatar() {
      this.formData.avatar = this.defaultAvatar;
      this.$root.$emit('show-island-message', '已重置为默认头像', 'success');
    },
    // 限制信誉分范围
    handleReputationBlur() {
      let val = parseInt(this.formData.reputation);
      if (isNaN(val)) val = 100; // 默认为100
      if (val < 0) val = 0;
      if (val > 100) val = 100;
      this.formData.reputation = val;
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

// 隐藏数字输入框的微调按钮
::v-deep .no-spin-button {
  input::-webkit-outer-spin-button,
  input::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
  input[type=number] {
    -moz-appearance: textfield;
  }
}

// 用户表单样式
.user-form {
  padding: 10px 20px;

  // 头像上传
  .avatar-upload-container {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;

    .avatar-preview {
      width: 72px;
      height: 72px;
      border-radius: 50%;
      overflow: hidden;
      border: 2px solid rgba($color-primary, 0.4);
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .avatar-actions {
      display: flex;
      gap: 10px;
      align-items: center;

      .glass-btn {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        border: none;
        border-radius: 8px;
        font-size: 13px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;
        outline: none;

        &.btn-upload {
          background: rgba($color-primary, 0.15);
          border: 1px solid rgba($color-primary, 0.3);
          color: $color-primary;

          &:hover {
            background: rgba($color-primary, 0.25);
          }
        }

        &.btn-reset {
          background: rgba($color-text-secondary, 0.1);
          border: 1px solid rgba($color-border, 0.3);
          color: $color-text-secondary;

          &:hover {
            background: rgba($color-text-secondary, 0.2);
          }
        }
      }
    }

    .avatar-tips {
      width: 100%;
      font-size: 12px;
      color: $color-text-disabled;
      display: flex;
      align-items: center;
      gap: 4px;
      margin-top: -8px;
    }
  }

  ::v-deep .el-form-item {
    margin-bottom: 24px;

    .el-form-item__label {
      color: $color-text-secondary;
      font-weight: 600;
      font-size: 13px;
      line-height: 40px;
    }

    .el-form-item__content {
      line-height: 40px;
    }

    // 隐藏 el-upload 默认样式
    .el-upload {
      display: inline-block;
    }

    .el-input__inner {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba($color-border, 0.3);
      border-radius: 12px;
      color: $color-text-primary;
      height: 48px;
      padding-left: 40px;
      transition: all 0.3s ease;
      
      &:focus {
        border-color: $color-primary;
        background: rgba(255, 255, 255, 0.08);
        box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
      }
      
      &:disabled {
        background: rgba(255, 255, 255, 0.02);
        color: $color-text-disabled;
        cursor: not-allowed;
      }
    }
    
    .el-input__prefix {
      left: 12px;
      display: flex;
      align-items: center;
      
      i {
        font-size: 18px;
        color: $color-primary;
      }
    }
    
    .el-input__suffix {
      right: 12px;
      
      .el-input__count {
        background: transparent !important;
        color: $color-text-secondary !important;
        font-size: 12px !important;
        line-height: 20px !important;
        border: none !important;
        box-shadow: none !important;
        backdrop-filter: none !important;
        margin-right: 8px !important;
        
        .el-input__count-inner {
          background: transparent !important;
          color: $color-text-secondary !important;
          line-height: 1 !important;
          font-weight: 500 !important;
        }
      }
    }
  }
  
  .el-select {
    width: 100%;
    
    .el-input__inner {
      padding-left: 12px;
    }
  }
}

// 对话框底部按钮
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  padding-top: 10px;
  
  .glass-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px 32px;
    border: none;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    outline: none;
    backdrop-filter: blur(10px);
    
    i {
      font-size: 16px;
    }
    
    &.btn-cancel {
      background: rgba($color-text-secondary, 0.1);
      border: 1px solid rgba($color-border, 0.3);
      color: $color-text-secondary;
      
      &:hover {
        background: rgba($color-text-secondary, 0.15);
        border-color: rgba($color-border, 0.5);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
    }
    
    &.btn-save {
      background: linear-gradient(135deg, 
        rgba(var(--color-primary-rgb), 0.95), 
        rgba(var(--color-primary-rgb), 0.85));
      border: 1px solid rgba(var(--color-primary-rgb), 0.3);
      color: #fff;
      box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
      
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
}

// 弹窗样式
::v-deep {
  .glass-dialog.user-dialog {
    @include glass-card;
    border: 1px solid rgba($color-border, 0.5);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    
    .el-dialog__header {
      padding: 24px 28px 20px;
      border-bottom: 1px solid rgba($color-border, 0.2);
      background: rgba($color-primary, 0.02);
      
      .el-dialog__title {
        font-size: 20px;
        font-weight: 700;
        color: $color-text-primary;
      }
      
      .el-dialog__headerbtn {
        top: 24px;
        right: 24px;
        width: 36px;
        height: 36px;
        
        .el-dialog__close {
          font-size: 20px;
          font-weight: 700;
          color: $color-text-secondary;
          
          &:hover {
            color: $color-primary;
          }
        }
      }
    }
    
    .el-dialog__body {
      padding: 20px 28px;
    }
    
    .el-dialog__footer {
      padding: 16px 28px 24px;
      background: rgba($color-primary, 0.02);
      border-top: 1px solid rgba($color-border, 0.2);
    }
  }
  
  .glass-select-dropdown {
    @include glass-card;
    border: 1px solid rgba($color-border, 0.5);
    
    .el-select-dropdown__item {
      color: $color-text-primary;
      padding: 12px 16px;
      display: flex;
      align-items: center;
      gap: 10px;
      transition: all 0.3s ease;
      
      i {
        font-size: 16px;
        color: $color-primary;
      }
      
      &:hover {
        background: rgba($color-primary, 0.1);
      }
      
      &.selected {
        color: $color-primary;
        font-weight: 600;
        background: rgba($color-primary, 0.15);
      }
    }
  }
}
</style>

<!-- 全局样式覆盖日期选择器 -->
<style lang="scss">
@import '@/assets/css/themes/index.scss';

/* 全局样式覆盖 Element UI 下拉框 */
.glass-select-dropdown {
  background: rgba(30, 30, 35, 0.95) !important;
  backdrop-filter: blur(20px) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5) !important;

  .el-select-dropdown__item {
    color: #a0a0a0 !important;
    padding: 12px 16px;
    height: auto;
    line-height: 1.5;
    
    &:hover, &.hover {
      background: rgba(255, 255, 255, 0.05) !important;
      color: #fff !important;
    }
    
    &.selected {
      color: $color-primary !important;
      background: rgba($color-primary, 0.1) !important;
      font-weight: 600;
    }

    span {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .popper__arrow {
    border-bottom-color: rgba(255, 255, 255, 0.1) !important;
    &::after {
      border-bottom-color: rgba(30, 30, 35, 0.95) !important;
    }
  }
}

.glass-date-picker {
  background: rgba(30, 30, 35, 0.95) !important;
  backdrop-filter: blur(20px) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5) !important;
  color: #fff !important;

  // 顶部日期/时间选择输入框容器
  .el-date-picker__time-header {
    border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
    background: transparent !important;
  }

  // 顶部输入框本身
  .el-date-picker__editor-wrap {
    .el-input__inner {
      background: rgba(255, 255, 255, 0.05) !important;
      border: 1px solid rgba(255, 255, 255, 0.15) !important;
      color: #fff !important;
      box-shadow: none !important;
      
      &::placeholder {
        color: rgba(255, 255, 255, 0.5) !important;
      }
      
      &:focus {
        border-color: #409EFF !important;
        background: rgba(255, 255, 255, 0.1) !important;
      }
    }
  }

  // 头部区域
  .el-date-picker__header {
    margin: 12px;
    
    .el-date-picker__header-label {
      color: #fff !important;
      font-weight: 600;
      
      &:hover {
        color: #409EFF !important;
      }
    }
    
    .el-picker-panel__icon-btn {
      color: rgba(255, 255, 255, 0.6) !important;
      
      &:hover {
        color: #409EFF !important;
      }
    }
  }
  
  // 日历表格
  .el-date-table {
    th {
      color: rgba(255, 255, 255, 0.6) !important;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
    }
    
    td {
      &.next-month, &.prev-month {
        color: rgba(255, 255, 255, 0.3) !important;
      }
      
      &.available {
        color: #fff !important;
        &:hover {
          color: #409EFF !important;
        }
      }
      
      &.current:not(.disabled) span {
        background-color: #409EFF !important;
        color: #fff !important;
      }

      &.today span {
        color: #409EFF !important;
        font-weight: bold;
      }
    }
  }

  // 时间选择面板
  .el-time-panel {
    background: rgba(30, 30, 35, 0.95) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    
    .el-time-spinner__item {
      color: rgba(255, 255, 255, 0.6) !important;
      
      &.active:not(.disabled) {
        color: #fff !important;
        font-weight: bold;
      }
      
      &:hover:not(.disabled):not(.active) {
        background: rgba(255, 255, 255, 0.05) !important;
      }
    }
    
    .el-time-panel__footer {
      border-top: 1px solid rgba(255, 255, 255, 0.1) !important;
      
      .el-time-panel__btn.confirm {
        color: #409EFF !important;
        font-weight: bold;
      }
      
      .el-time-panel__btn.cancel {
        color: rgba(255, 255, 255, 0.6) !important;
      }
    }
    
    .el-time-panel__content::after, .el-time-panel__content::before {
      border-top: 1px solid rgba(255, 255, 255, 0.1) !important;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
    }
  }

  // 底部按钮
  .el-picker-panel__footer {
    border-top: 1px solid rgba(255, 255, 255, 0.1) !important;
    background: transparent !important;
    
    .el-button--text {
      color: rgba(255, 255, 255, 0.6) !important;
      
      &:hover {
        color: #409EFF !important;
      }
    }
    
    .el-button--default {
      background: rgba(64, 158, 255, 0.2) !important;
      border: 1px solid rgba(64, 158, 255, 0.5) !important;
      color: #fff !important;
      
      &:hover {
        background: rgba(64, 158, 255, 0.3) !important;
      }
    }
  }
}