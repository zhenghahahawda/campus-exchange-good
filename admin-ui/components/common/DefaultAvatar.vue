<template>
  <div class="default-avatar" :style="{ width: size, height: size }">
    <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
      <circle cx="50" cy="50" r="50" :fill="backgroundColor" />
      <text
        x="50"
        y="50"
        text-anchor="middle"
        dominant-baseline="central"
        :font-size="fontSize"
        :fill="textColor"
        font-weight="600"
        font-family="Arial, sans-serif"
      >
        {{ initials }}
      </text>
    </svg>
  </div>
</template>

<script>
export default {
  name: 'DefaultAvatar',
  props: {
    username: {
      type: String,
      default: 'User'
    },
    size: {
      type: String,
      default: '40px'
    }
  },
  computed: {
    initials() {
      if (!this.username) return 'U'

      // 获取用户名的首字母
      const names = this.username.trim().split(' ')
      if (names.length >= 2) {
        return (names[0][0] + names[1][0]).toUpperCase()
      }

      // 中文名取前两个字
      if (/[\u4e00-\u9fa5]/.test(this.username)) {
        return this.username.substring(0, 2)
      }

      // 英文名取前两个字母
      return this.username.substring(0, 2).toUpperCase()
    },
    backgroundColor() {
      // 根据用户名生成一致的颜色
      const colors = [
        '#0075ff', // 主题蓝
        '#2dce89', // 成功绿
        '#fb6340', // 警告橙
        '#11cdef', // 信息青
        '#5e72e4', // 紫色
        '#f5365c', // 危险红
      ]

      let hash = 0
      for (let i = 0; i < this.username.length; i++) {
        hash = this.username.charCodeAt(i) + ((hash << 5) - hash)
      }

      return colors[Math.abs(hash) % colors.length]
    },
    textColor() {
      return '#ffffff'
    },
    fontSize() {
      // 根据头像大小动态调整字体大小
      const sizeNum = parseInt(this.size)
      return `${sizeNum * 0.4}px`
    }
  }
}
</script>

<style lang="scss" scoped>
.default-avatar {
  display: inline-block;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;

  svg {
    width: 100%;
    height: 100%;
    display: block;
  }
}
</style>
