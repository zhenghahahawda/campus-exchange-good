<template>
  <div class="seller-card">
    <div class="card-header">
      <h3>发布者信息</h3>
    </div>
    <div class="seller-info">
      <div class="avatar-wrapper">
        <img :src="seller.avatar" :alt="seller.name" />
        <div class="online-status" :class="{ online: seller.isOnline }"></div>
      </div>
      <div class="info-content">
        <h4 class="seller-name">{{ seller.name }}</h4>
        <div class="seller-tags">
          <span class="tag verified-college">
            <i class="el-icon-school"></i> {{ seller.college ? '已通过学籍' : '未认证' }}
          </span>
          <span class="tag credit-score">
            <i class="el-icon-medal"></i> 信誉分 {{ seller.credit_score !== undefined ? seller.credit_score : (seller.creditScore !== undefined ? seller.creditScore : 0) }}
          </span>
        </div>
      </div>
    </div>
    
    <div class="stats-row">
      <div class="stat-item">
        <span class="value">{{ seller.goodsCount || 0 }}</span>
        <span class="label">在售</span>
      </div>
      <div class="stat-item">
        <span class="value">{{ seller.exchangeCount || 0 }}</span>
        <span class="label">已交换</span>
      </div>
      <div class="stat-item">
        <span class="value">{{ seller.responseRate || '100%' }}</span>
        <span class="label">回复率</span>
      </div>
    </div>
    
    <div class="actions">
      <el-button 
        v-if="!isOwnGoods"
        type="primary" 
        class="contact-btn" 
        icon="el-icon-chat-dot-round" 
        @click="$emit('contact-seller')"
      >
        联系卖家
      </el-button>
      <el-button class="profile-btn" icon="el-icon-user" plain @click="$emit('view-profile')">
        查看主页
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SellerInfoCard',
  props: {
    seller: {
      type: Object,
      required: true,
      default: () => ({})
    },
    isOwnGoods: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.seller-card {
  @include glass-card;
  padding: 24px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  box-shadow: var(--card-shadow);
  
  .card-header {
    margin-bottom: 20px;
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: var(--color-text-primary);
      margin: 0;
    }
  }
  
  .seller-info {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    
    .avatar-wrapper {
      position: relative;
      width: 64px;
      height: 64px;
      flex-shrink: 0;
      
      img {
        width: 100%;
        height: 100%;
        border-radius: 50%;
        object-fit: cover;
        border: 2px solid rgba(255, 255, 255, 0.1);
      }
      
      .online-status {
        position: absolute;
        bottom: 2px;
        right: 2px;
        width: 12px;
        height: 12px;
        border-radius: 50%;
        background: var(--color-text-disabled);
        border: 2px solid var(--card-bg);
        
        &.online {
          background: var(--color-secondary-success);
        }
      }
    }
    
    .info-content {
      flex: 1;
      min-width: 0; // 防止溢出
      
      .seller-name {
        font-size: 18px;
        font-weight: 600;
        color: var(--color-text-primary);
        margin: 0 0 8px 0;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .seller-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        
        .tag {
          font-size: 12px;
          padding: 2px 8px;
          border-radius: 6px;
          display: flex;
          align-items: center;
          gap: 4px;
          white-space: nowrap;
          
          &.verified-college {
            background: var(--color-pill-bg);
            color: var(--color-text-secondary);
            border: 1px solid rgba(255, 255, 255, 0.05);
          }
          
          &.credit-score {
            color: var(--color-secondary-danger);
            background: rgba(var(--color-secondary-danger-rgb), 0.1);
            border: 1px solid rgba(var(--color-secondary-danger-rgb), 0.2);
          }
        }
      }
    }
  }
  
  .stats-row {
    display: flex;
    justify-content: space-around;
    padding: 16px 0;
    border-top: 1px solid rgba(255, 255, 255, 0.05);
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    margin-bottom: 24px;
    
    .stat-item {
      text-align: center;
      
      .value {
        display: block;
        font-size: 18px;
        font-weight: 700;
        color: var(--color-text-primary);
        margin-bottom: 4px;
      }
      
      .label {
        font-size: 12px;
        color: var(--color-text-secondary);
      }
    }
  }
  
  .actions {
    display: flex;
    flex-direction: column;
    gap: 12px;
    
    .contact-btn {
      width: 100%;
      height: 44px;
      border-radius: 22px;
      font-size: 14px;
      font-weight: 600;
      background: var(--color-primary);
      color: white;
      border: none;
      transition: all 0.3s ease;
      box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(var(--color-primary-rgb), 0.4);
        opacity: 0.9;
      }
      
      i { font-size: 16px; margin-right: 6px; }
    }
    
    ::v-deep .contact-btn {
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      
      span {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
      }
    }
    
    .profile-btn {
      width: 100%;
      height: 40px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 500;
      background: rgba(255, 255, 255, 0.05);
      color: var(--color-text-secondary);
      border: 1px solid rgba(255, 255, 255, 0.1);
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: var(--color-text-primary);
        border-color: rgba(255, 255, 255, 0.2);
      }
      
      i { font-size: 14px; margin-right: 6px; }
    }
    
    ::v-deep .profile-btn {
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      
      span {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
      }
    }
  }
}
</style>
