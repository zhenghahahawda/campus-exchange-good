<template>
  <div class="login-layout">
    <!-- 动态背景组件 -->
    <DynamicBackground />

    <Nuxt />
  </div>
</template>

<script>
import DynamicBackground from '@/components/ui/DynamicBackground.vue'

export default {
  name: 'LoginLayout',
  components: {
    DynamicBackground
  }
}
</script>

<style lang="scss" scoped>
.login-layout {
  min-height: 100vh;
  width: 100vw;
  overflow-x: hidden;
  position: relative;
}
</style>
