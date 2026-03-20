<template>
  <div class="category-tabs">
    <div 
      v-for="category in categories" 
      :key="category.id"
      class="category-tab"
      :class="{ active: currentCategory === category.id }"
      @click="$emit('select', category.id)"
    >
      <span class="category-icon">{{ category.icon }}</span>
      <span class="category-name">{{ category.name }}</span>
      <span class="category-count">{{ category.count }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ThemeCategoryTabs',
  props: {
    categories: {
      type: Array,
      required: true
    },
    currentCategory: {
      type: String,
      default: 'all'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.category-tabs {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.category-tab {
  @include glass-card;
  padding: 10px 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
  
  .category-icon {
    font-size: 18px;
  }
  
  .category-name {
    font-size: 14px;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }
  
  .category-count {
    font-size: 12px;
    color: $color-text-secondary;
    background: rgba(var(--color-primary-rgb), 0.1);
    padding: 2px 8px;
    border-radius: 10px;
    font-weight: $font-weight-bold;
  }
  
  &:hover {
    transform: translateY(-2px);
    border-color: rgba(var(--color-primary-rgb), 0.3);
  }
  
  &.active {
    border-color: $color-primary;
    background: rgba(var(--color-primary-rgb), 0.08);
    
    .category-name {
      color: $color-primary;
    }
    
    .category-count {
      background: $color-primary;
      color: #fff;
    }
  }
}

// 响应式设计
@media (max-width: $breakpoint-xs) {
  .category-tabs {
    gap: 8px;
  }
  
  .category-tab {
    padding: 8px 14px;
    
    .category-icon {
      font-size: 16px;
    }
    
    .category-name {
      font-size: 13px;
    }
    
    .category-count {
      font-size: 11px;
      padding: 1px 6px;
    }
  }
}
</style>