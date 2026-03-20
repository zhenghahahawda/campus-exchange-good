<template>
  <div class="theme-selector-page">
    <div class="page-header">
      <h2 class="title">主题选择</h2>
      <p class="subtitle">选择你喜欢的主题风格，实时预览效果</p>
    </div>

    <!-- 搜索和筛选栏 -->
    <div class="filter-bar">
      <theme-search-bar
        :value="searchQuery"
        @input="searchQuery = $event"
        @clear="searchQuery = ''"
      />
      
      <theme-category-tabs
        :categories="categoriesWithCounts"
        :current-category="currentCategory"
        @select="currentCategory = $event"
      />
    </div>

    <!-- 主题网格 -->
    <div class="theme-grid">
      <theme-card
        v-for="theme in paginationData.items"
        :key="theme.id"
        :theme="theme"
        :is-active="currentTheme === theme.id"
        @select="handleThemeSelect"
      />
    </div>

    <!-- 分页器 -->
    <theme-pagination
      v-if="paginationData.totalPages > 1"
      :current-page="currentPage"
      :total-pages="paginationData.totalPages"
      @change="handlePageChange"
    />

    <!-- 结果统计 -->
    <div class="result-info">
      共找到 {{ filteredThemes.length }} 个主题，当前显示第 {{ paginationData.startIndex + 1 }}-{{ paginationData.endIndex }} 个
    </div>
  </div>
</template>

<script>
import { themes, themeCategories } from '@/assets/css/themes/themes-config.js';
import { ThemeManager, ThemeFilter, Paginator } from '@/utils/themeManager.js';
import ThemeCard from '@/components/theme/ThemeCard.vue';
import ThemeSearchBar from '@/components/theme/ThemeSearchBar.vue';
import ThemeCategoryTabs from '@/components/theme/ThemeCategoryTabs.vue';
import ThemePagination from '@/components/theme/ThemePagination.vue';

export default {
  name: 'SelectTheme',
  components: {
    ThemeCard,
    ThemeSearchBar,
    ThemeCategoryTabs,
    ThemePagination
  },
  data() {
    return {
      currentTheme: 'default',
      isTransitioning: false,
      searchQuery: '',
      currentCategory: 'all',
      currentPage: 1,
      pageSize: 12,
      themes: Object.freeze([...themes]) // 冻结数组提升性能
    }
  },
  computed: {
    // 筛选后的主题列表
    filteredThemes() {
      return ThemeFilter.filter(this.themes, this.currentCategory, this.searchQuery);
    },
    
    // 分类列表（带计数）
    categoriesWithCounts() {
      return ThemeFilter.updateCategoryCounts(themeCategories, this.themes);
    },
    
    // 分页数据
    paginationData() {
      return Paginator.paginate(this.filteredThemes, this.currentPage, this.pageSize);
    }
  },
  watch: {
    // 筛选条件变化时重置到第一页
    filteredThemes() {
      this.currentPage = 1;
    }
  },
  mounted() {
    // 获取当前主题
    this.currentTheme = ThemeManager.getCurrentTheme();
  },
  methods: {
    // 处理主题选择
    async handleThemeSelect(themeId, event) {
      if (this.isTransitioning || this.currentTheme === themeId) return;
      
      this.isTransitioning = true;
      
      try {
        await ThemeManager.transitionTheme(themeId, event);
        this.currentTheme = themeId;
        
        const theme = this.themes.find(t => t.id === themeId);
        this.$root.$emit('show-island-message', `已切换至 ${theme.name}`, 'success');
      } finally {
        this.isTransitioning = false;
      }
    },
    
    // 处理分页变化
    handlePageChange(page) {
      if (page === '...' || page < 1 || page > this.paginationData.totalPages) return;
      this.currentPage = page;
      
      // 滚动到顶部
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.theme-selector-page {
  padding-bottom: 40px;
}

.page-header {
  margin-bottom: 32px;
  
  .title {
    font-size: 32px;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin-bottom: 8px;
  }
  
  .subtitle {
    font-size: 16px;
    color: $color-text-secondary;
  }
}

.filter-bar {
  margin-bottom: 32px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.result-info {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: $color-text-secondary;
  padding: 12px;
  @include glass-card;
  display: inline-block;
  margin-left: 50%;
  transform: translateX(-50%);
}
</style>
