<template>
  <div class="user-info-card">
    <el-upload
      class="avatar-uploader"
      :action="uploadUrl"
      :headers="uploadHeaders"
      name="avatar"
      :show-file-list="false"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
    >
      <div class="avatar-wrapper">
        <img :src="avatar || defaultAvatar" class="avatar-preview" />
        <div class="avatar-overlay">
          <i class="el-icon-camera"></i>
          <span>点击上传</span>
        </div>
      </div>
    </el-upload>
    <div class="user-info-display">
      <h4 class="username">{{ username || '未设置用户名' }}</h4>
      <div class="info-item" v-for="item in infoItems" :key="item.key">
        <i :class="item.icon"></i>
        <span>{{ item.label }}: {{ item.value || item.placeholder }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import { isSuccess } from '@/composables/useApi'

export default {
  name: 'UserInfoCard',
  props: {
    avatar: {
      type: String,
      default: ''
    },
    username: {
      type: String,
      default: ''
    },
    uid: {
      type: String,
      default: ''
    },
    phone: {
      type: String,
      default: ''
    },
    email: {
      type: String,
      default: ''
    },
    goodsCount: {
      type: Number,
      default: 0
    },
    ordersCount: {
      type: Number,
      default: 0
    },
    uploadUrl: {
      type: String,
      default: '/api/upload/avatar'
    }
  },
  data() {
    return {
      defaultAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
    }
  },
  computed: {
    uploadHeaders() {
      const token = getToken()
      return token ? { Authorization: `Bearer ${token}` } : {}
    },
    infoItems() {
      return [
        {
          key: 'uid',
          icon: 'el-icon-postcard',
          label: 'ID',
          value: this.uid,
          placeholder: '未设置'
        },
        {
          key: 'phone',
          icon: 'el-icon-phone',
          label: '电话',
          value: this.phone,
          placeholder: '未设置电话'
        },
        {
          key: 'email',
          icon: 'el-icon-message',
          label: '邮箱',
          value: this.email,
          placeholder: '未设置邮箱'
        },
        {
          key: 'goodsCount',
          icon: 'el-icon-goods',
          label: '发布商品',
          value: this.goodsCount,
          placeholder: '0'
        },
        {
          key: 'ordersCount',
          icon: 'el-icon-s-order',
          label: '交易订单',
          value: this.ordersCount,
          placeholder: '0'
        }
      ]
    }
  },
  methods: {
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$root.$emit('show-island-message', '只能上传图片文件', 'error')
        return false
      }
      if (!isLt2M) {
        this.$root.$emit('show-island-message', '图片大小不能超过 2MB', 'error')
        return false
      }
      return true
    },
    handleSuccess(res) {
      if (isSuccess(res)) {
        let url = res.data
        // 兼容可能返回对象的情况
        if (url && typeof url === 'object' && url.url) {
          url = url.url
        }

        if (url) {
          this.$emit('avatar-change', url)
          this.$root.$emit('show-island-message', '头像上传成功', 'success')
        } else {
          this.$root.$emit('show-island-message', '头像上传失败：未获取到图片地址', 'error')
        }
      } else {
        this.$root.$emit('show-island-message', res.message || '头像上传失败', 'error')
      }
    },
    handleError(err) {
      console.error('头像上传失败:', err)
      this.$root.$emit('show-island-message', '头像上传失败，请重试', 'error')
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.user-info-card {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 32px;
  background: var(--glass-bg);
  border-radius: 16px;
  border: 1px solid var(--glass-border);

  .avatar-uploader {
    cursor: pointer;
    flex-shrink: 0;

    .avatar-wrapper {
      position: relative;
      width: 100px;
      height: 100px;
      border-radius: 50%;
      overflow: hidden;
      border: 3px solid $color-primary;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      transition: all 0.3s;

      &:hover {
        transform: scale(1.05);
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);

        .avatar-overlay {
          opacity: 1;
        }
      }

      .avatar-preview {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }

      .avatar-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.6);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s;
        color: white;

        i {
          font-size: 24px;
          margin-bottom: 4px;
        }

        span {
          font-size: 12px;
        }
      }
    }
  }

  .user-info-display {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;

    .username {
      font-size: 24px;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin: 0 0 8px 0;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 14px;
      color: $color-text-secondary;

      i {
        color: $color-primary;
        font-size: 16px;
      }

      span {
        color: $color-text-primary;
      }
    }
  }
}

@media (max-width: 768px) {
  .user-info-card {
    flex-direction: column;
    text-align: center;
  }
}
</style>
