<template>
  <div class="page-header">
    <div class="header-top">
      <div class="header-content">
        <h1 class="page-title">{{ title }}</h1>
        <p class="page-desc">{{ description }}</p>
      </div>
    </div>
    <div class="header-extra" v-if="$slots.extra">
      <slot name="extra"></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PageHeader',
  props: {
    title: {
      type: String,
      required: true
    },
    description: {
      type: String,
      default: ''
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.page-header {
  margin-bottom: 24px;
  
  .header-top {
    margin-bottom: 20px;
    
    .header-content {
      .page-title {
        font-size: 42px;
        font-weight: 700;
        margin-bottom: 8px;
        color: $color-text-primary;
        text-shadow: 0 2px 4px rgba(0,0,0,0.3);
        letter-spacing: 1px;
      }
      
      .page-desc {
        color: $color-text-primary;
        opacity: 0.9;
        font-size: 18px;
        font-weight: 500;
        text-shadow: 0 1px 2px rgba(0,0,0,0.2);
        letter-spacing: 0.5px;
      }
    }
  }
  
  .header-extra {
    width: 100%;
  }
}
</style>
