<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">让闲置重获价值</h1>
        <p class="hero-subtitle">校园绿色换物平台，发现身边的宝藏</p>
        
        <div class="search-container">
          <div class="search-bar">
            <i class="el-icon-search search-icon"></i>
            <input 
              v-model="searchQuery" 
              type="text" 
              placeholder="搜索你想要的物品，例如：iPhone、考研资料..." 
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>
          <div class="hot-keywords">
            <span>热门搜索：</span>
            <a href="#" @click.prevent="quickSearch('考研资料')">考研资料</a>
            <a href="#" @click.prevent="quickSearch('自行车')">自行车</a>
            <a href="#" @click.prevent="quickSearch('显示器')">显示器</a>
            <a href="#" @click.prevent="quickSearch('护肤品')">护肤品</a>
          </div>
        </div>
      </div>
      
    </section>

    <!-- 为你推荐 -->
    <section class="latest-section">
      <div class="section-header">
        <h2 class="section-title">智能推荐</h2>
        <nuxt-link to="/goods" class="view-all">发现更多 <i class="el-icon-arrow-right"></i></nuxt-link>
      </div>
      
      <div class="goods-grid">
        <div 
          v-for="item in latestGoods" 
          :key="item.good_id" 
          class="goods-card"
          @click="goToDetail(item.good_id)"
        >
          <div class="goods-image">
            <img 
              :src="getGoodImage(item)" 
              :alt="item.good_name"
              @error="handleImageError"
            />
            <div class="goods-tag" v-if="item.badge">{{ item.badge }}</div>
          </div>
          <div class="goods-info">
            <h3 class="goods-title">{{ item.good_name }}</h3>
            <div class="condition-info">
              <span class="condition-label">新旧：</span>
              <span class="condition-text">{{ getConditionText(item.condition_level) }}</span>
            </div>
            <div class="goods-footer">
              <div class="user-info">
                <img :src="item.seller.avatar" class="user-avatar" />
                <span class="user-name">{{ item.seller.name }}</span>
              </div>
              <span class="publish-time">{{ item.publishTime }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 平台统计 (简化版) -->
  </div>
</template>

<script>
import { GOOD_CATEGORIES, CONDITION_LEVEL_TEXT } from '@/utils/constants'
import { getGoodImage, handleImageError } from '@/utils/imageHelper'

export default {
  name: 'HomePage',
  data() {
    return {
      searchQuery: '',
      categories: GOOD_CATEGORIES.filter(c => c.value !== ''), // fallback，API加载后会替换
      latestGoods: [],
      loading: false
    }
  },
  mounted() {
    this.fetchLatestGoods();
    this.fetchCategories();
  },
  methods: {
    async fetchCategories() {
      try {
        const res = await this.$axios.get('/categories')
        if (res.code === 20000 && Array.isArray(res.data) && res.data.length > 0) {
          // 过滤启用的，按sortOrder排序，映射成和constants一致的格式
          this.categories = res.data
            .filter(c => c.isActive === 1)
            .sort((a, b) => a.sortOrder - b.sortOrder)
            .map(c => ({
              value: c.categoryId,
              label: c.categoryName,
              code: c.categoryCode,
              // 从constants里找对应的icon和color，找不到用默认值
              icon: GOOD_CATEGORIES.find(g => g.code === c.categoryCode)?.icon || 'el-icon-goods',
              color: GOOD_CATEGORIES.find(g => g.code === c.categoryCode)?.color || '#909399'
            }))
        }
      } catch (e) {
        // 静默失败，保留constants里的静态数据
      }
    },
    async fetchLatestGoods() {
      this.loading = true;
      try {
        const response = await this.$axios.get('/goods', {
          params: {
            page: 1,
            size: 4
          }
        });
        
        if (response.code === 20000 && response.data) {
          // 后端返回格式：{ list: [], total: 0, page: 1, limit: 100 }
          const goodsArray = response.data.list || response.data;
          // 只显示已上架、审核通过、未完成交易的商品
          this.latestGoods = goodsArray
            .filter(item => {
              const shelfStatus = item.shelfStatus !== undefined ? item.shelfStatus : item.shelf_status;
              const auditStatus = item.status;
              return shelfStatus === 1 && auditStatus === 1;
            })
            .slice(0, 8)
            .map(item => ({
            ...item,
            // 兼容驼峰和下划线命名
            good_id: item.goodId || item.good_id,
            good_name: item.goodName || item.good_name,
            category_id: item.categoryId || item.category_id,
            condition_level: item.conditionLevel || item.condition_level,
            view_count: item.viewCount !== undefined ? item.viewCount : item.view_count,
            created_at: item.createdAt || item.created_at,
            badge: this.getBadge(item),
            location: item.location || '校园',
            publishTime: this.formatTime(item.createdAt || item.created_at),
            seller: item.seller ? {
              name: item.seller.username,
              avatar: item.seller.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
              school: item.seller.school || '未知学校'
            } : {
              name: '匿名用户',
              avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
              school: '未知学校'
            }
          }));
          console.log('✅ 首页商品加载成功:', this.latestGoods.length, '件');
        }
      } catch (error) {
        console.error('❌ 获取推荐商品失败:', error);
        // 静默失败，不影响用户体验
      } finally {
        this.loading = false;
      }
    },
    getBadge(item) {
      if (item.condition_level === 1) return '新品';
      if (item.view_count > 200) return '热门';
      return '推荐';
    },
    formatTime(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      const now = new Date();
      const diff = now - date;
      const hours = Math.floor(diff / (1000 * 60 * 60));
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      
      if (hours < 1) return '刚刚';
      if (hours < 24) return `${hours}小时前`;
      if (days < 7) return `${days}天前`;
      return `${Math.floor(days / 7)}周前`;
    },
    handleSearch() {
      if (this.searchQuery.trim()) {
        this.$router.push({ path: '/goods', query: { q: this.searchQuery } });
      }
    },
    quickSearch(keyword) {
      this.searchQuery = keyword;
      this.handleSearch();
    },
    goToCategory(cat) {
      this.$router.push({ path: '/goods', query: { category: cat } });
    },
    goToDetail(id) {
      if (!id) {
        console.error('商品ID不存在');
        this.$message.error('商品ID无效');
        return;
      }
      this.$router.push(`/goods/${id}`);
    },
    
    // 图片处理方法
    getGoodImage(good) {
      return getGoodImage(good)
    },
    
    handleImageError(event) {
      handleImageError(event)
    },
    
    getConditionText(level) {
      return CONDITION_LEVEL_TEXT[level] || '未知'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.home-page {
  padding-bottom: 80px;
}

// Hero Section
.hero-section {
  position: relative;
  padding: 80px 0 100px;
  text-align: center;
  overflow: hidden;
  
  .hero-content {
    position: relative;
    z-index: 2;
    max-width: 800px;
    margin: 0 auto;
  }
  
  .hero-title {
    font-size: 48px;
    font-weight: 800;
    color: $color-text-primary;
    margin-bottom: 16px;
    letter-spacing: -1px;
    
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary-info) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  .hero-subtitle {
    font-size: 18px;
    color: $color-text-secondary;
    margin-bottom: 48px;
  }
}

.search-container {
  max-width: 640px;
  margin: 0 auto;
  
  .search-bar {
    @include glass-card;
    display: flex;
    align-items: center;
    padding: 6px 6px 6px 20px;
    border-radius: 32px;
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
    
    .search-icon {
      font-size: 20px;
      color: $color-text-disabled;
      margin-right: 12px;
    }
    
    input {
      flex: 1;
      border: none;
      background: transparent;
      height: 44px;
      font-size: 16px;
      color: $color-text-primary;
      outline: none;
      
      &::placeholder {
        color: $color-text-disabled;
      }
    }
    
    .search-btn {
      background: $color-primary;
      color: white;
      border: none;
      padding: 0 28px;
      height: 44px;
      border-radius: 22px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        background: var(--color-primary);
        filter: brightness(0.9);
        transform: scale(1.02);
      }
    }
  }
  
  .hot-keywords {
    margin-top: 16px;
    font-size: 13px;
    color: $color-text-secondary;
    
    a {
      color: $color-primary;
      text-decoration: none;
      margin-right: 12px;
      &:hover { text-decoration: underline; }
    }
  }
}

// Common Section Styles
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .section-title {
    font-size: 24px;
    font-weight: 700;
    color: $color-text-primary;
  }
  
  .view-all {
    font-size: 14px;
    color: $color-primary;
    text-decoration: none;
    font-weight: 500;
    
    &:hover { opacity: 0.8; }
  }
}

// Category Grid
.category-section {
  margin-bottom: 60px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
}

.category-card {
  @include glass-card;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    background: rgba(var(--color-primary-rgb), 0.05);
    border-color: $color-primary;
  }
  
  .cat-icon {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 12px;
    color: white;
    font-size: 24px;
  }
  
  .cat-name {
    font-size: 15px;
    font-weight: 600;
    color: $color-text-primary;
  }
}

// Goods Grid
.latest-section {
  margin-bottom: 60px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.goods-card {
  @include glass-card;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
  }
  
  .goods-image {
    position: relative;
    height: 180px;
    img { width: 100%; height: 100%; object-fit: cover; }
    .goods-tag {
      position: absolute;
      top: 12px;
      left: 12px;
      background: $color-primary;
      color: white;
      padding: 4px 10px;
      border-radius: 8px;
      font-size: 12px;
      font-weight: 600;
    }
  }
  
  .goods-info {
    padding: 16px;
    
    .goods-title {
      font-size: 16px;
      font-weight: 600;
      color: $color-text-primary;
      margin-bottom: 8px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .condition-info {
      font-size: 13px;
      margin-bottom: 16px;
      .condition-label { color: $color-text-disabled; }
      .condition-text { color: $color-primary; font-weight: 500; }
    }
    
    .goods-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 12px;
      border-top: 1px solid rgba($color-border, 0.05);
      
      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        .user-avatar { width: 24px; height: 24px; border-radius: 50%; }
        .user-name { font-size: 12px; color: $color-text-secondary; }
      }
      
      .publish-time { font-size: 11px; color: $color-text-disabled; }
    }
  }
}

@media (max-width: 768px) {
  .hero-title { font-size: 32px; }
  .category-grid { grid-template-columns: repeat(3, 1fr); }
}
</style>
