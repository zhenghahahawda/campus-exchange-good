<template>
  <div class="glass-dropdown" v-click-outside="close">
    <div class="glass-dropdown-trigger" @click="toggle">
      <slot name="trigger" :isOpen="isOpen"></slot>
    </div>
    <transition name="fade">
      <div v-show="isOpen" class="glass-dropdown-menu">
        <slot></slot>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'GlassDropdown',
  directives: {
    clickOutside: {
      bind(el, binding, vnode) {
        el.clickOutsideEvent = function(event) {
          if (!(el == event.target || el.contains(event.target))) {
            binding.value(event);
          }
        };
        document.body.addEventListener('click', el.clickOutsideEvent);
      },
      unbind(el) {
        document.body.removeEventListener('click', el.clickOutsideEvent);
      },
    }
  },
  data() {
    return {
      isOpen: false
    }
  },
  methods: {
    toggle() {
      this.isOpen = !this.isOpen;
    },
    close() {
      this.isOpen = false;
    },
    handleItemClick(command) {
      this.$emit('command', command);
      this.close();
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.glass-dropdown {
  position: relative;
  display: inline-block;
}

.glass-dropdown-trigger {
  cursor: pointer;
}

.glass-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  min-width: 140px;
  z-index: 2000;
  padding: 8px 0;
  
  @include glass-effect;
  border-radius: 16px;
  overflow: hidden;
}

.fade-enter-active, .fade-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.fade-enter, .fade-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}
</style>