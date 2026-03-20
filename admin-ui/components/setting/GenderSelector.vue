<template>
  <div class="gender-selector">
    <div
      v-for="option in genderOptions"
      :key="option.value"
      class="gender-option"
      :class="{ active: value === option.value }"
      @click="handleSelect(option.value)"
    >
      <i :class="option.icon"></i>
      <span>{{ option.label }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GenderSelector',
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      genderOptions: [
        { label: '男', value: 'male', icon: 'el-icon-male' },
        { label: '女', value: 'female', icon: 'el-icon-female' },
        { label: '保密', value: 'other', icon: 'el-icon-remove-outline' }
      ]
    }
  },
  methods: {
    handleSelect(value) {
      this.$emit('input', value)
      this.$emit('change', value)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.gender-selector {
  display: flex;
  gap: 12px;

  .gender-option {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 10px 16px;
    background: var(--glass-bg);
    border: 2px solid var(--glass-border);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    font-size: 14px;
    color: $color-text-primary;

    i {
      font-size: 18px;
      color: $color-text-secondary;
      transition: color 0.3s;
    }

    &:hover {
      border-color: $color-primary;
      background: rgba(102, 126, 234, 0.05);

      i {
        color: $color-primary;
      }
    }

    &.active {
      border-color: $color-primary;
      background: rgba(102, 126, 234, 0.1);
      color: $color-primary;
      font-weight: 600;

      i {
        color: $color-primary;
      }
    }
  }
}
</style>
