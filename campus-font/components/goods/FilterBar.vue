<template>
  <div class="filter-bar">
    <div class="filter-left">
      <el-input
        :value="searchQuery"
        placeholder="搜索物品名称、描述..."
        prefix-icon="el-icon-search"
        clearable
        class="search-input"
        @input="$emit('update:searchQuery', $event)"
      />
      
      <el-select 
        :value="filters.category" 
        placeholder="选择分类" 
        clearable 
        class="filter-select"
        @input="$emit('update:category', $event)"
      >
        <el-option 
          v-for="cat in GOOD_CATEGORIES" 
          :key="cat.value" 
          :label="cat.label" 
          :value="cat.value" 
        />
      </el-select>
    </div>
    
    <div class="filter-right">
      <el-select 
        :value="filters.sort" 
        placeholder="排序方式" 
        class="sort-select"
        @input="$emit('update:sort', $event)"
      >
        <el-option 
          v-for="opt in SORT_OPTIONS" 
          :key="opt.value" 
          :label="opt.label" 
          :value="opt.value" 
        />
      </el-select>
      
      <div class="view-toggle">
        <el-button-group>
          <el-button 
            :type="viewMode === 'grid' ? 'primary' : ''" 
            icon="el-icon-menu"
            @click="$emit('update:viewMode', 'grid')"
            size="small"
          />
          <el-button 
            :type="viewMode === 'list' ? 'primary' : ''" 
            icon="el-icon-s-unfold"
            @click="$emit('update:viewMode', 'list')"
            size="small"
          />
        </el-button-group>
      </div>
    </div>
  </div>
</template>

<script>
import { GOOD_CATEGORIES, SORT_OPTIONS } from '@/utils/constants'

export default {
  name: 'GoodsFilterBar',
  props: {
    searchQuery: { type: String, default: '' },
    filters: { type: Object, required: true },
    viewMode: { type: String, default: 'grid' }
  },
  data() {
    return {
      GOOD_CATEGORIES,
      SORT_OPTIONS
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.filter-bar {
  @include glass-card;
  padding: 20px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  
  .filter-left {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;
    
    .search-input { width: 280px; }
    .filter-select { width: 140px; }
  }
  
  .filter-right {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .sort-select { width: 120px; }
    
    .view-toggle {
      .el-button-group {
        display: flex;
        
        .el-button {
          padding: 8px 12px;
          border: 1px solid var(--glass-border);
          background-color: var(--glass-bg);
          color: var(--color-text-secondary);
          
          &:first-child {
            border-top-left-radius: 8px;
            border-bottom-left-radius: 8px;
          }
          
          &:last-child {
            border-top-right-radius: 8px;
            border-bottom-right-radius: 8px;
          }
          
          &.el-button--primary {
            background-color: var(--color-primary);
            border-color: var(--color-primary);
            color: #fff;
            box-shadow: 0 2px 8px rgba(var(--color-primary-rgb), 0.3);
            z-index: 1;
          }
          
          &:hover:not(.el-button--primary) {
            color: var(--color-primary);
            background-color: rgba(var(--color-primary-rgb), 0.05);
          }
        }
      }
    }
  }
}

@media (max-width: 992px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
    
    .filter-left {
      flex-wrap: wrap;
      .search-input { width: 100%; order: -1; }
    }
    
    .filter-right { justify-content: space-between; }
  }
}
</style>
