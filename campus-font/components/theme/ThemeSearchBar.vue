<template>
  <div class="search-box">
    <i class="el-icon-search"></i>
    <input 
      :value="value"
      type="text" 
      placeholder="搜索主题名称或描述..."
      @input="$emit('input', $event.target.value)"
    />
    <i 
      v-if="value" 
      class="el-icon-circle-close clear-icon" 
      @click="$emit('clear')"
    ></i>
  </div>
</template>

<script>
export default {
  name: 'ThemeSearchBar',
  props: {
    value: {
      type: String,
      default: ''
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.search-box {
  @include glass-card;
  display: flex;
  align-items: center;
  padding: 14px 20px;
  gap: 12px;
  
  .el-icon-search {
    font-size: 18px;
    color: $color-text-secondary;
  }
  
  input {
    flex: 1;
    border: none;
    background: transparent;
    color: $color-text-primary;
    font-size: 15px;
    outline: none;
    
    &::placeholder {
      color: $color-text-disabled;
    }
  }
  
  .clear-icon {
    font-size: 16px;
    color: $color-text-secondary;
    cursor: pointer;
    transition: all 0.2s ease;
    
    &:hover {
      color: $color-primary;
      transform: scale(1.1);
    }
  }
}

// 响应式设计
@media (max-width: $breakpoint-xs) {
  .search-box {
    padding: 12px 16px;
    
    input {
      font-size: 14px;
    }
  }
}
</style>