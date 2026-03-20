<template>
  <div class="goods-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
           <h1>换物大厅</h1>
          <p>在这里，您可以探索全校范围内的闲置物品，开启绿色生活</p>
        </div>
      </div>
    </div>

    <!-- 分类导航卡片 -->
    <div class="category-nav-scroll">
      <div class="category-list">
        <div 
          v-for="cat in GOOD_CATEGORIES" 
          :key="cat.value"
          class="category-card-item"
          :class="{ active: filters.category === cat.value }"
          @click="filters.category = cat.value"
        >
          <div class="cat-icon-wrapper" :style="{ '--icon-color': cat.color }">
            <i :class="cat.icon"></i>
          </div>
          <span class="cat-label">{{ cat.label }}</span>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <FilterBar 
      :search-query.sync="searchQuery"
      :filters="filters"
      :view-mode.sync="viewMode"
      @update:category="filters.category = $event"
      @update:sort="filters.sort = $event"
    />

    <!-- 物品列表 -->
    <div v-if="loading" class="loading-container">
      <i class="el-icon-loading"></i>
      <p>加载中...</p>
    </div>
    
    <div v-else-if="paginatedGoods.length > 0" :class="['goods-container', `view-${viewMode}`]">
      <div 
        v-for="item in paginatedGoods" 
        :key="item.id"
        class="goods-card"
        @click="goToDetail(item)"
      >
        <div class="goods-image">
          <img 
            :src="getGoodImage(item)" 
            :alt="item.good_name"
            @error="handleImageError"
          />
          <div class="goods-badge" v-if="item.badge">
            {{ item.badge }}
          </div>
          <div class="goods-status" :class="getShelfStatusClass(item)">
            {{ getShelfStatusText(item) }}
          </div>
        </div>
        
        <div class="goods-info">
          <div class="goods-header">
            <h3 class="goods-title">{{ item.good_name }}</h3>
            <div class="goods-actions">
              <el-button type="text" icon="el-icon-view" @click.stop="handlePreview(item)">预览</el-button>
              <el-button type="text" icon="el-icon-chat-dot-round" @click.stop="handleContact(item)">联系</el-button>
            </div>
          </div>
          
          <p class="goods-desc">{{ item.description }}</p>
          
          <div class="goods-meta">
            <div class="condition-info">
              <div class="condition-label">新旧程度：</div>
              <div class="condition-value">
                <span class="condition-badge">
                  {{ getConditionText(item.condition_level) }}
                </span>
              </div>
            </div>
            
            <div class="exchange-info">
              <div class="user-info">
                <img :src="item.seller.avatar" :alt="item.seller.name" class="user-avatar" />
                <span class="user-name">{{ item.seller.name }}</span>
              </div>
              <div class="location-time">
                <div class="location">
                  <i class="el-icon-location"></i>
                  {{ item.location }}
                </div>
                <div class="time">{{ item.publishTime }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="filteredGoods.length > 0" class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-sizes="[9, 18, 27, 36]"
        :page-size="pageSize"
        :total="filteredGoods.length"
        :pager-count="5"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        class="glass-pagination"
      />
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading" class="empty-state">
      <div class="empty-icon">
        <i class="el-icon-box"></i>
      </div>
      <div class="empty-text">
        <h3>暂无符合条件的物品</h3>
        <p>试试调整筛选条件或搜索其他关键词</p>
      </div>
      <el-button type="primary" @click="handleClearFilters">清除筛选条件</el-button>
    </div>
  </div>
</template>

<script>
import FilterBar from '@/components/goods/FilterBar.vue'
import { GOOD_SHELF_STATUS, GOOD_SHELF_STATUS_TEXT, GOOD_CATEGORIES, CONDITION_LEVEL_TEXT } from '@/utils/constants'
import { GoodsFilter, GoodsSorter } from '@/utils/goodsManager'
import { MessageHelper } from '@/utils/messageHelper'
import { getGoodImage, handleImageError } from '@/utils/imageHelper'

export default {
  name: 'GoodsPage',
  components: {
    FilterBar
  },
  data() {
    return {
      searchQuery: '',
      viewMode: 'grid', // 'grid' or 'list'
      filters: {
        category: '',
        sort: 'newest'
      },
      currentPage: 1,
      pageSize: 9,
      goodsList: [],
      loading: false,
      GOOD_CATEGORIES,
      GOOD_SHELF_STATUS,
      CONDITION_LEVEL_TEXT
    }
  },
  computed: {
    filteredGoods() {
      const filtered = GoodsFilter.filter(this.goodsList, {
        query: this.searchQuery,
        category: this.filters.category
      });
      return GoodsSorter.sort(filtered, this.filters.sort);
    },
    paginatedGoods() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredGoods.slice(start, end);
    }
  },
  mounted() {
    this.handleInitialQueries();
    this.fetchGoods();
  },
  methods: {
    async fetchGoods() {
      this.loading = true;
      try {
        const params = {}
        if (this.filters.sort === 'popular') params.sort = 'view_count'
        const response = await this.$axios.get('/goods', { params });
        if (response.code === 20000 && response.data) {
          // 后端返回格式：{ list: [], total: 0, page: 1, limit: 100 }
          const goodsArray = response.data.list || response.data;
          // 转换后端数据格式为前端需要的格式
          // 注意：后端返回驼峰命名（goodId, goodName）
          this.goodsList = goodsArray
            .filter(item => {
              const shelfStatus = item.shelfStatus !== undefined ? item.shelfStatus : item.shelf_status;
              const auditStatus = item.status;
              // 只显示：已上架(1) + 审核通过(1)，shelf_status=2 是已售出也不显示
              return shelfStatus === 1 && auditStatus === 1;
            })
            .map(item => ({
            ...item,
            // 兼容驼峰和下划线命名
            good_id: item.goodId || item.good_id,
            good_name: item.goodName || item.good_name,
            category_id: item.categoryId || item.category_id,
            condition_level: item.conditionLevel ?? item.condition_level ?? 3,
            shelf_status: item.shelfStatus !== undefined ? item.shelfStatus : item.shelf_status,
            view_count: item.viewCount !== undefined ? item.viewCount : item.view_count,
            like_count: item.likeCount !== undefined ? item.likeCount : item.like_count,
            favorite_count: item.favoriteCount !== undefined ? item.favoriteCount : item.favorite_count,
            created_at: item.createdAt || item.created_at,
            // 添加前端展示需要的字段
            badge: this.getBadge(item),
            location: item.location || '校园',
            publishTime: this.formatTime(item.createdAt || item.created_at),
            isToday: this.isToday(item.createdAt || item.created_at),
            seller: item.seller ? {
              name: item.seller.username,
              avatar: item.seller.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
              school: item.seller.school || '未知学校',
              credit_score: item.seller.creditScore || item.seller.credit_score || 0
            } : {
              name: '匿名用户',
              avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
              school: '未知学校',
              credit_score: 0
            }
          }));
          console.log('✅ 商品列表加载成功:', this.goodsList.length, '件商品');
        }
      } catch (error) {
        console.error('❌ 获取商品列表失败:', error);
        MessageHelper.error(this, '获取商品列表失败');
      } finally {
        this.loading = false;
      }
    },
    handleInitialQueries() {
      const { q, category } = this.$route.query;
      if (q) this.searchQuery = q;
      if (category) this.filters.category = category;
    },
    getBadge(item) {
      // 根据商品属性生成标签
      if (item.condition_level === 1) return '新品';
      if (item.view_count > 200) return '热门';
      if (this.isToday(item.created_at)) return '推荐';
      return '';
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
      if (days < 30) return `${Math.floor(days / 7)}周前`;
      return `${Math.floor(days / 30)}个月前`;
    },
    isToday(dateStr) {
      if (!dateStr) return false;
      const date = new Date(dateStr);
      const now = new Date();
      return date.toDateString() === now.toDateString();
    },
    goToDetail(item) {
      const goodId = item.good_id || item.goodId || item.id;
      if (!goodId) {
        console.error('商品ID不存在:', item);
        this.$message.error('商品ID无效');
        return;
      }
      this.$router.push(`/goods/${goodId}`);
    },
    
    handlePageChange(page) {
      this.currentPage = page;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
    },
    
    handlePreview(item) {
      MessageHelper.info(this, `正在打开物品预览: ${item.good_name}`);
    },
    
    handleContact(item) {
      MessageHelper.success(this, `正在联系卖家: ${item.seller.name}`);
    },
    
    handleClearFilters() {
      this.searchQuery = '';
      this.filters = {
        category: '',
        sort: 'newest'
      };
      this.currentPage = 1;
      MessageHelper.success(this, '筛选条件已清除');
    },
    
    getShelfStatusText(item) {
      const shelf = item.shelf_status !== undefined ? item.shelf_status : item.shelfStatus;
      if (shelf === 1) return '在售';
      if (shelf === 2) return '已售出';
      return '已下架';
    },
    
    getShelfStatusClass(item) {
      const shelf = item.shelf_status !== undefined ? item.shelf_status : item.shelfStatus;
      if (shelf === 1) return 'on-shelf';
      return 'off-shelf';
    },
    
    getConditionText(level) {
      return CONDITION_LEVEL_TEXT[level] || CONDITION_LEVEL_TEXT[3]
    },
    
    // 图片处理方法
    getGoodImage(good) {
      return getGoodImage(good)
    },
    
    handleImageError(event) {
      handleImageError(event)
    }
  },
  
  watch: {
    'filters.category'() { this.currentPage = 1; },
    'filters.sort'() { this.currentPage = 1; this.fetchGoods(); },
    searchQuery() { this.currentPage = 1; }
  },
  
  head() {
    return {
      title: '换物大厅 - 校园换物平台'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.goods-page {
  padding: 20px;
  min-height: 100vh;
  color: $color-text-primary;
}

// 页面头部
.page-header {
  margin-bottom: 32px;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 32px;
    
    .header-text {
      h1 {
        font-size: 32px;
        font-weight: $font-weight-bold;
        color: $color-text-primary;
        margin-bottom: 8px;
      }
      
      p {
        font-size: 16px;
        color: $color-text-secondary;
        margin: 0;
      }
    }
    
    .header-extra {
      flex-shrink: 0;
    }
  }
}

// 分类导航
.category-nav-scroll {
  margin-bottom: 24px;
  overflow-x: auto;
  padding-bottom: 8px; // 滚动条空间
  
  // 隐藏滚动条但保留功能
  &::-webkit-scrollbar {
    height: 4px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(var(--color-primary-rgb), 0.2);
    border-radius: 2px;
  }

  .category-list {
    display: flex;
    gap: 16px;
    min-width: min-content;
    
    .category-card-item {
      @include glass-card;
      flex: 0 0 120px; // 固定宽度
      height: 120px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s ease;
      background: var(--color-pill-bg); // 深色背景
      border: 1px solid transparent;
      
      .cat-icon-wrapper {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12px;
        background: var(--icon-color); // 使用定义的颜色
        color: white;
        font-size: 24px;
        transition: all 0.3s ease;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
      }
      
      .cat-label {
        font-size: 14px;
        font-weight: 600;
        color: var(--color-text-primary);
        transition: color 0.3s ease;
      }
      
      &:hover {
        background: var(--color-bg-surface);
      }
      
      &.active {
        border-color: var(--color-primary);
        box-shadow: 0 0 0 1px var(--color-primary), 0 8px 24px rgba(var(--color-primary-rgb), 0.2);
        background: rgba(var(--color-primary-rgb), 0.1);
        
        .cat-label {
          color: var(--color-primary);
        }
      }
    }
  }
}

// 物品容器
.goods-container {
  margin-bottom: 32px;
  
  &.view-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 24px;
  }
  
  &.view-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
    
    .goods-card {
      display: flex;
      flex-direction: row;
      height: 160px;
      
      .goods-image {
        width: 200px;
        flex-shrink: 0;
      }
      
      .goods-info {
        flex: 1;
        padding: 20px;
        
        .goods-header {
          flex-direction: row;
          justify-content: space-between;
          align-items: flex-start;
        }
      }
    }
  }
}

// 物品卡片
.goods-card {
  @include glass-card;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
  }
  
  .goods-image {
    position: relative;
    height: 200px;
    overflow: hidden;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
    
    .goods-badge {
      position: absolute;
      top: 12px;
      left: 12px;
      background: $color-primary;
      color: white;
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: $font-weight-medium;
      z-index: 2;
    }
    
    .goods-status {
      position: absolute;
      top: 12px;
      right: 12px;
      padding: 4px 8px;
      border-radius: 8px;
      font-size: 11px;
      font-weight: $font-weight-medium;
      backdrop-filter: blur(8px);
      
      &.on-shelf {
        background: rgba(var(--color-secondary-success-rgb), 0.9);
        color: white;
      }
      
      &.off-shelf {
        background: rgba(var(--color-text-disabled-rgb), 0.9);
        color: white;
      }
    }
  }
  
  &:hover .goods-image img {
    transform: scale(1.05);
  }
  
  .goods-info {
    padding: 20px;
    
    .goods-header {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-bottom: 12px;
      
      .goods-title {
        font-size: 16px;
        font-weight: $font-weight-semibold;
        color: $color-text-primary;
        margin: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .goods-actions {
        display: flex;
        gap: 8px;
        opacity: 0;
        transition: opacity 0.3s ease;
        
        .el-button {
          padding: 4px 8px;
          font-size: 12px;
        }
      }
    }
    
    .goods-desc {
      font-size: 14px;
      color: $color-text-secondary;
      margin-bottom: 16px;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .goods-meta {
      display: flex;
      flex-direction: column;
      gap: 12px;
      
      .condition-info {
        .condition-label {
          font-size: 12px;
          color: $color-text-secondary;
          margin-bottom: 6px;
        }
        
        .condition-value {
          display: flex;
          flex-wrap: wrap;
          gap: 4px;
          
          .condition-badge {
            background: rgba(var(--color-primary-rgb), 0.1);
            color: $color-primary;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: $font-weight-medium;
          }
        }
      }
      
      .exchange-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .user-info {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .user-avatar {
            width: 24px;
            height: 24px;
            border-radius: 50%;
            object-fit: cover;
          }
          
          .user-name {
            font-size: 12px;
            color: $color-text-secondary;
            font-weight: $font-weight-medium;
          }
        }
        
        .location-time {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          gap: 2px;
          
          .location {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;
            color: $color-text-secondary;
            
            i {
              font-size: 12px;
            }
          }
          
          .time {
            font-size: 11px;
            color: $color-text-disabled;
          }
        }
      }
    }
  }
  
  &:hover .goods-actions {
    opacity: 1;
  }
}

// 分页样式
@import '@/assets/css/components/pagination.scss';

// 加载状态
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  
  i {
    font-size: 48px;
    color: $color-primary;
    margin-bottom: 16px;
  }
  
  p {
    font-size: 16px;
    color: $color-text-secondary;
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  
  .empty-icon {
    font-size: 64px;
    color: $color-text-disabled;
    margin-bottom: 24px;
    
    i {
      font-size: inherit;
    }
  }
  
  .empty-text {
    margin-bottom: 32px;
    
    h3 {
      font-size: 18px;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      margin-bottom: 8px;
    }
    
    p {
      font-size: 14px;
      color: $color-text-secondary;
      margin: 0;
    }
  }
}

// 响应式设计
@media (max-width: $breakpoint-md) {
  .page-header .header-content {
    flex-direction: column;
    align-items: stretch;
    gap: 24px;
  }
}
</style>
