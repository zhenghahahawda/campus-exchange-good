<template>
  <div class="category-selector">
    <GlassDropdown @command="handleCommand" ref="glassDropdown">
      <template slot="trigger" slot-scope="{ isOpen }">
        <div class="filter-dropdown">
          <span>{{ currentLabel }}</span>
          <i class="el-icon-arrow-down" :class="{ 'rotate-180': isOpen }" />
        </div>
      </template>
      
      <div class="dropdown-content-wrapper">
        <!-- 搜索框 -->
        <div class="search-box" @click.stop>
          <el-input
            v-model="searchQuery"
            placeholder="搜索分类"
            prefix-icon="el-icon-search"
            size="small"
            clearable
          ></el-input>
        </div>

        <div class="list-container">
          <!-- 默认选项 -->
          <GlassDropdownItem 
            v-if="showAllOption && !searchQuery"
            command=""
            :class="{ 'is-active': value === '' || value === null }"
          >
            <div class="dropdown-item-content">
              <i class="el-icon-menu item-icon"></i>
              <span>全部品类</span>
            </div>
          </GlassDropdownItem>

          <!-- 分类列表 -->
          <GlassDropdownItem 
            v-for="item in filteredCategories" 
            :key="item.categoryId"
            :command="item.categoryId"
            :class="{ 'is-active': value === item.categoryId }"
          >
            <div class="dropdown-item-content">
              <i :class="getCategoryIcon(item.categoryCode)" class="item-icon"></i>
              <span>{{ item.categoryName }}</span>
            </div>
          </GlassDropdownItem>

          <!-- 无搜索结果 -->
          <div v-if="filteredCategories.length === 0 && searchQuery" class="no-data">
            无匹配分类
          </div>
        </div>
      </div>
    </GlassDropdown>
  </div>
</template>

<script>
import GlassDropdown from '@/components/ui/GlassDropdown.vue';
import GlassDropdownItem from '@/components/ui/GlassDropdownItem.vue';

export default {
  name: 'CategorySelector',
  components: {
    GlassDropdown,
    GlassDropdownItem
  },
  props: {
    // 当前选中的分类ID
    value: {
      type: [String, Number],
      default: ''
    },
    // 是否显示“全部”选项
    showAllOption: {
      type: Boolean,
      default: true
    },
    // 默认显示的文本（当没有选中且没有全部选项时）
    placeholder: {
      type: String,
      default: '选择分类'
    }
  },
  data() {
    return {
      categories: [],
      loading: false,
      searchQuery: ''
    };
  },
  computed: {
    currentLabel() {
      if (!this.value && this.showAllOption) return '全部品类';
      const category = this.categories.find(c => c.categoryId === this.value);
      return category ? category.categoryName : this.placeholder;
    },
    filteredCategories() {
      if (!this.searchQuery) return this.categories;
      const query = this.searchQuery.toLowerCase();
      return this.categories.filter(item => 
        item.categoryName.toLowerCase().includes(query) || 
        (item.categoryCode && item.categoryCode.toLowerCase().includes(query))
      );
    }
  },
  watch: {
    // 当下拉框关闭时清空搜索
    '$refs.glassDropdown.isOpen': function(val) {
      if (!val) {
        setTimeout(() => {
          this.searchQuery = '';
        }, 300);
      }
    }
  },
  created() {
    this.fetchCategories();
  },
  methods: {
    async fetchCategories() {
      this.loading = true;
      try {
        const response = await this.$axios.get('/api/categories/active');
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          this.categories = response.data || [];
        } else {
          // 如果接口失败，使用默认模拟数据（对应截图）
          this.categories = [
            { categoryId: 1, categoryName: '数码产品', categoryCode: 'digital' },
            { categoryId: 2, categoryName: '家居生活', categoryCode: 'home' },
            { categoryId: 3, categoryName: '服饰鞋包', categoryCode: 'clothing' },
            { categoryId: 4, categoryName: '图书文具', categoryCode: 'books' }
          ];
        }
      } catch (error) {
        console.error('Fetch categories error:', error);
        // 出错时也使用默认模拟数据
        this.categories = [
          { categoryId: 1, categoryName: '数码产品', categoryCode: 'digital' },
          { categoryId: 2, categoryName: '家居生活', categoryCode: 'home' },
          { categoryId: 3, categoryName: '服饰鞋包', categoryCode: 'clothing' },
          { categoryId: 4, categoryName: '图书文具', categoryCode: 'books' }
        ];
      } finally {
        this.loading = false;
      }
    },
    
    handleCommand(command) {
      this.$emit('input', command);
      this.$emit('change', command);
    },

    getCategoryIcon(code) {
      const iconMap = {
        'digital': 'el-icon-mobile-phone',
        'home': 'el-icon-house',
        'clothing': 'el-icon-shopping-bag-1',
        'books': 'el-icon-notebook-1'
      };
      return iconMap[code] || 'el-icon-price-tag';
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.filter-dropdown {
  @include glass-effect;
  padding: 10px 20px;
  border-radius: 50px;
  cursor: pointer;
  font-size: 14px;
  color: $color-text-secondary;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  
  &:hover {
    color: $color-primary;
    background: var(--color-bg-surface);
  }
  
  i.rotate-180 { transform: rotate(180deg); }
}

.dropdown-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  
  .item-icon {
    font-size: 16px;
    color: inherit;
  }
}

// 选中状态样式
.is-active {
  ::v-deep .glass-dropdown-item {
    background-color: rgba(255, 255, 255, 0.05); // 选中项背景微亮
  }

  .dropdown-item-content {
    color: #f0a621 !important; // 强制使用橙色高亮
    font-weight: 600;
    
    .item-icon {
      color: #f0a621 !important;
    }
  }
}

.dropdown-content-wrapper {
  width: 220px;
}

.search-box {
  padding: 8px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  
  ::v-deep .el-input__inner {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    color: $color-text-primary;
    height: 32px;
    line-height: 32px;
    font-size: 13px;
    
    &:focus {
      border-color: $color-primary;
      background: rgba(255, 255, 255, 0.15);
    }

    &::placeholder {
      color: rgba($color-text-secondary, 0.7);
    }
  }

  ::v-deep .el-input__icon {
    line-height: 32px;
  }
}

.list-container {
  max-height: 200px;
  overflow-y: auto;
  padding: 4px 0;

  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba($color-text-secondary, 0.2);
    border-radius: 3px;
    
    &:hover {
      background: rgba($color-text-secondary, 0.4);
    }
  }
}

.no-data {
  padding: 12px;
  text-align: center;
  color: $color-text-secondary;
  font-size: 12px;
}
</style>
