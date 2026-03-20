<template>
  <div 
    class="glass-switch" 
    :class="{ 'is-checked': value, 'is-disabled': disabled }"
    @click="handleClick"
  >
    <div class="switch-track">
      <div class="switch-thumb"></div>
    </div>
    <span v-if="activeText && value" class="switch-label">{{ activeText }}</span>
    <span v-if="inactiveText && !value" class="switch-label">{{ inactiveText }}</span>
  </div>
</template>

<script>
export default {
  name: 'GlassSwitch',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    activeText: {
      type: String,
      default: ''
    },
    inactiveText: {
      type: String,
      default: ''
    }
  },
  methods: {
    handleClick() {
      if (this.disabled) return;
      this.$emit('input', !this.value);
      this.$emit('change', !this.value);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.glass-switch {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
  
  &.is-disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  
  .switch-track {
    position: relative;
    width: 48px;
    height: 24px;
    background: var(--color-pill-bg);
    border-radius: 999px;
    border: 1px solid rgba(255, 255, 255, 0.15);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(4px);
    
    .switch-thumb {
      position: absolute;
      top: 2px;
      left: 2px;
      width: 18px;
      height: 18px;
      background: #fff;
      border-radius: 50%;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 
        0 2px 4px rgba(0, 0, 0, 0.2),
        inset 0 1px 0 rgba(255, 255, 255, 0.5);
    }
  }
  
  &.is-checked {
    .switch-track {
      background: rgba(var(--color-primary-rgb), 0.85);
      border-color: rgba(var(--color-primary-rgb), 0.3);
      box-shadow: 
        inset 0 2px 4px rgba(0, 0, 0, 0.15),
        0 0 12px rgba(var(--color-primary-rgb), 0.3);
      
      .switch-thumb {
        left: calc(100% - 20px);
        background: #fff;
        box-shadow: 
          0 2px 6px rgba(0, 0, 0, 0.3),
          inset 0 1px 0 rgba(255, 255, 255, 0.6);
      }
    }
  }
  
  &:not(.is-disabled):hover {
    .switch-track {
      border-color: rgba(var(--color-primary-rgb), 0.4);
      box-shadow: 
        inset 0 2px 4px rgba(0, 0, 0, 0.1),
        0 0 8px rgba(var(--color-primary-rgb), 0.2);
    }
  }
  
  .switch-label {
    font-size: 13px;
    font-weight: 500;
    color: $color-text-secondary;
    transition: color 0.3s ease;
  }
  
  &.is-checked .switch-label {
    color: $color-primary;
    font-weight: 600;
  }
}
</style>
