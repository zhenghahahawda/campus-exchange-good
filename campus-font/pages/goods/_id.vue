<template>
  <div class="goods-detail-page">
    <div v-if="!goods.id" class="loading-container">
      <i class="el-icon-loading"></i>
      <p>加载中...</p>
    </div>
    
    <div v-else class="page-container">
      <!-- 顶部面包屑 -->
      <div class="breadcrumb-nav">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/goods' }">换物大厅</el-breadcrumb-item>
          <el-breadcrumb-item>物品详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="content-layout">
        <!-- 左列：图片 + 卖家信息 -->
        <div class="left-column">
          <ImageGallery :images="goodsImages" :title="goods.title" />
          <SellerInfoCard 
            :seller="goods.seller"
            :is-own-goods="isOwnGoods"
            @view-profile="handleViewProfile"
            @contact-seller="handleContactSeller"
          />
        </div>

        <!-- 右列：物品详情 -->
        <div class="right-column">
          <div class="goods-info-card">
            <div class="header-section">
              <div class="title-row">
                <h1 class="goods-title">{{ goods.title }}</h1>
                <span class="status-badge" :class="goods.status">{{ statusText }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-item"><i class="el-icon-time"></i> {{ goods.publishTime }}</span>
                <span class="meta-item"><i class="el-icon-view"></i> {{ goods.views || 0 }}次浏览</span>
                <span class="meta-item"><i class="el-icon-star-off"></i> {{ goods.favoriteCount || 0 }}收藏</span>
                <span class="meta-item"><i class="el-icon-caret-top"></i> {{ goods.likeCount || 0 }}点赞</span>
              </div>
            </div>

            <div class="exchange-section">
              <div class="wanted-box">
                <span class="label">期望交换</span>
                <div class="wanted-items">
                  <span 
                    v-for="(item, idx) in goods.wantedItems" 
                    :key="idx" 
                    class="wanted-tag"
                    :class="{ 'is-primary': idx === 0, 'is-text': idx > 0 }"
                  >{{ item }}</span>
                </div>
              </div>
              <div class="description-box">
                <span class="label">物品描述</span>
                <p class="desc-text">{{ goods.description }}</p>
              </div>
            </div>

            <div class="action-section">
              <el-button 
                type="primary" 
                class="main-btn" 
                icon="el-icon-s-promotion" 
                @click="handleExchange"
                :disabled="goods.shelf_status !== 1 || goods.status !== 1"
              >发起交换申请</el-button>
              <el-button 
                class="favorite-btn"
                :class="{ 'is-active': isFavorited }"
                :icon="isFavorited ? 'el-icon-star-on' : 'el-icon-star-off'"
                @click="handleFavorite"
              >{{ isFavorited ? '已收藏' : '收藏' }}</el-button>
              <el-button
                v-if="!isOwnGoods"
                class="report-btn"
                icon="el-icon-warning"
                @click="showReportDialog = true"
              >举报</el-button>
              <el-button
                class="like-btn"
                :class="{ 'is-liked': isLiked }"
                :icon="isLiked ? 'el-icon-caret-top' : 'el-icon-caret-top'"
                @click="handleLike"
              >{{ isLiked ? '已点赞' : '点赞' }}</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 举报弹窗 -->
    <ReportDialog
      :visible.sync="showReportDialog"
      target-type="product"
      :target-id="goods.id"
    />
  </div>
</template>

<script>
import { GOOD_STATUS_TEXT } from '@/utils/constants'
import { MessageHelper } from '@/utils/messageHelper'
import ImageGallery from '@/components/goods/detail/ImageGallery.vue'
import SellerInfoCard from '@/components/goods/detail/SellerInfoCard.vue'
import ReportDialog from '@/components/violation/ReportDialog.vue'

export default {
  name: 'GoodsDetailPage',
  components: {
    ImageGallery,
    SellerInfoCard,
    ReportDialog
  },
  // 移除 asyncData，改为在 mounted 中加载
  // asyncData 在服务端渲染时可能因为没有 token 而失败
  data() {
    return {
      goods: {
        id: null,
        title: '',
        description: '',
        status: 0, // 数字：0=待审核，1=已通过，2=已拒绝
        shelf_status: 0, // 数字：0=已下架，1=已上架
        images: [],
        publishTime: '',
        views: 0,
        wantedItems: [],
        seller: {
          name: '',
          avatar: '',
          school: '',
          credit_score: 0
        }
      },
      isFavorited: false,
      isLiked: false,
      showReportDialog: false
    }
  },
  computed: {
    goodsImages() {
      // 如果没有多图，就用单图重复模拟演示
      return this.goods.images && this.goods.images.length > 0 
        ? this.goods.images 
        : [this.goods.image || 'https://via.placeholder.com/400']
    },
    statusText() {
      if (!this.goods) return '未知状态';
      
      // 根据status和shelfStatus判断
      if (this.goods.status === 1 && this.goods.shelf_status === 1) {
        return '已上架';
      } else if (this.goods.status === 0) {
        return '待审核';
      } else if (this.goods.status === 2) {
        return '已拒绝';
      } else if (this.goods.shelf_status === 0) {
        return '已下架';
      }
      return '未知状态';
    },
    isOwnGoods() {
      // 判断是否是自己的商品
      try {
        const userInfo = localStorage.getItem('userInfo');
        if (!userInfo) return false;
        
        const user = JSON.parse(userInfo);
        const currentUserId = user.userId || user.id;
        const sellerId = this.goods.seller?.userId;
        
        return currentUserId && sellerId && currentUserId === sellerId;
      } catch (e) {
        console.error('判断商品所有权失败:', e);
        return false;
      }
    }
  },
  mounted() {
    // 总是在mounted中加载数据
    this.loadGoodsDetail();
  },
  methods: {
    async loadGoodsDetail() {
      try {
        const goodId = this.$route.params.id;
        if (!goodId || goodId === 'undefined') {
          this.$router.push('/goods');
          return;
        }
        
        const response = await this.$axios.get(`/goods/${goodId}`);
        
        if (response.code === 20000 && response.data) {
          const item = response.data;
          
          // 转换数据格式
          this.goods = {
            id: item.goodId || item.good_id,
            good_id: item.goodId || item.good_id,
            title: item.goodName || item.good_name,
            good_name: item.goodName || item.good_name,
            description: item.description,
            category_id: item.categoryId || item.category_id,
            condition_level: item.conditionLevel || item.condition_level,
            status: item.status, // 保持原始数字值：0=待审核，1=已通过，2=已拒绝
            shelf_status: item.shelfStatus || item.shelf_status, // 保持原始数字值：0=已下架，1=已上架
            images: typeof item.images === 'string' ? JSON.parse(item.images) : item.images,
            image: (() => {
              const imgs = typeof item.images === 'string' ? JSON.parse(item.images) : item.images;
              return Array.isArray(imgs) && imgs.length > 0 ? imgs[0] : 'https://via.placeholder.com/400';
            })(),
            publishTime: (() => {
              if (!item.createdAt && !item.created_at) return '未知时间';
              const date = new Date(item.createdAt || item.created_at);
              const now = new Date();
              const diff = now - date;
              const hours = Math.floor(diff / (1000 * 60 * 60));
              const days = Math.floor(diff / (1000 * 60 * 60 * 24));
              if (hours < 1) return '刚刚';
              if (hours < 24) return `${hours}小时前`;
              if (days < 7) return `${days}天前`;
              return date.toLocaleDateString();
            })(),
            views: item.viewCount || item.view_count || 0,
            likeCount: item.likeCount || 0,
            favoriteCount: item.favoriteCount || 0,            wantedItems: (() => {
              const ef = item.exchangeFor || item.exchange_for;
              if (ef && ef.trim()) return [ef.trim()];
              return ['任意物品'];
            })(),
            seller: item.seller ? {
              userId: item.seller.userId,
              name: item.seller.username,
              avatar: item.seller.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
              school: item.seller.school || '未知学校',
              credit_score: item.seller.creditScore || item.seller.credit_score || 0
            } : {
              userId: null,
              name: '匿名用户',
              avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
              school: '未知学校',
              credit_score: 0
            }
          };
          
          // 加载完商品后检查收藏状态
          this.checkFavoriteStatus();
          // 从接口读取点赞状态
          this.isLiked = !!item.isLiked;
        } else {
          this.$message.error('商品不存在');
          this.$router.push('/goods');
        }
      } catch (error) {
        console.error('加载商品详情失败:', error);
        this.$message.error('加载失败');
        this.$router.push('/goods');
      }
    },
    async handleExchange() {
      try {
        const goodId = this.goods.good_id || this.goods.id;
        
        // 调用后端API创建交换请求
        const response = await this.$axios.post('/exchanges', {
          goodsId: goodId,
          message: `我想和你交换 ${this.goods.title}`,
          offerItems: [] // 可以让用户选择提供的商品
        });
        
        if (response.code === 20000) {
          MessageHelper.success(this, '交换申请已发送！');
          
          // 可以跳转到"我的换物"页面查看
          setTimeout(() => {
            this.$router.push('/exchanges');
          }, 1000);
        }
      } catch (error) {
        console.error('发起交换失败:', error);
        
        if (error.response?.status === 401) {
          // 401 由 axios 拦截器统一处理
        } else if (error.response?.data?.code === 40001) {
          MessageHelper.error(this, '不能和自己的商品交换');
        } else if (error.response?.data?.message) {
          MessageHelper.error(this, error.response.data.message);
        } else {
          MessageHelper.error(this, '发起交换失败，请稍后重试');
        }
      }
    },
    handleViewProfile() {
      const userId = this.goods.seller?.userId;
      if (userId) {
        this.$router.push(`/user/${userId}`);
      }
    },
    async handleContactSeller() {
      try {
        const sellerId = this.goods.seller?.userId;
        
        console.log('卖家信息:', this.goods.seller);
        console.log('卖家ID:', sellerId);
        
        if (!sellerId) {
          MessageHelper.error(this, '卖家信息不完整');
          return;
        }
        
        // 调用后端API创建或获取会话
        const response = await this.$axios.post('/chat/conversations', {
          participantId: sellerId,
          goodsId: this.goods.good_id || this.goods.id
        });
        
        if (response.code === 20000 && response.data) {
          // 跳转到聊天页面
          this.$router.push(`/chat?conversationId=${response.data.id}`);
        }
      } catch (error) {
        console.error('创建会话失败:', error);
        if (error.response?.status === 401) {
          // 401 由 axios 拦截器统一处理
        } else {
          MessageHelper.error(this, '无法联系卖家，请稍后重试');
        }
      }
    },
    async handleFavorite() {
      try {
        const goodId = this.goods.good_id || this.goods.id;
        
        if (this.isFavorited) {
          // 取消收藏
          await this.$axios.delete(`/favorites/${goodId}`);
          this.isFavorited = false;
          MessageHelper.success(this, '已取消收藏');
        } else {
          // 添加收藏
          await this.$axios.post('/favorites', { goodsId: goodId });
          this.isFavorited = true;
          MessageHelper.success(this, '已添加至我的收藏');
        }
      } catch (error) {
        console.error('收藏操作失败:', error);
        if (error.response?.status === 401) {
          // 401 由 axios 拦截器统一处理
        } else if (error.response?.data?.message) {
          MessageHelper.error(this, error.response.data.message);
        } else {
          MessageHelper.error(this, '操作失败');
        }
      }
    },
    
    async checkFavoriteStatus() {
      // 检查当前商品是否已收藏
      try {
        const response = await this.$axios.get('/favorites');
        if (response.code === 20000 && response.data) {
          const goodId = this.goods.good_id || this.goods.id;
          // 后端直接返回商品对象数组
          this.isFavorited = response.data.some(item => 
            (item.goodId || item.good_id) === goodId
          );
        }
      } catch (error) {
        console.error('检查收藏状态失败:', error);
      }
    },

    async handleLike() {
      const goodId = this.goods.good_id || this.goods.id
      try {
        if (this.isLiked) {
          await this.$axios.delete(`/goods/${goodId}/like`)
          this.isLiked = false
          this.goods.likeCount = Math.max(0, (this.goods.likeCount || 1) - 1)
        } else {
          await this.$axios.post(`/goods/${goodId}/like`)
          this.isLiked = true
          this.goods.likeCount = (this.goods.likeCount || 0) + 1
        }
      } catch (e) {
        if (e.response?.status !== 401) {
          this.$message.warning('操作失败，请稍后重试')
        }
        // 401 由 axios 拦截器统一处理
      }
    }
  },
  head() {
    return {
      title: `${this.goods.title} - 物品详情`
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.goods-detail-page {
  min-height: 100vh;
  padding-bottom: 60px;
}

.page-container {
  max-width: 1440px;
  margin: 0 auto;
}

.breadcrumb-nav {
  margin-bottom: 24px;
  
  ::v-deep .el-breadcrumb__inner {
    color: var(--color-text-secondary);
    font-size: 14px;
    &.is-link:hover { color: var(--color-primary); }
  }
  
  ::v-deep .el-breadcrumb__item:last-child .el-breadcrumb__inner {
    color: var(--color-text-secondary); // 图片中看起来是灰色
    font-weight: normal;
  }
}

.content-layout {
  display: grid;
  grid-template-columns: 480px 1fr;
  gap: 28px;
  align-items: start;
  
  .left-column {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
}

.goods-info-card {
  @include glass-card;
  padding: 28px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  box-shadow: var(--card-shadow);
  
  .header-section {
    padding-bottom: 20px;
    margin-bottom: 20px;
    border-bottom: 1px solid rgba(255,255,255,0.06);
    
    .title-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 12px;
      margin-bottom: 10px;
      
      .goods-title {
        font-size: 24px;
        font-weight: 700;
        color: var(--color-text-primary);
        margin: 0;
        line-height: 1.3;
        flex: 1;
      }
      
      .status-badge {
        font-size: 12px;
        padding: 3px 10px;
        border-radius: 20px;
        font-weight: 600;
        white-space: nowrap;
        flex-shrink: 0;
        background: rgba(var(--color-primary-rgb), 0.1);
        color: var(--color-primary);
        
        &.available { background: rgba(var(--color-secondary-success-rgb), 0.1); color: var(--color-secondary-success); }
        &.exchanged { background: rgba(var(--color-text-disabled-rgb), 0.1); color: var(--color-text-disabled); }
      }
    }
    
    .meta-row {
      display: flex;
      gap: 20px;
      color: var(--color-text-secondary);
      font-size: 13px;
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 5px;
        &.like-item {
          transition: color 0.2s;
          &:hover { color: #409EFF; }
          &.liked { color: #409EFF; }
        }
      }
    }
  }
  
  .exchange-section {
    .wanted-box {
      background: rgba(0, 0, 0, 0.15);
      border-radius: 10px;
      padding: 16px 20px;
      margin-bottom: 20px;
      
      .label {
        display: block;
        font-size: 12px;
        color: var(--color-text-secondary);
        margin-bottom: 10px;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
      
      .wanted-items {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        
        .wanted-tag {
          font-size: 14px;
          font-weight: 600;
          
          &.is-primary {
            color: #fff;
            background: var(--color-primary);
            padding: 6px 16px;
            border-radius: 8px;
          }
          
          &.is-text {
            color: var(--color-primary);
            background: transparent;
          }
        }
      }
    }
    
    .description-box {
      margin-bottom: 28px;
      
      .label {
        display: block;
        font-size: 14px;
        font-weight: 600;
        color: var(--color-text-primary);
        margin-bottom: 10px;
      }
      
      .desc-text {
        font-size: 14px;
        color: var(--color-text-secondary);
        line-height: 1.7;
        white-space: pre-wrap;
      }
    }
  }
  
  .action-section {
    display: flex;
    gap: 12px;
    
    .el-button {
      height: 48px;
      font-size: 15px;
      border-radius: 10px;
    }
    
    .main-btn {
      flex: 1;
      font-weight: 600;
      background: var(--color-primary);
      border: none;
      
      &:hover { filter: brightness(0.9); }
      i { font-size: 16px; margin-right: 6px; }
    }
    
    .favorite-btn {
      width: 110px;
      background: var(--glass-bg);
      border: 1px solid var(--glass-border);
      color: var(--color-text-primary);
      
      &:hover, &.is-active {
        border-color: var(--color-primary);
        color: var(--color-primary);
      }
    }

    .report-btn {
      background: transparent;
      border: 1px solid rgba(245,108,108,0.3);
      color: #F56C6C;
      &:hover { border-color: #F56C6C; background: rgba(245,108,108,0.08); }
    }

    .like-btn {
      background: transparent;
      border: 1px solid rgba(64,158,255,0.3);
      color: rgba(255,255,255,0.6);
      &:hover { border-color: #409EFF; color: #409EFF; background: rgba(64,158,255,0.08); }
      &.is-liked { border-color: #409EFF; color: #409EFF; background: rgba(64,158,255,0.1); }
    }  }
}

// 响应式
@media (max-width: 1100px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .goods-info-card { padding: 20px; }
}
</style>
