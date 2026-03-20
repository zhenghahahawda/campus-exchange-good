<template>
  <div class="info-display">
    <i :class="icon"></i>
    <span>{{ value || placeholder }}</span>
  </div>
</template>

<script>
export default {
  name: 'InfoDisplay',
  props: {
    icon: {
      type: String,
      required: true
    },
    value: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '未知'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.info-display {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 8px;
  color: $color-text-primary;
  font-size: 14px;
  min-height: 40px;

  i {
    color: $color-primary;
    font-size: 16px;
  }

  span {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
