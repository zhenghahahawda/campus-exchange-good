<template>
  <div class="glass-dropdown-item" @click="handleClick">
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: 'GlassDropdownItem',
  props: {
    command: {
      type: String,
      default: ''
    }
  },
  methods: {
    handleClick() {
      this.$parent.handleItemClick(this.command);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.glass-dropdown-item {
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: $color-text-primary;
  font-size: 14px;
  
  &:hover {
    background: rgba(var(--color-primary-rgb), 0.1);
    color: $color-primary;
  }
}
</style>