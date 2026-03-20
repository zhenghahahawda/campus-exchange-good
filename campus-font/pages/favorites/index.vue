<template>
  <div class="favorites-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h1>我的收藏</h1>
          <p>您收藏的心仪物品</p>
        </div>
      </div>
    </div>

    <!-- 物品列表 -->
    <div v-if="favoriteGoods.length > 0" class="goods-container view-grid">
      <div 
        v-for="item in favoriteGoods" 
        :key="item.id"
        v-if="item"
        class="goods-card"
        @click="goToDetail(item)"
      >
        <div class="goods-image">
          <img 
            :src="getGoodImage(item)" 
            :alt="item.title"
            @error="handleImageError"
          />
          <div class="goods-badge" v-if="item.badge">
            {{ item.badge }}
          </div>
          <div class="goods-status" :class="item.status">
            {{ getStatusText(item.status) }}
          </div>
        </div>
        
        <div class="goods-info">
          <div class="goods-header">
            <h3 class="goods-title">{{ item.title }}</h3>
          </div>
          
          <p class="goods-desc">{{ item.description }}</p>
          
          <div class="goods-meta">
            <div class="wanted-info">
              <div class="wanted-label">想要换取：</div>
              <div class="wanted-items">
                <span v-for="(wantedItem, index) in item.wantedItems" :key="index" class="wanted-item">
                  {{ wantedItem }}
                </span>
              </div>
            </div>
            
            <div class="exchange-info">
              <div class="user-info">
                <img :src="item.seller?.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" :alt="item.seller?.name || '用户'" class="user-avatar" />
                <span class="user-name">{{ item.seller?.name || '匿名用户' }}</span>
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

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">
        <i class="el-icon-star-off"></i>
      </div>
      <div class="empty-text">
        <h3>暂无收藏物品</h3>
        <p>去换物大厅看看吧</p>
      </div>
      <el-button type="primary" @click="$router.push('/goods')">去逛逛</el-button>
    </div>
  </div>
</template>

<script>
import { GOOD_STATUS_TEXT } from '@/utils/constants'
import { getGoodImage, handleImageError } from '@/utils/imageHelper'

export default {
  name: 'FavoritesPage',
  data() {
    return {
      favoriteGoods: [],
      loading: false
    }
  },
  mounted() {
    this.loadFavorites()
  },
  methods: {
    async loadFavorites() {
      this.loading = true;
      try {
        console.log('📦 加载我的收藏...');
        const response = await this.$axios.get('/favorites');
        
        console.log('✅ 收藏响应:', response);
        
        if (response.code === 20000 && response.data) {
          // 后端直接返回商品对象数组，不是嵌套在 good 字段中
          this.favoriteGoods = response.data.map(item => {
            // item 直接就是商品对象
            const shelfStatus = item.shelfStatus !== undefined ? item.shelfStatus : (item.shelf_status !== undefined ? item.shelf_status : 1);
            const createdAt = item.createdAt || item.created_at || new Date().toISOString();
            const images = (() => {
              try {
                if (!item.images) return [];
                return typeof item.images === 'string' ? JSON.parse(item.images) : (Array.isArray(item.images) ? item.images : []);
              } catch {
                return [];
              }
            })();
            
            return {
              id: item.goodId || item.good_id || 0,
              good_id: item.goodId || item.good_id || 0,
              title: item.goodName || item.good_name || '未命名商品',
              good_name: item.goodName || item.good_name || '未命名商品',
              description: item.description || '暂无描述',
              status: shelfStatus === 1 ? 'available' : 'exchanged',
              shelf_status: shelfStatus,
              images: images,
              badge: (item.conditionLevel || item.condition_level) === 1 ? '新品' : '',
              location: '校园',
              publishTime: this.formatTime(createdAt),
              wantedItems: ['任意物品'],
              seller: item.seller ? {
                name: item.seller.username || '匿名用户',
                avatar: item.seller.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
                school: item.seller.school || '未知学校'
              } : {
                name: '匿名用户',
                avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
                school: '未知学校'
              }
            };
          });
          console.log(`✅ 加载了 ${this.favoriteGoods.length} 件收藏`);
          console.log('收藏商品数据:', this.favoriteGoods);
        }
      } catch (error) {
        console.error('❌ 加载收藏失败:', error);
        // 401 由 axios 拦截器统一处理（自动刷新 token 或跳转登录），这里不重复处理
        if (error.response?.status !== 401) {
          this.$message.error('加载失败，请稍后重试');
        }
      } finally {
        this.loading = false;
      }
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
      return date.toLocaleDateString();
    },
    
    goToDetail(item) {
      const goodId = item.good_id || item.goodId || item.id;
      if (!goodId) {
        this.$message.error('商品ID无效');
        return;
      }
      this.$router.push(`/goods/${goodId}`);
    },
    
    getStatusText(status) {
      return GOOD_STATUS_TEXT[status] || '未知';
    },
    
    // 图片处理方法
    getGoodImage(good) {
      return getGoodImage(good)
    },
    
    handleImageError(event) {
      handleImageError(event)
    }
  },
  head() {
    return {
      title: '我的收藏 - 校园换物平台'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.favorites-page {
  padding: 20px;
  min-height: 100vh;
  color: $color-text-primary;
  max-width: 1440px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
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
}

.goods-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

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
      
      &.available { background: rgba(var(--color-secondary-success-rgb), 0.9); color: white; }
      &.exchanging { background: rgba(var(--color-secondary-warning-rgb), 0.9); color: white; }
      &.exchanged { background: rgba(var(--color-text-disabled-rgb), 0.9); color: white; }
    }
  }
  
  &:hover .goods-image img {
    transform: scale(1.05);
  }
  
  .goods-info {
    padding: 20px;
    
    .goods-header {
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
      
      .wanted-info {
        .wanted-label {
          font-size: 12px;
          color: $color-text-secondary;
          margin-bottom: 6px;
        }
        .wanted-items {
          display: flex;
          flex-wrap: wrap;
          gap: 4px;
          .wanted-item {
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
          .user-avatar { width: 24px; height: 24px; border-radius: 50%; object-fit: cover; }
          .user-name { font-size: 12px; color: $color-text-secondary; font-weight: $font-weight-medium; }
        }
        
        .location-time {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          gap: 2px;
          .location {
            display: flex; align-items: center; gap: 4px; font-size: 12px; color: $color-text-secondary;
            i { font-size: 12px; }
          }
          .time { font-size: 11px; color: $color-text-disabled; }
        }
      }
    }
  }
}

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
  }
  
  .empty-text {
    margin-bottom: 32px;
    h3 { font-size: 18px; font-weight: $font-weight-semibold; color: $color-text-primary; margin-bottom: 8px; }
    p { font-size: 14px; color: $color-text-secondary; margin: 0; }
  }
}
</style>
