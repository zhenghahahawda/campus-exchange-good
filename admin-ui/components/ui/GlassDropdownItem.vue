<template>
  <div class="glass-dropdown-item" @click.stop="handleClick">
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: 'GlassDropdownItem',
  props: {
    command: {
      type: [String, Number, Object],
      default: ''
    }
  },
  methods: {
    handleClick() {
      // 向上寻找 GlassDropdown 父组件并触发 command 事件
      let parent = this.$parent;
      while (parent && parent.$options.name !== 'GlassDropdown') {
        parent = parent.$parent;
      }
      if (parent) {
        parent.handleItemClick(this.command);
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.glass-dropdown-item {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: $color-text-secondary;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  
  &:hover {
    background: rgba(255, 255, 255, 0.15); // 使用半透明白色以适应深色模式
    color: $color-text-primary; // 悬浮时使用主要文字色
    padding-left: 24px;
  }
}
</style>
