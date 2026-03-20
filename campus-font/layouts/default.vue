<template>
  <div class="app-container">
    <!-- 动态背景组件 -->
    <DynamicBackground />

    <AppHeader />
    <main class="main-content">
      <div class="page-content" ref="pageContent">
        <Nuxt />
      </div>
    </main>
    
    <!-- 移动端底部导航 (可选) -->
    <nav class="mobile-nav" v-if="isMobile">
      <!-- 移动端导航项 -->
    </nav>
  </div>
</template>

<script>
import AppHeader from '@/components/layout/AppHeader.vue'
import DynamicBackground from '@/components/ui/DynamicBackground.vue'

export default {
  name: 'DefaultLayout',
  components: {
    AppHeader,
    DynamicBackground
  },
  data() {
    return {
      isMobile: false
    }
  },
  mounted() {
    this.checkMobile();
    window.addEventListener('resize', this.checkMobile);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.checkMobile);
  },
  methods: {
    checkMobile() {
      this.isMobile = window.innerWidth <= 768;
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  z-index: 10;
  position: relative;
  max-width: 1440px;
  margin: 0 auto;
  width: 100%;
}

.page-content {
  flex: 1;
  padding: 20px 40px 60px;
  
  @media (max-width: 768px) {
    padding: 10px 20px 80px;
  }
}
</style>
