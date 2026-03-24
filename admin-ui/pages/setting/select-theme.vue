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

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <i class="el-icon-loading"></i> 加载主题中...
    </div>

    <!-- 主题网格 -->
    <div v-else class="theme-grid">
      <theme-card
        v-for="theme in paginationData.items"
        :key="theme.themeKey"
        :theme="normalizeTheme(theme)"
        :is-active="currentTheme === theme.themeKey"
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
import { getActiveThemes } from '@/api/theme';
import { updateUserInfo } from '@/api/auth';
import { ThemeManager, ThemeFilter, Paginator } from '@/utils/themeManager.js';
import ThemeCard, { clearVideoQueue } from '@/components/theme/ThemeCard.vue';
import ThemeSearchBar from '@/components/theme/ThemeSearchBar.vue';
import ThemeCategoryTabs from '@/components/theme/ThemeCategoryTabs.vue';
import ThemePagination from '@/components/theme/ThemePagination.vue';

const themeCategories = [
  { id: 'all', name: '全部', icon: '🎨', count: 0 },
  { id: 'dark', name: '深色', icon: '🌙', count: 0 },
  { id: 'light', name: '浅色', icon: '☀️', count: 0 },
  { id: 'tech', name: '科技', icon: '🚀', count: 0 },
  { id: 'nature', name: '自然', icon: '🌿', count: 0 },
  { id: 'luxury', name: '奢华', icon: '💎', count: 0 },
  { id: 'static', name: '静态', icon: '🖼️', count: 0 },
  { id: 'video', name: '动态', icon: '🎬', count: 0 }
];

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
      currentTheme: 'ios-style',
      isTransitioning: false,
      searchQuery: '',
      currentCategory: 'all',
      currentPage: 1,
      pageSize: 24,
      themes: [],
      loading: true
    }
  },
  computed: {
    filteredThemes() {
      return ThemeFilter.filter(this.themes, this.currentCategory, this.searchQuery);
    },
    categoriesWithCounts() {
      return ThemeFilter.updateCategoryCounts(themeCategories, this.themes);
    },
    paginationData() {
      return Paginator.paginate(this.filteredThemes, this.currentPage, this.pageSize);
    }
  },
  watch: {
    filteredThemes() {
      clearVideoQueue()
      this.currentPage = 1;
    }
  },
  mounted() {
    this.currentTheme = ThemeManager.getCurrentTheme();
    this.fetchThemes();
  },
  methods: {
    async fetchThemes() {
      this.loading = true;
      try {
        const res = await getActiveThemes(this.$axios);
        const list = res.data || res;
        this.themes = Array.isArray(list) ? list : (list.data || []);
      } catch (e) {
        console.error('获取主题列表失败', e);
        this.themes = [];
      } finally {
        this.loading = false;
      }
    },

    /**
     * 将后端返回的主题数据格式化为 ThemeCard 需要的格式
     */
    normalizeTheme(theme) {
      let tags = theme.tags;
      if (typeof tags === 'string') {
        try { tags = JSON.parse(tags); } catch (e) { tags = []; }
      }
      let category = theme.category;
      if (typeof category === 'string') {
        try { category = JSON.parse(category); } catch (e) { category = []; }
      }
      return {
        id: theme.themeKey,
        name: theme.name,
        description: theme.description,
        gradient: theme.gradient,
        primaryColor: theme.primaryColor,
        sidebarBg: theme.sidebarBg,
        tags: tags || [],
        category: category || [],
        video: theme.wallpaperType === 'video' ? theme.wallpaperUrl : undefined,
        wallpaper: theme.wallpaperType === 'image' ? theme.wallpaperUrl : undefined
      };
    },

    async handleThemeSelect(themeId, event) {
      if (this.isTransitioning || this.currentTheme === themeId) return;

      this.isTransitioning = true;

      // 找到完整的主题数据
      const themeData = this.themes.find(t => t.themeKey === themeId);
      if (!themeData) return;

      try {
        await ThemeManager.transitionTheme(themeData, event);
        this.currentTheme = themeId;

        // 同步主题偏好到数据库
        try {
          await updateUserInfo(this.$axios, { themePreference: themeId });
        } catch (e) {
          console.warn('主题偏好保存失败，仅本地生效', e);
        }

        this.$root.$emit('show-island-message', `已切换至 ${themeData.name}`, 'success');
      } finally {
        this.isTransitioning = false;
      }
    },

    handlePageChange(page) {
      if (page === '...' || page < 1 || page > this.paginationData.totalPages) return;
      this.currentPage = page;
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

.loading-state {
  text-align: center;
  padding: 60px 0;
  font-size: 16px;
  color: $color-text-secondary;
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
