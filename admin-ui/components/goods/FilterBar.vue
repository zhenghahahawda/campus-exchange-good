<template>
  <div class="filter-bar">
    <div class="search-wrapper">
      <el-input
        :placeholder="placeholder"
        :value="searchQuery"
        @input="$emit('update:searchQuery', $event)"
        prefix-icon="el-icon-search"
        clearable
        class="capsule-input"
      >
      </el-input>
    </div>
    
    <div class="filter-actions">
      <div class="category-wrapper">
        <GlassDropdown @command="handleCategoryCommand">
          <template slot="trigger" slot-scope="{ isOpen }">
            <div class="filter-dropdown">
              <span>{{ categoryLabel }}</span>
              <i class="el-icon-arrow-down" :class="{ 'rotate-180': isOpen }" />
            </div>
          </template>
          <GlassDropdownItem command="">全部分类</GlassDropdownItem>
          <GlassDropdownItem
            v-for="cat in categories"
            :key="cat.categoryId"
            :command="String(cat.categoryId)"
          >{{ cat.categoryName }}</GlassDropdownItem>
        </GlassDropdown>
      </div>

      <div class="sort-wrapper">
        <GlassDropdown @command="handleSortCommand">
          <template slot="trigger" slot-scope="{ isOpen }">
            <div class="filter-dropdown">
              <span>{{ sortLabel }}</span>
              <i class="el-icon-arrow-down" :class="{ 'rotate-180': isOpen }" />
            </div>
          </template>
          <GlassDropdownItem command="default">默认排序</GlassDropdownItem>
          <GlassDropdownItem command="time_desc">最新发布</GlassDropdownItem>
          <GlassDropdownItem command="views_desc">浏览量最多</GlassDropdownItem>
          <GlassDropdownItem command="favorites_desc">收藏量最多</GlassDropdownItem>
          <GlassDropdownItem command="likes_desc">喜欢量最多</GlassDropdownItem>
        </GlassDropdown>
      </div>
    </div>
  </div>
</template>

<script>
import GlassDropdown from '@/components/ui/GlassDropdown.vue';
import GlassDropdownItem from '@/components/ui/GlassDropdownItem.vue';
import { SORT_LABELS } from '@/utils/constants';
import { isSuccess } from '@/composables/useApi';

export default {
  name: 'FilterBar',
  components: {
    GlassDropdown,
    GlassDropdownItem
  },
  props: {
    searchQuery: {
      type: String,
      default: ''
    },
    sortBy: {
      type: String,
      default: 'default'
    },
    filterCategory: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '搜索商品名称/ID/描述/卖家...'
    }
  },
  data() {
    return {
      categories: []
    };
  },
  computed: {
    sortLabel() {
      return SORT_LABELS[this.sortBy] || SORT_LABELS.default;
    },
    categoryLabel() {
      if (!this.filterCategory) return '全部分类';
      const cat = this.categories.find(c => String(c.categoryId) === this.filterCategory);
      return cat ? cat.categoryName : '全部分类';
    }
  },
  created() {
    this.fetchCategories();
  },
  methods: {
    handleSortCommand(command) {
      this.$emit('update:sortBy', command);
    },
    handleCategoryCommand(command) {
      this.$emit('update:filterCategory', command);
    },
    async fetchCategories() {
      try {
        const res = await this.$axios.get('/api/categories/active');
        if (isSuccess(res)) {
          this.categories = res.data || [];
        }
      } catch (e) {
        console.error('获取分类失败:', e);
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/components/filter-bar.scss';

.filter-bar {
  .search-wrapper {
    max-width: 400px;
  }

  .actions-wrapper {
    margin-left: 16px;

    .glass-button-primary {
      @include glass-button;
      background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.9), rgba(var(--color-primary-rgb), 0.7));
      border: 1px solid rgba(var(--color-primary-rgb), 0.3);
      color: #fff;
      font-weight: 600;
      border-radius: 12px;
      padding: 10px 24px;
      height: 46px;
      box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);

      &:hover {
        background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 1), rgba(var(--color-primary-rgb), 0.8));
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(var(--color-primary-rgb), 0.4);
      }

      &:active {
        transform: translateY(0);
      }

      i {
        font-weight: bold;
        margin-right: 4px;
      }
    }
  }

  .filter-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}
</style>
