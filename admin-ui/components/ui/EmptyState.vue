<template>
  <div class="empty-state glass-panel">
    <div class="empty-icon-wrapper">
      <i :class="iconClass"></i>
    </div>
    <p class="empty-text">{{ message }}</p>
    <el-button 
      v-if="showAction" 
      class="glass-button" 
      @click="$emit('action')"
    >
      {{ actionText }}
    </el-button>
  </div>
</template>

<script>
export default {
  name: 'EmptyState',
  props: {
    icon: {
      type: String,
      default: 'el-icon-box'
    },
    message: {
      type: String,
      default: '暂无数据'
    },
    actionText: {
      type: String,
      default: '刷新'
    },
    showAction: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    iconClass() {
      return this.icon;
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.empty-state {
  text-align: center;
  padding: 60px 20px;
  
  .empty-icon-wrapper {
    font-size: 64px;
    color: $color-text-disabled;
    margin-bottom: 20px;
    opacity: 0.5;
  }
  
  .empty-text {
    color: $color-text-secondary;
    font-size: 16px;
    margin-bottom: 20px;
  }
  
  .glass-button {
    @include glass-button;
  }
}
</style>
