<template>
  <div class="theme-selector-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <el-button 
          type="text" 
          icon="el-icon-arrow-left" 
          @click="goBack"
          class="back-btn"
        >
          返回主页
        </el-button>
        <div class="header-title">
          <h1>主题工坊</h1>
          <p>个性化你的校园换物体验</p>
        </div>
        <div class="header-stats">
          <div class="stat-item">
            <span class="stat-value">{{ themes.length }}</span>
            <span class="stat-label">总主题</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="theme-content">
      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <div class="search-wrapper">
          <ThemeSearchBar 
            v-model="searchQuery" 
            @clear="searchQuery = ''"
          />
        </div>
        <div class="category-wrapper">
          <ThemeCategoryTabs 
            :categories="categories" 
            :current-category="currentCategory"
            @select="handleCategorySelect"
          />
        </div>
      </div>

      <!-- 当前主题展示 -->
      <div v-if="!searchQuery && currentCategory === 'all' && currentPage === 1" class="current-theme-section">
        <h2 class="section-title">当前应用</h2>
        <div class="current-theme-wrapper">
          <ThemeCard 
            v-if="activeThemeObj"
            :theme="activeThemeObj" 
            :is-active="true"
          />
        </div>
      </div>
      
      <!-- 主题列表 -->
      <div class="themes-section">
        <div class="section-header">
          <h2 class="section-title">
            {{ searchQuery ? '搜索结果' : (currentCategory === 'all' ? '全部主题' : getCategoryName(currentCategory)) }}
            <span class="count-badge">{{ filteredThemes.length }}</span>
          </h2>
        </div>

        <div v-if="paginatedThemes.length > 0" class="themes-grid">
          <ThemeCard 
            v-for="theme in paginatedThemes" 
            :key="theme.id"
            :theme="theme"
            :is-active="currentTheme === theme.id"
            @select="handleThemeSelect"
          />
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">🎨</div>
          <h3>未找到匹配主题</h3>
          <p>尝试换个搜索关键词或分类吧</p>
          <el-button type="primary" @click="resetFilters">重置筛选</el-button>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="pagination-wrapper">
          <ThemePagination 
            :current-page="currentPage" 
            :total-pages="totalPages"
            @change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { themes, themeCategories } from '@/assets/css/themes/themes-config'
import { ThemeManager, ThemeFilter, Paginator } from '@/utils/themeManager'
import { MessageHelper } from '@/utils/messageHelper'
import ThemeCard from '@/components/theme/ThemeCard.vue'
import ThemeCategoryTabs from '@/components/theme/ThemeCategoryTabs.vue'
import ThemePagination from '@/components/theme/ThemePagination.vue'
import ThemeSearchBar from '@/components/theme/ThemeSearchBar.vue'

export default {
  name: 'ThemeSelector',
  components: {
    ThemeCard,
    ThemeCategoryTabs,
    ThemePagination,
    ThemeSearchBar
  },
  data() {
    return {
      themes: Object.freeze([...themes]),
      categories: [...themeCategories],
      currentTheme: 'default',
      currentCategory: 'all',
      searchQuery: '',
      currentPage: 1,
      pageSize: 9,
      isTransitioning: false
    }
  },
  computed: {
    filteredThemes() {
      return ThemeFilter.filter(this.themes, this.currentCategory, this.searchQuery)
    },
    paginatedThemes() {
      const { items } = Paginator.paginate(this.filteredThemes, this.currentPage, this.pageSize)
      return items
    },
    totalPages() {
      return Math.ceil(this.filteredThemes.length / this.pageSize)
    },
    activeThemeObj() {
      return this.themes.find(t => t.id === this.currentTheme) || this.themes[0]
    }
  },
  watch: {
    searchQuery() {
      this.currentPage = 1
      this.updateCounts()
    },
    currentCategory() {
      this.currentPage = 1
    }
  },
  mounted() {
    this.currentTheme = ThemeManager.loadSavedTheme()
    this.updateCounts()
  },
  methods: {
    updateCounts() {
      this.categories = ThemeFilter.updateCategoryCounts(this.categories, this.themes)
    },
    handleCategorySelect(categoryId) {
      this.currentCategory = categoryId
    },
    handleThemeSelect(themeId, event) {
      if (this.isTransitioning || this.currentTheme === themeId) return
      
      this.isTransitioning = true
      ThemeManager.transitionTheme(themeId, event, () => {
        this.currentTheme = themeId
        this.isTransitioning = false
        MessageHelper.success(this, `已切换至 ${this.activeThemeObj.name}`)
      })
    },
    handlePageChange(page) {
      this.currentPage = page
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    handleSearch(query) {
      this.searchQuery = query
    },
    getCategoryName(id) {
      const cat = this.categories.find(c => c.id === id)
      return cat ? cat.name : ''
    },
    resetFilters() {
      this.searchQuery = ''
      this.currentCategory = 'all'
      this.currentPage = 1
    },
    goBack() {
      this.$router.push('/')
    }
  },
  head() {
    return {
      title: '主题工坊 - 校园换物平台'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.theme-selector-page {
  min-height: 100vh;
  padding-bottom: 60px;
  background: transparent;
}

.page-header {
  @include glass-card;
  margin-bottom: 40px;
  padding: 30px 0;
  border-radius: 0 0 32px 32px;
  
  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .back-btn {
      color: $color-text-secondary;
      font-size: 15px;
      font-weight: $font-weight-medium;
      
      &:hover {
        color: $color-primary;
        transform: translateX(-4px);
      }
    }
    
    .header-title {
      text-align: center;
      
      h1 {
        font-size: 28px;
        font-weight: $font-weight-bold;
        color: $color-text-primary;
        margin: 0 0 4px 0;
        letter-spacing: -0.5px;
      }
      
      p {
        font-size: 14px;
        color: $color-text-secondary;
        margin: 0;
      }
    }
    
    .header-stats {
      .stat-item {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        
        .stat-value {
          font-size: 24px;
          font-weight: $font-weight-bold;
          color: $color-primary;
          line-height: 1;
        }
        
        .stat-label {
          font-size: 12px;
          color: $color-text-secondary;
          margin-top: 4px;
        }
      }
    }
  }
}

.theme-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 48px;
  
  .search-wrapper {
    max-width: 600px;
    margin: 0 auto;
    width: 100%;
  }
  
  .category-wrapper {
    display: flex;
    justify-content: center;
  }
}

.section-title {
  font-size: 20px;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  
  .count-badge {
    font-size: 12px;
    background: rgba(var(--color-primary-rgb), 0.1);
    color: $color-primary;
    padding: 2px 8px;
    border-radius: 10px;
  }
}

.current-theme-section {
  margin-bottom: 56px;
  
  .current-theme-wrapper {
    max-width: 400px;
  }
}

.themes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.pagination-wrapper {
  margin-top: 56px;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  
  .empty-icon {
    font-size: 64px;
    margin-bottom: 24px;
  }
  
  h3 {
    font-size: 20px;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 15px;
    color: $color-text-secondary;
    margin-bottom: 24px;
  }
}

// 响应式
@media (max-width: $breakpoint-md) {
  .page-header {
    padding: 20px 0;
    
    .header-content {
      .header-stats {
        display: none;
      }
    }
  }
}

@media (max-width: $breakpoint-sm) {
  .themes-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-section {
    .category-wrapper {
      justify-content: flex-start;
      overflow-x: auto;
      padding-bottom: 8px;
      
      &::-webkit-scrollbar {
        height: 4px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: rgba(var(--color-primary-rgb), 0.2);
        border-radius: 2px;
      }
    }
  }
}
</style>
