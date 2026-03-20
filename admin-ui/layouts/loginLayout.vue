<template>
  <div class="login-layout">
    <!-- 动态背景组件 -->
    <DynamicBackground />

    <!-- 灵动岛 -->
    <DynamicIsland
      standalone
      :expanded-title="'校园以物换物'"
      :expanded-subtitle="'管理系统'"
      :show-controls="false"
    />

    <Nuxt />
  </div>
</template>

<script>
import DynamicBackground from '@/components/ui/DynamicBackground.vue'
import DynamicIsland from '@/components/common/DynamicIsland.vue'

export default {
  name: 'LoginLayout',
  components: {
    DynamicBackground,
    DynamicIsland
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
