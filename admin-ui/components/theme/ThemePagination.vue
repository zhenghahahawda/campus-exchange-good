<template>
  <div class="pagination">
    <button 
      class="page-btn"
      :disabled="currentPage === 1"
      @click="$emit('change', currentPage - 1)"
    >
      <i class="el-icon-arrow-left"></i>
    </button>
    
    <div class="page-numbers">
      <button
        v-for="page in visiblePages"
        :key="page"
        class="page-number"
        :class="{ active: currentPage === page, ellipsis: page === '...' }"
        :disabled="page === '...'"
        @click="$emit('change', page)"
      >
        {{ page }}
      </button>
    </div>
    
    <button 
      class="page-btn"
      :disabled="currentPage === totalPages"
      @click="$emit('change', currentPage + 1)"
    >
      <i class="el-icon-arrow-right"></i>
    </button>
  </div>
</template>

<script>
export default {
  name: 'ThemePagination',
  props: {
    currentPage: {
      type: Number,
      required: true
    },
    totalPages: {
      type: Number,
      required: true
    }
  },
  computed: {
    visiblePages() {
      const pages = [];
      const total = this.totalPages;
      const current = this.currentPage;
      
      if (total <= 7) {
        for (let i = 1; i <= total; i++) {
          pages.push(i);
        }
      } else {
        pages.push(1);
        
        if (current > 3) {
          pages.push('...');
        }
        
        for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) {
          pages.push(i);
        }
        
        if (current < total - 2) {
          pages.push('...');
        }
        
        pages.push(total);
      }
      
      return pages;
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 40px;
}

.page-btn {
  @include glass-card;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  color: $color-text-primary;
  font-size: 16px;
  
  &:hover:not(:disabled) {
    background: rgba(var(--color-primary-rgb), 0.1);
    color: $color-primary;
    transform: translateY(-2px);
  }
  
  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.page-numbers {
  display: flex;
  gap: 8px;
}

.page-number {
  @include glass-card;
  min-width: 40px;
  height: 40px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  color: $color-text-primary;
  font-size: 14px;
  font-weight: $font-weight-medium;
  
  &:hover:not(:disabled):not(.active) {
    background: rgba(var(--color-primary-rgb), 0.1);
    color: $color-primary;
    transform: translateY(-2px);
  }
  
  &.active {
    background: $color-primary;
    color: #fff;
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
  }
  
  &.ellipsis {
    cursor: default;
    background: transparent;
    border: none;
    
    &:hover {
      transform: none;
    }
  }
  
  &:disabled {
    cursor: default;
  }
}
</style>
