<template>
  <div class="order-list">
    <div 
      v-for="order in (orders || [])" 
      :key="order.id" 
      class="order-card"
    >
      <!-- 订单头部 -->
      <div class="card-header">
        <div class="header-left">
          <div class="order-id">订单号: {{ order.orderNo }}</div>
          <div class="order-date">{{ formatFullTime(order.createTime) }}</div>
        </div>
        <div class="header-right">
          <div :class="['status-pill', getStatusClass(order.status)]">
            {{ getStatusLabel(order.status) }}
          </div>
        </div>
      </div>

      <!-- 中间主体：双面板 -->
      <div class="card-body">
        <!-- 左侧面板 (买家/发起方) -->
        <div class="user-panel left-panel">
          <div class="panel-header">
            <div :class="['avatar-circle', getAvatarClass(order.buyer?.id)]">
              <span v-if="!order.buyer?.avatar">{{ getInitials(order.buyer?.name) }}</span>
              <img v-else :src="order.buyer.avatar" :alt="order.buyer?.name" />
            </div>
            <div class="user-info">
              <div class="user-name">{{ order.buyer?.name || '未知用户' }}</div>
              <div class="user-sub">ID：{{ order.buyer?.id || '-' }}</div>
              <div class="user-sub" v-if="order.buyer?.realName"><i class="el-icon-user"></i> {{ order.buyer?.realName }}</div>
              <div class="user-sub"><i class="el-icon-phone"></i> {{ order.buyer?.phone || '-' }}</div>
              <div class="user-sub" v-if="order.buyer?.school"><i class="el-icon-school"></i> {{ order.buyer?.school }}</div>
              <div class="user-sub"><i class="el-icon-location-information"></i> {{ order.buyer?.address || '未知区域' }}</div>
            </div>
          </div>
          
          <div class="panel-content">
            <div class="section-label">| 归属物品</div>
            <div class="item-title">{{ order.buyerItem?.name || '暂无物品' }}</div>
            <div class="item-thumb-area" v-if="order.buyerItem">
              <div class="thumbs-row">
                <div 
                  v-for="(img, idx) in (order.buyerItem.previewImages || []).slice(0, 2)" 
                  :key="idx" 
                  class="thumb-wrapper"
                >
                  <img :src="img" alt="item" />
                </div>
                <div v-if="!order.buyerItem.previewImages?.length" class="thumb-wrapper">
                  <div class="no-image">无图</div>
                </div>
              </div>
               <!-- 凭证上传状态 -->
               <div class="upload-status" v-if="order.initiator_confirmed === 1 || order.status === 'completed'">
                 <i class="el-icon-check"></i> 已上传凭证
               </div>
               <div class="upload-status pending" v-else>
                 <i class="el-icon-warning-outline"></i> 未上传凭证
               </div>
            </div>
            <!-- 完成状态下的额外图标 -->
            <div v-if="order.status === 'completed'" class="status-icon-row">
               <i class="el-icon-success"></i> 完成
            </div>
          </div>
        </div>

        <!-- 中间交换图标 -->
        <div class="swap-icon">
          <i class="el-icon-sort" style="transform: rotate(90deg);"></i>
        </div>

        <!-- 右侧面板 (卖家/接收方) -->
        <div class="user-panel right-panel" :class="{'completed-border': order.status === 'completed'}">
          <div class="panel-header">
            <div :class="['avatar-circle', getAvatarClass(order.seller?.id)]">
              <span v-if="!order.seller?.avatar">{{ getInitials(order.seller?.name) }}</span>
              <img v-else :src="order.seller.avatar" :alt="order.seller?.name" />
            </div>
            <div class="user-info">
              <div class="user-name">{{ order.seller?.name || '未知用户' }}</div>
              <div class="user-sub">ID：{{ order.seller?.id || '-' }}</div>
              <div class="user-sub" v-if="order.seller?.realName"><i class="el-icon-user"></i> {{ order.seller?.realName }}</div>
              <div class="user-sub"><i class="el-icon-phone"></i> {{ order.seller?.phone || '-' }}</div>
              <div class="user-sub" v-if="order.seller?.school"><i class="el-icon-school"></i> {{ order.seller?.school }}</div>
              <div class="user-sub"><i class="el-icon-location-information"></i> {{ order.seller?.address || '未知区域' }}</div>
            </div>
          </div>

          <div class="panel-content">
            <div class="section-label">| 归属物品</div>
            <div class="item-title">{{ order.sellerItem?.name || '暂无物品' }}</div>
            <div class="item-thumb-area" v-if="order.sellerItem">
              <div class="thumbs-row">
                <div 
                  v-for="(img, idx) in (order.sellerItem.previewImages || []).slice(0, 2)" 
                  :key="idx" 
                  class="thumb-wrapper"
                >
                  <img :src="img" alt="item" />
                </div>
                <div v-if="!order.sellerItem.previewImages?.length" class="thumb-wrapper">
                  <div class="no-image">无图</div>
                </div>
              </div>
              <!-- 凭证上传状态 -->
              <div class="upload-status" v-if="order.receiver_confirmed === 1 || order.status === 'completed'">
                 <i class="el-icon-check"></i> 已上传凭证
               </div>
               <div class="upload-status pending" v-else>
                 <i class="el-icon-warning-outline"></i> 未上传凭证
               </div>
            </div>
            <!-- 完成状态下的额外图标 -->
            <div v-if="order.status === 'completed'" class="status-icon-row">
               <i class="el-icon-success"></i> 完成
            </div>
          </div>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="card-footer">
        <button class="footer-btn btn-detail" @click="$emit('view-detail', order)">
          <i class="el-icon-document"></i> 查看详情
        </button>

        <button 
          v-if="order.status === 'completed'" 
          class="footer-btn btn-green" 
          @click="$emit('move-to-process', order)"
        >
          <i class="el-icon-warning-outline"></i> 终止快速处理
        </button>

        <button 
          v-if="order.status === 'cancelled'" 
          class="footer-btn btn-red" 
          @click="$emit('delete', order)"
        >
          <i class="el-icon-delete"></i> 删除
        </button>

        <!-- 其他状态的按钮保留但样式调整 -->
        <button 
          v-if="order.status === 'pending'" 
          class="footer-btn btn-red" 
          @click="$emit('cancel', order)"
        >
          <i class="el-icon-circle-close"></i> 取消订单
        </button>

        <button
          v-if="order.status === 'to_process'"
          class="footer-btn btn-green"
          @click="$emit('handle-order', order)"
        >
          <i class="el-icon-s-claim"></i> 处理
        </button>

         <button
          v-if="order.status === 'processing'"
          class="footer-btn btn-green"
          @click="$emit('complete-processing', order)"
        >
          <i class="el-icon-circle-check"></i> 完成
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ORDER_STATUS_LABELS } from '@/utils/orderConstants';

export default {
  name: 'OrderList',
  props: {
    orders: {
      type: Array,
      required: true,
      default: () => []
    }
  },
  methods: {
    getStatusLabel(status) {
      return ORDER_STATUS_LABELS[status] || status;
    },
    
    getStatusClass(status) {
      const map = {
        'completed': 'status-completed', // Green
        'cancelled': 'status-cancelled', // Blue/Grey
        'pending': 'status-pending',     // Orange?
        'to_process': 'status-process',
        'processing': 'status-process'
      };
      return map[status] || 'status-default';
    },
    
    getInitials(name) {
      if (!name) return '??';
      // Simple logic for initials, can be improved for Chinese names
      return name.substring(0, 2).toUpperCase();
    },

    formatFullTime(timestamp) {
      if (!timestamp) return '-';
      const date = new Date(timestamp);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      }).replace(/\//g, '/'); 
    },
    
    maskPhone(phone) {
      if (!phone) return '-';
      return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
    },

    getAvatarClass(id) {
      if (!id) return 'avatar-default';
      const colors = ['red-avatar', 'pink-avatar', 'cyan-avatar', 'orange-avatar', 'purple-avatar', 'blue-avatar'];
      // Simple hash to pick a color
      const index = String(id).charCodeAt(0) % colors.length;
      return colors[index];
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.order-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  
  @media (max-width: 1200px) {
    grid-template-columns: 1fr;
  }
}

.order-card {
  @include glass-card; // Use theme mixin
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: transform 0.2s;
  border-radius: 16px; // Override if needed, or stick to mixin

  &:hover {
    transform: translateY(-2px);
    border-color: rgba(var(--color-primary-rgb), 0.3);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;

  .header-left {
    .order-id {
      font-size: 14px;
      font-weight: 600;
      color: $color-text-primary;
      margin-bottom: 4px;
    }
    .order-date {
      font-size: 12px;
      color: $color-text-secondary;
    }
  }

  .status-pill {
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: bold;
    
    &.status-completed {
      color: $color-secondary-success;
      border: none;
      background: rgba(var(--color-secondary-success-rgb), 0.15);
    }
    
    &.status-cancelled {
      color: $color-text-disabled;
      border: none;
      background: rgba(var(--color-secondary-info-rgb), 0.15);
    }

    &.status-pending {
       color: $color-secondary-warning;
       border: none;
       background: rgba(var(--color-secondary-warning-rgb), 0.15);
    }
    
    &.status-process {
       color: $color-secondary-info;
       border: none;
       background: rgba(var(--color-secondary-info-rgb), 0.15);
    }
  }
}

.card-body {
  display: flex;
  align-items: stretch;
  gap: 10px;
}

.user-panel {
  flex: 1;
  border: 1px solid var(--glass-border);
  border-radius: 12px;
  padding: 12px;
  background: var(--color-sidebar-user-card-bg);
  display: flex;
  flex-direction: column;
  gap: 12px;

  &.completed-border {
    border-color: rgba(var(--color-secondary-success-rgb), 0.5);
  }

  .panel-header {
    display: flex;
    gap: 10px;
    
    .avatar-circle {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      font-size: 14px;
      color: #fff; // Avatar text usually white on colored bg
      overflow: hidden;
      flex-shrink: 0;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      // Keep these distinct colors as they are data-driven, not theme-driven
      &.red-avatar { background: #ff4d4f; }
      &.pink-avatar { background: #ff4081; }
      &.cyan-avatar { background: #13c2c2; }
      &.orange-avatar { background: #fa8c16; }
      &.purple-avatar { background: #722ed1; }
      &.blue-avatar { background: #1890ff; }
      &.avatar-default { background: #8c8c8c; }
    }

    .user-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
      overflow: hidden;

      .user-name {
        font-size: 14px;
        font-weight: bold;
        color: $color-text-primary;
      }
      .user-sub {
        font-size: 10px;
        color: $color-text-secondary;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }

  .panel-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;

    .section-label {
      font-size: 10px;
      color: $color-text-disabled;
    }

    .item-title {
      font-size: 13px;
      color: $color-text-primary;
      font-weight: 500;
    }

    .item-thumb-area {
      display: flex;
      flex-direction: column;
      gap: 6px;
      align-items: flex-start;

      .thumbs-row {
        display: flex;
        gap: 8px;
        
        .thumb-wrapper {
          width: 60px;
          height: 60px;
          border-radius: 8px;
          overflow: hidden;
          background: var(--color-bg-surface);
          border: 1px solid var(--color-border);
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
          
          .no-image {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 10px;
            color: $color-text-disabled;
          }
        }
      }

      .mini-btn {
        padding: 2px 8px;
        font-size: 10px;
        background: var(--color-pill-bg);
        color: $color-text-secondary;
        border: 1px solid var(--color-border);
        border-radius: 10px;
        cursor: pointer;
        transition: all 0.2s;
        &:hover { 
          background: var(--color-pill-active-bg); 
          color: var(--color-pill-active-text);
        }
      }

      .upload-status {
        font-size: 10px;
        padding: 2px 8px;
        border-radius: 10px;
        background: rgba(var(--color-secondary-success-rgb), 0.1);
        color: $color-secondary-success;
        border: 1px solid rgba(var(--color-secondary-success-rgb), 0.2);
        display: flex;
        align-items: center;
        gap: 4px;
        white-space: nowrap;

        &.pending {
          background: rgba(var(--color-secondary-warning-rgb), 0.1);
          color: $color-secondary-warning;
          border-color: rgba(var(--color-secondary-warning-rgb), 0.2);
        }
      }
    }
    
    .status-icon-row {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      font-size: 10px;
      color: $color-secondary-success;
      margin-top: auto;
    }
  }

  .panel-footer {
    margin-top: auto;
    .panel-action-btn {
      width: 100%;
      padding: 6px;
      background: var(--color-pill-bg);
      border: 1px solid transparent;
      border-radius: 6px;
      color: $color-text-secondary;
      font-size: 11px;
      cursor: pointer;
      transition: all 0.2s;
      &:hover { 
        background: var(--color-pill-active-bg); 
        color: var(--color-pill-active-text);
      }
    }
  }
}

.swap-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-text-disabled;
  font-size: 16px;
}

.card-footer {
  display: flex;
  gap: 10px;
  padding-top: 4px;
  
  .footer-btn {
    padding: 6px 12px;
    border-radius: 6px;
    font-size: 12px;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
    transition: all 0.2s;

    &.btn-detail {
      background: var(--color-pill-bg);
      color: $color-text-secondary;
      &:hover { 
        background: var(--color-bg-surface);
        color: $color-text-primary;
      }
    }

    &.btn-green {
      background: rgba(var(--color-secondary-success-rgb), 0.1);
      color: $color-secondary-success;
      border: 1px solid rgba(var(--color-secondary-success-rgb), 0.2);
      &:hover { background: rgba(var(--color-secondary-success-rgb), 0.2); }
    }

    &.btn-red {
      background: rgba(var(--color-secondary-danger-rgb), 0.1);
      color: $color-secondary-danger;
      border: 1px solid rgba(var(--color-secondary-danger-rgb), 0.2);
      &:hover { background: rgba(var(--color-secondary-danger-rgb), 0.2); }
    }
  }
}
</style>
