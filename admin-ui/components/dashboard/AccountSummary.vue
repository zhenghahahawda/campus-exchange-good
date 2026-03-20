<template>
  <div class="bottom-section">
    <!-- 左侧两个小卡片 -->
    <div class="small-cards">
      <div class="card bg-purple-light">
        <div class="content">
          <span class="label">本月浏览</span>
          <div class="value">8.6k</div>
          <div class="dots">...</div>
        </div>
        <div class="indicator purple"></div>
      </div>
      <div class="card bg-orange-light">
        <div class="content">
          <span class="label">本月成交</span>
          <div class="value">428</div>
          <div class="dots">...</div>
        </div>
        <div class="indicator orange"></div>
      </div>
    </div>

    <!-- 右侧账户摘要 -->
    <div class="account-summary base-card">
      <h3 class="title">待处理事项</h3>
      <div class="progress-list">
        <div class="progress-item" v-for="(item, index) in summaryItems" :key="index">
          <div class="chart-wrapper">
             <el-progress 
               type="circle" 
               :percentage="item.percentage" 
               :width="60" 
               :color="item.color"
               :show-text="false"
               :stroke-width="6"
             />
          </div>
          <div class="info">
            <span class="name">{{ item.name }}</span>
            <span class="count">{{ item.current }}/{{ item.total }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AccountSummary',
  data() {
    return {
      summaryItems: [
        { name: '待审商品', current: 12, total: 15, percentage: 80, color: '#6C5DD3' },
        { name: '举报处理', current: 5, total: 15, percentage: 33, color: '#FF9F43' },
        { name: '提现申请', current: 8, total: 20, percentage: 40, color: '#F2C94C' },
      ]
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.bottom-section {
  display: flex;
  margin-top: 30px;
  gap: 30px;
  height: 160px;
}

.small-cards {
  display: flex;
  gap: 30px;
  flex: 0 0 auto;

  .card {
    width: 180px;
    height: 100%;
    // 使用玻璃拟态
    @include glass-card;
    padding: 24px;
    display: flex;
    justify-content: space-between;
    position: relative;
    
    // 移除硬编码的浅色背景，改用微弱的彩色光晕或边框区分
    &.bg-purple-light { 
      border-color: rgba($color-primary-rgb, 0.3);
      box-shadow: 0 4px 20px rgba($color-primary-rgb, 0.15);
    }
    &.bg-orange-light { 
      border-color: rgba($color-secondary-warning-rgb, 0.3);
      box-shadow: 0 4px 20px rgba($color-secondary-warning-rgb, 0.15);
    }
    
    .content {
      display: flex;
      flex-direction: column;
      justify-content: center;
      
      .label {
        font-size: 13px;
        color: $color-text-secondary;
        font-weight: $font-weight-medium;
      }
      
      .value {
        font-size: 28px;
        font-weight: $font-weight-bold;
        color: $color-text-primary;
        margin: 8px 0;
      }
      
      .dots {
        color: $color-text-disabled;
        font-size: 20px;
        line-height: 10px;
      }
    }
    
    .indicator {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      position: absolute;
      top: 30px;
      right: 30px;
      
      &.purple { background-color: $color-primary; }
      &.orange { background-color: $color-secondary-warning; }
    }
  }
}

.account-summary {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 40px;
  
  .title {
    font-size: 18px;
    margin-bottom: 20px;
    color: $color-text-primary;
  }
  
  .progress-list {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .progress-item {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .info {
        display: flex;
        flex-direction: column;
        
        .name {
          font-size: 12px;
          color: $color-text-secondary;
          margin-bottom: 4px;
        }
        
        .count {
          font-size: 16px;
          font-weight: $font-weight-bold;
          color: $color-text-primary;
        }
      }
    }
  }
}
</style>
