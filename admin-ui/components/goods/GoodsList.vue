<template>
  <div class="goods-grid">
    <transition-group name="list-fade" tag="div" class="goods-grid-inner">
      <div 
        v-for="good in goods" 
        :key="good.id" 
        class="good-card"
      >
        <!-- 商品图片区域 -->
        <div class="card-image-wrapper">
          <!-- 图片轮播 -->
          <div class="image-carousel">
            <div 
              v-for="(img, imgIndex) in getGoodImages(good)" 
              :key="imgIndex"
              class="carousel-item"
              :class="{ active: good.currentImageIndex === imgIndex }"
            >
              <img :src="img" :alt="good.name" class="card-image">
            </div>
          </div>
          
          <!-- 轮播指示器 -->
          <div v-if="getGoodImages(good).length > 1" class="carousel-indicators">
            <span 
              v-for="(img, imgIndex) in getGoodImages(good)" 
              :key="imgIndex"
              class="indicator-dot"
              :class="{ active: good.currentImageIndex === imgIndex }"
              @click.stop="changeImage(good, imgIndex)"
            ></span>
          </div>
          
          <!-- 轮播箭头 -->
          <div v-if="getGoodImages(good).length > 1" class="carousel-arrows">
            <div class="arrow arrow-left" @click.stop="prevImage(good)">
              <i class="el-icon-arrow-left"></i>
            </div>
            <div class="arrow arrow-right" @click.stop="nextImage(good)">
              <i class="el-icon-arrow-right"></i>
            </div>
          </div>
          
          <!-- 卖家头像 - 左上角 -->
          <div class="seller-avatar-wrapper" v-if="good.seller">
            <el-avatar :size="40" :src="good.seller.avatar" class="seller-avatar"></el-avatar>
            <span class="seller-name">{{ good.seller.name }}</span>
          </div>
          <div class="seller-avatar-wrapper" v-else>
            <el-avatar :size="40" icon="el-icon-user" class="seller-avatar"></el-avatar>
            <span class="seller-name">未知用户</span>
          </div>
          
          <!-- 更多操作按钮 - 右上角 -->
          <div class="more-dropdown">
            <GlassDropdown @command="handleCommand($event, good)">
              <template slot="trigger" slot-scope="{ isOpen }">
                <div class="more-btn" :class="{ 'is-open': isOpen }">
                  <i class="el-icon-more"></i>
                </div>
              </template>
              <GlassDropdownItem command="edit">
                <i class="el-icon-edit"></i> 编辑详情
              </GlassDropdownItem>
              <GlassDropdownItem command="delete" class="text-danger">
                <i class="el-icon-delete"></i> 删除商品
              </GlassDropdownItem>
            </GlassDropdown>
          </div>
          
          <!-- 状态徽标 - 左下角 -->
          <div class="card-badges">
            <span :class="['glass-badge', getStatusBadgeClass(good.status)]">
              {{ getStatusLabel(good.status) }}
            </span>
            <span class="glass-badge badge-condition">
              {{ getConditionLabel(good.condition) }}
            </span>
          </div>

          <!-- 悬浮操作层 -->
          <div class="card-overlay">
            <el-button circle icon="el-icon-view" @click="$emit('preview', good)"></el-button>
            <template v-if="good.status === 'pending'">
              <el-button circle type="success" icon="el-icon-check" title="通过审核" @click="$emit('approve', good)"></el-button>
              <el-button circle type="danger" icon="el-icon-close" title="驳回审核" @click="$emit('reject', good)"></el-button>
            </template>
            <template v-else>
               <el-button circle type="primary" icon="el-icon-edit" @click="$emit('edit', good)"></el-button>
            </template>
          </div>
        </div>

        <!-- 商品信息区域 -->
        <div class="card-body">
          <div class="title-row">
            <h3 class="good-title" :title="good.name">{{ good.name }}</h3>
            <span class="good-id">#{{ good.id }}</span>
          </div>
          
          <p class="good-desc">{{ good.description }}</p>
          
          <div class="good-meta">
            <!-- 统计数据行 -->
            <div class="meta-row meta-stats">
              <div class="meta-item" title="浏览量">
                <i class="el-icon-view"></i>
                <span>{{ formatViews(good.views) }}</span>
              </div>
              <div class="meta-item meta-likes" title="点赞数">
                <i class="el-icon-star-off"></i>
                <span>{{ formatViews(good.likes) }}</span>
              </div>
              <div class="meta-item meta-favorites" title="收藏数">
                <i class="el-icon-collection-tag"></i>
                <span>{{ formatViews(good.favorites) }}</span>
              </div>
            </div>
          </div>
          
          <div class="good-tags">
            <span class="tag-item">{{ good.category }}</span>
            <span class="tag-item">{{ good.deliveryType === 'express' ? '快递' : '自提' }}</span>
          </div>
        </div>
        
        <!-- 卡片底部审核栏 (仅待审核显示) -->
        <div class="card-footer audit-footer" v-if="good.status === 'pending'">
          <div class="footer-btn-group">
            <button class="glass-btn btn-reject" @click="$emit('reject', good)">
              <i class="el-icon-close"></i>
              <span>驳回</span>
            </button>
            <button class="glass-btn btn-approve" @click="$emit('approve', good)">
              <i class="el-icon-check"></i>
              <span>通过</span>
            </button>
          </div>
        </div>
        
        <!-- 卡片底部普通栏 -->
        <div class="card-footer" v-else>
           <div class="status-actions">
             <!-- 显示交换凭证码 -->
             <div v-if="good.exchangeCode && (good.status === 'active' || good.status === 'off_shelf')" class="exchange-code">
               <i class="el-icon-tickets"></i>
               <span class="code-label">凭证码:</span>
               <span class="code-value">{{ good.exchangeCode }}</span>
             </div>
             
             <span v-else-if="good.status === 'rejected'" class="text-danger text-sm">
               <i class="el-icon-warning-outline"></i> 已驳回: {{ good.rejectReason }}
             </span>
             
             <GlassSwitch
               v-if="good.status === 'active' || good.status === 'off_shelf'"
               v-model="good.isActive"
               active-text="上架"
               inactive-text="下架"
               @change="$emit('toggle-shelf', good)"
             />
             
             <span v-if="good.status === 'sold'" class="text-muted text-sm">
               已售出 · {{ formatTime(good.soldTime) }}
             </span>
           </div>
        </div>
      </div>
    </transition-group>
  </div>
</template>

<script>
import GlassDropdown from '@/components/ui/GlassDropdown.vue'
import GlassDropdownItem from '@/components/ui/GlassDropdownItem.vue'
import GlassSwitch from '@/components/ui/GlassSwitch.vue'
import { CONDITION_OPTIONS } from '@/utils/constants'

export default {
  name: 'GoodsList',
  components: {
    GlassDropdown,
    GlassDropdownItem,
    GlassSwitch
  },
  props: {
    goods: {
      type: Array,
      required: true
    }
  },
  mounted() {
    // 初始化每个商品的当前图片索引
    this.initializeImageIndex();
  },
  watch: {
    // 监听 goods 变化，重新初始化图片索引
    goods: {
      handler() {
        this.initializeImageIndex();
      },
      immediate: false
    }
  },
  methods: {
    // 初始化图片索引
    initializeImageIndex() {
      this.goods.forEach(good => {
        if (!good.hasOwnProperty('currentImageIndex')) {
          this.$set(good, 'currentImageIndex', 0);
        }
      });
    },
    getConditionLabel(condition) {
      const option = CONDITION_OPTIONS.find(opt => opt.value === condition);
      return option ? option.label : condition;
    },
    getGoodImages(good) {
      // 优先使用 images 字段，其次是 image 字段
      let val = good.images || good.image;
      
      if (!val) return [];
      
      // 如果已经是数组，直接返回
      if (Array.isArray(val)) return val;
      
      // 如果是字符串
      if (typeof val === 'string') {
        // 去除可能的空白字符
        val = val.trim();
        
        // 尝试 JSON 解析 (针对 ["url1", "url2"] 格式)
        try {
          if (val.startsWith('[') && val.endsWith(']')) {
            const parsed = JSON.parse(val);
            if (Array.isArray(parsed)) return parsed;
          }
        } catch (e) {
          // 忽略解析错误
        }

        // 尝试逗号分隔 (针对 url1,url2 格式)
        if (val.includes(',')) {
          return val.split(',').map(s => s.trim()).filter(s => s);
        }
        
        // 单张图片 URL
        return [val];
      }
      
      return [];
    },
    changeImage(good, index) {
      this.$set(good, 'currentImageIndex', index);
    },
    prevImage(good) {
      const images = this.getGoodImages(good);
      const currentIndex = good.currentImageIndex || 0;
      const newIndex = currentIndex === 0 ? images.length - 1 : currentIndex - 1;
      this.$set(good, 'currentImageIndex', newIndex);
    },
    nextImage(good) {
      const images = this.getGoodImages(good);
      const currentIndex = good.currentImageIndex || 0;
      const newIndex = currentIndex === images.length - 1 ? 0 : currentIndex + 1;
      this.$set(good, 'currentImageIndex', newIndex);
    },
    formatTime(timestamp) {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      const now = new Date();
      const diff = now - date;
      
      // 小于1小时
      if (diff < 1000 * 60 * 60) {
        return Math.floor(diff / (1000 * 60)) + '分钟前';
      }
      // 小于24小时
      if (diff < 1000 * 60 * 60 * 24) {
        return Math.floor(diff / (1000 * 60 * 60)) + '小时前';
      }
      // 大于24小时显示日期
      return date.toLocaleDateString();
    },
    formatViews(views) {
      if (views >= 10000) {
        return (views / 10000).toFixed(1) + 'w';
      }
      if (views >= 1000) {
        return (views / 1000).toFixed(1) + 'k';
      }
      return views.toString();
    },
    getStatusLabel(status) {
      const map = {
        'pending': '待审核',
        'active': '出售中',
        'sold': '已售出',
        'rejected': '已驳回',
        'off_shelf': '已下架'
      };
      return map[status] || status;
    },
    getStatusBadgeClass(status) {
      const map = {
        'pending': 'badge-warning',
        'active': 'badge-success',
        'sold': 'badge-info',
        'rejected': 'badge-danger',
        'off_shelf': 'badge-default'
      };
      return map[status] || 'badge-default';
    },
    handleCommand(command, good) {
      if (command === 'edit') this.$emit('edit', good);
      if (command === 'preview') this.$emit('preview', good);
      if (command === 'delete') this.$emit('delete', good);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.goods-grid-inner {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

// 商品卡片
.good-card {
  @include glass-card;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.08);
  
  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 15px 40px rgba(0,0,0,0.15);
    
    .card-image {
      transform: scale(1.05);
    }
    
    .card-overlay {
      opacity: 1;
    }
  }
  
  // 图片区域
  .card-image-wrapper {
    position: relative;
    height: 240px;
    overflow: hidden;
    background: rgba(0,0,0,0.05);
    
    .image-carousel {
      position: relative;
      width: 100%;
      height: 100%;
      
      .carousel-item {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        opacity: 0;
        transition: opacity 0.5s ease;
        
        &.active {
          opacity: 1;
        }
        
        .card-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.6s ease;
        }
      }
    }
    
    // 轮播指示器
    .carousel-indicators {
      position: absolute;
      bottom: 8px;
      left: 50%;
      transform: translateX(-50%);
      display: flex;
      gap: 6px;
      z-index: 5;
      background: rgba(0, 0, 0, 0.4);
      backdrop-filter: blur(8px);
      padding: 6px 10px;
      border-radius: 20px;
      pointer-events: auto;
      
      .indicator-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.5);
        cursor: pointer;
        transition: all 0.3s ease;
        pointer-events: auto;
        
        &:hover {
          background: rgba(255, 255, 255, 0.8);
          transform: scale(1.3);
        }
        
        &.active {
          width: 20px;
          border-radius: 3px;
          background: #fff;
        }
      }
    }
    
    // 轮播箭头
    .carousel-arrows {
      .arrow {
        position: absolute;
        top: 50%;
        transform: translateY(-50%);
        width: 32px;
        height: 32px;
        background: rgba(0, 0, 0, 0.6);
        backdrop-filter: blur(8px);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        opacity: 0;
        transition: all 0.3s ease;
        z-index: 5;
        pointer-events: auto;
        
        i {
          color: #fff;
          font-size: 16px;
          font-weight: bold;
          pointer-events: none;
        }
        
        &:hover {
          background: rgba(var(--color-primary-rgb), 0.9);
          transform: translateY(-50%) scale(1.15);
        }
        
        &:active {
          transform: translateY(-50%) scale(1.05);
        }
        
        &.arrow-left {
          left: 8px;
        }
        
        &.arrow-right {
          right: 8px;
        }
      }
    }
    
    &:hover {
      .carousel-arrows .arrow {
        opacity: 1;
      }
      
      .carousel-item.active .card-image {
        transform: scale(1.05);
      }
    }
    
    // 卖家头像 - 左上角
    .seller-avatar-wrapper {
      position: absolute;
      top: 12px;
      left: 12px;
      display: flex;
      align-items: center;
      gap: 8px;
      z-index: 4;
      background: rgba(var(--color-primary-rgb), 0.85);
      backdrop-filter: blur(10px);
      padding: 6px 12px 6px 6px;
      border-radius: 50px;
      border: 1px solid rgba(255, 255, 255, 0.3);
      box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
      max-width: calc(100% - 60px); // 留出右侧按钮空间
      transition: all 0.3s ease;
      
      .seller-avatar {
        border: 2px solid rgba(255, 255, 255, 0.5);
        flex-shrink: 0;
      }
      
      .seller-name {
        font-size: 13px;
        font-weight: 600;
        color: #fff;
        text-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
    
    // 更多操作按钮 - 右上角
    .more-dropdown {
      position: absolute;
      top: 12px;
      right: 12px;
      z-index: 4;
      
      .more-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 36px;
        height: 36px;
        background: rgba(0, 0, 0, 0.6);
        backdrop-filter: blur(10px);
        border-radius: 50%;
        border: 1px solid rgba(255, 255, 255, 0.2);
        color: #fff;
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover, &.is-open {
          background: rgba(var(--color-primary-rgb), 0.9);
          border-color: rgba(255, 255, 255, 0.4);
          transform: scale(1.1);
          box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.4);
        }
        
        i {
          font-size: 18px;
        }
      }
    }
    
    .card-badges {
      position: absolute;
      bottom: 12px;
      left: 12px;
      display: flex;
      flex-direction: column;
      gap: 6px;
      z-index: 2;
      
      .glass-badge {
        padding: 5px 12px;
        border-radius: 10px;
        font-size: 12px;
        font-weight: 600;
        backdrop-filter: blur(10px);
        color: #fff;
        border: 1px solid rgba(255,255,255,0.3);
        box-shadow: 0 2px 8px rgba(0,0,0,0.15);
        width: fit-content;
        transition: all 0.3s ease;
        
        &.badge-warning { 
          background: rgba(var(--color-secondary-warning-rgb), 0.9);
          box-shadow: 0 2px 12px rgba(var(--color-secondary-warning-rgb), 0.4);
        }
        &.badge-success { 
          background: rgba(var(--color-secondary-success-rgb), 0.9);
          box-shadow: 0 2px 12px rgba(var(--color-secondary-success-rgb), 0.4);
        }
        &.badge-danger { 
          background: rgba(var(--color-secondary-danger-rgb), 0.9);
          box-shadow: 0 2px 12px rgba(var(--color-secondary-danger-rgb), 0.4);
        }
        &.badge-info { 
          background: rgba(var(--color-secondary-info-rgb), 0.9);
          box-shadow: 0 2px 12px rgba(var(--color-secondary-info-rgb), 0.4);
        }
        &.badge-default { 
          background: rgba(100,100,100, 0.7); 
          box-shadow: 0 2px 8px rgba(0,0,0,0.2);
        }
        
        &.badge-condition {
          background: rgba(var(--color-primary-rgb), 0.85);
          color: #fff;
          font-weight: 600;
          box-shadow: 0 2px 12px rgba(var(--color-primary-rgb), 0.4);
        }
      }
    }
    
    .card-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0,0,0,0.4);
      backdrop-filter: blur(4px);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      opacity: 0;
      transition: opacity 0.3s ease;
      z-index: 3;
    }
  }
  
  // 内容区域
  .card-body {
    padding: 16px;
    flex: 1;
    
    .title-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 8px;
      margin-bottom: 8px;
    }
    
    .good-title {
      font-size: 15px;
      font-weight: 600;
      margin: 0;
      flex: 1;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      color: $color-text-primary;
    }
    
    .good-id {
      font-size: 12px;
      font-weight: 500;
      color: $color-text-disabled;
      background: rgba($color-primary, 0.08);
      padding: 2px 8px;
      border-radius: 6px;
      white-space: nowrap;
      flex-shrink: 0;
    }
    
    .good-desc {
      font-size: 13px;
      color: $color-text-secondary;
      line-height: 1.5;
      margin-bottom: 12px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      height: 39px;
    }
    
    .good-meta {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-bottom: 12px;
      
      .meta-row {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        
        &.meta-stats {
          .meta-item {
            flex: 1;
            justify-content: center;
            min-width: 0;
          }
        }
      }
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: $color-text-secondary;
        background: rgba($color-primary, 0.05);
        padding: 4px 8px;
        border-radius: 6px;
        
        i {
          font-size: 13px;
          color: $color-primary;
          flex-shrink: 0;
        }
        
        span {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        &.meta-likes {
          background: rgba(#F59E0B, 0.08);
          
          i {
            color: #F59E0B;
          }
        }
        
        &.meta-favorites {
          background: rgba(#EF4444, 0.08);
          
          i {
            color: #EF4444;
          }
        }
      }
    }
    
    .good-tags {
      display: flex;
      gap: 6px;
      
      .tag-item {
        font-size: 11px;
        padding: 2px 8px;
        border-radius: 4px;
        background: rgba($color-primary, 0.1);
        color: $color-primary;
      }
    }
  }
  
  // 底部操作栏
  .card-footer {
    padding: 12px 16px;
    border-top: 1px solid rgba($color-border, 0.3);
    background: rgba($color-primary, 0.02);
    
    &.audit-footer {
      background: rgba(var(--color-secondary-warning-rgb), 0.05);
      padding: 16px;
      
      .footer-btn-group {
        display: flex;
        gap: 12px;
        
        .glass-btn {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 6px;
          padding: 10px 20px;
          border: none;
          border-radius: 12px;
          font-size: 14px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          backdrop-filter: blur(10px);
          position: relative;
          overflow: hidden;
          
          i {
            font-size: 16px;
            font-weight: bold;
          }
          
          &::before {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            width: 0;
            height: 0;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.3);
            transform: translate(-50%, -50%);
            transition: width 0.6s, height 0.6s;
          }
          
          &:active::before {
            width: 300px;
            height: 300px;
          }
          
          &.btn-reject {
            background: rgba(var(--color-secondary-danger-rgb), 0.1);
            border: 1px solid rgba(var(--color-secondary-danger-rgb), 0.3);
            color: $color-secondary-danger;
            
            &:hover {
              background: rgba(var(--color-secondary-danger-rgb), 0.2);
              border-color: rgba(var(--color-secondary-danger-rgb), 0.5);
              box-shadow: 0 4px 12px rgba(var(--color-secondary-danger-rgb), 0.3);
              transform: translateY(-2px);
            }
            
            &:active {
              transform: translateY(0);
            }
          }
          
          &.btn-approve {
            background: rgba(var(--color-secondary-success-rgb), 0.1);
            border: 1px solid rgba(var(--color-secondary-success-rgb), 0.3);
            color: $color-secondary-success;
            
            &:hover {
              background: rgba(var(--color-secondary-success-rgb), 0.2);
              border-color: rgba(var(--color-secondary-success-rgb), 0.5);
              box-shadow: 0 4px 12px rgba(var(--color-secondary-success-rgb), 0.3);
              transform: translateY(-2px);
            }
            
            &:active {
              transform: translateY(0);
            }
          }
        }
      }
    }
    
    .status-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
      
      .exchange-code {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 6px 12px;
        background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.1), rgba(var(--color-primary-rgb), 0.05));
        border: 1px solid rgba(var(--color-primary-rgb), 0.3);
        border-radius: 8px;
        font-size: 12px;
        flex: 1;
        
        i {
          font-size: 16px;
          color: $color-primary;
        }
        
        .code-label {
          color: $color-text-secondary;
          font-weight: 500;
        }
        
        .code-value {
          color: $color-primary;
          font-weight: 700;
          font-family: 'Courier New', monospace;
          letter-spacing: 0.5px;
        }
      }
    }
  }
}

// 动画
.list-fade-enter-active, .list-fade-leave-active {
  transition: all 0.5s;
}
.list-fade-enter, .list-fade-leave-to {
  opacity: 0;
  transform: translateY(30px);
}
.list-fade-move {
  transition: transform 0.5s;
}

// 下拉菜单项样式
::v-deep {
  .text-danger {
    color: $color-secondary-danger !important;
    
    &:hover {
      background: rgba(var(--color-secondary-danger-rgb), 0.1) !important;
    }
  }
}
</style>
